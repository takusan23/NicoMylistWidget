package io.github.nicomylistwidget.NicoAPI

import android.content.Context
import android.graphics.Insets.add
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.regex.Pattern

// コルーチン
class NicoVideoMylistAPI(context: Context?) {
    val prefSetting = PreferenceManager.getDefaultSharedPreferences(context)
    val usersession = prefSetting.getString("user_session", "")
    val mylistId = prefSetting.getString("mylist", "")!!

    /** マイリスト取得に必要なトークンを取得する */
    fun getNicoVideoMylistToken(): Deferred<String?> = GlobalScope.async {
        //200件最大まで取得する
        val url = "https://www.nicovideo.jp/my/mylist"
        val request = Request.Builder()
            .url(url)
            .header("Cookie", "user_session=${usersession}")
            .header("x-frontend-id", "6") //3でスマホ、6でPC　なんとなくPCを指定しておく。 指定しないと成功しない
            .header("User-Agent", "NicoMylistWidget;@takusan_23")
            .get()
            .build()
        val okHttpClient = OkHttpClient()
        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            //正規表現で取り出す。
            val html = Jsoup.parse(response.body?.string())
            val regex = "NicoAPI.token = \"(.+?)\";"
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(html.html())
            if (matcher.find()) {
                val token = matcher.group(1)
                return@async token
            }
            return@async ""
        } else {
            return@async ""
        }
    }

    /** マイリストとる */
    fun getNicoMylistList(token: String): Deferred<String?> = GlobalScope.async {
        val url = "https://www.nicovideo.jp/api/mylist/list"
        val post = FormBody.Builder().apply {
            add("token", token)
            add("group_id", mylistId)
        }.build()
        val request = Request.Builder()
            .url(url)
            .header("Cookie", "user_session=${usersession}")
            .header("x-frontend-id", "6") //3でスマホ、6でPC　なんとなくPCを指定しておく。 指定しないと成功しない
            .header("User-Agent", "NicoMylistWidget;@takusan_23")
            .post(post)
            .build()
        val okHttpClient = OkHttpClient()
        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            return@async response.body?.string()
        } else {
            return@async ""
        }
    }

}