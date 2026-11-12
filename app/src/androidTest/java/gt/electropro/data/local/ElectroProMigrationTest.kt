package gt.electropro.data.local

import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ElectroProMigrationTest {
    @Test
    fun migration12PreservesAssetsAndSetNullRelationships() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "electropro_migration_test.db"
        context.deleteDatabase(databaseName)
        val legacyFile = context.getDatabasePath(databaseName)
        legacyFile.parentFile?.mkdirs()

        val legacy = SQLiteDatabase.openOrCreateDatabase(legacyFile, null)
        legacy.execSQL(
            """
            CREATE TABLE substation_assets (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                tag TEXT NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                ratedVoltageV REAL NOT NULL,
                ratedCurrentA REAL,
                ratedPowerKva REAL,
                shortCircuitRatingKA REAL,
                location TEXT NOT NULL,
                notes TEXT NOT NULL,
                createdAtEpochMs INTEGER NOT NULL
            )
            """.trimIndent()
        )
        legacy.execSQL(
            """
            INSERT INTO substation_assets
            (id, tag, name, type, ratedVoltageV, ratedCurrentA, ratedPowerKva,
             shortCircuitRatingKA, location, notes, createdAtEpochMs)
            VALUES (7, 'TR-LEG-01', 'Transformador legado', 'TRANSFORMER',
                    13800, 31.4, 750, NULL, 'Patio', 'Migrado', 1700000000000)
            """.trimIndent()
        )
        legacy.version = 1
        legacy.close()

        val database = androidx.room.Room.databaseBuilder(
            context,
            ElectroProDatabase::class.java,
            databaseName
        ).addMigrations(ElectroProDatabase.MIGRATION_1_2, ElectroProDatabase.MIGRATION_2_3).build()

        try {
            val migrated = database.substationAssetDao().observeAll().first()
            assertEquals(1, migrated.size)
            assertEquals(7L, migrated.first().id)
            assertEquals("TR-LEG-01", migrated.first().tag)
            assertNull(migrated.first().substationId)

            val projectId = database.projectDao().insert(
                ProjectEntity(code = "MIG-PRJ", name = "Proyecto migración")
            )
            val substationId = database.substationDao().insert(
                SubstationEntity(
                    projectId = projectId,
                    code = "MIG-SE-01",
                    name = "Subestación migración",
                    nominalVoltageV = 13800.0
                )
            )
            database.substationAssetDao().insert(
                SubstationAssetEntity(
                    tag = "TR-NEW-01",
                    name = "Transformador nuevo",
                    type = "TRANSFORMER",
                    ratedVoltageV = 13800.0,
                    ratedCurrentA = null,
                    ratedPowerKva = 750.0,
                    shortCircuitRatingKA = null,
                    location = "",
                    notes = "",
                    substationId = substationId
                )
            )

            database.projectDao().deleteById(projectId)
            assertNull(database.substationDao().observeAll().first().single().projectId)

            database.substationDao().deleteById(substationId)
            val remainingAssets = database.substationAssetDao().observeAll().first()
            assertNull(remainingAssets.first { it.tag == "TR-NEW-01" }.substationId)
            assertEquals("TR-LEG-01", remainingAssets.first { it.id == 7L }.tag)
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
        }
    }
}
