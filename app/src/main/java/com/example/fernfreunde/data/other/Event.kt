package com.example.fernfreunde.data.other

// Event_Klasse, um Events einmalig auszuführen
open class Event<out T> (private val data: T) {

    // wird auf true gesetzt sobald das Event ausgeführt wurde -> verhindert, dass Events mehrfach
    // ausgeführt werden
    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            data
        }
    }

    fun peekContent() = data
}