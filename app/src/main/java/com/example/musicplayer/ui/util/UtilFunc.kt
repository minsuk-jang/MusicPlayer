package com.example.musicplayer.ui.util

import androidx.lifecycle.Lifecycle
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext


val exception = CoroutineExceptionHandler { coroutineContext, throwable ->
    throwable.printThrowable()
}
val supervisorJob: CoroutineContext = SupervisorJob()

fun Throwable.printThrowable() {
    val message = this.message
    val localMessage = this.localizedMessage
    val causeMessage = this.cause?.message


    Logger.e(
        "Error\n" +
                "message: $message\n" +
                "local message: $localMessage\n" +
                "Cause message: $causeMessage"
    )
}