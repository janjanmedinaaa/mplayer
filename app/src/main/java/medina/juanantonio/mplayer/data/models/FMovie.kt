package medina.juanantonio.mplayer.data.models

data class FMovie(
    override val title: String,
    override val imageUrl: String,
    override val videoUrl: String,
    override val currentPage: Int,
    override val nextPage: Int
) : FItem {

    override val type: FItem.Type
        get() = FItem.Type.VIDEO_GRID
}