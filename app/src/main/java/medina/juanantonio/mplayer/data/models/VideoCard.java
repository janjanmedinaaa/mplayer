/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package medina.juanantonio.mplayer.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.List;
import medina.juanantonio.mplayer.data.database.dao.MPlayerTypeConverter;

/**
 * The video card data structure used to hold the fields of each video card fetched from the
 * url: https://storage.googleapis.com/android-tv/android_tv_videos_new.json
 */
@TypeConverters(MPlayerTypeConverter.class)
@Entity
public class VideoCard extends Card {

    @PrimaryKey(autoGenerate = true) private Integer mIndex;
    private List<String> mVideoSources = null;
    private String mBackgroundUrl = "";
    private String mStudio = "";

    public VideoCard() {
        super();
        setType(Type.VIDEO_GRID);
    }

    public Integer getIndex() {
        return mIndex;
    }

    public void setIndex(Integer mIndex) {
        this.mIndex = mIndex;
    }

    public List<String> getVideoSources() {
        return mVideoSources;
    }

    public void setVideoSources(List<String> sources) {
        mVideoSources = sources;
    }

    public String getBackgroundUrl() {
        return mBackgroundUrl;
    }

    public void setBackgroundUrl(String background) {
        mBackgroundUrl = background;
    }

    public String getStudio() {
        return mStudio;
    }

    public void setStudio(String studio) {
        mStudio = studio;
    }
}
