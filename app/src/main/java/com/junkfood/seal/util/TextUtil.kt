package com.junkfood.seal.util

import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.junkfood.seal.BaseApplication.Companion.applicationScope
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

object TextUtil {

    fun String.isNumberInRange(start: Int, end: Int): Boolean {
        return this.isNotEmpty() && this.isDigitsOnly() && this.length < 5 && this.toInt() >= start && this.toInt() <= end
    }

    fun matchUrlFromClipboard(s: String): String? {
        matchUrlFromString(s).run {
            if (isNullOrEmpty())
                makeToast(R.string.paste_fail_msg)
            else
                makeToast(R.string.paste_msg)
            return this
        }
    }

    fun matchUrlFromSharedText(s: String): String? {
        matchUrlFromString(s).run {
            if (isNullOrEmpty())
                makeToast(R.string.share_fail_msg)
            else
                makeToast(R.string.share_success_msg)
            return this
        }
    }

    private fun matchUrlFromString(s: String): String? {
        val pattern =
            Pattern.compile("(http|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?")
        with(pattern.matcher(s)) {
            if (find()) {
                return group()
            }
        }
        return null
    }

    fun urlHttpToHttps(url: String?): String {
        with(url.toString()) {
            if (matches(Regex("^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?\$"))) {
                return replace("http", "https")
            } else return this
        }
    }

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