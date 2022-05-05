package com.junkfood.seal.util

import android.content.ClipDescription
import android.widget.Toast
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import java.util.regex.Pattern

object TextUtil {

    fun readUrlFromClipboard(): String? {
        if (BaseApplication.clipboard.hasPrimaryClip()) {
            if (BaseApplication.clipboard.primaryClipDescription?.hasMimeType(
                    ClipDescription.MIMETYPE_TEXT_PLAIN
                ) == true
            ) {
                val item =
                    BaseApplication.clipboard.primaryClip?.getItemAt(0)?.text
                        ?: return null
                val pattern =
                    Pattern.compile("(http|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?")
                with(pattern.matcher(item)) {
                    if (find()) {
                        makeToast(R.string.paste_msg)
                        return group()
                    }
                }
            }
        }
        makeToast(R.string.paste_fail_msg)
        return null
    }

    fun makeToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun makeToast(stringId: Int) {
        Toast.makeText(context, context.getString(stringId), Toast.LENGTH_SHORT).show()
    }
}