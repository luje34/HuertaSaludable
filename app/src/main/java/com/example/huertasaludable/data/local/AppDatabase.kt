package com.example.huertasaludable.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.huertasaludable.data.local.dao.CultivoDao
import com.example.huertasaludable.data.local.dao.HistorialActividadDao
import com.example.huertasaludable.data.local.dao.LecturaSensorDao
import com.example.huertasaludable.data.local.dao.ReporteProblemaDao
import com.example.huertasaludable.data.local.dao.SectorDao
import com.example.huertasaludable.data.local.dao.TareaDao
import com.example.huertasaludable.data.local.dao.UsuarioDao
import com.example.huertasaludable.data.local.entity.CultivoEntity
import com.example.huertasaludable.data.local.entity.HistorialActividadEntity
import com.example.huertasaludable.data.local.entity.LecturaSensorEntity
import com.example.huertasaludable.data.local.entity.ReporteProblemaEntity
import com.example.huertasaludable.data.local.entity.SectorEntity
import com.example.huertasaludable.data.local.entity.TareaEntity
import com.example.huertasaludable.data.local.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        SectorEntity::class,
        CultivoEntity::class,
        TareaEntity::class,
        ReporteProblemaEntity::class,
        LecturaSensorEntity::class,
        HistorialActividadEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun sectorDao(): SectorDao
    abstract fun cultivoDao(): CultivoDao
    abstract fun tareaDao(): TareaDao
    abstract fun reporteProblemaDao(): ReporteProblemaDao
    abstract fun lecturaSensorDao(): LecturaSensorDao
    abstract fun historialActividadDao(): HistorialActividadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huerta_saludable_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
