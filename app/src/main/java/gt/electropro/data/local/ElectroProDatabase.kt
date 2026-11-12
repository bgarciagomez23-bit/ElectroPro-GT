package gt.electropro.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "projects", indices = [Index(value = ["code"], unique = true)])
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val client: String = "",
    val location: String = "",
    val status: String = "EN_PROCESO",
    val notes: String = "",
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "substations",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("projectId"), Index(value = ["code"], unique = true)]
)
data class SubstationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long?,
    val code: String,
    val name: String,
    val nominalVoltageV: Double?,
    val location: String = "",
    val notes: String = "",
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "substation_assets",
    foreignKeys = [
        ForeignKey(
            entity = SubstationEntity::class,
            parentColumns = ["id"],
            childColumns = ["substationId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("substationId")]
)
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
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val substationId: Long? = null
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAtEpochMs DESC, id DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Query("SELECT COUNT(*) FROM projects WHERE LOWER(code) = LOWER(:code) AND id != :projectId")
    suspend fun countByCodeExceptId(code: String, projectId: Long): Int

    @Query("SELECT COUNT(*) FROM projects WHERE LOWER(code) = LOWER(:code)")
    suspend fun countByCode(code: String): Int

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface SubstationDao {
    @Query("SELECT * FROM substations ORDER BY createdAtEpochMs DESC, id DESC")
    fun observeAll(): Flow<List<SubstationEntity>>

    @Query("SELECT * FROM substations WHERE projectId = :projectId ORDER BY code")
    fun observeByProject(projectId: Long): Flow<List<SubstationEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(substation: SubstationEntity): Long

    @Update
    suspend fun update(substation: SubstationEntity)

    @Query("SELECT COUNT(*) FROM substations WHERE LOWER(code) = LOWER(:code) AND id != :substationId")
    suspend fun countByCodeExceptId(code: String, substationId: Long): Int

    @Query("SELECT COUNT(*) FROM substations WHERE LOWER(code) = LOWER(:code)")
    suspend fun countByCode(code: String): Int

    @Query("DELETE FROM substations WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface SubstationAssetDao {
    @Query("SELECT * FROM substation_assets ORDER BY createdAtEpochMs DESC, id DESC")
    fun observeAll(): Flow<List<SubstationAssetEntity>>

    @Query("SELECT * FROM substation_assets WHERE substationId = :substationId ORDER BY tag")
    fun observeBySubstation(substationId: Long): Flow<List<SubstationAssetEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(asset: SubstationAssetEntity)

    @Update
    suspend fun update(asset: SubstationAssetEntity)

    @Query("SELECT COUNT(*) FROM substation_assets WHERE LOWER(tag) = LOWER(:tag) AND id != :assetId")
    suspend fun countByTagExceptId(tag: String, assetId: Long): Int

    @Query("SELECT COUNT(*) FROM substation_assets WHERE LOWER(tag) = LOWER(:tag)")
    suspend fun countByTag(tag: String): Int

    @Query("UPDATE substation_assets SET substationId = :substationId WHERE id = :assetId")
    suspend fun assignToSubstation(assetId: Long, substationId: Long?)

    @Query("DELETE FROM substation_assets WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Database(
    entities = [ProjectEntity::class, SubstationEntity::class, SubstationAssetEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ElectroProDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun substationDao(): SubstationDao
    abstract fun substationAssetDao(): SubstationAssetDao

    companion object {
        @Volatile private var INSTANCE: ElectroProDatabase? = null

        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS projects (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        code TEXT NOT NULL, name TEXT NOT NULL, client TEXT NOT NULL,
                        location TEXT NOT NULL, status TEXT NOT NULL, notes TEXT NOT NULL,
                        createdAtEpochMs INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_projects_code ON projects(code)")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS substations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        projectId INTEGER, code TEXT NOT NULL, name TEXT NOT NULL,
                        nominalVoltageV REAL, location TEXT NOT NULL, notes TEXT NOT NULL,
                        createdAtEpochMs INTEGER NOT NULL,
                        FOREIGN KEY(projectId) REFERENCES projects(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_substations_projectId ON substations(projectId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_substations_code ON substations(code)")
                db.execSQL("""
                    CREATE TABLE substation_assets_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tag TEXT NOT NULL, name TEXT NOT NULL, type TEXT NOT NULL,
                        ratedVoltageV REAL NOT NULL, ratedCurrentA REAL, ratedPowerKva REAL,
                        shortCircuitRatingKA REAL, location TEXT NOT NULL, notes TEXT NOT NULL,
                        createdAtEpochMs INTEGER NOT NULL, substationId INTEGER,
                        FOREIGN KEY(substationId) REFERENCES substations(id)
                        ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO substation_assets_new (
                        id, tag, name, type, ratedVoltageV, ratedCurrentA, ratedPowerKva,
                        shortCircuitRatingKA, location, notes, createdAtEpochMs, substationId
                    )
                    SELECT id, tag, name, type, ratedVoltageV, ratedCurrentA, ratedPowerKva,
                        shortCircuitRatingKA, location, notes, createdAtEpochMs, NULL
                    FROM substation_assets
                """.trimIndent())
                db.execSQL("DROP TABLE substation_assets")
                db.execSQL("ALTER TABLE substation_assets_new RENAME TO substation_assets")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_substation_assets_substationId ON substation_assets(substationId)")
            }
        }

        fun getInstance(context: Context): ElectroProDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ElectroProDatabase::class.java,
                    "electropro_gt.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
