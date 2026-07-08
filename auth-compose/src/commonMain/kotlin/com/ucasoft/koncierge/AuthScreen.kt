package com.ucasoft.koncierge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.launch

@Composable
@Preview(
    widthDp = 800,
)
fun AuthScreenWidePreview() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF006D3D),
            surface = Color(0xFFF2FFF6),
            onSurface = Color(0xFF102018),
        )
    ) {
        AuthScreenPreviewContent()
    }
}

@Composable
@Preview(
    name = "Dark Coral",
    widthDp = 360,
)
fun AuthScreenDarkPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFFB4A8),
            surface = Color(0xFF241816),
            onSurface = Color(0xFFFFEDEA),
        )
    ) {
        AuthScreenPreviewContent(false)
    }
}

@Composable
@Preview
fun AuthScreenPreview() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF4B5DCE),
            surface = Color(0xFFF7F4FF),
            onSurface = Color(0xFF181A2F),
        )
    ) {
        AuthScreenPreviewContent()
    }
}

@Composable
private fun AuthScreenPreviewContent(showBiometry: Boolean = true) {
    AuthScreen(
        title = {
            Text("Koncierge", fontSize = 36.sp, fontWeight = FontWeight.Bold)
        },
        description = {
            Text(
                text = "Your Multiplatform Compose UI Auth Screen",
                fontSize = 18.sp
            )
        },
        showBiometry = showBiometry
    )
}

@Composable
fun AuthScreen(
    authenticator: Authenticator,
    title: @Composable () -> Unit,
    description: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    invitation: @Composable () -> Unit = { Text("Enter your PIN", style = MaterialTheme.typography.headlineSmall) },
    supportingContent: @Composable () -> Unit = {},
    pinLength: Int = 4,
    biometryEnabled: Boolean = true,
    onAuthorizationFailed: (AuthScreenAuthorizationMethod) -> Unit = {},
    onAuthorized: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val showBiometry = biometryEnabled && authenticator.isBiometryAvailable()

    AuthScreen(
        title = title,
        description = description,
        modifier = modifier,
        invitation = invitation,
        supportingContent = supportingContent,
        pinLength = pinLength,
        showBiometry = showBiometry,
        onPinCodeEntered = { pinCode ->
            scope.launch {
                if (authenticator.verifyPinCode(pinCode)) {
                    onAuthorized()
                } else {
                    onAuthorizationFailed(AuthScreenAuthorizationMethod.PinCode)
                }
            }
        },
        onBiometryRequested = {
            scope.launch {
                if (authenticator.verifyBiometry()) {
                    onAuthorized()
                } else {
                    onAuthorizationFailed(AuthScreenAuthorizationMethod.Biometry)
                }
            }
        },
    )
}

enum class AuthScreenAuthorizationMethod {
    PinCode,
    Biometry,
}

@Composable
fun AuthScreen(
    title: @Composable () -> Unit,
    description: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    invitation: @Composable () -> Unit = { Text("Enter your PIN", style = MaterialTheme.typography.headlineSmall) },
    supportingContent: @Composable () -> Unit = {},
    pinLength: Int = 4,
    showBiometry: Boolean = true,
    onPinCodeEntered: (String) -> Unit = {},
    onBiometryRequested: () -> Unit = {},
) {
    var pin by remember { mutableStateOf("") }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerColor = MaterialTheme.colorScheme.surface
        val contentColor = contentColorFor(containerColor)

        val isWideScreen = maxWidth > WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(containerColor)
                        .padding(48.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        title()
                        Spacer(modifier = Modifier.height(16.dp))
                        description()
                    }
                }

                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        Column(
                            modifier = Modifier
                                .width(360.dp)
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            invitation()
                            Spacer(modifier = Modifier.height(32.dp))
                            PinDots(pin.length, pinLength)
                            Spacer(modifier = Modifier.weight(1f))
                            PinKeypad(
                                onNumberClick = {
                                    pin += it
                                    if (pin.length == pinLength) {
                                        onPinCodeEntered(pin)
                                        pin = ""
                                    }
                                },
                                onBiometricClick = {
                                    onBiometryRequested()
                                },
                                onDeleteClick = {
                                    if (pin.isNotEmpty()) {
                                        pin = pin.dropLast(1)
                                    }
                                },
                                showBiometry = showBiometry,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            supportingContent()
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier
                .fillMaxHeight()
                .background(containerColor),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    Spacer(modifier = Modifier.weight(0.4f))
                    title()
                    Spacer(modifier = Modifier.height(32.dp))
                    invitation()
                    Spacer(modifier = Modifier.height(32.dp))
                    PinDots(pin.length, pinLength)
                    Spacer(modifier = Modifier.height(24.dp))
                    PinKeypad(
                        onNumberClick = {
                            pin += it
                            if (pin.length == pinLength) {
                                onPinCodeEntered(pin)
                                pin = ""
                            }
                        },
                        onBiometricClick = {
                            onBiometryRequested()
                        },
                        onDeleteClick = {
                            if (pin.isNotEmpty()) {
                                pin = pin.dropLast(1)
                            }
                        },
                        showBiometry = showBiometry,
                    )
                    supportingContent()
                }
            }
        }
    }
}

@Composable
private fun PinDots(currentLength: Int, pinLength: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(pinLength) {
            val isFilled = it < currentLength
            val baseColor = MaterialTheme.colorScheme.primary
            Box(modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (isFilled) baseColor else baseColor.copy(alpha = 0.25f))
            )
        }
    }
}

@Composable
private fun PinKeypad(
    onNumberClick: (Char) -> Unit,
    onBiometricClick: () -> Unit,
    onDeleteClick: () -> Unit,
    showBiometry: Boolean,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(9) {
            val caption = (it + 1).digitToChar()
            KeypadItem(caption) {
                onNumberClick(caption)
            }
        }
        item {
            if (showBiometry) {
                Box(modifier = Modifier.height(64.dp).clickable { onBiometricClick() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Fingerprint, contentDescription = "Bio", modifier = Modifier.size(36.dp))
                }
            } else {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
        val zeroKeyCaption = '0'
        item { KeypadItem(zeroKeyCaption) { onNumberClick(zeroKeyCaption) } }
        item {
            Box(modifier = Modifier.height(64.dp).clickable { onDeleteClick() }, contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Del", modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun KeypadItem(text: Char, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(64.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.toString(),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center)
    }
}
