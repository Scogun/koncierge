package com.ucasoft.koncierge

import kotlinx.coroutines.runBlocking
import platform.windows.GetConsoleWindow
import platform.windows.SetForegroundWindow

fun main() {
    val koncierge = Koncierge()
    if (koncierge.isBiometricAvailable()) {
        println("Biometric available")
        val window = GetConsoleWindow()
        SetForegroundWindow(window)
        runBlocking {
            println("Authenticating...")
            println(koncierge.authenticate("Touch the sensor to authenticate"))
        }
    }
}