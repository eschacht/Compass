package eschacht.eng.compass
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import eschacht.eng.compass.R
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf

import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eschacht.eng.compass.ui.theme.CompassTheme
import eschacht.eng.compass.ui.theme.White


import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Paint as AndroidPaint
private lateinit var compassSensorManager: CompassSensorManager
private var azimuth by mutableFloatStateOf(0f)
const val version = "1.0.1"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")

        compassSensorManager = CompassSensorManager(this) { newAzimuth ->
            azimuth = newAzimuth
            Log.d("MainActivity", "Azimuth changed: $azimuth")
        }
        setContent {
            CompassTheme {

                // A surface container using the 'background' color from the theme
                Surface (
                    color = MaterialTheme.colorScheme.primary,
                ){
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 15.dp,vertical = 5.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Compass(azimuth = azimuth) // Replace with dynamic azimuth if needed
                        }
                    }

                }
                MyAppBar()
            }
        }


    }

    override fun onResume() {
        super.onResume()
        compassSensorManager.start()
        Log.d("MainActivity", "onResume")

    }

    override fun onPause() {
        super.onPause()
        compassSensorManager.stop()
        Log.d("MainActivity", "onPause")
    }
}

@Composable
fun Compass(azimuth: Float) {
    Log.d("Compass Composable", "Rendering with azimuth: $azimuth")
    val displayColor = MaterialTheme.colorScheme.secondary
    val transparentDisplayColor = displayColor.copy(alpha = 0.5f)
    val fontPixelSize =  with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.fontSize.toPx()}

    Canvas(modifier = Modifier.size(275.dp)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2



        val textPaintDeg = AndroidPaint().apply {
            color = displayColor.toArgb()
            textSize = 30f
            textAlign = AndroidPaint.Align.CENTER
            isAntiAlias = true
        }
        val textPaintText = AndroidPaint().apply {
            color = displayColor.toArgb()
            typeface = Typeface.DEFAULT
            textSize = fontPixelSize
            textAlign = AndroidPaint.Align.CENTER
            isAntiAlias = true
        }

        val fontMetricsDeg: android.graphics.Paint.FontMetrics = textPaintDeg.fontMetrics
        val textHeightDeg: Float = (fontMetricsDeg.bottom - fontMetricsDeg.top)/4

        val fontMetricsText: android.graphics.Paint.FontMetrics = textPaintText.fontMetrics
        val textHeightText: Float = (fontMetricsText.bottom - fontMetricsText.top)/4

        // Draw lines and texts
        for (deg in 0 until 360 step 2) {
            val angle = Math.toRadians(deg.toDouble() - azimuth - 90)
            val isMajor = deg % 90 == 0
            val isMinor = deg % 30 == 0
            val lineLength = when {
                isMajor -> 50f
                isMinor -> 40f
                else -> 30f
            }
            val lineStartRadius = radius + lineLength

            val lineStartX = (centerX + lineStartRadius * cos(angle)).toFloat()
            val lineStartY = (centerY + lineStartRadius * sin(angle)).toFloat()
            val lineEndX = (centerX + radius * cos(angle)).toFloat()
            val lineEndY = (centerY + radius * sin(angle)).toFloat()

            drawLine(
                displayColor,
                start = Offset(lineStartX, lineStartY),
                end = Offset(lineEndX, lineEndY),
                strokeWidth = if (isMajor) 10f else 5f
            )

            // Draw text for cardinal points inside the circle
            if (isMajor) {
                val textRadius = radius - 50f
                val textX = (centerX + textRadius * cos(angle)).toFloat()
                val textY = (centerY + textRadius * sin(angle)).toFloat() + textHeightText

                val text = when (deg) {
                    0 -> "N"
                    90 -> "E"
                    180 -> "S"
                    270 -> "W"
                    else -> ""
                }

                drawContext.canvas.save()
                drawContext.canvas.nativeCanvas.drawText(text, textX, textY, textPaintText)
                drawContext.canvas.restore()
            }

            // Draw degree markers outside the circle
            if (isMinor) {
                val textRadius = radius + 80f
                val textX = (centerX + textRadius * cos(angle)).toFloat()
                val textY = (centerY + textRadius * sin(angle)).toFloat() + textHeightDeg

                val text = "$deg"

                drawContext.canvas.save()
                drawContext.canvas.nativeCanvas.drawText(text, textX, textY, textPaintDeg)
                drawContext.canvas.restore()
            }
        }


        drawLine(
            transparentDisplayColor,
            start = Offset(centerX, 0f),
            end = Offset(centerX,   -size.minDimension/7),
            strokeWidth = size.minDimension/50
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(), // Fill the maximum width available
        horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
    ) {
        // Display azimuth
        Text(
            text = "${azimuth.toInt()}°",
            color = displayColor,
            style = MaterialTheme.typography.bodyLarge
        )

        // Spacer for some space between the texts
        Spacer(modifier = Modifier.height(1.dp))

        // Display cardinal direction
        val cardinalDirectionResId = getCardinalDirectionResource(azimuth)
        Text(
            text = stringResource(id = cardinalDirectionResId),
            color = displayColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppBar() {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val email = "eschacht.eng@gmail.com"
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    }
    val licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0"
    val licenseIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(licenseUrl)
    }
    val gitHubUrl = "https://github.com/eschacht/Compass"
    val gitHubIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(gitHubUrl)
    }

    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            // IconButton for showing the menu
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }

            // DropdownMenu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("${stringResource(R.string.author)}:\nEnno Schacht") },
                    onClick = {
                        showMenu = false
                        context.startActivity(Intent.createChooser(emailIntent, "Send Email"))

                        }
                )

                DropdownMenuItem(
                    text = { Text("${stringResource(R.string.licence)}:\nApache License 2.0") },
                    onClick = {
                        showMenu = false
                        context.startActivity(Intent.createChooser(licenseIntent, "OpenBrowser"))


                    }
                )
                DropdownMenuItem(
                    text = { Text("${stringResource(R.string.source)}:\nGitHub") },
                    onClick = {
                        context.startActivity(Intent.createChooser(gitHubIntent, "OpenBrowser"))

                        showMenu = false

                    }
                )
                DropdownMenuItem(
                    text = { Text("${stringResource(R.string.version)}:\n$version") },
                    onClick = {
                        showMenu = false
                    }
                )
                // Add other items if needed
            }
        }
    )
}


fun getCardinalDirectionResource(azimuth: Float): Int {
    val directionResourceIds = arrayOf(
        R.string.north, R.string.northeast, R.string.east, R.string.southeast,
        R.string.south, R.string.southwest, R.string.west, R.string.northwest, R.string.north
    )
    val index = ((azimuth + 22.5f) / 45.0f).toInt() % 8
    return directionResourceIds[index]
}

@Preview(showSystemUi = true)
@Preview(showBackground = true)
@Composable
fun CompassPreview() {
    CompassTheme {
        // A surface container using the 'background' color from the theme
        Surface (
            color = MaterialTheme.colorScheme.primary,
        ){

            Surface(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 15.dp,vertical = 5.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Compass(2f) // Replace with dynamic azimuth if needed
                }
            }
        }
        MyAppBar()
    }
}