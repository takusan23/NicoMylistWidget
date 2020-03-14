package io.github.nicomylistwidget

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ListViewWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return ListViewWidgetFactory()
    }

    private inner class ListViewWidgetFactory() : RemoteViewsFactory {

        var mylist = arrayListOf<String>()

        override fun onCreate() {

        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun onDataSetChanged() {
            // データ取得
            val mylistDB = MylistDB(applicationContext)
            val sqLiteDatabase = mylistDB.writableDatabase
            val cursor =
                sqLiteDatabase.query("mylist", arrayOf("json"), null, null, null, null, null)
            cursor.moveToFirst()
            val tmpList = arrayListOf<String>()
            repeat(cursor.count) {
                tmpList.add(cursor.getString(0))
                cursor.moveToNext()
            }
            cursor.close()

            // その月に投稿された動画だけ取り出す
            mylist = tmpList.filter { s: String ->
                // JSONパース
                val jsonObject = JSONObject(s)
                val itemData = jsonObject.getJSONObject("item_data")
                val updateTime = itemData.getLong("first_retrieve")
                // UnixTime -> Calendar
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = updateTime * 1000
                val nowCalendar = Calendar.getInstance()
                calendar.get(Calendar.MONTH) == nowCalendar.get(Calendar.MONTH) // 月が同じだったら配列に残す
            } as ArrayList<String>

            mylist.sortBy { s: String ->
                // JSONパース
                val jsonObject = JSONObject(s)
                val itemData = jsonObject.getJSONObject("item_data")
                val updateTime = itemData.getLong("first_retrieve")
                // UnixTime -> Calendar
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = updateTime * 1000
                val nowCalendar = Calendar.getInstance()
                var calc = calendar[Calendar.DAY_OF_MONTH] - nowCalendar[Calendar.DAY_OF_MONTH]
                // すでに過ぎていれば最後尾へ
                if (calc < 0) {
                    calc = 32
                }
                calc
            }

        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getViewAt(position: Int): RemoteViews {
            val json = mylist[position]
            val jsonObject = JSONObject(json)
            val itemData = jsonObject.getJSONObject("item_data")
            val title = itemData.getString("title")
            val videoId = itemData.getString("video_id")
            val updateTime = itemData.getLong("first_retrieve")

            // 日付フォーマット
            val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日 HH時mm分", Locale.JAPAN)
            val formattedTime = simpleDateFormat.format(updateTime * 1000)

            val remoteViews =
                RemoteViews(packageName, R.layout.listview_item_layout)
            remoteViews.setTextViewText(R.id.widget_listview_title, title)
            remoteViews.setTextViewText(R.id.widget_listview_update, formattedTime)

            // 押したら動画へ
            val intent = Intent(applicationContext, MylistWidget::class.java)
            intent.putExtra("video_id", videoId)
            remoteViews.setOnClickFillInIntent(R.id.widget_listview_title, intent)

            return remoteViews
        }

        override fun getCount(): Int {
            return mylist.size
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun onDestroy() {

        }


    }

}
