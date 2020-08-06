/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azhariharisalhamdi.ODIR5K.objectdetection;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Holds the detected object and its related image info.
 */
public class DetectedObject {

  private static final String TAG = "DetectedObject";
  private static final int MAX_IMAGE_WIDTH = 720;
  private static final double widthPercentage = 0.85;

  private final FirebaseVisionObject object;
  private final int objectIndex;
  private final FirebaseVisionImage image;

  @Nullable
  private Bitmap bitmap = null;
  @Nullable
  private byte[] jpegBytes = null;

  static {
    if (!OpenCVLoader.initDebug()) {
      Log.e("OpenCV", "Cannot connect to OpenCV Manager");
    } else {
      Log.e("OpenCV", "Connected Successfully");
    }
  }

  public DetectedObject(FirebaseVisionObject object, int objectIndex, FirebaseVisionImage image) {
    this.object = object;
    this.objectIndex = objectIndex;
    this.image = image;
  }

  @Nullable
  public Integer getObjectId() {
    return object.getTrackingId();
  }

  public int getObjectIndex() {
    return objectIndex;
  }

  public Rect getBoundingBox() {
    return object.getBoundingBox();
  }

  public synchronized Bitmap getBitmap() {
    if (bitmap == null) {
      Rect boundingBox = object.getBoundingBox();

      int preferedWidth = (int) widthPercentage * getScreenWidth();

      bitmap =
          Bitmap.createBitmap(
              image.getBitmap(),
              boundingBox.left,
              boundingBox.top,
              boundingBox.width(),
              boundingBox.height());
    }
    bitmap = processCLAHE(bitmap);
    return bitmap;
  }

  public Bitmap processCLAHE(Bitmap mbitmap){
    int w = mbitmap.getWidth();
    int h = mbitmap.getHeight();
    Mat mMat = new Mat(h,w, CvType.CV_8UC4);
    Mat labImage = new Mat(h,w, CvType.CV_8UC4);
    Bitmap tempBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    org.opencv.android.Utils.bitmapToMat(mbitmap, mMat);

    Imgproc.cvtColor(mMat, labImage, Imgproc.COLOR_BGR2Lab,3);

    java.util.List<Mat> Lab = new ArrayList<Mat>();

    Core.split(labImage,Lab);
    Mat L = Lab.get(0); // L,a,b are references, not copies
    Mat a = Lab.get(1);
    Mat b = Lab.get(2);

    CLAHE ce = Imgproc.createCLAHE();
    ce.setClipLimit(20);
    ce.setTilesGridSize(new Size(10, 10));
    ce.apply(L, L);

    Core.merge(new ArrayList<>(Arrays.asList(L, a, b)),labImage);
    Imgproc.cvtColor(labImage,mMat,Imgproc.COLOR_Lab2BGR);
    org.opencv.android.Utils.matToBitmap(mMat, tempBitmap);
    return tempBitmap;
  }

  @Nullable
  public synchronized byte[] getImageData() {
    if (jpegBytes == null) {
      try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
        getBitmap().compress(CompressFormat.JPEG, /* quality= */ 100, stream);
        jpegBytes = stream.toByteArray();
      } catch (IOException e) {
        Log.e(TAG, "Error getting object image data!");
      }
    }

    return jpegBytes;
  }

  public static int getScreenWidth() {
    return Resources.getSystem().getDisplayMetrics().widthPixels;
  }

  public static int getScreenHeight() {
    return Resources.getSystem().getDisplayMetrics().heightPixels;
  }
}
