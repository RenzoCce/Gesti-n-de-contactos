package com.example.gestioncontactos.domain

object ContactoValidator {
    private val telefonoRegex = Regex("[0-9]{9}")

    private val correoRegex =
        Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")

    fun validar(
        telefono: String,
        nombre: String,
        apellido: String,
        correo: String
    ): String?{
        if (!telefonoRegex.matches(telefono)) {
            return "El teléfono debe tener exactamente 9 dígitos."
        }

        if (nombre.isBlank() || nombre.length > 200) {
            return "El nombre debe tener entre 1 y 200 caracteres."
        }

        if (apellido.isBlank() || apellido.length > 200) {
            return "El apellido debe tener entre 1 y 200 caracteres."
        }

        if (!correoRegex.matches(correo)) {
            return "El correo electrónico no es válido."
        }

        return null
    }

}