package com.junkfood.seal.ui.theme

import androidx.compose.ui.graphics.Color
import com.junkfood.seal.ui.color.scheme.Scheme.dark
import com.junkfood.seal.ui.color.scheme.Scheme.light

const val seedColorArgb = 0xFF415f76.toInt()
const val errorColorArgb = 0xFFba1b1b.toInt()
val lightScheme = light(seedColorArgb)!!
val darkScheme = dark(seedColorArgb)!!
val seed = Color(seedColorArgb)
val error = Color(errorColorArgb)