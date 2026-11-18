
package com.example.gestioncontactos.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestioncontactos.data.local.Contacto
import com.example.gestioncontactos.data.local.ContactoDatabase
import com.example.gestioncontactos.data.repository.ContactoRepository
import com.example.gestioncontactos.data.repository.ResultadoContacto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface EventoContacto {
    data class Guardado(val mensaje: String) : EventoContacto
    data class Eliminado(val mensaje: String) : EventoContacto
    data class Error(val mensaje: String) : EventoContacto
}

@OptIn(ExperimentalCoroutinesApi::class)
class ContactoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dao = ContactoDatabase
        .getDatabase(application)
        .contactoDao()

    private val repository = ContactoRepository(dao)

    val busqueda = MutableStateFlow("")
    val procesando = MutableStateFlow(false)

    val contactos = busqueda
        .flatMapLatest { texto ->
            repository.buscar(texto)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList<Contacto>()
        )

    private val _eventos =
        Channel<EventoContacto>(Channel.BUFFERED)

    val eventos = _eventos.receiveAsFlow()

    fun buscar(texto: String) {
        busqueda.value = texto
    }

    fun guardar(
        id: Long?,
        telefono: String,
        nombre: String,
        apellido: String,
        correo: String
    ) {
        if (procesando.value) return
        procesando.value = true

        viewModelScope.launch {
            try {
                val resultado = repository.guardar(
                    id, telefono, nombre, apellido, correo
                )

                when (resultado) {
                    is ResultadoContacto.Exito ->
                        _eventos.send(
                            EventoContacto.Guardado(resultado.mensaje)
                        )

                    is ResultadoContacto.Error ->
                        _eventos.send(
                            EventoContacto.Error(resultado.mensaje)
                        )
                }
            } finally {
                procesando.value = false
            }
        }
    }

    fun eliminar(ids: Set<Long>) {
        if (procesando.value) return
        procesando.value = true

        viewModelScope.launch {
            try {
                val resultado = repository.eliminar(
                    ids.toList()
                )

                when (resultado) {
                    is ResultadoContacto.Exito ->
                        _eventos.send(
                            EventoContacto.Eliminado(resultado.mensaje)
                        )

                    is ResultadoContacto.Error ->
                        _eventos.send(
                            EventoContacto.Error(resultado.mensaje)
                        )
                }
            } finally {
                procesando.value = false
            }
        }
    }
}
