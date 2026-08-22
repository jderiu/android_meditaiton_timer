package com.example.meditationtimer.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meditationtimer.billing.TipJar
import com.example.meditationtimer.ui.theme.Brass
import com.example.meditationtimer.ui.theme.Cormorant
import com.example.meditationtimer.ui.theme.Cream
import com.example.meditationtimer.ui.theme.CreamBright
import com.example.meditationtimer.ui.theme.EspressoMid
import com.example.meditationtimer.ui.theme.Jost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSheet(tipJar: TipJar, onDismiss: () -> Unit) {
    val tips by tipJar.tips.collectAsState()
    val thanks by tipJar.thanks.collectAsState()
    val activity = LocalActivity.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = EspressoMid,
        contentColor = Cream
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Meditation Timer",
                fontFamily = Cormorant,
                fontWeight = FontWeight.Medium,
                fontSize = 26.sp,
                color = CreamBright
            )
            Spacer(Modifier.height(8.dp))
            TinyLabel("a quiet timer · no ads · no accounts", fontSize = 11, tracking = 2f, alpha = 0.55f)
            Spacer(Modifier.height(26.dp))
            when {
                thanks -> Text(
                    "Thank you. May your practice be steady.",
                    fontFamily = Jost,
                    fontSize = 14.sp,
                    color = Cream.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                tips.isNotEmpty() -> {
                    TinyLabel("if it serves your practice, leave a tip", fontSize = 10, tracking = 2.5f, alpha = 0.5f)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        tips.forEach { tip ->
                            TipPill(tip.price) {
                                activity?.let { tipJar.purchase(it, tip) }
                            }
                        }
                    }
                }
                else -> TinyLabel(
                    "tips appear here once the app is on google play",
                    fontSize = 10,
                    tracking = 2f,
                    alpha = 0.4f
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TipPill(price: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier
            .height(44.dp)
            .clip(shape)
            .background(Color.Transparent, shape)
            .border(1.dp, Brass.copy(alpha = 0.45f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            price,
            fontFamily = Jost,
            fontSize = 13.sp,
            letterSpacing = 1.sp,
            color = Cream.copy(alpha = 0.8f)
        )
    }
}
