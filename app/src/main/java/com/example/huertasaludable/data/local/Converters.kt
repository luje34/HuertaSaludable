package com.example.huertasaludable.data.local

import androidx.room.TypeConverter
import com.example.huertasaludable.data.model.EstadoCultivo
import com.example.huertasaludable.data.model.EstadoProblema
import com.example.huertasaludable.data.model.EstadoSector
import com.example.huertasaludable.data.model.EstadoTarea
import com.example.huertasaludable.data.model.Rol
import com.example.huertasaludable.data.model.TipoProblema
import com.example.huertasaludable.data.model.TipoTarea

class Converters {
    @TypeConverter
    fun fromRol(value: Rol?): String? = value?.name

    @TypeConverter
    fun toRol(value: String?): Rol? = value?.let {
        try { Rol.valueOf(it) } catch (_: Exception) { null }
    }

    @TypeConverter
    fun fromEstadoSector(value: EstadoSector?): String? = value?.name

    @TypeConverter
    fun toEstadoSector(value: String?): EstadoSector? = value?.let {
        try { EstadoSector.valueOf(it) } catch (_: Exception) { null }
    }

    @TypeConverter
    fun fromEstadoCultivo(value: EstadoCultivo?): String? = value?.name

    @TypeConverter
    fun toEstadoCultivo(value: String?): EstadoCultivo? = value?.let {
        try { EstadoCultivo.valueOf(it) } catch (_: Exception) { null }
    }

    @TypeConverter
    fun fromTipoTarea(value: TipoTarea?): String? = value?.name

    @TypeConverter
    fun toTipoTarea(value: String?): TipoTarea? = value?.let {
        try { TipoTarea.valueOf(it) } catch (_: Exception) { null }
    }

    @TypeConverter
    fun fromEstadoTarea(value: EstadoTarea?): String? = value?.name

    @TypeConverter
    fun toEstadoTarea(value: String?): EstadoTarea? = value?.let {
        try { EstadoTarea.valueOf(it) } catch (_: Exception) { null }
    }

    @TypeConverter
    fun fromTipoProblema(value: TipoProblema?): String? = value?.name

    @TypeConverter
    fun toTipoProblema(value: String?): TipoProblema? = value?.let {
        try { TipoProblema.valueOf(it) } catch (_: Exception) { null }
    }

    @TypeConverter
    fun fromEstadoProblema(value: EstadoProblema?): String? = value?.name

    @TypeConverter
    fun toEstadoProblema(value: String?): EstadoProblema? = value?.let {
        try { EstadoProblema.valueOf(it) } catch (_: Exception) { null }
    }
}
