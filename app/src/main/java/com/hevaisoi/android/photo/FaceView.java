/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hevaisoi.android.photo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;
import com.hevaisoi.android.Constants;
import com.hevaisoi.android.R;
import com.hevaisoi.android.UtilitiesHelper;
import com.hevaisoi.android.databases.AppDataBaseHelper;
import com.hevaisoi.android.databases.HairDAO;
import com.hevaisoi.android.model.HairModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * View which displays a bitmap containing a face along with overlay graphics that identify the
 * locations of detected facial landmarks.
 */
public class FaceView extends View {
    private static final float fWidthPercent = 0.33f;
    private static final float fHeightPercent = 0.12f;

    //0..10 1 is default
    private static final float fBrightness = 0f;

    private Bitmap bmFace;
    private Bitmap bmBody;
    private Bitmap bmCloth;
    private HairModel hairModel;
    private int faceWidth = 0;
    private int faceHeight = 0;
    private int coverWidth = 0;
    private int coverHeight = 0;
    private float centerX = 0;
    private float centerY = 0;
    private float centerXCover = 0;
    private String selectedTrouserColor = null;

    //-255..255 0 is default
    private float fContrast = 1.5f;

    private SparseArray<Face> mFaces;

    private class RetrieveImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... args) {
            Bitmap bitmap = UtilitiesHelper.retrieveBitmap(args[0]);
            Log.d(Constants.LOG_TAG, "Begin get image asyn");
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
//                app.getImageCache().put(clothId,bitmap);
                bmCloth = bitmap;
                postInvalidate();
            }
        }
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Load first hair model on create
        AppDataBaseHelper helper = new AppDataBaseHelper(getContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        HairDAO hairDAO = new HairDAO(db);
        hairModel = hairDAO.getFirst();
        if (db.isOpen()) {
            db.close();
        }
        setDrawingCacheEnabled(true);
    }

    /**
     * Sets the bitmap background and the associated face detections.
     */
    public void setBmFace(Bitmap bitmap, SparseArray<Face> faces) {
        bmFace = bitmap;
        mFaces = faces;
        invalidate();
    }

    public void setSelectedHair(int selectedHair) {
        AppDataBaseHelper helper = new AppDataBaseHelper(getContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        HairDAO hairDAO = new HairDAO(db);
        hairModel = hairDAO.get(selectedHair);
    }

    public void setSelectedTrouserColor(String strTrouserColor) {
        this.selectedTrouserColor = strTrouserColor;
    }

    /*
    public boolean save() {
        Bitmap saveBitmap = getDrawingCache();
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File file = new_flag File(path, "hevaisoi.jpg");
        try {
            FileOutputStream fileOutputStream = new_flag FileOutputStream(file, true);
            if (saveBitmap != null) {
                saveBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                return true;
            }
        } catch (FileNotFoundException e) {
            Log.e(Constants.LOG_TAG, "Error when saving canvas", e);
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "Error when saving canvas", e);
        }
        return false;
    }
*/

    /**
     * @param canvas input canvas
     * @param bmp    input bitmap
     * @param paint  paint to draw
     */
    public void changeBitmapContrastBrightness(Canvas canvas, Bitmap bmp, Paint paint) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        fContrast, 0, 0, 0, fBrightness,
                        0, fContrast, 0, 0, fBrightness,
                        0, 0, fContrast, 0, fBrightness,
                        0, 0, 0, 1, 0
                });
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, centerX, centerY, paint);
    }

    public void setContrast(float fContrast) {
        this.fContrast = fContrast;
//        this.invalidate();
    }

    /**
     * Draws the bitmap background and the associated face landmarks.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((bmFace != null) && (mFaces != null)) {
            Bitmap bitmap = Bitmap.createBitmap((int) canvas.getWidth(), (int) canvas.getHeight(), Bitmap.Config.ARGB_8888);

            Canvas tempCanvas = new Canvas(bitmap);
            double scale = 0;

            // Load cloth from URL, if cannot redraw it with default
            try {
                scale = getBodySize(tempCanvas);
                if (drawFace(tempCanvas)) {
                    drawTextile(tempCanvas);
                    drawBody(tempCanvas, scale);
                    drawHair(tempCanvas);
                }
            } catch (IOException e) {
                Log.e(Constants.LOG_TAG, "Error when drawTextile:", e);
                Toast.makeText(getContext(),
                        getContext().getString(R.string.cannot_load_cloth), Toast.LENGTH_LONG).show();
                bmCloth = null;
                try {
                    drawTextile(tempCanvas);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            int replaceColor = Color.TRANSPARENT;
            foodFill(bitmap, new Point((int) centerXCover, 1), replaceColor);
            canvas.drawBitmap(bitmap, 0, 0, null);
            bitmap.recycle();
            Log.w(Constants.LOG_TAG, "Scale: " + scale);
        }
    }

    private double getBodySize(Canvas canvas) throws IOException {
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        InputStream stream = getContext().getAssets().open("cover.png");
        bmBody = BitmapFactory.decodeStream(stream);

        double imageWidth = bmBody.getWidth();
        double imageHeight = bmBody.getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
        coverWidth = (int) (imageWidth * scale);
        coverHeight = (int) (imageHeight * scale);
        centerXCover = (float) (viewWidth * 0.3f);

        return scale;
    }

    private boolean drawFace(Canvas canvas) {
        if (mFaces == null || mFaces.size() == 0) return false;

        Bitmap faceBitmap = getBitmapFace(mFaces.valueAt(0));
        if (faceBitmap == null) return false;

        Log.w(Constants.LOG_TAG, String.format("Face width: %1$d, height: %2$d", faceBitmap.getWidth(), faceBitmap.getHeight()));
        faceBitmap = rotateFace(faceBitmap, mFaces.valueAt(0));
        faceBitmap = scaleFace(canvas, faceBitmap);
        Log.w(Constants.LOG_TAG, String.format("After scale, face width: %1$d, height: %2$d", faceBitmap.getWidth(), faceBitmap.getHeight()));

        centerX = (float) (coverWidth - faceWidth) * 0.52f + canvas.getWidth() * 0.3f;
        centerY = (float) (coverHeight - faceHeight) * 0.11f;
        Log.w(Constants.LOG_TAG, String.format("centerX: %1$f - centerY: %2$f", centerX, centerY));
        Matrix matrix = new Matrix();
        matrix.setTranslate(centerX, centerY);
        Log.w(Constants.LOG_TAG, String.format("Canvas width: %1$d, height: %2$d", canvas.getWidth(), canvas.getHeight()));
        BitmapShader shader;
        shader = new BitmapShader(faceBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        shader.setLocalMatrix(matrix);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        changeBitmapContrastBrightness(canvas, faceBitmap, paint);
        canvas.drawBitmap(faceBitmap, centerX, centerY, paint);

        faceBitmap.recycle();

        return true;
    }

    /**
     * Draws the bitmap background, scaled to the device size.  Returns the scale for future use in
     * positioning the facial landmark graphics.
     */
    private void drawBody(Canvas canvas, double scale) {
        double imageWidth = bmBody.getWidth();
        double imageHeight = bmBody.getHeight();
        Bitmap bitmap = null;
        if (selectedTrouserColor != null &&
                selectedTrouserColor != "") {

            int replaceColor = Color.parseColor(selectedTrouserColor);
            Log.d(Constants.LOG_TAG, String.format("Replace color: %1$d (R: %2$d, G: %3$d, B: %4$d)", replaceColor, Color.red(replaceColor), Color.green(replaceColor), Color.blue(replaceColor)));
            Bitmap copyBitmap = bmBody.copy(Bitmap.Config.ARGB_8888, true);
            copyBitmap.setHasAlpha(true);
            foodFill(copyBitmap, new Point(171, 371), replaceColor);
            bitmap = Bitmap.createScaledBitmap(copyBitmap,
                    (int) (imageWidth * scale),
                    (int) (imageHeight * scale),
                    false);
            copyBitmap.recycle();
        } else {
            bitmap = Bitmap.createScaledBitmap(bmBody,
                    (int) (imageWidth * scale),
                    (int) (imageHeight * scale),
                    false);
        }
        Log.w(Constants.LOG_TAG, String.format("centerX of Cover: %1$f", centerXCover));
        canvas.drawBitmap(bitmap, centerXCover, 0, null);
        bitmap.recycle();
        bmBody.recycle();
    }

    private void drawHair(Canvas canvas) throws IOException {
       /*
        hairModel = new HairModel();
        hairModel.setFileName("normal_short_hair");
        hairModel.setWidthScale(0.40f);
        hairModel.setHeightScale(0.14f);
        hairModel.setxScale(0.13);
        hairModel.setyScale(0.08);
       */

        if (hairModel == null) {
            Log.e(Constants.LOG_TAG, "Cannot load first hair entity in database");
            Toast.makeText(getContext(),
                    getContext().getString(R.string.cannot_load_hair), Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(Constants.LOG_TAG, "Selected hair: " + hairModel.getFileName());
        InputStream stream = getContext().getAssets().open(hairModel.getFileName() + ".png"); /*getResources().openRawResource(UtilitiesHelper.getResId(hairModel.getFileName(), R.raw.class));*/
        Bitmap bmHair = BitmapFactory.decodeStream(stream);
        double crownWidth = coverWidth * hairModel.getWidthScale();
        double crownHeight = coverHeight * hairModel.getHeightScale();

        bmHair = Bitmap.createScaledBitmap(bmHair,
                (int) crownWidth,
                (int) crownHeight,
                false);

        float xHair = centerX - (float) crownWidth * (float) hairModel.getxScale();
        float yHair = centerY - (float) crownHeight * (float) hairModel.getyScale();

        canvas.drawBitmap(bmHair, xHair, yHair, null);
        bmHair.recycle();
    }

    private void drawTextile(Canvas canvas) throws IOException {
        InputStream stream = null;
        if (bmCloth == null) {
            stream = getResources().openRawResource(R.raw.cloth_default);
            bmCloth = BitmapFactory.decodeStream(stream);
            stream.close();
        }

        int offset = (int) (coverHeight * 0.215f);
        int textileHeight = (int) (coverHeight - offset);
        Log.w(Constants.LOG_TAG, String.format("Cover width: %1$d, cover height: %2$d", coverWidth, coverHeight));
        Log.w(Constants.LOG_TAG, "Textile height: " + textileHeight);
        Bitmap bitmap = Bitmap.createScaledBitmap(bmCloth, coverWidth, textileHeight, false);
        canvas.drawBitmap(bitmap, centerXCover, offset, null);
        bitmap.recycle();
    }

    private Bitmap getBitmapFace(Face face) {
        if (face == null) return null;
        for (Landmark landmark : face.getLandmarks()) {
            if (landmark.getType() == Landmark.NOSE_BASE) {
                int cx = (int) (landmark.getPosition().x);
                int cy = (int) (landmark.getPosition().y);
                float left = cx - face.getWidth() / 2;
                float top = cy - face.getHeight() / 2;
                float right = cx + face.getWidth() / 2;
                float bottom = cy + face.getHeight() / 3;

                Log.w(Constants.LOG_TAG, String.format("L: %1$f, R: %2$f, T: %3$f, B: %4$f", left, right, top, bottom));

                Bitmap bitmapFace = null;
                try {
                    bitmapFace = Bitmap.createBitmap(bmFace,
                            (int) left,
                            (int) top,
                            (int) (right - left),
                            (int) (bottom - top));
                } catch (IllegalArgumentException ex){
                    UtilitiesHelper.showMessage(getContext(), getResources().getString(R.string.face_irregular_detect));
                    return bitmapFace;
                }

                bitmapFace.setConfig(Bitmap.Config.ARGB_8888);
                return bitmapFace;
            }
        }
        return null;
    }

    private Bitmap rotateFace(Bitmap src, Face face) {
        if (face == null) return src;
        PointF pointLEye = null;
        PointF pointREye = null;
        //get position of left and right eye
        for (Landmark landmark : face.getLandmarks()) {
            switch (landmark.getType()) {
                case Landmark.LEFT_EYE:
                    pointLEye = landmark.getPosition();
                    continue;
                case Landmark.RIGHT_EYE:
                    pointREye = landmark.getPosition();
                    continue;
                default:
                    continue;
            }
        }

        if (pointLEye == null || pointREye == null) return src;

        Matrix matrix = new Matrix();
        double degree = Math.toDegrees(Math.atan2((pointLEye.y - pointREye.y),
                (pointLEye.x - pointREye.x)));
        Log.w(Constants.LOG_TAG, "Degree before offset: " + degree);
        if (degree < 0) {
            degree += 360;
        } else if (degree > 0) {
            degree -= 360;
        }
        Log.w(Constants.LOG_TAG, "Degree to rotate face: " + degree);
        matrix.postRotate((float) Math.abs(degree));

        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    //    This function will return faceWidth and faceHeight after scaling
    private Bitmap scaleFace(Canvas canvas, Bitmap src) {
        if (src == null) return null;

        faceWidth = Math.round((int) (coverWidth * fWidthPercent));
        faceHeight = Math.round((int) (coverHeight * fHeightPercent));

        return Bitmap.createScaledBitmap(src, faceWidth, faceHeight, false);
    }

    private void foodFill(Bitmap bmp, Point pt, int replacementColor) {

        int targetColor = bmp.getPixel(pt.x, pt.y);
        Log.d(Constants.LOG_TAG, String.format("Before source color: %1$d (R: %2$d, G: %3$d, B: %4$d)", targetColor, Color.red(targetColor), Color.green(targetColor), Color.blue(targetColor)));

        if (targetColor == replacementColor) return;

        Queue<Point> q = new LinkedList<>();
        q.add(pt);
        while (q.size() > 0) {
            Point n = q.poll();
            if (bmp.getPixel(n.x, n.y) != targetColor)
                continue;

            Point w = n, e = new Point(n.x + 1, n.y);

            while ((w.x > 0) && (bmp.getPixel(w.x, w.y) == targetColor)) {
                bmp.setPixel(w.x, w.y, replacementColor);
                if ((w.y > 0) && (bmp.getPixel(w.x, w.y - 1) == targetColor))
                    q.add(new Point(w.x, w.y - 1));
                if ((w.y < bmp.getHeight() - 1) && (bmp.getPixel(w.x, w.y + 1) == targetColor))
                    q.add(new Point(w.x, w.y + 1));

                w.x--;
            }

            while ((e.x < bmp.getWidth() - 1) && (bmp.getPixel(e.x, e.y) == targetColor)) {
                bmp.setPixel(e.x, e.y, replacementColor);
                if ((e.y > 0) && (bmp.getPixel(e.x, e.y - 1) == targetColor))
                    q.add(new Point(e.x, e.y - 1));
                if ((e.y < bmp.getHeight() - 1) && (bmp.getPixel(e.x, e.y + 1) == targetColor))
                    q.add(new Point(e.x, e.y + 1));

                e.x++;
            }
        }

        targetColor = bmp.getPixel(pt.x, pt.y);
        Log.d(Constants.LOG_TAG, String.format("After replacing color: %1$d (R: %2$d, G: %3$d, B: %4$d)", targetColor, Color.red(targetColor), Color.green(targetColor), Color.blue(targetColor)));
    }

    public void setCloth(String clothUrl) {
        new RetrieveImageTask().execute(clothUrl);
    }
}
