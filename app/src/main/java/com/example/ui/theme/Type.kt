package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val BeVietnamProFamily = FontFamily(
    Font(R.font.be_vietnam_pro, FontWeight.Normal),
    Font(R.font.be_vietnam_pro, FontWeight.Medium),
    Font(R.font.be_vietnam_pro, FontWeight.SemiBold),
    Font(R.font.be_vietnam_pro, FontWeight.Bold)
)

fun farmCareTypography(largeText: Boolean = false): Typography {
    val scale = if (largeText) 1.12f else 1.0f
    return Typography(
        displaySmall = TextStyle(
            fontFamily = BeVietnamProFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * scale).sp,
            lineHeight = (36 * scale).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = BeVietnamProFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (24 * scale).sp,
            lineHeight = (32 * scale).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = BeVietnamProFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20 * scale).sp,
            lineHeight = (28 * scale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = BeVietnamProFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18 * scale).sp,
            lineHeight = (26 * scale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = BeVietnamProFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * scale).sp,
            lineHeight = (24 * scale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = BeVietnamProFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = BeVietnamProFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * scale).sp,
            lineHeight = (24 * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = BeVietnamProFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * scale).sp,
            lineHeight = (21 * scale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = BeVietnamProFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * scale).sp,
            lineHeight = (18 * scale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = BeVietnamProFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (15 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = BeVietnamProFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = BeVietnamProFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * scale).sp,
            lineHeight = (16 * scale).sp
        )
    )
}
