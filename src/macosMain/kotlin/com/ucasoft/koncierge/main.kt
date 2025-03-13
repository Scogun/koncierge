package com.ucasoft.koncierge

import kotlinx.coroutines.runBlocking
import touchId.authenticateWithTouchId
import kotlin.time.Duration.Companion.seconds

fun main() {
  val koncierge = Koncierge()
  if (koncierge.isBiometricAvailable()) {
    runBlocking {
      println(koncierge.authenticate("authorize to proceed", 5.seconds))
    }
  }
  //authenticateWithTouchId()
}