package com.ucasoft.koncierge

import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val koncierge = Koncierge()
    if (koncierge.isBiometricAvailable()) {
        println(koncierge.authenticate("authorize to proceed", 5.seconds))
    }
}