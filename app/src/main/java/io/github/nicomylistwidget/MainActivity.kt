package io.github.nicomylistwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import io.github.nicomylistwidget.NicoAPI.NicoVideoMylistAPI
import io.github.nicomylistwidget.NicoAPI.nicoLogin
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    lateinit var prefSetting: SharedPreferences

    lateinit var mylistDB: MylistDB
    lateinit var sqLiteDatabase: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefSetting = PreferenceManager.getDefaultSharedPreferences(this)

        mylistDB = MylistDB(this)
        sqLiteDatabase = mylistDB.writableDatabase
        mylistDB.setWriteAheadLoggingEnabled(false)

        // ログイン
        login_button.setOnClickListener {
            prefSetting.edit {
                putString("mail", mail_editText.text.toString())
                putString("pass", pass_editText2.text.toString())
            }
            // 非同期処理
            thread {
                nicoLogin(this) {
                    // 成功
                    runOnUiThread {
                        Toast.makeText(this, "ログイン成功", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // マイリスト登録
        mylist_set_button.setOnClickListener {
            prefSetting.edit {
                putString("mylist", mylist_id_editText.text.toString())
            }
        }

        // ウィジェット更新
        update_widget.setOnClickListener {
            val componentName = ComponentName(this, MylistWidget::class.java)
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val idList = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in idList) {
                updateAppWidget(this, appWidgetManager, appWidgetId)
            }
        }

        // マイリスト取得
        mylist_get_button.setOnClickListener {
            val nicoVideoMylistAPI = NicoVideoMylistAPI(this)
            GlobalScope.launch {
                val token = nicoVideoMylistAPI.getNicoVideoMylistToken().await()
                val response = nicoVideoMylistAPI.getNicoMylistList(token!!).await()
                // DBクリア
                sqLiteDatabase.delete("mylist", null, null)
                // JSONパース
                val jsonObject = JSONObject(response)
                val jsonArray = jsonObject.getJSONArray("mylistitem")
                repeat(jsonArray.length()) {
                    val videoObject = jsonArray.getJSONObject(it)
                    val itemData = videoObject.getJSONObject("item_data")
                    val title = itemData.getString("title")
                    val videoId = itemData.getString("video_id")
                    val updateTime = itemData.getLong("first_retrieve")
                    // DB追加
                    val contentValues = ContentValues().apply {
                        put("json", videoObject.toString())
                        put("title", title)
                        put("video_id", videoId)
                        put("update_time", updateTime)
                        put("description", "")
                    }
                    sqLiteDatabase.insert("mylist", null, contentValues)
                }
            }
        }

    }
}
