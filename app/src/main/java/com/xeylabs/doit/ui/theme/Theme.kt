package com.xeylabs.doit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

@Composable
fun DoitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DoitColorScheme,
        typography = Typography(),
        content = content
    )
}
