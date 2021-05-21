package medina.juanantonio.mplayer.data.models

import kotlinx.android.parcel.Parcelize

@Parcelize
data class FMovie(
    override val title: String,
    override val imageUrl: String,
    override val videoUrl: String,
    override val currentPage: Int,
    override val nextPage: Int
) : FItem, ParcelableFItem {

    override val type: FItem.Type
        get() = FItem.Type.VIDEO_GRID
}