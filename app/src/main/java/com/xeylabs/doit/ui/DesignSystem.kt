package com.xeylabs.doit.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.ui.theme.CardBorder
import com.xeylabs.doit.ui.theme.SurfaceDark

@Composable
fun DoitCard(
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceDark,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun AnimatedMoney(
    amount: Long,
    style: TextStyle,
    color: Color = MaterialTheme.colorScheme.onSurface,
    currency: com.xeylabs.doit.data.Currency = com.xeylabs.doit.data.Currency.IDR,
    modifier: Modifier = Modifier
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(amount) {
        anim.snapTo(0f)
        anim.animateTo(1f, animationSpec = tween(durationMillis = 700))
    }
    Text(
        text = formatMoney((amount * anim.value).toLong(), currency),
        style = style,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}
