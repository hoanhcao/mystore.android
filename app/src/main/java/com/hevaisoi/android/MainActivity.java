package com.hevaisoi.android;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.gson.reflect.TypeToken;
import com.hevaisoi.android.databases.AppDataBaseHelper;
import com.hevaisoi.android.databases.HairDAO;
import com.hevaisoi.android.datasource.ClothAdapter;
import com.hevaisoi.android.datasource.HairAdapter;
import com.hevaisoi.android.datasource.TrouserColorAdapter;
import com.hevaisoi.android.model.CatalogModel;
import com.hevaisoi.android.model.ClothModel;
import com.hevaisoi.android.model.HairModel;
import com.hevaisoi.android.patch.SafeFaceDetector;
import com.hevaisoi.android.photo.FaceView;
import com.hevaisoi.android.webservice.MyJsonParser;
import com.hevaisoi.android.webservice.WebServiceHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class MainActivity extends BaseActivity
        implements /*SinchService.StartFailedListener,*/ NavigationView.OnNavigationItemSelectedListener {
    private static boolean isVisibleBottom = true;
    private final Handler mDrawHandler = new Handler();
    private ProgressDialog progressDialog;
    private MyStoreApp app;
    private NavigationView leftMenu;
    private FaceView faceView;
    private LinearLayout bottomGroup;
    private RecyclerView bottomList;
    private FloatingActionButton fab;
    private ImageButton btnAppCall;

    private int selectedContract = 5;
    private String selectedTrouserColor = "#B200FF";
    private int selectedClothId;
    private List<CatalogModel> catalogModelList;
    /*
    private InterstitialAd gInterstitialAd;
    private com.facebook.ads.InterstitialAd fInterstitialAd;
    */

    //region Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isVisibleBottom = true;
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.product_list_retrieving_data));
        setWaiting(true);
        mDrawHandler.postDelayed(mDrawRunnable, 500);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Hide the app_name, show logo
        ActionBar actBar = getSupportActionBar();
        if (actBar != null) {
            actBar.setDisplayShowTitleEnabled(false);
        }
        this.app = (MyStoreApp) getApplication();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        new ParsedCatalogAsyn().execute();

        leftMenu = (NavigationView) findViewById(R.id.nav_view);
        faceView = (FaceView) findViewById(R.id.faceView);
        bottomGroup = (LinearLayout) findViewById(R.id.bottom_group);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        btnAppCall = (ImageButton) findViewById(R.id.app_call_button);
        initialChangeBrightness();
        initialEvents();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    SeekBar.OnSeekBarChangeListener adjustBrightnessChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            setWaiting(true);

            selectedContract = seekBar.getProgress();
            float contrast = 1.5f + (((float) selectedContract - 5) / 10);
            Log.d(Constants.LOG_TAG, String.format("Selected contrast: %1$f", contrast));

            faceView.setContrast(contrast);
            faceView.postInvalidate();
            faceView.postDelayed(mDrawFaceViewRunnable, 500);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != Constants.MY_STORAGE_PERMISSIONS_REQUEST) {
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
            shareNetworkSocialAction();
        } else {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            };

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("HeVaiSoi App")
                    .setMessage(R.string.no_write_storage_permission)
                    .setPositiveButton(R.string.ok, listener)
                    .show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_change_brightness) {
            Log.d(Constants.LOG_TAG, "User clicked on nav_change_brightness");
            initialChangeBrightness();
            showBottomMenu();
        } else if (id == R.id.nav_change_cloth) {
            Log.d(Constants.LOG_TAG, "User clicked on nav_change_cloth");
            initialChangeCloth();
            showBottomMenu();
        } else if (id == R.id.nav_change_trousers) {
            Log.d(Constants.LOG_TAG, "User clicked on nav_change_trousers");
            initialChangeTrouser();
            showBottomMenu();
        } else if (id == R.id.nav_change_hair) {
            Log.d(Constants.LOG_TAG, "User clicked on nav_change_hair");
            initialChangeHair();
            showBottomMenu();
        } else if (id == R.id.nav_share) {
            if (!canWriteToStorage()) return false;
            shareNetworkSocialAction();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void detectFaceAction() {
        byte[] bytesFace = app.getPhotoBytes();
        if (bytesFace == null) {
            return;
        }

        InputStream stream = new ByteArrayInputStream(bytesFace);
        Bitmap bitmap = BitmapFactory.decodeStream(stream);
        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();
        Log.d(Constants.LOG_TAG, String.format("Bitmap size for face-detector: %1$d, %2$d", bWidth, bHeight));
        //rotate to portrait before run face detect
        if (bWidth > 1000 && bHeight > 1000) {
            bitmap = Bitmap.createScaledBitmap(bitmap, bWidth / 2, bHeight / 2, false);
        }

        bWidth = bitmap.getWidth();
        bHeight = bitmap.getHeight();
        if (bWidth > bHeight) {
            Matrix rotateMatrix = new Matrix();
            rotateMatrix.postRotate(-90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bWidth, bHeight, rotateMatrix, false);
            bWidth = bitmap.getWidth();
            bHeight = bitmap.getHeight();
            Log.d(Constants.LOG_TAG, String.format("Bitmap size after rotate: %1$d, %2$d", bWidth, bHeight));
        }


        // A new_flag face detector is created for detecting the face and its landmarks.
        //
        // Setting "tracking enabled" to false is recommended for detection with unrelated
        // individual images (as opposed to video or a series of consecutively captured still
        // images).  For detection on unrelated individual images, this will give a more accurate
        // result.  For detection on consecutive images (e.g., live video), tracking gives a more
        // accurate (and faster) result.
        //
        // By default, landmark detection is not enabled since it increases detection time.  We
        // enable it here in order to visualize detected landmarks.
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_LANDMARKS)
                .setLandmarkType(FaceDetector.ACCURATE_MODE)
                .setProminentFaceOnly(true)
                .build();
        // This is a temporary workaround for a bug in the face detector with respect to operating
        // on very small images.  This will be fixed in a future release.  But in the near term, use
        // of the SafeFaceDetector class will patch the issue.
        Detector<Face> safeDetector = new SafeFaceDetector(detector);

        // Create a frame from the bitmap and run face detection on the frame.
        Frame frame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();
        SparseArray<Face> faces = safeDetector.detect(frame);

        if (!safeDetector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection. Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(Constants.LOG_TAG, "Face detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(Constants.LOG_TAG, getString(R.string.low_storage_error));
            } else {
                Toast.makeText(this, R.string.face_not_yet_download, Toast.LENGTH_LONG).show();
            }
        }


        // Although detector may be used multiple times for different images, it should be released
        // when it is no longer needed in order to free native resources.
        safeDetector.release();
        // Reset photo which was stored in memory
        app.setPhotoBytes(null);
        if (faces != null &&
                faces.size() > 0 &&
                faces.valueAt(0) != null) {
            faceView.setBmFace(bitmap, faces);
        } else {
            Toast toast = Toast.makeText(this, R.string.face_cannot_detect, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public int getSelectedClothId() {
        return selectedClothId;
    }

    public void setCloth(ClothModel cloth) {
        setWaiting(true);
        if (selectedClothId != cloth.getId()) {
            selectedClothId = cloth.getId();
            faceView.setCloth(cloth.getImgUrl());
            faceView.postDelayed(mDrawFaceViewRunnable, 500);
        }
    }

    public void setHair(int hairId) {
        setWaiting(true);
        faceView.setSelectedHair(hairId);
        faceView.postInvalidate();
        faceView.postDelayed(mDrawFaceViewRunnable, 500);
    }

    public void setTrouserColor(String trouserColor) {
        setWaiting(true);
        selectedTrouserColor = trouserColor;
        faceView.setSelectedTrouserColor(selectedTrouserColor);
        faceView.postInvalidate();
        faceView.postDelayed(mDrawFaceViewRunnable, 500);
    }

    public void showHideBottomMenu() {
        if (isVisibleBottom) {
            hideBottomMenu();
        } else {
            showBottomMenu();
        }
    }

    public void setWaiting(boolean isWaiting) {
        if (isWaiting) {
            progressDialog.show();
        } else {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public void onbtnPhotoClick(View view) {
        Intent intent = new Intent(getApplicationContext(), FaceTrackerActivity.class);
        startActivity(intent);
    }

    private boolean canWriteToStorage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog diagCameraPermission = new AlertDialog.Builder(this)
                        .setMessage(R.string.write_storage_request_permission)
                        .setPositiveButton(R.string.ok, perMissionDialogListener)
                        .setNegativeButton(R.string.cancel, perMissionDialogListener)
                        .create();
                diagCameraPermission.show();
                return false;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.MY_STORAGE_PERMISSIONS_REQUEST);
            return false;
        }
        return true;
    }

    private void shareNetworkSocialAction() {
        Bitmap mBitmap = faceView.getDrawingCache();

        String path = MediaStore.Images.Media.insertImage(getContentResolver(),
                mBitmap, "HeVaiSoi", "HeVaiSoi Images");
        Log.d(Constants.LOG_TAG, "Path image after saving: " + path);
        Uri uri = Uri.parse(path);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_TEXT, "I feel great!");
        startActivity(Intent.createChooser(share, "Share Your Design!"));
    }

    private void hideBottomMenu() {
        Log.d(Constants.LOG_TAG, "Hide bottom menu");
        bottomGroup.animate()
                .translationY(bottomGroup.getHeight());
        fab.animate().translationY(0);
        Log.d(Constants.LOG_TAG, "bottomGroup top: " + bottomGroup.getTop());
        isVisibleBottom = false;
    }

    private void showBottomMenu() {
        Log.d(Constants.LOG_TAG, "Show bottom menu");
        bottomGroup.animate()
                .translationY(0);
        fab.animate().translationY(-fab.getHeight());
        Log.d(Constants.LOG_TAG, "float button height:" + fab.getHeight());
        isVisibleBottom = true;
    }

    private void initialChangeBrightness() {
        bottomGroup.removeAllViewsInLayout();
        View view = getLayoutInflater().inflate(R.layout.change_light_face_seekbar, bottomGroup, false);
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seek_bar_brightness);
        seekBar.setProgress(selectedContract);
        seekBar.setOnSeekBarChangeListener(adjustBrightnessChanged);

        bottomGroup.addView(view);
    }

    private void initialChangeCloth() {
        //check internet access
        if (app.connectionPresent()) {
            if (catalogModelList == null) {
                UtilitiesHelper.showMessage(this, getString(R.string.error_when_load_catalog));
                return;
            }
            final CharSequence[] catalogNames = new String[catalogModelList.size()];
            for (int i = 0; i < catalogModelList.size(); i++) {
                catalogNames[i] = catalogModelList.get(i).getDescription();
            }
            AlertDialog.Builder catalogDiag = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_MinWidth);
            catalogDiag.setTitle(R.string.select_cloth_title)
                    .setItems(catalogNames, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bottomGroup.removeAllViewsInLayout();

                            View view = getLayoutInflater().inflate(R.layout.bottom_recycle_view, bottomGroup, false);
                            bottomList = (RecyclerView) view.findViewById(R.id.horizal_recyler_view);

                            LinearLayoutManager horizalLayout = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
                            bottomList.setLayoutManager(horizalLayout);

                            final ClothAdapter adapter = new ClothAdapter(MainActivity.this, catalogModelList.get(which).getId());
                            bottomList.swapAdapter(adapter, true);
                            // TODO: need lazy load in the future.
/*
           bottomList.clearOnScrollListeners();
            ClothRecyclerViewScrollListener listener = new_flag ClothRecyclerViewScrollListener(horizalLayout) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    if ((page * 9) >= totalItemsCount) return;

                    adapter.loadMoreItem(page);
                }
            };
            listener.resetState();
            bottomList.addOnScrollListener(listener);
*/
                            bottomGroup.addView(bottomList);
                        }
                    });
            catalogDiag.show();
        } else {
            UtilitiesHelper.showMessage(MainActivity.this, getString(R.string.connection_fail));
        }
    }

   /* private void initialFInterstitialAd() {
        fInterstitialAd = new com.facebook.ads.InterstitialAd(this, getString(R.string.fan_interestial_placement_id));
        fInterstitialAd.setAdListener(new AbstractAdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(Constants.LOG_TAG, "Error when load FAN: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
//                getAdRequestDialog().show();
                showAdInterstial();
            }

            @Override
            public void onAdClicked(Ad ad) {
                super.onAdClicked(ad);
            }

            @Override
            public void onInterstitialDisplayed(Ad ad) {
                super.onInterstitialDisplayed(ad);
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                super.onInterstitialDismissed(ad);
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                super.onLoggingImpression(ad);
            }
        });

        fInterstitialAd.loadAd();
    }*/

//Remove full screen Ad
   /* private void initialGInterstitialAd() {
        gInterstitialAd = new InterstitialAd(this);

        // set the ad unit ID
        gInterstitialAd.setAdUnitId(getString(R.string.admob_interstitial_full_screen));

        AdRequest adRequest = UtilitiesHelper.getAdmobRequest();

        // Load ads into Interstitial Ads
        gInterstitialAd.loadAd(adRequest);
        gInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showAdInterstial();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.d(Constants.LOG_TAG, "Fail to load admob interstitial ads");
                initialFInterstitialAd();
            }
        });
    }*/

    private void initialChangeHair() {
        bottomGroup.removeAllViewsInLayout();

        View view = getLayoutInflater().inflate(R.layout.bottom_recycle_view, bottomGroup, false);
        bottomList = (RecyclerView) view.findViewById(R.id.horizal_recyler_view);
        List<HairModel> lstHair = null;
        LinearLayoutManager horizalLayout = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        bottomList.setLayoutManager(horizalLayout);

        try {
            AppDataBaseHelper helper = new AppDataBaseHelper(MainActivity.this);
            SQLiteDatabase db = helper.getWritableDatabase();
            HairDAO hairDAO = new HairDAO(db);
            lstHair = hairDAO.getAll();
            HairAdapter adapter = new HairAdapter(MainActivity.this, lstHair);
            bottomList.swapAdapter(adapter, true);
            bottomGroup.addView(bottomList);
        } catch (SQLiteCantOpenDatabaseException ex) {
            Log.e(Constants.LOG_TAG, "Error on initialChangeHair", ex);
            UtilitiesHelper.showMessage(MainActivity.this, getString(R.string.load_hair_fail));
        }
    }

    private void initialChangeTrouser() {
        bottomGroup.removeAllViewsInLayout();

        View view = getLayoutInflater().inflate(R.layout.bottom_recycle_view, bottomGroup, false);
        bottomList = (RecyclerView) view.findViewById(R.id.horizal_recyler_view);
        LinearLayoutManager horizalLayout = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        bottomList.setLayoutManager(horizalLayout);

        List<String> colors = new ArrayList<>();
        colors.add("#B200FF");
        colors.add("#FF006E");
        colors.add("#FF00DC");
        colors.add("#4800FF");
        colors.add("#00137F");
        colors.add("#0094FF");
        colors.add("#00FFFF");
        colors.add("#00FF90");
        colors.add("#00FF21");
        colors.add("#4CFF00");
        colors.add("#B6FF00");
        colors.add("#FFD800");
        colors.add("#FF6A00");
        colors.add("#FF0000");
        colors.add("#404040");
        colors.add("#000000");
        colors.add("#7F0037");
        colors.add("#7F006E");
        colors.add("#57007F");
        colors.add("#21007F");
        colors.add("#00137F");
        colors.add("#004A7F");
        colors.add("#007F7F");
        colors.add("#007F46");
        colors.add("#007F7F");
        colors.add("#007F46");
        colors.add("#007F0E");
        colors.add("#267F00");
        colors.add("#5B7F00");
        colors.add("#7F6A00");
        colors.add("#7F3300");
        colors.add("#7F0000");
        colors.add("#808080");

        Log.d(Constants.LOG_TAG, "Colors count: " + colors.size());
        TrouserColorAdapter adapter = new TrouserColorAdapter(MainActivity.this, colors, selectedTrouserColor);
        bottomList.swapAdapter(adapter, true);
        bottomGroup.addView(bottomList);
    }

    private void initialEvents() {
        leftMenu.setNavigationItemSelectedListener(this);

        Log.d(Constants.LOG_TAG, String.format("bottomGroup Y: $1%s, fabButton Y: $2%s", bottomGroup.getHeight(), fab.getHeight()));
        fab.setTranslationY(-112f);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onbtnPhotoClick(view);
            }
        });

        faceView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showHideBottomMenu();
            }
        });

        btnAppCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + Constants.CALL_CENTER_NUMBER));
                startActivity(dialIntent);
            }
        });
    }
   /*

    private void showAdInterstial() {
        if (gInterstitialAd != null && gInterstitialAd.isLoaded()) {
            gInterstitialAd.show();
        } else if (fInterstitialAd != null && fInterstitialAd.isAdLoaded()) {
            fInterstitialAd.show();
        }
    }

   private AlertDialog getAdRequestDialog() {
        AlertDialog diagAdRequest = new AlertDialog.Builder(MainActivity.this)
                .setMessage(R.string.show_ad_request_message)
                .setNegativeButton(R.string.ok, showAdRequestListener)
                .setPositiveButton(R.string.no, showAdRequestListener)
                .create();
        return diagAdRequest;
    }
    */

    DialogInterface.OnClickListener perMissionDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
                case BUTTON_POSITIVE:
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            Constants.MY_STORAGE_PERMISSIONS_REQUEST);
                    dialog.dismiss();
                    break;
                default:
                    dialog.dismiss();
                    break;
            }
        }
    };

    /* DialogInterface.OnClickListener showAdRequestListener = new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialogInterface, int i) {
             switch (i) {
                 case BUTTON_NEGATIVE:
                     dialogInterface.dismiss();
                     showAdInterstial();
                     break;
                 case BUTTON_POSITIVE:
                     dialogInterface.dismiss();
                     break;
                 default:
                     dialogInterface.dismiss();
                     break;
             }
         }
     };*/
    private final Runnable mDrawRunnable = new Runnable() {
        @Override
        public void run() {
            detectFaceAction();
            setWaiting(false);
        }
    };

    private final Runnable mDrawFaceViewRunnable = new Runnable() {
        @Override
        public void run() {
            setWaiting(false);
        }
    };

    class ParsedCatalogAsyn extends AsyncTask<Integer, Void, List<CatalogModel>> {
        @Override
        protected void onPreExecute() {
            Log.d(Constants.LOG_TAG, "Begin parse catalog from Json");
        }

        @Override
        protected List<CatalogModel> doInBackground(Integer... args) {
            WebServiceHelper wsHelper = WebServiceHelper.getInstance();
            String strJson = null;
            if (app.getPreferences().contains(Constants.CAT_JSON_CATCHED_NAME)) {
                Log.d(Constants.LOG_TAG, "Got catalog json from shared memory");
                strJson = app.getPreferences().getString(Constants.CAT_JSON_CATCHED_NAME, null);
            }

            if (strJson == null) {
                Log.d(Constants.LOG_TAG, "Cannot see catalog json from shared memory, begin get from URL");
                strJson = wsHelper.GetJsonFromUrl(String.format(Constants.GET_CATALOG_URL));
            }

            MyJsonParser<CatalogModel> parser = new MyJsonParser<>();
            Type collectionType = new TypeToken<List<CatalogModel>>() {
            }.getType();

            return parser.parseJSON(strJson, collectionType);
        }

        @Override
        protected void onPostExecute(List<CatalogModel> parsedObj) {
            catalogModelList = parsedObj;
        }
    }
}
