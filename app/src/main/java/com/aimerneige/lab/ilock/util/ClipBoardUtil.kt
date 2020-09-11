package com.aimerneige.lab.ilock.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

/**
 * 复制内容到剪贴板
 */
fun paste2ClipBoard(lable: String, data: String, context: Context) {
    val clipData: ClipData = ClipData.newPlainText(lable, data)
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(clipData)
    Toast.makeText(context, "已将公钥复制到剪切板", Toast.LENGTH_SHORT).show()
}
