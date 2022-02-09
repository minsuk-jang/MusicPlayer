package com.example.musicplayer.ui.util

import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext


val exception = CoroutineExceptionHandler { coroutineContext, throwable ->
    throwable.printThrowable()
}
val supervisorJob: CoroutineContext = SupervisorJob() + Dispatchers.IO + exception

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