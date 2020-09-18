package com.aimerneige.lab.ilock.net

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import kotlin.concurrent.thread

fun sendRequest() {
    // TODO 发送开门请求
    


}

private fun sendPost(id: String, data: String, host: String): String {
    var ret: String = "";
    thread {
        try {
            val client = OkHttpClient()
            val requestBody = FormBody.Builder()
                .add("user_id", id)
                .add("send_data", data)
                .build()
            val request = Request.Builder()
                .url(host)
                .post(requestBody)
                .build()
            val response = client.newCall(request).execute()
            val responseData = response.body?.string()
            if (responseData != null) {
                ret = responseData
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return ret
}