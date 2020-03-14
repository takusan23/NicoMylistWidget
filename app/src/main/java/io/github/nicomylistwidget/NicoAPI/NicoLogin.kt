package io.github.nicomylistwidget.NicoAPI

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.preference.PreferenceManager
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

internal fun nicoLogin(context: Context, loginSuccessful: () -> Unit) {
    val prefSetting = PreferenceManager.getDefaultSharedPreferences(context)
    val mail = prefSetting.getString("mail", "")
    val pass = prefSetting.getString("pass", "")
    // 使用するサーバーのURLに合わせる
    val urlSt = "https://secure.nicovideo.jp/secure/login?site=niconico"

    var httpConn: HttpURLConnection? = null

    val postData = "mail_tel=$mail&password=$pass"

    try {
        // URL設定
        val url = URL(urlSt)

        // HttpURLConnection
        httpConn = url.openConnection() as HttpURLConnection

        // request POST
        httpConn.requestMethod = "POST"

        // no Redirects
        httpConn.instanceFollowRedirects = false

        // データを書き込む
        httpConn.doOutput = true

        // 時間制限
        httpConn.readTimeout = 10000
        httpConn.connectTimeout = 20000

        //ユーザーエージェント
        httpConn.setRequestProperty("User-Agent", "TatimiDroid;@takusan_23")

        // 接続
        httpConn.connect()

        try {
            httpConn.outputStream.use { outStream ->
                outStream.write(postData.toByteArray(StandardCharsets.UTF_8))
                outStream.flush()
            }
        } catch (e: IOException) {
            // POST送信エラー
            e.printStackTrace()
        }
        // POSTデータ送信処理
        val status = httpConn.responseCode
        if (status == HttpURLConnection.HTTP_MOVED_TEMP) {
            // レスポンスを受け取る処理等
            for (cookie in httpConn.headerFields.get("Set-Cookie")!!) {
                //user_sessionだけほしい！！！
                if (cookie.contains("user_session") &&
                    !cookie.contains("deleted") &&
                    !cookie.contains("secure")
                ) {
                    //邪魔なのを取る
                    var user_session = cookie.replace("user_session=", "")
                    //uset_settionは文字数86なので切り取る
                    user_session = user_session.substring(0, 86)
                    //保存する
                    val editor = prefSetting.edit()
                    editor.putString("user_session", user_session)
                    //めあど、ぱすわーども保存する
                    editor.putString("mail", mail)
                    editor.putString("password", pass)
                    editor.apply()
                    // ログイン成功なので関数を呼ぶ
                    loginSuccessful()
                }
            }
        } else {
            //失敗
            val mHandler = Handler(Looper.getMainLooper())
            mHandler.post {
                // 失敗メッセージ
                Toast.makeText(
                    context,
                    "問題が発生しました。\n${status}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        httpConn?.disconnect()
    }
}
