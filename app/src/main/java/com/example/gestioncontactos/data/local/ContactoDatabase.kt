package com.example.gestioncontactos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Contacto::class],
    version = 1,
    exportSchema = false
)

abstract class ContactoDatabase : RoomDatabase(){
    abstract  fun contactoDao(): ContactoDao
    companion object{
        @Volatile
        private var INSTANCE: ContactoDatabase? = null

        fun getDatabase(context: Context): ContactoDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContactoDatabase::class.java,

                    "contactos_db"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}