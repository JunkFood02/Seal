package com.junkfood.seal.util

import android.widget.Toast
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import java.util.regex.Pattern

object TextUtil {

    fun matchUrlFromClipboard(s: String): String? {
        val pattern =
            Pattern.compile("(http|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?")
        with(pattern.matcher(s)) {
            if (find()) {
                makeToast(R.string.paste_msg)
                return group()
            }
        }
        makeToast(R.string.paste_fail_msg)
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

    fun makeToast(stringId: Int) {
        Toast.makeText(context, context.getString(stringId), Toast.LENGTH_SHORT).show()
    }
}