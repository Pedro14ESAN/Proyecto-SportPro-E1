package pe.edu.esan.sportpro.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SportDark = Color(0xFF0D2D3D)
private val SportGreen = Color(0xFF9AF35A)
private val SportWhite = Color(0xFFF8FAF7)

@Composable
fun WelcomeScreen(
    onStart: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SportDark)
    ) {

        // Balón grande decorativo del fondo
        Text(
            text = "⚽",
            fontSize = 190.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(
                    x = 40.dp,
                    y = (-70).dp
                )
                .graphicsLayer {
                    alpha = 0.08f
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 34.dp,
                    vertical = 42.dp
                )
        ) {

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Text(
                text = "SPORTPRO  /  2026",
                color = SportGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )

            Spacer(
                modifier = Modifier.weight(0.75f)
            )

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
                    .background(SportGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚽",
                    fontSize = 28.sp
                )
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            Text(
                text = "SPORTPRO",
                color = Color.White,
                fontSize = 46.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "La operación de tu academia,\nsiempre en juego.",
                color = SportWhite,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SportGreen,
                    contentColor = SportDark
                )
            ) {

                Text(
                    text = "Comenzar →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }
    }
}