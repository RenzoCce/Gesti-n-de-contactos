
package com.example.gestioncontactos.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.gestioncontactos.data.local.Contacto
import com.example.gestioncontactos.data.local.ContactoDao
import com.example.gestioncontactos.domain.ContactoValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

sealed interface ResultadoContacto {
    data class Exito(val mensaje: String) : ResultadoContacto
    data class Error(val mensaje: String) : ResultadoContacto
}

class ContactoRepository(
    private val dao: ContactoDao
) {

    fun buscar(texto: String): Flow<List<Contacto>> {
        return dao.buscar(texto.trim())
    }

    suspend fun guardar(
        id: Long?,
        telefono: String,
        nombre: String,
        apellido: String,
        correo: String
    ): ResultadoContacto {

        val t = telefono.trim()
        val n = nombre.trim()
        val a = apellido.trim()
        val c = correo.trim()

        val error = ContactoValidator.validar(t, n, a, c)

        if (error != null) {
            return ResultadoContacto.Error(error)
        }

        return try {
            val duplicado = dao.buscarPorTelefono(t)

            if (duplicado != null && duplicado.id != id) {
                return ResultadoContacto.Error(
                    "El teléfono ya pertenece a otro contacto."
                )
            }

            if (id == null) {
                // CREATE
                val nuevoId = dao.insertar(
                    Contacto(
                        telefono = t,
                        nombre = n,
                        apellido = a,
                        correo = c
                    )
                )

                val guardado = dao.buscarPorId(nuevoId)

                if (guardado != null) {
                    ResultadoContacto.Exito(
                        "Contacto registrado correctamente."
                    )
                } else {
                    ResultadoContacto.Error(
                        "No se pudo confirmar el registro."
                    )
                }

            } else {
                // UPDATE
                val original = dao.buscarPorId(id)
                    ?: return ResultadoContacto.Error(
                        "El contacto no existe."
                    )

                val filas = dao.actualizar(
                    original.copy(
                        telefono = t,
                        nombre = n,
                        apellido = a,
                        correo = c
                    )
                )

                if (filas > 0) {
                    ResultadoContacto.Exito(
                        "Contacto actualizado correctamente."
                    )
                } else {
                    ResultadoContacto.Error(
                        "No se pudo actualizar el contacto."
                    )
                }
            }

        } catch (e: CancellationException) {
            throw e

        } catch (e: SQLiteConstraintException) {
            ResultadoContacto.Error(
                "No se pudo guardar: datos duplicados."
            )

        } catch (e: Exception) {
            ResultadoContacto.Error(
                "Error de almacenamiento al guardar."
            )
        }
    }

    suspend fun eliminar(
        ids: List<Long>
    ): ResultadoContacto {

        if (ids.isEmpty()) {
            return ResultadoContacto.Error(
                "No seleccionaste contactos."
            )
        }

        return try {
            val eliminados = dao.eliminarVarios(ids.distinct())

            if (eliminados > 0) {
                ResultadoContacto.Exito(
                    "$eliminados contacto(s) eliminado(s)."
                )
            } else {
                ResultadoContacto.Error(
                    "No se encontraron contactos para eliminar."
                )
            }

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            ResultadoContacto.Error(
                "Error de almacenamiento al eliminar."
            )
        }
    }
}
