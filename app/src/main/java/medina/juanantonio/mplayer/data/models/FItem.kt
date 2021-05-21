package medina.juanantonio.mplayer.data.models

import android.os.Parcelable

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

interface ParcelableFItem: FItem, Parcelable