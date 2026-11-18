
package com.example.gestioncontactos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestioncontactos.data.local.Contacto
import com.example.gestioncontactos.ui.ContactoViewModel
import com.example.gestioncontactos.ui.EventoContacto

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val vm: ContactoViewModel = viewModel()
                PantallaContactos(vm)
            }
        }
    }
}

@Composable
fun PantallaContactos(vm: ContactoViewModel) {

    val contactos by vm.contactos.collectAsState()
    val busqueda by vm.busqueda.collectAsState()
    val procesando by vm.procesando.collectAsState()

    var mostrarFormulario by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<Contacto?>(null) }
    var seleccionando by remember { mutableStateOf(false) }
    var seleccionados by remember {
        mutableStateOf(setOf<Long>())
    }
    var confirmarEliminar by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(vm) {
        vm.eventos.collect { evento ->
            when (evento) {
                is EventoContacto.Guardado -> {
                    mostrarFormulario = false
                    editando = null
                    snackbar.showSnackbar(evento.mensaje)
                }

                is EventoContacto.Eliminado -> {
                    seleccionados = emptySet()
                    seleccionando = false
                    snackbar.showSnackbar(evento.mensaje)
                }

                is EventoContacto.Error -> {
                    snackbar.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                "Gestión de contactos",
                style = MaterialTheme.typography.headlineSmall
            )

            if (mostrarFormulario) {

                FormularioContacto(
                    contacto = editando,
                    procesando = procesando,
                    modifier = Modifier.weight(1f),
                    onCancelar = {
                        if (!procesando) mostrarFormulario = false
                    },
                    onGuardar = { telefono, nombre, apellido, correo ->
                        vm.guardar(
                            editando?.id,
                            telefono,
                            nombre,
                            apellido,
                            correo
                        )
                    }
                )

            } else {

                OutlinedTextField(
                    value = busqueda,
                    onValueChange = vm::buscar,
                    label = { Text("Buscar contacto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = {
                        editando = null
                        mostrarFormulario = true
                    }) {
                        Text("Nuevo")
                    }

                    OutlinedButton(onClick = {
                        seleccionando = !seleccionando
                        seleccionados = emptySet()
                    }) {
                        Text(
                            if (seleccionando) "Cancelar"
                            else "Seleccionar"
                        )
                    }
                }

                if (seleccionando) {
                    Button(
                        enabled = seleccionados.isNotEmpty() && !procesando,
                        onClick = { confirmarEliminar = true }
                    ) {
                        Text("Eliminar (${seleccionados.size})")
                    }
                }

                Text("${contactos.size} contacto(s)")

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(contactos, key = { it.id }) { contacto ->

                        Card {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(6.dp)
                            ) {

                                if (seleccionando) {
                                    Checkbox(
                                        checked = contacto.id in seleccionados,
                                        onCheckedChange = { marcado ->
                                            seleccionados =
                                                if (marcado) {
                                                    seleccionados + contacto.id
                                                } else {
                                                    seleccionados - contacto.id
                                                }
                                        }
                                    )
                                }

                                Text(
                                    "${contacto.nombre} ${contacto.apellido}",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(contacto.telefono)
                                Text(contacto.correo)

                                if (!seleccionando) {
                                    TextButton(onClick = {
                                        editando = contacto
                                        mostrarFormulario = true
                                    }) {
                                        Text("Editar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Confirmar eliminación") },
            text = {
                Text("¿Eliminar ${seleccionados.size} contacto(s)?")
            },
            confirmButton = {
                TextButton(
                    enabled = !procesando,
                    onClick = {
                        confirmarEliminar = false
                        vm.eliminar(seleccionados)
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun FormularioContacto(
    contacto: Contacto?,
    procesando: Boolean,
    modifier: Modifier = Modifier,
    onCancelar: () -> Unit,
    onGuardar: (String, String, String, String) -> Unit
) {
    var telefono by remember(contacto?.id) {
        mutableStateOf(contacto?.telefono.orEmpty())
    }
    var nombre by remember(contacto?.id) {
        mutableStateOf(contacto?.nombre.orEmpty())
    }
    var apellido by remember(contacto?.id) {
        mutableStateOf(contacto?.apellido.orEmpty())
    }
    var correo by remember(contacto?.id) {
        mutableStateOf(contacto?.correo.orEmpty())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            if (contacto == null) "Nuevo contacto"
            else "Editar contacto",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            enabled = !procesando,
            onClick = {
                onGuardar(telefono, nombre, apellido, correo)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (procesando) "Guardando..." else "Guardar")
        }

        OutlinedButton(
            enabled = !procesando,
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

