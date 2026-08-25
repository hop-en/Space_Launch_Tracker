package dnu.ffeks.soy.spacelaunchtracker.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dnu.ffeks.soy.spacelaunchtracker.data.network.*

class LaunchTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromLaunchStatus(status: LaunchStatus?): String? = gson.toJson(status)

    @TypeConverter
    fun toLaunchStatus(json: String?): LaunchStatus? = gson.fromJson(json, LaunchStatus::class.java)

    @TypeConverter
    fun fromLaunchProvider(provider: LaunchProvider?): String? = gson.toJson(provider)

    @TypeConverter
    fun toLaunchProvider(json: String?): LaunchProvider? = gson.fromJson(json, LaunchProvider::class.java)

    @TypeConverter
    fun fromRocket(rocket: Rocket?): String? = gson.toJson(rocket)

    @TypeConverter
    fun toRocket(json: String?): Rocket? = gson.fromJson(json, Rocket::class.java)

    @TypeConverter
    fun fromMission(mission: Mission?): String? = gson.toJson(mission)

    @TypeConverter
    fun toMission(json: String?): Mission? = gson.fromJson(json, Mission::class.java)

    @TypeConverter
    fun fromPad(pad: Pad?): String? = gson.toJson(pad)

    @TypeConverter
    fun toPad(json: String?): Pad? = gson.fromJson(json, Pad::class.java)

    @TypeConverter
    fun fromVidUrlsList(urls: List<VidUrl>?): String? = gson.toJson(urls)

    @TypeConverter
    fun toVidUrlsList(json: String?): List<VidUrl>? {
        if (json == null) return emptyList()
        val type = object : TypeToken<List<VidUrl>>() {}.type
        return gson.fromJson(json, type)
    }
}