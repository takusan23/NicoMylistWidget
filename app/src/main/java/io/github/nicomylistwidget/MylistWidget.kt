package io.github.nicomylistwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.net.toUri
import java.util.*


/**
 * Implementation of App Widget functionality.
 */
class MylistWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val videoId = intent?.getStringExtra("video_id")
        println("video_id : $videoId")
        if (videoId != null) {
            val intent =
                Intent(Intent.ACTION_VIEW, "https://www.nicovideo.jp/watch/$videoId".toUri())
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context?.startActivity(intent)
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.mylist_widget)

    // ListView
    val remoteViewsFactoryIntent = Intent(context, ListViewWidgetService::class.java)
    views.setRemoteAdapter(R.id.listView, remoteViewsFactoryIntent)
    // ListViewの中身クリックさせるなら必要
    val itemClick = Intent(context, MylistWidget::class.java)
    val itemClickPendingIntent =
        PendingIntent.getBroadcast(context, 23, itemClick, PendingIntent.FLAG_UPDATE_CURRENT)
    views.setPendingIntentTemplate(R.id.listView, itemClickPendingIntent)

    // ウィジェットのタイトル
    val calendar = Calendar.getInstance()
    val month = calendar[Calendar.MONTH] + 1
    views.setTextViewText(R.id.widget_title_textview, "${month}月に投稿されたボカロ")

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}