package com.devtilians.docutilians.constants

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle

/** Docutilians CLI Color System */
object Colors {
    // ============================================
    // Primary Colors
    // ============================================

    /** Primary Purple - 주요 브랜드 컬러 */
    val primary: TextStyle = TextColors.rgb("#8b5cf6")

    /** Secondary Cyan - 보조 브랜드 컬러 */
    val secondary: TextStyle = TextColors.rgb("#06b6d4")

    /** Accent Electric Blue - 강조 컬러 */
    val accent: TextStyle = TextColors.rgb("#3b82f6")

    // ============================================
    // Status Colors
    // ============================================

    /** Success Green - 성공 상태 */
    val success: TextStyle = TextColors.rgb("#10b981")

    /** Warning Amber - 경고 상태 */
    val warning: TextStyle = TextColors.rgb("#f59e0b")

    /** Error Red - 오류 상태 */
    val error: TextStyle = TextColors.rgb("#ef4444")

    /** Error Bright - 강조 오류 (테두리 등) */
    val errorBright: TextStyle = TextColors.rgb("#f87171")

    /** Info Cyan - 정보 상태 */
    val info: TextStyle = TextColors.rgb("#06b6d4")

    // ============================================
    // Text Colors
    // ============================================

    /** Text Primary - 주요 텍스트 (밝은 흰색) */
    val textPrimary: TextStyle = TextColors.rgb("#f9fafb")

    /** Text Secondary - 보조 텍스트 */
    val textSecondary: TextStyle = TextColors.rgb("#9ca3af")

    /** Text Muted - 비활성 텍스트 */
    val textMuted: TextStyle = TextColors.rgb("#6b7280")

    /** Text White - 순수 흰색 */
    val textWhite: TextStyle = TextColors.rgb("#ffffff")

    /** Text Black - 순수 검정 */
    val textBlack: TextStyle = TextColors.rgb("#000000")

    // ============================================
    // Border / Panel Colors
    // ============================================

    /** Border Primary - 기본 테두리 (보라색) */
    val borderPrimary: TextStyle = TextColors.rgb("#8b5cf6")

    /** Border Secondary - 보조 테두리 (시안) */
    val borderSecondary: TextStyle = TextColors.rgb("#06b6d4")

    /** Border Accent - 강조 테두리 */
    val borderAccent: TextStyle = TextColors.rgb("#7c3aed")

    /** Border Muted - 약한 테두리 */
    val borderMuted: TextStyle = TextColors.rgb("#4b5563")

    // ============================================
    // Table Colors
    // ============================================

    /** Table Border - 테이블 테두리 */
    val tableBorder: TextStyle = TextColors.rgb("#7c3aed")

    /** Table Header Background - 테이블 헤더 배경 */
    val tableHeaderBg: TextStyle = TextColors.rgb("#06b6d4")

    /** Table Header Text - 테이블 헤더 텍스트 */
    val tableHeaderText: TextStyle = TextColors.rgb("#000000")

    /** Table Footer - 테이블 푸터 */
    val tableFooter: TextStyle = TextColors.rgb("#f9fafb")

    /** Table Cell Muted - 테이블 셀 (약한) */
    val tableCellMuted: TextStyle = TextColors.rgb("#9ca3af")

    // ============================================
    // Semantic Aliases (용도별 별칭)
    // ============================================

    val brand: TextStyle = primary
    val progress: TextStyle = secondary
    val prompt: TextStyle = warning
    val done: TextStyle = success
    val hint: TextStyle = textMuted

    // ============================================
    // TextColors 버전 (Panel borderStyle 등에서 필요)
    // ============================================

    object Raw {
        val primary: TextStyle = TextColors.rgb("#8b5cf6")
        val secondary: TextStyle = TextColors.rgb("#06b6d4")
        val accent: TextStyle = TextColors.rgb("#3b82f6")
        val error: TextStyle = TextColors.rgb("#ef4444")
        val errorBright: TextStyle = TextColors.rgb("#f87171")
        val warning: TextStyle = TextColors.rgb("#f59e0b")
        val success: TextStyle = TextColors.rgb("#10b981")
        val textWhite: TextStyle = TextColors.rgb("#ffffff")
        val textMuted: TextStyle = TextColors.rgb("#6b7280")
        val borderPrimary: TextStyle = TextColors.rgb("#8b5cf6")
        val borderAccent: TextStyle = TextColors.rgb("#7c3aed")
        val tableBorder: TextStyle = TextColors.rgb("#7c3aed")
        val tableHeaderBg: TextStyle = TextColors.rgb("#06b6d4")
    }
}
