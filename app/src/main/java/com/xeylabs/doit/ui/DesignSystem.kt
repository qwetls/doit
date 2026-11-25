package com.xeylabs.doit.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.data.Category as DoitCategory
import com.xeylabs.doit.data.Receipts
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.data.WalletType
import com.xeylabs.doit.ui.theme.CardBorder
import com.xeylabs.doit.ui.theme.Emerald
import com.xeylabs.doit.ui.theme.SurfaceDark

@Composable
fun DoitCard(
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceDark,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun FluxHeader(subtitle: String, displayName: String, onAvatarClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "do.it",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(Emerald.copy(alpha = 0.15f))
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName.trim().firstOrNull()?.uppercase() ?: "A",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Emerald
            )
        }
    }
}

fun categoryIcon(category: DoitCategory): ImageVector {
    return when (category) {
        DoitCategory.GAJI -> Icons.Filled.AccountBalance
        DoitCategory.LEMBUR -> Icons.Filled.Schedule
        DoitCategory.SAMPINGAN -> Icons.Filled.Work
        DoitCategory.LAIN_MASUK -> Icons.Filled.Payments
        DoitCategory.BELANJA -> Icons.Filled.ShoppingBag
        DoitCategory.MAKAN -> Icons.Filled.Restaurant
        DoitCategory.TRANSPORT -> Icons.Filled.DirectionsCar
        DoitCategory.DARURAT -> Icons.Filled.HealthAndSafety
        DoitCategory.LAIN_KELUAR -> Icons.Filled.Category
        DoitCategory.TRANSFER -> Icons.Filled.SwapHoriz
    }
}

fun customCategoryIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "shopping" -> Icons.Filled.ShoppingBag
        "restaurant" -> Icons.Filled.Restaurant
        "car" -> Icons.Filled.DirectionsCar
        "health" -> Icons.Filled.HealthAndSafety
        "work" -> Icons.Filled.Work
        "schedule" -> Icons.Filled.Schedule
        "payments" -> Icons.Filled.Payments
        "bank" -> Icons.Filled.AccountBalance
        "phone" -> Icons.Filled.PhoneAndroid
        else -> Icons.Filled.Category
    }
}

fun categoryRefIcon(ref: com.xeylabs.doit.data.CategoryRef): ImageVector {
    if (com.xeylabs.doit.data.customCategoryId(ref.key) != null) return customCategoryIcon(ref.iconKey)
    return DoitCategory.entries.firstOrNull { it.name == ref.iconKey }?.let { categoryIcon(it) }
        ?: Icons.Filled.Category
}

fun categoryRefColor(ref: com.xeylabs.doit.data.CategoryRef): Color = Color(ref.colorHex)

fun walletTypeIcon(type: WalletType): ImageVector {
    return when (type) {
        WalletType.TUNAI -> Icons.Filled.Payments
        WalletType.BANK -> Icons.Filled.AccountBalance
        WalletType.EWALLET -> Icons.Filled.PhoneAndroid
    }
}

@Composable
fun CategoryIconTile(category: DoitCategory, size: Int = 40, tint: Color? = null) {
    val color = tint ?: if (category.type == TxnType.INCOME) Emerald else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier.size(size.dp).clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}

@Composable
fun CategoryIconTile(ref: com.xeylabs.doit.data.CategoryRef, size: Int = 40, tint: Color? = null) {
    val color = tint ?: categoryRefColor(ref)
    Box(
        modifier = Modifier.size(size.dp).clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = categoryRefIcon(ref),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}

@Composable
fun ReceiptImage(relativePath: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(relativePath) {
        val file = Receipts.receiptFile(context, relativePath) ?: return@remember null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        var sample = 1
        while (bounds.outWidth / sample > 512 || bounds.outHeight / sample > 512) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        BitmapFactory.decodeFile(file.absolutePath, opts)
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Struk",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
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
