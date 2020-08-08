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

package com.azhariharisalhamdi.ODIR5K;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azhariharisalhamdi.ODIR5K.imageprocessing.ImageProcessing;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.common.collect.ImmutableList;
import com.azhariharisalhamdi.ODIR5K.R;
import com.azhariharisalhamdi.ODIR5K.productsearch.BottomSheetScrimView;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.azhariharisalhamdi.ODIR5K.objectdetection.DetectedObject;
import com.azhariharisalhamdi.ODIR5K.objectdetection.StaticObjectDotView;
import com.azhariharisalhamdi.ODIR5K.productsearch.PreviewCardAdapter;
import com.azhariharisalhamdi.ODIR5K.productsearch.Product;
import com.azhariharisalhamdi.ODIR5K.productsearch.ProductAdapter;
import com.azhariharisalhamdi.ODIR5K.productsearch.SearchEngine;
import com.azhariharisalhamdi.ODIR5K.productsearch.SearchEngine.SearchResultListener;
import com.azhariharisalhamdi.ODIR5K.productsearch.SearchedObject;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/** Demonstrates the object detection and visual search workflow using static image. */
public class StaticObjectDetectionActivity extends AppCompatActivity
    implements View.OnClickListener, PreviewCardAdapter.CardItemListener, SearchResultListener {

  private static final String TAG = "StaticObjectActivity";
  private static final int MAX_IMAGE_DIMENSION = 1024;

  private final TreeMap<Integer, SearchedObject> searchedObjectMap = new TreeMap<>();

  private View loadingView;
  private Chip bottomPromptChip;
  private ImageView inputImageView;
  private RecyclerView previewCardCarousel;
  private ViewGroup dotViewContainer;

  private BottomSheetBehavior<View> bottomSheetBehavior;
  private BottomSheetScrimView bottomSheetScrimView;
  private TextView bottomSheetTitleView, bottomSheetPredictionview;
  private RecyclerView productRecyclerView;
    protected ImageView bottomSheetArrowImageView;

  private Bitmap inputBitmap;
  private SearchedObject searchedObjectForBottomSheet;
  private int dotViewSize;
  private int detectedObjectNum = 0;
  private int currentSelectedObjectIndex = 0;

  private FirebaseVisionObjectDetector detector;
  private SearchEngine searchEngine;
  private ImageProcessing imageProcessing;

  public boolean object_detection_mode = false;

  public String title_buttom_sheet;

  static {
    if (!OpenCVLoader.initDebug()) {
      Log.e("OpenCV", "Cannot connect to OpenCV Manager");
    } else {
      Log.e("OpenCV", "Connected Successfully");
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    searchEngine = new SearchEngine(getApplicationContext());
    if(object_detection_mode)
        setContentView(R.layout.activity_static_object);
    else
        setContentView(R.layout.activity_static_no_object);

    loadingView = findViewById(R.id.loading_view);
    loadingView.setOnClickListener(this);
    inputImageView = findViewById(R.id.input_image_view);
    bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);

    Resources res = getResources();
    title_buttom_sheet = res.getString(R.string.diagnosis_predictions);

    if(object_detection_mode) {
      bottomPromptChip = findViewById(R.id.bottom_prompt_chip);
      previewCardCarousel = findViewById(R.id.card_recycler_view);
      previewCardCarousel.setHasFixedSize(true);
      previewCardCarousel.setLayoutManager(
              new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
      previewCardCarousel.addItemDecoration(new CardItemDecoration(getResources()));

      dotViewContainer = findViewById(R.id.dot_view_container);
      dotViewSize = getResources().getDimensionPixelOffset(R.dimen.static_image_dot_view_size);
    }

    setUpBottomSheet();

    findViewById(R.id.close_button).setOnClickListener(this);
    findViewById(R.id.photo_library_button).setOnClickListener(this);

    if (object_detection_mode){
      detector =
              FirebaseVision.getInstance()
                      .getOnDeviceObjectDetector(
                              new FirebaseVisionObjectDetectorOptions.Builder()
                                      .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
                                      .enableMultipleObjects()
                                      .build());
    }

    if (getIntent().getData() != null) {
      if(object_detection_mode) {
        detectObjects(getIntent().getData());
      }else{
        try {
          diagnosePict(getIntent().getData());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if(object_detection_mode)
    {
      try {
        detector.close();
      } catch (IOException e) {
        Log.e(TAG, "Failed to close the detector!", e);
      }
    }
    imageProcessing.close();
    searchEngine.shutdown();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == Utils.REQUEST_CODE_PHOTO_LIBRARY
        && resultCode == Activity.RESULT_OK
        && data != null
        && data.getData() != null) {
      if(object_detection_mode){
        detectObjects(data.getData());
      }else {
        try {
          diagnosePict(data.getData());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  public void onBackPressed() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
      super.onBackPressed();
  }

  @Override
  public void onClick(View view) {
    int id = view.getId();
    if(object_detection_mode){
        if (id == R.id.close_button) {
          onBackPressed();
        } else if (id == R.id.photo_library_button) {
          Utils.openImagePicker(this);
        } else if (id == R.id.bottom_sheet_scrim_view) {
          bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }else{
        if (id == R.id.close_button) {
            onBackPressed();
        } else if (id == R.id.photo_library_button) {
            Utils.openImagePicker(this);
        }
    }
  }

  @Override
  public void onPreviewCardClicked(SearchedObject searchedObject) {
    showSearchResults(searchedObject);
  }

    private void showSearchResults(SearchedObject searchedObject) {
        searchedObjectForBottomSheet = searchedObject;
        List<Product> productList = searchedObject.getProductList();
        String results = searchedObject.getResults();
        bottomSheetTitleView.setText(title_buttom_sheet);
        bottomSheetPredictionview.setText(results);
        productRecyclerView.setAdapter(new ProductAdapter(productList));
        bottomSheetBehavior.setPeekHeight(((View) inputImageView.getParent()).getHeight() / 2);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void showDiagnosisResults(List<Product> product_List, String result) {
        Log.d(TAG, "in showDiagnosisResults");
        List<Product> productList = product_List;
        String results = result;
        Log.d(TAG, "in showDiagnosisResults result:"+results);
        bottomSheetTitleView.setText(title_buttom_sheet);
        bottomSheetPredictionview.setText(results);
        productRecyclerView.setAdapter(new ProductAdapter(productList));
        int line_count = results.split("\n").length + 1;
        int peekHeight = line_count > 1 ? 155 + line_count*50 : 155;
        bottomSheetBehavior.setPeekHeight(peekHeight);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void callImagePicker(){
        Utils.openImagePicker(this);
    }

  private void setUpBottomSheet() {
    bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
    bottomSheetBehavior.setBottomSheetCallback(
        new BottomSheetBehavior.BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {
              Log.d(TAG, "Bottom sheet new state: " + newState);
              if(object_detection_mode) {
                 bottomSheetScrimView.setVisibility(newState == BottomSheetBehavior.STATE_HIDDEN ? View.GONE : View.VISIBLE);
              }
              switch (newState) {
                  case BottomSheetBehavior.STATE_HIDDEN:
                      break;
                  case BottomSheetBehavior.STATE_EXPANDED:
                  {
                      bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down);
                  }
                  break;
                  case BottomSheetBehavior.STATE_COLLAPSED:
                  {
                      bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                  }
                  break;
                  case BottomSheetBehavior.STATE_DRAGGING:
                      break;
                  case BottomSheetBehavior.STATE_SETTLING:
                      bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                      break;
              }
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            if (Float.isNaN(slideOffset)) {
              return;
            }

            int collapsedStateHeight =
                Math.min(bottomSheetBehavior.getPeekHeight(), bottomSheet.getHeight());
            if(object_detection_mode) {
              bottomSheetScrimView.updateWithThumbnailTranslate(
                      searchedObjectForBottomSheet.getObjectThumbnail(),
                      collapsedStateHeight,
                      slideOffset,
                      bottomSheet);
            }
          }
        });

    if(object_detection_mode) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetScrimView = findViewById(R.id.bottom_sheet_scrim_view);
        bottomSheetScrimView.setOnClickListener(this);
    }
    bottomSheetTitleView = findViewById(R.id.bottom_sheet_title);
    bottomSheetPredictionview = findViewById(R.id.diagnosis_prediction);
    productRecyclerView = findViewById(R.id.product_recycler_view);
    productRecyclerView.setHasFixedSize(true);
    productRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    productRecyclerView.setAdapter(new ProductAdapter(ImmutableList.of()));
  }

  private void detectObjects(Uri imageUri) {
    inputImageView.setImageDrawable(null);
    bottomPromptChip.setVisibility(View.GONE);
    previewCardCarousel.setAdapter(new PreviewCardAdapter(ImmutableList.of(), this));
    previewCardCarousel.clearOnScrollListeners();
    dotViewContainer.removeAllViews();
    currentSelectedObjectIndex = 0;
    try {
        inputBitmap = Utils.loadImage(this, imageUri, MAX_IMAGE_DIMENSION);
        imageProcessing = new ImageProcessing(inputBitmap);
        imageProcessing.processGammaHE(0.7, 1.3,20, 10,10);
        inputBitmap = imageProcessing.getBitmapImage();
    } catch (IOException e) {
      Log.e(TAG, "Failed to load file: " + imageUri, e);
      showBottomPromptChip("Failed to load file!");
      return;
    }

    inputImageView.setImageBitmap(inputBitmap);
    loadingView.setVisibility(View.VISIBLE);
    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(inputBitmap);
    detector
        .processImage(image)
        .addOnSuccessListener(objects -> onObjectsDetected(image, objects))
        .addOnFailureListener(e -> onObjectsDetected(image, ImmutableList.of()));
  }

    @MainThread
    public void diagnosePict(Uri imageUri) throws IOException {
        inputImageView.setImageDrawable(null);
        productRecyclerView.setAdapter(new ProductAdapter(ImmutableList.of()));
        productRecyclerView.clearOnScrollListeners();
        try {
            inputBitmap = Utils.loadImage(this, imageUri, MAX_IMAGE_DIMENSION);
            imageProcessing = new ImageProcessing(inputBitmap);
            imageProcessing.getMatCropCenter();
            imageProcessing.processGammaHE(0.8, 1.2,20, 10,10);
            inputBitmap = imageProcessing.getBitmapImage();
        } catch (IOException e) {
            Log.e(TAG, "Failed to load file: " + imageUri, e);
            showBottomPromptChip("Failed to load file!");
            return;
        }

        Log.d(TAG, "in diagnosePict");
        inputImageView.setImageBitmap(inputBitmap);
        loadingView.setVisibility(View.VISIBLE);
        searchEngine.predict(this, inputBitmap,this);
    }

  @MainThread
  private void onObjectsDetected(FirebaseVisionImage image, List<FirebaseVisionObject> objects) {
    detectedObjectNum = objects.size();
    Log.d(TAG, "Detected objects num: " + detectedObjectNum);
    if (detectedObjectNum == 0) {
      loadingView.setVisibility(View.GONE);
      showBottomPromptChip(getString(R.string.static_image_prompt_detected_no_results));
    } else {
      searchedObjectMap.clear();
      for (int i = 0; i < objects.size(); i++) {
        try {
          searchEngine.search(this, new DetectedObject(objects.get(i), i, image), /* listener= */ this);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public void onSearchCompleted(DetectedObject object, List<Product> productList, String result) {
    if(object_detection_mode) {
      Log.d(TAG, "Search completed for object index: " + object.getObjectIndex());
      searchedObjectMap.put(
              object.getObjectIndex(), new SearchedObject(getResources(), object, productList, result));
      if (searchedObjectMap.size() < detectedObjectNum) {
        // Hold off showing the result until the search of all detected objects completes.
        return;
      }

      showBottomPromptChip(getString(R.string.static_image_prompt_detected_results));
      loadingView.setVisibility(View.GONE);
      previewCardCarousel.setAdapter(
              new PreviewCardAdapter(ImmutableList.copyOf(searchedObjectMap.values()), this));
      previewCardCarousel.addOnScrollListener(
              new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                  Log.d(TAG, "New card scroll state: " + newState);
                  if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    for (int i = 0; i < recyclerView.getChildCount(); i++) {
                      View childView = recyclerView.getChildAt(i);
                      if (childView.getX() >= 0) {
                        int cardIndex = recyclerView.getChildAdapterPosition(childView);
                        if (cardIndex != currentSelectedObjectIndex) {
                          selectNewObject(cardIndex);
                        }
                        break;
                      }
                    }
                  }
                }
              });

      for (SearchedObject searchedObject : searchedObjectMap.values()) {
        StaticObjectDotView dotView = createDotView(searchedObject);
        dotView.setOnClickListener(
                v -> {
                  if (searchedObject.getObjectIndex() == currentSelectedObjectIndex) {
                    showSearchResults(searchedObject);
                  } else {
                    selectNewObject(searchedObject.getObjectIndex());
                    showSearchResults(searchedObject);
                    previewCardCarousel.smoothScrollToPosition(searchedObject.getObjectIndex());
                  }
                });

        dotViewContainer.addView(dotView);
        AnimatorSet animatorSet =
                ((AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.static_image_dot_enter));
        animatorSet.setTarget(dotView);
        animatorSet.start();
      }
    }
  }

    @Override
    public void onSearchCompleted(List<Product> productList, String result) {
      loadingView.setVisibility(View.GONE);
      Log.d(TAG, "in onSearchCompleted type 2");
      showDiagnosisResults(productList, result);
    }

    private StaticObjectDotView createDotView(SearchedObject searchedObject) {
        float viewCoordinateScale;
        float horizontalGap;
        float verticalGap;
        float inputImageViewRatio = (float) inputImageView.getWidth() / inputImageView.getHeight();
        float inputBitmapRatio = (float) inputBitmap.getWidth() / inputBitmap.getHeight();
        if (inputBitmapRatio <= inputImageViewRatio) { // Image content fills height
          viewCoordinateScale = (float) inputImageView.getHeight() / inputBitmap.getHeight();
          horizontalGap =
              (inputImageView.getWidth() - inputBitmap.getWidth() * viewCoordinateScale) / 2;
          verticalGap = 0;
        } else { // Image content fills width
          viewCoordinateScale = (float) inputImageView.getWidth() / inputBitmap.getWidth();
          horizontalGap = 0;
          verticalGap =
              (inputImageView.getHeight() - inputBitmap.getHeight() * viewCoordinateScale) / 2;
        }

        Rect boundingBox = searchedObject.getBoundingBox();
        RectF boxInViewCoordinate =
            new RectF(
                boundingBox.left * viewCoordinateScale + horizontalGap,
                boundingBox.top * viewCoordinateScale + verticalGap,
                boundingBox.right * viewCoordinateScale + horizontalGap,
                boundingBox.bottom * viewCoordinateScale + verticalGap);
        boolean initialSelected = (searchedObject.getObjectIndex() == 0);
        StaticObjectDotView dotView = new StaticObjectDotView(this, initialSelected);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dotViewSize, dotViewSize);
        PointF dotCenter =
            new PointF(
                (boxInViewCoordinate.right + boxInViewCoordinate.left) / 2,
                (boxInViewCoordinate.bottom + boxInViewCoordinate.top) / 2);
        layoutParams.setMargins(
            (int) (dotCenter.x - dotViewSize / 2f), (int) (dotCenter.y - dotViewSize / 2f), 0, 0);
        dotView.setLayoutParams(layoutParams);
        return dotView;
    }

  private void selectNewObject(int objectIndex) {
    StaticObjectDotView dotViewToDeselect =
        (StaticObjectDotView) dotViewContainer.getChildAt(currentSelectedObjectIndex);
    dotViewToDeselect.playAnimationWithSelectedState(false);

    currentSelectedObjectIndex = objectIndex;

    StaticObjectDotView selectedDotView =
        (StaticObjectDotView) dotViewContainer.getChildAt(currentSelectedObjectIndex);
    selectedDotView.playAnimationWithSelectedState(true);
  }

  private void showBottomPromptChip(String message) {
    bottomPromptChip.setVisibility(View.VISIBLE);
    bottomPromptChip.setText(message);
  }

  private static class CardItemDecoration extends RecyclerView.ItemDecoration {

    private final int cardSpacing;

    private CardItemDecoration(Resources resources) {
      cardSpacing = resources.getDimensionPixelOffset(R.dimen.preview_card_spacing);
    }

    @Override
    public void getItemOffsets(
        @NonNull Rect outRect,
        @NonNull View view,
        @NonNull RecyclerView parent,
        @NonNull RecyclerView.State state) {
      int adapterPosition = parent.getChildAdapterPosition(view);
      outRect.left = adapterPosition == 0 ? cardSpacing * 2 : cardSpacing;
      if (parent.getAdapter() != null
          && adapterPosition == parent.getAdapter().getItemCount() - 1) {
        outRect.right = cardSpacing;
      }
    }
  }
}
