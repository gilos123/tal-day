package com.maayan.studytracker.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

/**
 * Celebration confetti overlay. Drops [particleCount] colored rectangles that fall
 * with slight horizontal drift + rotation, fading out, then self-terminates.
 *
 * Drive it by flipping [active] to true whenever a burst should fire (e.g. on
 * timer completion). The animation re-runs any time [active] transitions
 * false → true.
 *
 * Place at the top of a [androidx.compose.foundation.layout.Box] so it overlays
 * the rest of the content.
 */
@Composable
fun Confetti(
    active: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
    durationMillis: Long = 1800L,
    palette: List<Color> = defaultPalette()
) {
    var particles by remember { mutableStateOf(emptyList<Particle>()) }
    var elapsedMs by remember { mutableStateOf(0L) }

    LaunchedEffect(active) {
        if (!active) {
            particles = emptyList()
            return@LaunchedEffect
        }
        particles = List(particleCount) { Particle.random(palette) }
        elapsedMs = 0L
        val start = System.nanoTime()
        while (elapsedMs < durationMillis) {
            withFrameNanos { now -> elapsedMs = (now - start) / 1_000_000L }
        }
        particles = emptyList()
    }

    if (!active || particles.isEmpty()) return

    val progress = (elapsedMs.toFloat() / durationMillis).coerceIn(0f, 1f)
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val alpha = (1f - progress).coerceIn(0f, 1f)
        particles.forEach { p ->
            val t = progress * p.speed
            val x = p.startXFraction * w + sin(p.driftPhase + t * 6f) * p.driftAmp
            val y = (p.startYFraction + t * 1.3f) * h
            if (y !in -20f..(h + 20f)) return@forEach
            val deg = p.rotation + t * p.spin
            rotate(degrees = deg, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x - p.size / 2f, y - p.size / 2f),
                    size = Size(p.size, p.size * 0.45f)
                )
            }
        }
    }
}

private data class Particle(
    val startXFraction: Float,
    val startYFraction: Float,
    val driftPhase: Float,
    val driftAmp: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val spin: Float
) {
    companion object {
        fun random(palette: List<Color>): Particle {
            val r = Random.Default
            return Particle(
                startXFraction = r.nextFloat(),
                startYFraction = -0.15f - r.nextFloat() * 0.15f,
                driftPhase = r.nextFloat() * (2f * Math.PI.toFloat()),
                driftAmp = 20f + r.nextFloat() * 40f,
                speed = 0.8f + r.nextFloat() * 0.6f,
                color = palette.random(r),
                size = 8f + r.nextFloat() * 10f,
                rotation = r.nextFloat() * 360f,
                spin = (r.nextFloat() - 0.5f) * 720f
            )
        }
    }
}

private fun defaultPalette(): List<Color> = listOf(
    Color(0xFF5BE32A), // lime
    Color(0xFFFF6B6B), // coral
    Color(0xFFFF9F1C), // flame
    Color(0xFF4DB2FF), // sky
    Color(0xFFF59E0B), // amber
    Color(0xFFA855F7)  // grape
)
