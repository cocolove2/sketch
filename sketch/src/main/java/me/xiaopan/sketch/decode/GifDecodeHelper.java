/*
 * Copyright (C) 2017 Peng fei Pan <sky@xiaopan.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.sketch.decode;

import android.graphics.BitmapFactory;

import me.xiaopan.sketch.ErrorTracker;
import me.xiaopan.sketch.SLog;
import me.xiaopan.sketch.cache.BitmapPool;
import me.xiaopan.sketch.datasource.DataSource;
import me.xiaopan.sketch.drawable.ImageAttrs;
import me.xiaopan.sketch.drawable.SketchGifDrawable;
import me.xiaopan.sketch.drawable.SketchGifFactory;
import me.xiaopan.sketch.request.LoadRequest;

public class GifDecodeHelper extends DecodeHelper {

    @Override
    public boolean match(LoadRequest request, DataSource dataSource, ImageType imageType, BitmapFactory.Options boundOptions) {
        if (imageType != null && imageType == ImageType.GIF && request.getOptions().isDecodeGifImage()) {
            if (SketchGifFactory.isExistGifLibrary()) {
                return true;
            } else {
                SLog.e("GifDecodeHelper", "Not found libpl_droidsonroids_gif.so. Please go to “https://github.com/panpf/sketch” find how to import the sketch-gif library");
            }
        }
        return false;
    }

    @Override
    public DecodeResult decode(LoadRequest request, DataSource dataSource, ImageType imageType,
                               BitmapFactory.Options boundOptions, BitmapFactory.Options decodeOptions, int exifOrientation) {
        try {
            ImageAttrs imageAttrs = new ImageAttrs(boundOptions.outMimeType, boundOptions.outWidth,
                    boundOptions.outHeight, exifOrientation);
            BitmapPool bitmapPool = request.getConfiguration().getBitmapPool();

            SketchGifDrawable gifDrawable = dataSource.makeGifDrawable(request.getKey(), request.getUri(),
                    imageAttrs, bitmapPool);
            if (gifDrawable == null) {
                return null;
            }

            return new GifDecodeResult(imageAttrs, gifDrawable).setBanProcess(true);
        } catch (Throwable e) {
            e.printStackTrace();

            ErrorTracker errorTracker = request.getConfiguration().getErrorTracker();
            if (e instanceof UnsatisfiedLinkError || e instanceof ExceptionInInitializerError) {
                errorTracker.onNotFoundGifSoError(e);
            } else {
                errorTracker.onDecodeGifImageError(e, request,
                        boundOptions.outWidth, boundOptions.outHeight, boundOptions.outMimeType);
            }

            return null;
        }
    }
}
