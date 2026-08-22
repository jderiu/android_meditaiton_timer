package com.example.meditationtimer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.meditationtimer.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Loaded on-device via the Google Fonts provider; falls back to the
// platform serif/sans when the provider is unavailable.
val Cormorant = FontFamily(
    Font(GoogleFont("Cormorant Garamond"), fontProvider, FontWeight.Normal),
    Font(GoogleFont("Cormorant Garamond"), fontProvider, FontWeight.Medium),
    Font(GoogleFont("Cormorant Garamond"), fontProvider, FontWeight.SemiBold)
)

val Jost = FontFamily(
    Font(GoogleFont("Jost"), fontProvider, FontWeight.Light),
    Font(GoogleFont("Jost"), fontProvider, FontWeight.Normal),
    Font(GoogleFont("Jost"), fontProvider, FontWeight.Medium)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Jost,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Jost,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 3.sp
    )
)
