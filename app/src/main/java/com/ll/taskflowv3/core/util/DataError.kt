package com.ll.taskflowv3.core.util

// Sellamos la interfaz para que Kotlin sepa exactamente cuántos errores existen
sealed interface DataError {
    enum class Network : DataError {
        REQUEST_TIMEOUT,
        UNAUTHORIZED,
        CONFLICT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        UNKNOWN
    }
    enum class Local : DataError {
        DISK_FULL,
        UNKNOWN
    }
}