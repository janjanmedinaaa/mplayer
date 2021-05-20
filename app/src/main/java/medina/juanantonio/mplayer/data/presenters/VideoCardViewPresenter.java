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

package medina.juanantonio.mplayer.data.presenters;

import android.content.Context;
import android.view.ContextThemeWrapper;

import androidx.core.content.res.ResourcesCompat;
import androidx.leanback.widget.ImageCardView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;

import medina.juanantonio.mplayer.R;
import medina.juanantonio.mplayer.data.models.FItem;
import medina.juanantonio.mplayer.data.models.FMovie;
import medina.juanantonio.mplayer.data.models.FSeries;

/**
 * Presenter for rendering video cards on the Vertical Grid fragment.
 */
public class VideoCardViewPresenter extends AbstractCardPresenter<ImageCardView> {

    public VideoCardViewPresenter(Context context, int cardThemeResId) {
        super(new ContextThemeWrapper(context, cardThemeResId));
    }

    public VideoCardViewPresenter(Context context) {
        this(context, R.style.MovieCardBasicTheme);
    }

    @Override
    protected ImageCardView onCreateView() {
        return new ImageCardView(getContext());
    }

    @Override
    public void onBindViewHolder(FItem fItem, final ImageCardView cardView) {
        cardView.setTag(fItem);
        cardView.setTitleText(fItem.getTitle());
        if (fItem instanceof FMovie) {
            cardView.setBadgeImage(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_movie, getContext().getTheme()));
        } else if (fItem instanceof FSeries) {
            cardView.setBadgeImage(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_series, getContext().getTheme()));
        } else {
            cardView.setBadgeImage(null);
        }

        Shimmer shimmer = new Shimmer.AlphaHighlightBuilder()
                .setDuration(1800) // how long the shimmering animation takes to do one full sweep
                .setBaseAlpha(0.7f) //the alpha of the underlying children
                .setHighlightAlpha(0.6f) // the shimmer alpha amount
                .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                .setAutoStart(true)
                .build();

        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);

        cardView.getMainImageView().setBackgroundColor(
                getContext().getResources().getColor(R.color.search_button_color)
        );

        if (fItem.getImageUrl().equalsIgnoreCase("loading")) {
            cardView.getMainImageView().setImageDrawable(shimmerDrawable);
        } else {
            Glide.with(getContext())
                    .asBitmap()
                    .load(fItem.getImageUrl())
                    .placeholder(shimmerDrawable)
                    .into(cardView.getMainImageView());
        }

    }
}
