package dnu.ffeks.soy.spacelaunchtracker.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import androidx.room.Entity
import androidx.room.PrimaryKey

data class LaunchResponse(
    val count: Int,
    val results: List<SpaceLaunch>
)

@Entity(tableName = "launches")
data class SpaceLaunch(
    @PrimaryKey val id: String,
    val name: String,
    val net: String,
    val status: LaunchStatus?,
    val launch_service_provider: LaunchProvider?,
    val rocket: Rocket?,
    val mission: Mission?,
    val pad: Pad?,
    val image: String?,
    @SerializedName("vidURLs") val vidUrls: List<VidUrl>? = emptyList()
)

data class VidUrl(
    val title: String?,
    val url: String
)

data class LaunchStatus(val id: Int, val name: String, val abbrev: String)
data class LaunchProvider(val id: Int, val name: String, val type: String?)

data class Rocket(
    val id: Int,
    val configuration: RocketConfig?,
    val spacecraft_stage: SpacecraftStage? = null
)

data class RocketConfig(val id: Int, val name: String, val full_name: String)
data class Mission(val id: Int, val name: String, val description: String?, val type: String?)
data class Pad(val id: Int, val name: String, val location: Location?)
data class Location(val id: Int, val name: String)

data class SpacecraftStage(val launch_crew: List<LaunchCrew>? = null)
data class LaunchCrew(val role: CrewRole?, val astronaut: Astronaut?)
data class CrewRole(val role: String?)
data class Astronaut(val name: String?)

interface SpaceApi {

    @GET("2.2.0/launch/upcoming/")
    suspend fun getUpcomingLaunches(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("search") search: String? = null
    ): LaunchResponse

    @GET("2.2.0/launch/previous/")
    suspend fun getPastLaunches(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("search") search: String? = null
    ): LaunchResponse

    @GET("2.2.0/launch/{id}/")
    suspend fun getLaunchById(
        @retrofit2.http.Path("id") id: String,
        @Query("mode") mode: String = "detailed"
    ): SpaceLaunch
}

object ApiClient {
    private const val BASE_URL = "https://ll.thespacedevs.com/"

    val apiService: SpaceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpaceApi::class.java)
    }
}