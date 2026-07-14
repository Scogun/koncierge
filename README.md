# Koncierge

Authentication and biometric support for Kotlin Multiplatform.

[![GitHub](https://img.shields.io/github/license/Scogun/koncierge?color=blue)](LICENSE) ![Publish workflow](https://github.com/Scogun/koncierge/actions/workflows/publish.yml/badge.svg) [![Maven Central](https://img.shields.io/maven-central/v/com.ucasoft.koncierge/biometric?color=blue)](https://central.sonatype.com/artifact/com.ucasoft.koncierge/biometric)

### Features

* A common biometric API with availability checks and detailed authentication results.
* Platform biometric implementations:
  * Android Biometric Prompt.
  * Windows Hello on JVM and Kotlin/Native.
  * Touch ID on macOS ARM64 and JVM on macOS ARM64.
  * `fprintd` on Linux JVM.
* PIN authentication with pluggable hashing and storage providers.
* A responsive Compose Multiplatform PIN and biometric authentication screen.

### Modules

| Artifact | Description | Targets |
| --- | --- | --- |
| `biometric` | Common biometric API and platform implementations | JVM, Android, macOS ARM64, mingwX64 |
| `auth` | PIN storage, hashing, and combined PIN/biometric authentication | JVM, Android |
| `auth-compose` | Compose Multiplatform authentication screen | JVM, Android |

### Usage

#### Add a dependency

Choose the highest-level module that your application needs. `auth-compose` exposes `auth`, and `auth` exposes `biometric`.

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.ucasoft.koncierge:biometric:0.1.0")
            // Or:
            // implementation("com.ucasoft.koncierge:auth:0.1.0")
            // implementation("com.ucasoft.koncierge:auth-compose:0.1.0")
        }
    }
}
```

#### Create Koncierge

On JVM, macOS ARM64, and mingwX64:

```kotlin
val koncierge = Koncierge()
```

On Android, pass a `FragmentActivity` as the context:

```kotlin
val koncierge = Koncierge(activity)
```

Create the platform instance at the application boundary and pass it to common code.

#### Request biometric authentication

```kotlin
when (val result = koncierge.authenticate("Confirm your identity")) {
    BiometricResults.AuthenticationSuccessful -> {
        // Continue to protected content.
    }
    BiometricResults.AuthenticationCancelled -> {
        // The request was cancelled.
    }
    else -> {
        // Handle unavailable hardware, missing enrollment, lockout, or failure.
    }
}
```

Use `koncierge.isBiometricAvailable()` when you only need an availability check.

#### Store and verify a PIN

```kotlin
val authenticator = Authenticator(koncierge)

authenticator.storePinCode("1234")

if (authenticator.verifyPinCode("1234")) {
    // PIN verified.
}
```

By default, `Authenticator` hashes the PIN with SHA-256 and stores the result with Multiplatform Settings. Implement `PinCodeHashProvider` or `PinCodeStoreProvider` to use hashing and storage appropriate for your application's security requirements.

#### Show the Compose authentication screen

```kotlin
@Composable
fun LoginScreen(
    authenticator: Authenticator,
    onAuthorized: () -> Unit,
) {
    AuthScreen(
        authenticator = authenticator,
        title = { Text("Koncierge") },
        description = { Text("Authenticate to continue") },
        onAuthorizationFailed = { method ->
            // Handle a failed PIN or biometric attempt.
        },
        onAuthorized = onAuthorized,
    )
}
```

Biometric authentication is requested automatically when it is available. Set `autoRequestBiometry = false` to wait for the user to tap the biometric button.

### Platform requirements

* Android requires API 26 or newer. Pass a `FragmentActivity` to `Koncierge`.
* JVM applications require Java 17 or newer.
* Linux JVM biometric authentication requires `fprintd-list` and `fprintd-verify` on `PATH`, with a fingerprint already enrolled.
* The packaged macOS JVM native library currently supports ARM64.
* The packaged Windows JVM native library supports x86-64.

### Current status

Koncierge is currently at version `0.1.0` and is under active development.

### License

Koncierge is licensed under the [Apache License 2.0](LICENSE).
