package com.junkfood.seal.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getInt
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.PreferenceUtil.updateBoolean

inline val String.booleanState
    @Composable get() =
        remember { mutableStateOf(this.getBoolean()) }

inline val String.stringState
    @Composable get() =
        remember { mutableStateOf(this.getString()) }

inline val String.intState
    @Composable get() = remember {
        mutableStateOf(this.getInt())
    }
