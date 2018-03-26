package com.hevaisoi.android;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.hevaisoi.android.camera.CameraSourcePreview;
import com.hevaisoi.android.camera.FaceGraphic;
import com.hevaisoi.android.camera.GraphicOverlay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FaceTrackerActivity extends BaseActivity {
    private boolean isPhotoSelected = false;
    private int backPressCount = 0;
    //region PrivateClass
    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new_flag face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
            float xCenter = (imgShapeScan.getWidth() / 2) + imgShapeScan.getX();
            float yCenter = (imgShapeScan.getHeight() / 2) + imgShapeScan.getY();
            mFaceGraphic.setxCenter(xCenter);
            mFaceGraphic.setyCenter(yCenter);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
       /*
       @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }
        */

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
            mFaceGraphic.setOnFaceScanListener(faceScanListener);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }

    }
    //endregion

    //region StaticParam
    private static final int RESULT_LOAD_IMAGE = 2;
    private static final int RC_HANDLE_GMS = 9001;
    private final Handler mSwitchCameraHandler = new Handler();

    //endregion
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
//    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
//    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
//    private static final int UI_ANIMATION_DELAY = 300;
    //region Controls
    private ActionBar actionBar;

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private ImageButton btnOpenPhoto;
    private ImageButton btnSwitchCamera;
    private ImageView imgShapeScan;
    private TextView txtFaceScanStt;
    private ProgressBar progressBar;
    private boolean isCameraBack = false;
    private ProgressDialog progressDialog;
    private MyStoreApp app;
    private CountDownTimer takePhotoTimer;
    private com.google.android.gms.ads.AdView admobAdView;
    private com.facebook.ads.AdView faceAdView;
    private LinearLayout bottomGroup;

    private int progressCount = 0;

    //endregion

    //region Override Methods
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE) {
            if (resultCode == RESULT_OK) {
                isPhotoSelected = true;
                Log.d(Constants.LOG_TAG, "Begin reading image into memory");
                Uri temp = data.getData();
                if (temp != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(temp);
                        byte[] bytesPhoto = getBytes(inputStream);
                        Log.d(Constants.LOG_TAG, String.format("Photo's length: %1$d", bytesPhoto.length));
                        startMainActivity(bytesPhoto);
                    } catch (IOException e) {
                        Log.e(Constants.LOG_TAG, "Cannot read image from storage", e);
                    }
                }
                Log.d(Constants.LOG_TAG, "End reading image into memory");
            }
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.product_list_retrieving_data));

        app = (MyStoreApp) getApplication();
        setContentView(R.layout.activity_face_tracker);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar_color)));
        ActionBar.LayoutParams layout_params = new ActionBar.LayoutParams(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        View cameraMenuView = getLayoutInflater().inflate(R.layout.camera_action_menu, null);
        getSupportActionBar().setCustomView(cameraMenuView, layout_params);

        mPreview = (CameraSourcePreview) findViewById(R.id.facePreview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        btnOpenPhoto = (ImageButton) findViewById(R.id.btn_open_photo);
        btnSwitchCamera = (ImageButton) findViewById(R.id.btn_switch_camera);
        txtFaceScanStt = (TextView) findViewById(R.id.text_face_scan_status);
        imgShapeScan = (ImageView) findViewById(R.id.img_face_shape_scan);
        progressBar = (ProgressBar) findViewById(R.id.progress_face_scan_status);

        faceAdView = new com.facebook.ads.AdView(this, getString(R.string.fan_banner_placement_id), AdSize.BANNER_HEIGHT_50);
        //Add devices test to FAN
        AdSettings.addTestDevice("6966312571386480cf6a5803513f221e");
        AdSettings.addTestDevice("43bf8b9f79be9834c66ddca642601052");
        AdSettings.addTestDevice("5844ddb7ce260c3645c796b136d2ecf1");
        bottomGroup = (LinearLayout) findViewById(R.id.face_tracker_bottom_group);
        bottomGroup.addView(faceAdView, 0);
        faceAdView.loadAd();

        initialEvent();

        if (!canAccessCamera()) {
            return;
        } else {
            createCameraSource();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != Constants.MY_CAMERA_PERMISSIONS_REQUEST) {
            Log.d(Constants.LOG_TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length == 0) {
            Log.e(Constants.LOG_TAG, "Permission not granted: results len = " + grantResults.length +
                    " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.LOG_TAG, "Camera permission granted -  results len =" + grantResults.length + ", result code =" + grantResults[0]);
            // we have permission, so create the camerasource
            createCameraSource();
        } else {
            if (!isFinishing()) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                };

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("HeVaiSoi App")
                        .setMessage(R.string.no_camera_permission)
                        .setPositiveButton(R.string.ok, listener)
                        .show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (admobAdView != null) {
            admobAdView.resume();
        }

        if (!isPhotoSelected) {
            restartCamera();
            Log.d(Constants.LOG_TAG, "Resume camera source");
        } else {
            return;
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }

        if (admobAdView != null) {
            admobAdView.destroy();
        }
        if (faceAdView != null) {
            faceAdView.destroy();
        }
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
        stopCountDown();
        if (admobAdView != null) {
            admobAdView.pause();
        }
    }

    //endregion

    private boolean canAccessCamera() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(FaceTrackerActivity.this, Manifest.permission.CAMERA)) {
                AlertDialog diagCameraPermission = new AlertDialog.Builder(FaceTrackerActivity.this)
                        .setMessage(R.string.camera_request_permission)
                        .setPositiveButton(R.string.ok, perMissionDialogListener)
                        .setNegativeButton(R.string.cancel, perMissionDialogListener)
                        .create();
                diagCameraPermission.show();
                return false;
            }
            ActivityCompat.requestPermissions(FaceTrackerActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    Constants.MY_CAMERA_PERMISSIONS_REQUEST);
            return false;
        }
        return true;
    }

    private void createCameraSource() {
        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(true)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            UtilitiesHelper.showMessage(this, getString(R.string.face_not_yet_download));
            Log.w(Constants.LOG_TAG, "Face detector dependencies are not yet available.");
        }
        int cameraId = 0;
        if (!isCameraBack) {
            cameraId = CameraSource.CAMERA_FACING_FRONT;
            isCameraBack = true;
        } else {
            cameraId = CameraSource.CAMERA_FACING_BACK;
            isCameraBack = false;
        }
        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(cameraId)
                .setRequestedFps(30.0f)
                .build();

    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability
                .getInstance()
                .isGooglePlayServicesAvailable(getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance()
                            .getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(Constants.LOG_TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void stopCameraSource() {
        if (mCameraSource != null && mPreview != null) {
            mPreview.destroyDrawingCache();
            mPreview.release();
        }
    }

    @Override
    public void onBackPressed() {
        if (backPressCount >= 1) {
            AlertDialog exitAppDialog = new AlertDialog.Builder(FaceTrackerActivity.this)
                    .setMessage(R.string.exit_app_message)
                    .setPositiveButton(R.string.yes, exitAppDialogListener)
                    .setNegativeButton(R.string.no, exitAppDialogListener)
                    .create();
            exitAppDialog.show();
        }
        backPressCount++;
    }

    private void getImageFromAlbum() {
        try {
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
        } catch (Exception exp) {
            Log.i("Error", exp.toString());
        }
    }

    private void restartCamera() {
        if (canAccessCamera()) {
            stopCameraSource();
            createCameraSource();
            startCameraSource();
        }
    }

    private void initialEvent() {
        takePhotoTimer = new CountDownTimer(Constants.COUNT_TIME, Constants.COUNT_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressCount += 1;
                progressBar.setProgress(progressCount);
            }

            @Override
            public void onFinish() {
                takePhotoAction();
            }
        };

        btnOpenPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromAlbum();
            }
        });

        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                stopCameraSource();

                mSwitchCameraHandler.post(mSwitchCameraRunnable);
            }
        });

        faceAdView.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                Log.d(Constants.LOG_TAG, "Error load face adview: " + adError.getErrorMessage());
                admobAdView = new com.google.android.gms.ads.AdView(FaceTrackerActivity.this);
                admobAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
                admobAdView.setAdUnitId(getString(R.string.admob_banner_home_footer));

                AdRequest request = UtilitiesHelper.getAdmobRequest();
                admobAdView.loadAd(request);

                bottomGroup.removeView(faceAdView);
                bottomGroup.addView(admobAdView, 0);
            }

            @Override
            public void onAdLoaded(Ad ad) {

            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });
    }

    private void takePhotoAction() {
        mCameraSource.takePicture(shutterCallback, onPicTaken);
    }

    CameraSource.ShutterCallback shutterCallback = new CameraSource.ShutterCallback() {
        @Override
        public void onShutter() {

        }
    };
    final transient private CameraSource.PictureCallback onPicTaken = new CameraSource.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] bytes) {
            Log.d(Constants.LOG_TAG, String.format("Photo length: %1$d", bytes.length));
            startMainActivity(bytes);
        }
    };

    DialogInterface.OnClickListener exitAppDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch (i) {
                case BUTTON_POSITIVE:
                    dialogInterface.dismiss();
                    finish();
                    break;
            }
        }
    };

    DialogInterface.OnClickListener perMissionDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case BUTTON_NEGATIVE:
                    dialog.dismiss();
                    finish();
                    break;
                case BUTTON_POSITIVE:
                    ActivityCompat.requestPermissions(
                            FaceTrackerActivity.this, new String[]{android.Manifest.permission.CAMERA},
                            Constants.MY_CAMERA_PERMISSIONS_REQUEST);
                    dialog.dismiss();
                    break;
                default:
                    dialog.dismiss();
                    break;
            }
        }
    };

    private final Runnable mSwitchCameraRunnable = new Runnable() {
        @Override
        public void run() {
            restartCamera();

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

        }
    };

    private void startMainActivity(byte[] bytes) {
        app.setPhotoBytes(bytes);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void startCountDown() {
        txtFaceScanStt.setTextColor(Color.GREEN);
        txtFaceScanStt.setText(getResources().getString(R.string.face_scan_good));

        takePhotoTimer.start();
    }

    private void stopCountDown() {
        int textColor = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
        txtFaceScanStt.setTextColor(textColor);
        txtFaceScanStt.setText(getResources().getString(R.string.face_scan_not_good));
        progressCount = 0;
        progressBar.setProgress(0);
        takePhotoTimer.cancel();
    }

    FaceGraphic.OnFaceScanListener faceScanListener = new FaceGraphic.OnFaceScanListener() {
        @Override
        public void onGoodFace() {
            Log.d(Constants.LOG_TAG, "Ok, good face, let's scan");
            startCountDown();
        }

        @Override
        public void onNotGoodFace() {
            Log.d(Constants.LOG_TAG, "Not good face");
            stopCountDown();
        }
    };
}



