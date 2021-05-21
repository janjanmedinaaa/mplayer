package medina.juanantonio.mplayer.data.models

import kotlinx.android.parcel.Parcelize

@Parcelize
data class FEpisode(
    val sourceDataId: String,
    val backUpSourceId: String,
    val season: String,
    val episode: String,
    private val mTitle: String,
    override val videoUrl: String,
    override val imageUrl: String
) : FItem, ParcelableFItem {

    override val type: FItem.Type
        get() = FItem.Type.VIDEO_GRID

    override val title: String
        get() {
            return if (mTitle.isNotEmpty()) {
                val splitTitle = mTitle.split(" - ")
                if (splitTitle.size > 1) {
                    "S${season}E$episode: ${splitTitle[1].trim()}"
                } else {
                    mTitle.removeSurrounding("n").trim()
                }
            } else "No Title Available"
        }

    override val currentPage: Int = 1
    override val nextPage: Int = 1
}