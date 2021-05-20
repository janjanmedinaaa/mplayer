/*
 * Copyright (C) 2017 The Android Open Source Project
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

package medina.juanantonio.mplayer.features.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import androidx.leanback.media.PlaybackGlue;
import androidx.leanback.media.PlaybackTransportControlGlue;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

public class PlaybackSeekDiskDataProvider extends PlaybackSeekAsyncDataProvider {

    final Paint mPaint;
    final String mPathPattern;
    final String mStreamUrl;
    final Context mContext;

    PlaybackSeekDiskDataProvider(long duration, long interval, String pathPattern, String streamUrl, Context context) {
        mPathPattern = pathPattern;
        mStreamUrl = streamUrl;
        mContext = context;
        int size = (int) (duration / interval) + 1;
        long[] pos = new long[size];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = i * duration / pos.length;
        }
        setSeekPositions(pos);
        mPaint = new Paint();
        mPaint.setTextSize(16);
        mPaint.setColor(Color.BLUE);
    }

    protected Bitmap doInBackground(Object task, int index, long position) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            // Thread might be interrupted by cancel() call.
        }
        if (isCancelled(task)) {
            return null;
        }
        String path = String.format(mPathPattern, (index + 1));
        long pos = getSeekPositions()[index];
        Bitmap bmp;
        try {
            bmp = Glide.with(mContext)
                    .asBitmap()
                    .load("https://ieeecs-media.computer.org/wp-media/2019/12/02035009/fileformat550x295.jpg")
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
        } catch (Exception ex) {
            Log.d("DEVELOP", ex.getMessage());
            bmp = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(Color.YELLOW);
            canvas.drawText(path, 10, 80, mPaint);
            canvas.drawText(Integer.toString(index), 10, 150, mPaint);
        }
        return bmp;
    }

    /**
     * Helper function to set a demo seek provider on PlaybackTransportControlGlue based on
     * duration.
     */
    public static void setDemoSeekProvider(final PlaybackTransportControlGlue glue, final String streamUrl, final Context context) {
        if (glue.isPrepared()) {
            glue.setSeekProvider(new PlaybackSeekDiskDataProvider(
                    glue.getDuration(),
                    glue.getDuration() / 100,
                    "/sdcard/seek/frame_%04d.jpg",
                    streamUrl, context));
        } else {
            glue.addPlayerCallback(new PlaybackGlue.PlayerCallback() {
                @Override
                public void onPreparedStateChanged(PlaybackGlue glue) {
                    if (glue.isPrepared()) {
                        glue.removePlayerCallback(this);
                        PlaybackTransportControlGlue transportControlGlue =
                                (PlaybackTransportControlGlue) glue;
                        transportControlGlue.setSeekProvider(new PlaybackSeekDiskDataProvider(
                                transportControlGlue.getDuration(),
                                transportControlGlue.getDuration() / 100,
                                "/sdcard/seek/frame_%04d.jpg",
                                streamUrl, context));
                    }
                }
            });
        }
    }

}
