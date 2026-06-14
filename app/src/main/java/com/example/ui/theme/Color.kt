package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// --- Warm Organic-Minimal Core Palette ---
val WarmForestGreen = Color(0xFF1A6B47)      // Forest green (#1A6B47)
val TurmericOrange = Color(0xFFE8924A)       // Turmeric orange (#E8924A)
val CreamBackground = Color(0xFFFAF6EE)      // Warm cream background
val SoftSageTint = Color(0xFFE6EFEA)         // Soft green tint for secondary highlights
val SoftCreamContainer = Color(0xFFF3EDE2)   // Slightly darker warm cream for containers/cards
val AccentDeepGreen = Color(0xFF0F3A26)      // Deep forest green for dark text headers

val EarthyCharcoal = Color(0xFF1C221E)       // Ultra-dark charcoal green-black for optimal contrast text
val MutedEarthyGroup = Color(0xFF5B6660)     // Description / helper text color
val SandBorder = Color(0xFFD6CFC4)           // Neutral warm thin borders

// --- Bento Theme Name Mapping (for compatibility with existing UI files) ---
val BentoPurpleDark = AccentDeepGreen
val BentoPurpleMedium = WarmForestGreen
val BentoPurpleLight = TurmericOrange
val BentoPurpleContainer = SoftSageTint
val BentoSecondaryContainer = SoftCreamContainer
val BentoActivePill = SoftSageTint

val BentoBackground = CreamBackground
val BentoTextDark = EarthyCharcoal
val BentoTextGrey = MutedEarthyGroup
val BentoBorderGrey = SandBorder

// --- Legacy Palette Aliases mapped gracefully to Bento theme ---
val ForestGreen = WarmForestGreen
val BrightGreen = SoftSageTint
val SoftGreenTint = SoftSageTint

val SpiceBerry = AccentDeepGreen
val SoftOrangeTint = Color(0xFFFCF4EB)        // Soft turmeric-tint container
val CautionOrange = TurmericOrange

val OrganicBackground = CreamBackground
val SoftCream = SoftCreamContainer
val ContainerWhite = Color(0xFFFFFFFF)

val OnSurfaceDark = EarthyCharcoal
val OnSurfaceVariantDark = MutedEarthyGroup

val OutlineGrey = SandBorder
val OutlineVariantGrey = Color(0xFFEBE6DD)

