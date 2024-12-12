package org.example.project.utils

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

object ConsoleState {
    private val _logs = mutableStateListOf<String>()
    val logs: SnapshotStateList<String> = _logs

    fun log(message: String) {
        _logs.add(message)
        println(message)
    }

    fun clear() {
        _logs.clear()
    }
}