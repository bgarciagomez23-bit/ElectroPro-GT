package gt.electropro.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "substation_assets")
data class SubstationAssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tag: String,
    val name: String,
    val type: String,
    val ratedVoltageV: Double,
    val ratedCurrentA: Double?,
    val ratedPowerKva: Double?,
    val shortCircuitRatingKA: Double?,
    val location: String,
    val notes: String,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Dao
interface SubstationAssetDao {
    @Query("SELECT * FROM substation_assets ORDER BY createdAtEpochMs DESC, id DESC")
    fun observeAll(): Flow<List<SubstationAssetEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(asset: SubstationAssetEntity)

    @Query("SELECT COUNT(*) FROM substation_assets WHERE LOWER(tag) = LOWER(:tag)")
    suspend fun countByTag(tag: String): Int

    @Query("DELETE FROM substation_assets WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Database(entities = [SubstationAssetEntity::class], version = 1, exportSchema = false)
abstract class ElectroProDatabase : RoomDatabase() {
    abstract fun substationAssetDao(): SubstationAssetDao

    companion object {
        @Volatile private var INSTANCE: ElectroProDatabase? = null

        fun getInstance(context: Context): ElectroProDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ElectroProDatabase::class.java,
                    "electropro_gt.db"
                ).build().also { INSTANCE = it }
            }
    }
}
