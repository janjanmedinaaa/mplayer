/*
 * Copyright (C) 2015 The Android Open Source Project
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
 *
 */

package medina.juanantonio.mplayer.data.presenters;

import android.content.Context;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import java.util.HashMap;
import medina.juanantonio.mplayer.data.models.FItem;

/**
 * This PresenterSelector will decide what Presenter to use depending on a given card's type.
 */
public class CardPresenterSelector extends PresenterSelector {

    private final Context mContext;
    private final HashMap<FItem.Type, Presenter> presenters = new HashMap<>();

    public CardPresenterSelector(Context context) {
        mContext = context;
    }

    @Override
    public Presenter getPresenter(Object item) {
        if (!(item instanceof FItem)) throw new RuntimeException(
                String.format("The PresenterSelector only supports data items of type '%s', is '%s'",
                        FItem.class.getName(), item.getClass().getName()));
        FItem fItem = (FItem) item;
        Presenter presenter = presenters.get(fItem.getType());
        if (presenter == null) {
            if (fItem.getType() == FItem.Type.VIDEO_GRID) {
                presenter = new VideoCardViewPresenter(mContext);
            }
        }
        presenters.put(fItem.getType(), presenter);
        return presenter;
    }
}
