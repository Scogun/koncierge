package com.ucasoft.koncierge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass

@Composable
@Preview(
    widthDp = 800,
)
fun AuthScreenPreview() {
    AuthScreen(
        title = {
            Text("Koncierge", fontSize = 36.sp, fontWeight = FontWeight.Bold)
        },
        description = {
            Text(
                text = "Your Multiplatform Compose UI Auth Screen",
                fontSize = 18.sp
            )
        }
    ) {

    }
}

@Composable
fun AuthScreen(
    title: @Composable () -> Unit,
    description: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    invitation: @Composable () -> Unit = { Text("Enter your PIN", fontSize = 24.sp, fontWeight = FontWeight.SemiBold) },
    supportingContent: @Composable () -> Unit = {},
    pinLength: Int = 4,
    onAuthorize: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(48.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    title()
                    Spacer(modifier = Modifier.height(16.dp))
                    description()
                }

                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier
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
                                    onAuthorize()
                                }
                            },
                            onBiometricClick = {
                                onAuthorize()
                            },
                            onDeleteClick = {
                                if (pin.isNotEmpty()) {
                                    pin = pin.dropLast(1)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        supportingContent()
                    }
                }
            }
        } else {
            Column(modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                            onAuthorize()
                            pin = ""
                        }
                    },
                    onBiometricClick = {
                        onAuthorize()
                    },
                    onDeleteClick = {
                        if (pin.isNotEmpty()) {
                            pin = pin.dropLast(1)
                        }
                    }
                )
                supportingContent()
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
            Box(modifier = Modifier.height(64.dp).clickable { onBiometricClick() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Fingerprint, contentDescription = "Bio", modifier = Modifier.size(36.dp))
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
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text.toString(), fontSize = 32.sp, textAlign = TextAlign.Center)
    }
}