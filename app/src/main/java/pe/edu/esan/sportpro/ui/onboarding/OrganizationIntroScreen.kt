package pe.edu.esan.sportpro.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBlue = Color(0xFF0D2D3D)
private val Green = Color(0xFF159D61)
private val LightGreen = Color(0xFFE1F5D9)
private val GrayText = Color(0xFF77838B)

@Composable
fun OrganizationIntroScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFCF9))
            .padding(
                horizontal = 28.dp,
                vertical = 36.dp
            )
    ) {

        Text(
            text = "← Volver",
            color = Green,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable {
                onBack()
            }
        )

        Spacer(
            modifier = Modifier.height(38.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    LightGreen,
                    RoundedCornerShape(26.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .border(
                        BorderStroke(
                            14.dp,
                            Color(0xFF98ED6A)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "⚽",
                    fontSize = 50.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(36.dp)
        )

        Text(
            text = "01  /  03  ·  ORGANIZACIÓN",
            color = Green,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = "Todo tu equipo, en la\nmisma cancha.",
            color = DarkBlue,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Black,
            fontSize = 30.sp,
            lineHeight = 31.sp
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Entrenamientos, convocatorias y partidos\norganizados en un solo lugar.",
            color = GrayText,
            fontSize = 15.sp,
            lineHeight = 23.sp
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(5.dp)
                    .background(
                        Green,
                        RoundedCornerShape(10.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(
                        Color(0xFFD7E2DC),
                        CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(
                        Color(0xFFD7E2DC),
                        CircleShape
                    )
            )
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBlue,
                contentColor = Color.White
            )
        ) {

            Text(
                text = "Continuar",
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Saltar introducción",
            color = Green,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable {
                    onSkip()
                }
                .padding(14.dp)
        )
    }
}