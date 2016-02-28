/*
 * Copyright (C) 2015 Antonio Leiva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.msomu.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class ViewModel implements Parcelable {
    public static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185";
    public static final Creator<ViewModel> CREATOR = new Creator<ViewModel>() {
        @Override
        public ViewModel createFromParcel(Parcel in) {
            return new ViewModel(in);
        }

        @Override
        public ViewModel[] newArray(int size) {
            return new ViewModel[size];
        }
    };
    private String text;
    private String image;
    private String bgImage;
    private String releaseDate;
    private String voteAverage;
    private String plotSynopsis;

    public ViewModel(String text, String image, String bgImage, String releaseDate, String voteAverage, String plotSynopsis) {
        this.text = text;
        this.image = IMAGE_BASE_URL + image;
        this.bgImage = IMAGE_BASE_URL + bgImage;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
    }

    protected ViewModel(Parcel in) {
        text = in.readString();
        image = in.readString();
        bgImage = in.readString();
        releaseDate = in.readString();
        voteAverage = in.readString();
        plotSynopsis = in.readString();
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setPlotSynopsis(String plotSynopsis) {
        this.plotSynopsis = plotSynopsis;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getBgImage() {
        return bgImage;
    }

    public void setBgImage(String bgImage) {
        this.bgImage = bgImage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(image);
        dest.writeString(bgImage);
        dest.writeString(releaseDate);
        dest.writeString(voteAverage);
        dest.writeString(plotSynopsis);
    }
}
