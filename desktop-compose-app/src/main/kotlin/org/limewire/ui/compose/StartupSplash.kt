package org.limewire.ui.compose

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Component
import java.awt.EventQueue
import java.awt.Font
import java.awt.Frame
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.Window
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.min
import kotlin.math.roundToInt

internal interface StartupSplashHandle {
    fun close()
}

internal object StartupSplash {
    private const val WORDMARK_RESOURCE = "/org/limewire/ui/compose/art/wireshare-wordmark.png"
    private const val ICON_RESOURCE = "/org/limewire/ui/compose/art/wireshare-icon.png"

    fun show(): StartupSplashHandle? {
        if (GraphicsEnvironment.isHeadless()) {
            return null
        }

        return runCatching {
            val wordmark = loadImage(WORDMARK_RESOURCE)
            val icon = loadImage(ICON_RESOURCE)
            val window = Window(null as Frame?)
            val canvas = SplashCanvas(wordmark = wordmark, icon = icon)

            EventQueue.invokeAndWait {
                val splashSize = SplashCanvas.windowSize()
                window.background = SplashPalette.background
                window.add(canvas)
                window.setSize(splashSize.width, splashSize.height)
                window.setLocationRelativeTo(null)
                window.isVisible = true
                window.toFront()
            }

            object : StartupSplashHandle {
                override fun close() {
                    EventQueue.invokeLater {
                        window.isVisible = false
                        window.dispose()
                    }
                }
            }
        }.getOrNull()
    }

    private fun loadImage(path: String): BufferedImage? {
        return StartupSplash::class.java.getResource(path)?.openStream()?.use(ImageIO::read)
    }
}

private object SplashPalette {
    val background = Color(0x10, 0x3C, 0x48)
    val panel = Color(0x18, 0x49, 0x56)
    val panelBorder = Color(0x2A, 0xA1, 0x98)
    val foreground = Color(0xFD, 0xF6, 0xE3)
    val muted = Color(0xCA, 0xD8, 0xD9)
    val accent = Color(0xB5, 0x89, 0x00)
}

private class SplashCanvas(
    private val wordmark: BufferedImage?,
    private val icon: BufferedImage?
) : Component() {
    override fun paint(graphics: Graphics) {
        val g = graphics as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.color = SplashPalette.background
        g.fillRect(0, 0, width, height)

        val panelX = 24
        val panelY = 24
        val panelW = width - 48
        val panelH = height - 48
        g.color = SplashPalette.panel
        g.fillRoundRect(panelX, panelY, panelW, panelH, 36, 36)
        g.color = SplashPalette.panelBorder
        g.stroke = BasicStroke(1.4f)
        g.drawRoundRect(panelX, panelY, panelW, panelH, 36, 36)

        if (wordmark != null) {
            val wordmarkInset = (panelW * 0.05f).roundToInt().coerceAtLeast(42)
            val wordmarkMaxH = (panelH * 0.66f).roundToInt().coerceAtMost(320)
            drawImageFit(g, wordmark, panelX + wordmarkInset, panelY + 34, panelW - wordmarkInset * 2, wordmarkMaxH)
        } else {
            g.color = SplashPalette.foreground
            g.font = Font(Font.SERIF, Font.BOLD, 48)
            g.drawString("WireShare", panelX + 52, panelY + 105)
        }

        if (icon != null) {
            drawImageFit(g, icon, panelX + 46, panelY + panelH - 92, 54, 54)
        }

        g.color = SplashPalette.foreground
        g.font = Font(Font.SANS_SERIF, Font.BOLD, 16)
        g.drawString("Starting WireShare", panelX + 116, panelY + panelH - 58)
        g.color = SplashPalette.muted
        g.font = Font(Font.SANS_SERIF, Font.PLAIN, 13)
        g.drawString("Preparing network, library, and transfer services...", panelX + 116, panelY + panelH - 36)

        val barX = panelX + 42
        val barY = panelY + panelH - 18
        val barW = panelW - 84
        g.color = SplashPalette.background
        g.fillRoundRect(barX, barY, barW, 4, 4, 4)
        g.color = SplashPalette.accent
        g.fillRoundRect(barX, barY, (barW * 0.42f).roundToInt(), 4, 4, 4)
    }

    private fun drawImageFit(g: Graphics2D, image: BufferedImage, x: Int, y: Int, maxW: Int, maxH: Int) {
        val scale = min(maxW.toDouble() / image.width.toDouble(), maxH.toDouble() / image.height.toDouble())
        val drawW = (image.width * scale).roundToInt()
        val drawH = (image.height * scale).roundToInt()
        val drawX = x + (maxW - drawW) / 2
        val drawY = y + (maxH - drawH) / 2
        g.drawImage(image, drawX, drawY, drawW, drawH, null)
    }

    companion object {
        private const val PREFERRED_WIDTH = 1040
        private const val PREFERRED_HEIGHT = 520
        private const val MIN_WIDTH = 680
        private const val MIN_HEIGHT = 360

        fun windowSize(): java.awt.Dimension {
            val bounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .defaultScreenDevice
                .defaultConfiguration
                .bounds
            val width = min(PREFERRED_WIDTH, (bounds.width * 0.86f).roundToInt()).coerceAtLeast(MIN_WIDTH)
            val height = min(PREFERRED_HEIGHT, (bounds.height * 0.74f).roundToInt()).coerceAtLeast(MIN_HEIGHT)
            return java.awt.Dimension(width, height)
        }
    }
}
