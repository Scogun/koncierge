package touchId

import platform.darwin.DISPATCH_TIME_FOREVER
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_time
import kotlin.time.Duration

fun computeTime(duration: Duration) =
    if (duration.isInfinite())
        DISPATCH_TIME_FOREVER
    else
        dispatch_time(DISPATCH_TIME_NOW, duration.inWholeNanoseconds)