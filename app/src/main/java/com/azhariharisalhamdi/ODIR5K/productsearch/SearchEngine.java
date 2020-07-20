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

package com.azhariharisalhamdi.ODIR5K.productsearch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.azhariharisalhamdi.ODIR5K.R;
import com.azhariharisalhamdi.ODIR5K.tflite.Classifier;
import com.google.android.gms.tasks.Tasks;
import com.azhariharisalhamdi.ODIR5K.objectdetection.DetectedObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.android.OpenCVLoader;
import com.azhariharisalhamdi.ODIR5K.env.BorderedText;
import com.azhariharisalhamdi.ODIR5K.env.Logger;
import com.azhariharisalhamdi.ODIR5K.tflite.Classifier;
import com.azhariharisalhamdi.ODIR5K.tflite.Classifier.Device;
import com.azhariharisalhamdi.ODIR5K.tflite.Classifier.Model;

/** A fake search engine to help simulate the complete work flow. */
public class SearchEngine {

  private static final String TAG = "SearchEngine";
  private static final Logger LOGGER = new Logger();
  private Bitmap rgbFrameBitmap = null;
  private long lastProcessingTimeMs;
  private Integer sensorOrientation;
  private Classifier classifier;
  private BorderedText borderedText;
  String resultRecognition;

  public interface SearchResultListener {
    void onSearchCompleted(DetectedObject object, List<Product> productList, String result);
  }

  private final RequestQueue searchRequestQueue;
  private final ExecutorService requestCreationExecutor;

  public SearchEngine(Context context) {
    searchRequestQueue = Volley.newRequestQueue(context);
    requestCreationExecutor = Executors.newSingleThreadExecutor();
  }

  protected int getScreenOrientation(Activity activity) {
    switch (activity.getWindowManager().getDefaultDisplay().getRotation()) {
      case Surface.ROTATION_270:
        return 270;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_90:
        return 90;
      default:
        return 0;
    }
  }

  protected void showResultsInBottomSheet(List<Classifier.Recognition> results, List<Product> productList) {
    resultRecognition = "";
    if (results != null && results.size() >= 3) {
      Classifier.Recognition recognition = results.get(0);
      productList.add(new Product(recognition.getTitle(), String.format("%.2f", (100 * recognition.getConfidence()))+"%"));
      if (recognition != null && recognition.getConfidence() > 0.5) {
        if (recognition.getTitle() != null) resultRecognition = recognition.getTitle();
        if (recognition.getConfidence() != null)
          resultRecognition = resultRecognition + " " + String.format("%.2f", (100 * recognition.getConfidence())) + "%";
      }
      Classifier.Recognition recognition1 = results.get(1);
      productList.add(new Product(recognition1.getTitle(), String.format("%.2f", (100 * recognition1.getConfidence()))+"%"));
      if (recognition1 != null && recognition1.getConfidence() > 0.5) {
        resultRecognition = resultRecognition + "\n";
        if (recognition1.getTitle() != null)
          resultRecognition = resultRecognition + recognition1.getTitle();
        if (recognition1.getConfidence() != null)
          resultRecognition = resultRecognition + " " + String.format("%.2f", (100 * recognition1.getConfidence())) + "%";
      }
      Classifier.Recognition recognition2 = results.get(2);
      productList.add(new Product(recognition2.getTitle(), String.format("%.2f", (100 * recognition2.getConfidence()))+"%"));
      if (recognition2 != null && recognition2.getConfidence() > 0.5) {
        resultRecognition = resultRecognition + "\n";
        if (recognition2.getTitle() != null)
          resultRecognition = resultRecognition + recognition2.getTitle();
        if (recognition2.getConfidence() != null)
          resultRecognition = resultRecognition + " " + String.format("%.2f", (100 * recognition2.getConfidence())) + "%";
      }
      Classifier.Recognition recognition3 = results.get(3);
      productList.add(new Product(recognition3.getTitle(), String.format("%.2f", (100 * recognition3.getConfidence()))+"%"));
      if (recognition3 != null && recognition3.getConfidence() > 0.5) {
        resultRecognition = resultRecognition + "\n";
        if (recognition3.getTitle() != null)
          resultRecognition = resultRecognition + recognition3.getTitle();
        if (recognition3.getConfidence() != null)
          resultRecognition = resultRecognition + " " + String.format("%.2f", (100 * recognition3.getConfidence())) + "%";
      }
      Classifier.Recognition recognition4 = results.get(4);
      productList.add(new Product(recognition4.getTitle(), String.format("%.2f", (100 * recognition4.getConfidence()))+"%"));
      if (recognition4 != null && recognition4.getConfidence() > 0.5) {
        resultRecognition = resultRecognition + "\n";
        if (recognition4.getTitle() != null)
          resultRecognition = resultRecognition + recognition4.getTitle();
        if (recognition4.getConfidence() != null)
          resultRecognition = resultRecognition + " " + String.format("%.2f", (100 * recognition4.getConfidence())) + "%";
      }
      Classifier.Recognition recognition5 = results.get(5);
      productList.add(new Product(recognition5.getTitle(), String.format("%.2f", (100 * recognition5.getConfidence()))+"%"));
      if (recognition5 != null && recognition5.getConfidence() > 0.5) {
        resultRecognition = resultRecognition + "\n";
        if (recognition5.getTitle() != null)
          resultRecognition = resultRecognition + recognition5.getTitle();
        if (recognition5.getConfidence() != null)
          resultRecognition = resultRecognition + " " + String.format("%.2f", (100 * recognition5.getConfidence())) + "%";
      }
      Classifier.Recognition recognition6 = results.get(6);
      productList.add(new Product(recognition6.getTitle(), String.format("%.2f", (100 * recognition6.getConfidence()))+"%"));
      if (recognition6 != null && recognition6.getConfidence() > 0.5) {
        resultRecognition = resultRecognition + "\n";
        if (recognition6.getTitle() != null)
          resultRecognition = resultRecognition + recognition6.getTitle();
        if (recognition6.getConfidence() != null)
          resultRecognition = resultRecognition + " " + String.format("%.2f", (100 * recognition6.getConfidence())) + "%";
      }
      Classifier.Recognition recognition7 = results.get(7);
      productList.add(new Product(recognition7.getTitle(), String.format("%.2f", (100 * recognition7.getConfidence()))+"%"));
      if (recognition7 != null && recognition7.getConfidence() > 0.5) {
        resultRecognition = resultRecognition + "\n";
        if (recognition7.getTitle() != null)
          resultRecognition = resultRecognition + recognition7.getTitle();
        if (recognition7.getConfidence() != null)
          resultRecognition = resultRecognition + " " + String.format("%.2f", (100 * recognition7.getConfidence())) + "%";
      }
    }
    if(resultRecognition == "") {resultRecognition = "No Diagnosis Prediction";}
  }

  //edit
  public void search(Activity activity, DetectedObject object, SearchResultListener listener) throws IOException {
    sensorOrientation = 90 - getScreenOrientation(activity);
    classifier = Classifier.create(activity, Model.MULTI_LABEL_MODEL, Device.CPU, 2);
    final List<Classifier.Recognition> results = classifier.recognizeImage(object.getBitmap(), sensorOrientation);
    // Crops the object image out of the full image is expensive, so do it off the UI thread.
    List<Product> productList = new ArrayList<>();
    showResultsInBottomSheet(results, productList);
    listener.onSearchCompleted(object, productList, resultRecognition);
  }

  private static JsonObjectRequest createRequest(DetectedObject searchingObject) throws Exception {
    byte[] objectImageData = searchingObject.getImageData();
    if (objectImageData == null) {
      throw new Exception("Failed to get object image data!");
    }

    // Hooks up with your own product search backend here.
    throw new Exception("Hooks up with your own product search backend.");
  }

  public void shutdown() {
    searchRequestQueue.cancelAll(TAG);
    requestCreationExecutor.shutdown();
  }
}
