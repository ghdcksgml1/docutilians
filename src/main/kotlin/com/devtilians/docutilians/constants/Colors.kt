package com.devtilians.docutilians.constants

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle

/**
 * CHUCK_UI Design System - Cyberpunk Neon Color Palette
 *
 * Glassmorphism + Neon Glow aesthetic for high-tech CLI interface
 */
object Colors {
    // ============================================
    // Neon Accent Colors (CHUCK_UI Core)
    // ============================================

    /** Neon Blue - Primary info, borders, titles */
    val primary: TextStyle = TextColors.rgb("#00f3ff")

    /** Neon Pink - Alert, emphasis, active state */
    val secondary: TextStyle = TextColors.rgb("#bc13fe")

    /** Neon Cyan - Accent highlights */
    val accent: TextStyle = TextColors.rgb("#00f3ff")

    // ============================================
    // Status Colors (Cyberpunk Neon)
    // ============================================

    /** Neon Green - Success, positive, online */
    val success: TextStyle = TextColors.rgb("#0aff0a")

    /** Neon Yellow - Warning, attention needed */
    val warning: TextStyle = TextColors.rgb("#fefe00")

    /** Neon Red - Error, critical alert */
    val error: TextStyle = TextColors.rgb("#ff0055")

    /** Error Bright - Glow effect for errors */
    val errorBright: TextStyle = TextColors.rgb("#ff3377")

    /** Info Cyan - Information state */
    val info: TextStyle = TextColors.rgb("#00f3ff")

    // ============================================
    // Text Colors (Dark Theme Optimized)
    // ============================================

    /** Text Primary - Main body text */
    val textPrimary: TextStyle = TextColors.rgb("#e0e0e0")

    /** Text Secondary - Supporting text */
    val textSecondary: TextStyle = TextColors.rgb("#a0a0b0")

    /** Text Muted - Captions, labels, inactive */
    val textMuted: TextStyle = TextColors.rgb("#8888aa")

    /** Text White - High contrast text */
    val textWhite: TextStyle = TextColors.rgb("#f0f0f5")

    /** Text Black - Dark text on bright backgrounds */
    val textBlack: TextStyle = TextColors.rgb("#050510")

    // ============================================
    // Border / Panel Colors (Glass Effect)
    // ============================================

    /** Border Primary - Neon blue glow border */
    val borderPrimary: TextStyle = TextColors.rgb("#00f3ff")

    /** Border Secondary - Neon pink glow border */
    val borderSecondary: TextStyle = TextColors.rgb("#bc13fe")

    /** Border Accent - Bright cyan accent */
    val borderAccent: TextStyle = TextColors.rgb("#00ccff")

    /** Border Muted - Subtle dark border */
    val borderMuted: TextStyle = TextColors.rgb("#3a3a5a")

    // ============================================
    // Table Colors (Cyberpunk Data Grid)
    // ============================================

    /** Table Border - Neon frame */
    val tableBorder: TextStyle = TextColors.rgb("#00f3ff")

    /** Table Header Background - Dark with glow */
    val tableHeaderBg: TextStyle = TextColors.rgb("#bc13fe")

    /** Table Header Text - High contrast */
    val tableHeaderText: TextStyle = TextColors.rgb("#050510")

    /** Table Footer - Bright accent */
    val tableFooter: TextStyle = TextColors.rgb("#00f3ff")

    /** Table Cell Muted - Dim data cells */
    val tableCellMuted: TextStyle = TextColors.rgb("#8888aa")

    // ============================================
    // Semantic Aliases (용도별 별칭)
    // ============================================

    val brand: TextStyle = primary
    val progress: TextStyle = secondary
    val prompt: TextStyle = warning
    val done: TextStyle = success
    val hint: TextStyle = textMuted

    // ============================================
    // Raw TextColors (Panel borderStyle 등에서 필요)
    // ============================================

    object Raw {
        /** Neon Blue - Primary */
        val primary: TextStyle = TextColors.rgb("#00f3ff")
        /** Neon Pink - Secondary */
        val secondary: TextStyle = TextColors.rgb("#bc13fe")
        /** Neon Cyan - Accent */
        val accent: TextStyle = TextColors.rgb("#00ccff")
        /** Neon Red - Error */
        val error: TextStyle = TextColors.rgb("#ff0055")
        /** Error Glow */
        val errorBright: TextStyle = TextColors.rgb("#ff3377")
        /** Neon Yellow - Warning */
        val warning: TextStyle = TextColors.rgb("#fefe00")
        /** Neon Green - Success */
        val success: TextStyle = TextColors.rgb("#0aff0a")
        /** Text White */
        val textWhite: TextStyle = TextColors.rgb("#e0e0e0")
        /** Text Muted */
        val textMuted: TextStyle = TextColors.rgb("#8888aa")
        /** Border Primary - Neon Blue */
        val borderPrimary: TextStyle = TextColors.rgb("#00f3ff")
        /** Border Accent - Pink */
        val borderAccent: TextStyle = TextColors.rgb("#bc13fe")
        /** Table Border - Neon Blue */
        val tableBorder: TextStyle = TextColors.rgb("#00f3ff")
        /** Table Header - Neon Pink */
        val tableHeaderBg: TextStyle = TextColors.rgb("#bc13fe")
    }
}
