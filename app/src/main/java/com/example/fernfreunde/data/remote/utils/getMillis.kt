package com.example.fernfreunde.data.remote.utils

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

fun DocumentSnapshot.getMillis(field: String): Long? {
    val v = this.get(field) ?: return null
    return when (v) {
        is Timestamp -> v.toDate().time
        is Long -> v
        is Double -> v.toLong()
        is Int -> (v as Int).toLong()
        is Date -> v.time
        else -> null
    }
}

fun DocumentSnapshot.getStringSafe(field: String): String? = this.getString(field)
