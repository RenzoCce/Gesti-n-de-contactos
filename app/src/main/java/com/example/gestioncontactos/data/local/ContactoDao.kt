
package com.example.gestioncontactos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(contacto: Contacto): Long

    @Query("SELECT * FROM contactos ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Contacto>>

    @Query("""
        SELECT * FROM contactos
        WHERE :texto = ''
           OR nombre LIKE '%' || :texto || '%'
           OR apellido LIKE '%' || :texto || '%'
           OR telefono LIKE '%' || :texto || '%'
           OR correo LIKE '%' || :texto || '%'
        ORDER BY nombre ASC
    """)
    fun buscar(texto: String): Flow<List<Contacto>>

    @Query("""
        SELECT * FROM contactos
        WHERE telefono = :telefono
        LIMIT 1
    """)
    suspend fun buscarPorTelefono(
        telefono: String
    ): Contacto?

    @Query("SELECT * FROM contactos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Contacto?

    @Update
    suspend fun actualizar(contacto: Contacto): Int

    @Query("DELETE FROM contactos WHERE id IN (:ids)")
    suspend fun eliminarVarios(ids: List<Long>): Int
}
