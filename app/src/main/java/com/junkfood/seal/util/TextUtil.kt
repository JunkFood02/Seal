package com.junkfood.seal.util

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.text.isDigitsOnly
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.toEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import kotlin.math.roundToInt

object ToastUtil {
    fun makeToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun makeToastSuspend(text: String) {
        applicationScope.launch(Dispatchers.Main) {
            makeToast(text)
        }
    }

    fun makeToast(stringId: Int) {
        Toast.makeText(context, context.getString(stringId), Toast.LENGTH_SHORT).show()
    }
}

private const val GIGA_BYTES = 1024f * 1024f * 1024f
private const val MEGA_BYTES = 1024f * 1024f

@Composable
fun Long.toFileSizeText() = this.toFloat().run {
    if (this > GIGA_BYTES)
        stringResource(R.string.filesize_gb).format(this / GIGA_BYTES)
    else stringResource(R.string.filesize_mb).format(this / MEGA_BYTES)
}

/**
 * Convert time in **seconds** to `hh:mm:ss` or `mm:ss`
 */
fun Int.toDurationText(): String = this.run {
    if (this > 3600) "%d:%02d:%02d".format(this / 3600, (this % 3600) / 60, this % 60)
    else "%02d:%02d".format(this / 60, this % 60)
}

fun String.isNumberInRange(start: Int, end: Int): Boolean {
    return this.isNotEmpty() && this.isDigitsOnly() && this.length < 10 && this.toInt() >= start && this.toInt() <= end
}

fun String.isNumberInRange(range: IntRange): Boolean = this.isNumberInRange(range.first, range.last)

fun ClosedFloatingPointRange<Float>.toIntRange() =
    IntRange(start.roundToInt(), endInclusive.roundToInt())

fun String?.toHttpsUrl(): String =
    this?.run {
        if (matches(Regex("^(http:).*"))) replaceFirst("http", "https") else this
    } ?: ""


fun matchUrlFromClipboard(string: String, isMatchingMultiLink: Boolean = false): String {
    matchUrlFromString(string, isMatchingMultiLink).run {
        if (isEmpty())
            ToastUtil.makeToast(R.string.paste_fail_msg)
        else
            ToastUtil.makeToast(R.string.paste_msg)
        return this
    }
}

fun matchUrlFromSharedText(s: String): String {
    matchUrlFromString(s).run {
        if (isEmpty())
            ToastUtil.makeToast(R.string.share_fail_msg)
//            else makeToast(R.string.share_success_msg)
        return this
    }
}

private fun matchUrlFromString(s: String, isMatchingMultiLink: Boolean = false): String {
    val builder = StringBuilder()
    val pattern =
        Pattern.compile("(http|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?")
    with(pattern.matcher(s)) {
        if (isMatchingMultiLink)
            while (find()) {
                if (builder.isNotEmpty())
                    builder.append("\n")
                builder.append(group())
            }
        else if (find())
            builder.append(group())
    }
    return builder.toString()
}


fun connectWithDelimiter(vararg strings: String, delimiter: String): String {
    val builder = StringBuilder(strings.first())
    for (s in strings.asList().subList(1, strings.size)) {
        if (s.isNotEmpty()) {
            if (builder.isNotEmpty())
                builder.append(delimiter)
            builder.append(s)
        }
    }
    return builder.toString()
}

fun connectWithBlank(s1: String, s2: String): String {
    val f1 = s1.toEmpty()
    val f2 = s2.toEmpty()
    val blank = if (f1.isEmpty() || f2.isEmpty()) "" else " "
    return f1 + blank + f2
}