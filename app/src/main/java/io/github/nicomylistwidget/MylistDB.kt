package io.github.nicomylistwidget

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MylistDB(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    companion object {
        // データーベースのバージョン
        private val DATABASE_VERSION = 1

        // データーベース名
        private val DATABASE_NAME = "Mylist.db"
        private val TABLE_NAME = "mylist"
        private val JSON = "json" // JSON
        private val VIDEO_ID = "video_id" // 動画ID
        private val UPDATE_TIME = "update_time" // 投稿時間。UXNITIME
        private val TITLE = "title" // 動画タイトル
        private val DESCRIPTION = "description" // なんかつかうかも
        private val _ID = "_id"


        // , を付け忘れるとエラー
        private val SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                JSON + " TEXT ," +
                VIDEO_ID + " TEXT ," +
                UPDATE_TIME + " TEXT ," +
                TITLE + " TEXT ," +
                DESCRIPTION + " TEXT" +
                ")"

        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        //テーブル作成
        p0?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        onUpgrade(p0, p1, p2)
    }

}