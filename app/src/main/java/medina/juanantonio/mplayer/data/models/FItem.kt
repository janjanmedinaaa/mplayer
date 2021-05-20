package medina.juanantonio.mplayer.data.models

interface FItem {
    val type: Type
    val title: String
    val imageUrl: String
    val videoUrl: String
    val currentPage: Int
    val nextPage: Int

    enum class Type {
        VIDEO_GRID
    }
}