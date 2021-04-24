package medina.juanantonio.mplayer.data.database.dao

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import medina.juanantonio.mplayer.data.models.Card

class MPlayerTypeConverter {

    @TypeConverter
    fun listToString(list: List<String>): String {
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun stringToList(string: String): List<String> {
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(string, type)
    }

    @TypeConverter
    fun cardTypeToString(cardType: Card.Type): String {
        val gson = Gson()
        val type = object : TypeToken<Card.Type>() {}.type
        return gson.toJson(cardType, type)
    }

    @TypeConverter
    fun stringToCardType(string: String): Card.Type {
        val gson = Gson()
        val type = object : TypeToken<Card.Type>() {}.type
        return gson.fromJson(string, type)
    }
}