package com.example.fernfreunde.data.other

// Resource-Klasse für klare UI-State-Repräsentation (Loading, Success, Error)
// Wichtig für Fehlerbehandlungen, z.B. um zu verhindern dass bei Screen-Rotationen Fehlermeldungen#
// erneut angezeigt werden

data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String?)
{
    companion object {
        fun <T> success(data: T?) = Resource(Status.SUCCESS, data, null)

        fun <T> error(message: String, data: T?) = Resource(Status.ERROR, data, message)

        fun <T> loading(data: T?) = Resource(Status.LOADING, data, null)
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}