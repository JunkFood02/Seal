package com.junkfood.seal.util

import android.content.ClipDescription
import android.widget.Toast
import com.junkfood.seal.BaseApplication
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
                        Toast.makeText(
                            BaseApplication.context,
                            BaseApplication.context.getString(R.string.paste_msg),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return group()
                    }
                }
            }
        }
        Toast.makeText(
            BaseApplication.context,
            BaseApplication.context.getString(R.string.paste_fail_msg),
            Toast.LENGTH_SHORT
        ).show()
        return null
    }


}