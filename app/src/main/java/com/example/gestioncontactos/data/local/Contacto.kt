package com.example.gestioncontactos.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contactos",
    indices = [Index(value = ["telefono"], unique = true)]
)

data class Contacto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val telefono: String,
    val nombre: String,
    val apellido: String,
    val correo: String
)
