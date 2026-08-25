package dnu.ffeks.soy.spacelaunchtracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dnu.ffeks.soy.spacelaunchtracker.data.network.SpaceLaunch
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceLaunchDao {

    @Query("SELECT * FROM launches")
    fun getAllLaunches(): Flow<List<SpaceLaunch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLaunches(launches: List<SpaceLaunch>)

    @Query("DELETE FROM launches")
    fun clearAllLaunches()
}