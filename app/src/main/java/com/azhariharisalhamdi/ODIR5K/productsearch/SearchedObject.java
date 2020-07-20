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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import androidx.annotation.Nullable;
import com.azhariharisalhamdi.ODIR5K.R;
import com.azhariharisalhamdi.ODIR5K.Utils;
import com.azhariharisalhamdi.ODIR5K.objectdetection.DetectedObject;
import java.util.List;

/** Hosts the detected object info and its search result. */
public class SearchedObject {

  private final DetectedObject object;
  private final List<Product> productList;
  private final int objectThumbnailCornerRadius;
  private final String results;

  @Nullable
  private Bitmap objectThumbnail;

  public SearchedObject(Resources resources, DetectedObject object, List<Product> productList, String results) {
    this.object = object;
    this.productList = productList;
    this.objectThumbnailCornerRadius = resources.getDimensionPixelOffset(R.dimen.bounding_box_corner_radius);
    this.results = results;
  }

  public int getObjectIndex() {
    return object.getObjectIndex();
  }

  public List<Product> getProductList() {
    return productList;
  }

  public String getResults(){
    return results;
  }

  public Rect getBoundingBox() {
    return object.getBoundingBox();
  }

  public synchronized Bitmap getObjectThumbnail() {
    if (objectThumbnail == null) {
      objectThumbnail =
          Utils.getCornerRoundedBitmap(object.getBitmap(), objectThumbnailCornerRadius);
    }
    return objectThumbnail;
  }
}
