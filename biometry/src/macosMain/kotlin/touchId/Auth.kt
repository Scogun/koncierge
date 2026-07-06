package touchId

import kotlinx.cinterop.*
import platform.Foundation.NSError
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.darwin.dispatch_semaphore_create
import platform.darwin.dispatch_semaphore_signal
import platform.darwin.dispatch_semaphore_wait
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun authenticateWithTouchId() {
    val context = LAContext()
    val errorRef = memScoped { alloc<ObjCObjectVar<NSError?>>().ptr }

    if (context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, errorRef)) {

        val semaphore = dispatch_semaphore_create(0)

        context.evaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = "Authenticate to access secure data"
        ) { s,e ->
            if (s) {
                println("Successfully authenticated")
            } else {
                println("Error: ${e?.localizedDescription}")
            }

            dispatch_semaphore_signal(semaphore)
        }

        dispatch_semaphore_wait(semaphore, computeTime(5.seconds))
    } else {
        println("Error: ${errorRef.pointed.value?.localizedDescription}")
    }
}