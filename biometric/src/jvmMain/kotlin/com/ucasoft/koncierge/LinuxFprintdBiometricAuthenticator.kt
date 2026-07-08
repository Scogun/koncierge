package com.ucasoft.koncierge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class LinuxFprintdBiometricAuthenticator : JvmBiometricAuthenticator {

    override fun isBiometricAvailable(): Boolean = checkAvailability() == BiometricResults.Available

    override suspend fun authenticate(message: String, timeout: Duration): BiometricResults {
        val availability = checkAvailability()
        if (availability != BiometricResults.Available) {
            return availability
        }

        val result = runCommand(
            command = listOf("fprintd-verify", currentUser()),
            timeout = timeout,
        )

        return when {
            result.timedOut -> BiometricResults.AuthenticationCancelled
            result.exitCode == 0 -> BiometricResults.AuthenticationSuccessful
            result.outputContains("verify-no-match") -> BiometricResults.AuthenticationFailed
            result.outputContains("verify-disconnected") -> BiometricResults.HardwareUnavailable
            result.outputContains("No devices available") -> BiometricResults.HardwareUnavailable
            result.outputContains("No fingers enrolled") -> BiometricResults.NotEnrolled
            result.outputContains("verify-swipe-too-short") -> BiometricResults.AuthenticationFailed
            else -> BiometricResults.AuthenticationError(result.output.ifBlank { "fprintd verification failed" })
        }
    }

    private fun checkAvailability(): BiometricResults {
        val result = runCommandBlocking(
            command = listOf("fprintd-list", currentUser()),
            timeout = 3.seconds,
        )

        return when {
            result.commandMissing -> BiometricResults.FeatureUnavailable
            result.timedOut -> BiometricResults.FeatureUnavailable
            result.outputContains("No devices available") -> BiometricResults.HardwareUnavailable
            result.outputContains("No fingers enrolled") -> BiometricResults.NotEnrolled
            result.exitCode == 0 -> BiometricResults.Available
            else -> BiometricResults.AuthenticationError(result.output.ifBlank { "fprintd availability check failed" })
        }
    }

    private fun currentUser(): String {
        return System.getProperty("user.name").orEmpty()
    }

    private suspend fun runCommand(command: List<String>, timeout: Duration): CommandResult {
        return withContext(Dispatchers.IO) {
            runCommandBlocking(command, timeout)
        }
    }

    private fun runCommandBlocking(command: List<String>, timeout: Duration): CommandResult {
        val process = try {
            ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
        } catch (_: IOException) {
            return CommandResult(commandMissing = true)
        }

        val completed = if (timeout.isInfinite()) {
            process.waitFor()
            true
        } else {
            process.waitFor(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        }

        if (!completed) {
            process.destroyForcibly()
            return CommandResult(timedOut = true)
        }

        val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
        return CommandResult(exitCode = process.exitValue(), output = output)
    }

    private data class CommandResult(
        val exitCode: Int = -1,
        val output: String = "",
        val timedOut: Boolean = false,
        val commandMissing: Boolean = false,
    ) {
        fun outputContains(value: String): Boolean = output.contains(value, ignoreCase = true)
    }
}
