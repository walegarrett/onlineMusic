package com.example.onlineMusic_2.db

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.example.onlineMusic_2.bean.SongsBean
import java.util.*
import kotlin.collections.ArrayList

class MusicInfoDB(val context: Context): SQLiteOpenHelper(context, "musicInfo.db", null, 2) {
    companion object {
        /**
         * 表名
         */
        const val TABLE_NAME = "musicInfo"

        /**
         * 建表语句
         */
        const val CREATE_TBL = ("create table " + TABLE_NAME + "("
                + "id integer primary key autoincrement," + "m_musicId text,"
                + "m_musicName text," + "m_userId integer," + "m_playerName text,"
                + "m_musicPath text," + "m_musicUrl text," + "m_duration integer,"
                + "m_type text" + ")")

        /**
         * 实例
         */
        private var musicInfoDB: MusicInfoDB? = null

        /**
         * 供外部获取UserDB
         */
        fun getMusicInfoDB(context: Context): MusicInfoDB? {
            if (musicInfoDB == null) {
                musicInfoDB = MusicInfoDB(context)
            }
            return musicInfoDB
        }
    }

    /**
     * 向数据库添加用户数据
     */
    fun add(song:SongsBean, userId:Int):Boolean{
        val values: MutableList<ContentValues?> = ArrayList()
        val value = getContentValues(song, userId)
        values.add(value)

        return insert(values as List<ContentValues>)
    }
    private fun getContentValues(song: SongsBean, userId: Int): ContentValues? {
        val values = ContentValues()

        values.put("m_musicId", song.id)
        values.put("m_musicName", song.name)
        values.put("m_userId", userId)
        values.put("m_playerName", song.playerName)
        values.put("m_musicPath", song.musicPath)
        values.put("m_musicUrl", song.musicUrl)
        values.put("m_duration", song.duration)
        values.put("m_type", song.musicType) //0--表示android自带歌曲，1--表示网络歌曲，2--表示在线歌曲，3--表示我喜欢的歌曲
        return values
    }
    //插入数据辅助函数
    private fun insert(values: List<ContentValues>): Boolean {
        val db = writableDatabase
        try {
            db.beginTransaction() // 手动设置开始事务
            for (value in values) {
                db.insert(TABLE_NAME, null, value)
            }
            db.setTransactionSuccessful() // 设置事务处理成功，不设置会自动回滚不提交
            return true
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction() // 处理完成
        }
        return false
    }
    //查找某个用户的某首歌是否存在
    fun isExist(userId:Int, musicId:String): Boolean{
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(), " m_musicId=? and m_userId=? ", arrayOf(musicId, userId.toString()), null, null, null)
        if (!cursor.moveToNext()) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    /**
     * 根据用户id和歌曲id获取歌曲在数据库中的类型
     */
    fun getMusicType(userId:Int, musicId:String): Int {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(), " m_userId=? and m_musicId=? ", arrayOf(userId.toString(), musicId), null, null, null)
        if (cursor.moveToFirst()) {
            val m_type = cursor.getInt(cursor.getColumnIndex("m_type"))
            cursor.close()
            return m_type
        }
        cursor.close()
        return -1
    }

    //更新type类型为网络歌曲---1
    fun updateMusicType(userId:Int, musicId:String, originType:Int, newType:Int){
        val db = writableDatabase
        val values = ContentValues()
        values.put("m_type", newType)
        db.update(TABLE_NAME, values, "m_userId = ? and m_musicId = ? and m_type = ?", arrayOf(userId.toString(), musicId, originType.toString()))
    }
    //获取某个用户收藏的所有歌曲
    fun getLikeMusicByUserId(userId: Int):List<SongsBean>{
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(), " m_userId=? and m_type=? ", arrayOf(userId.toString(), "3"), null, null, null)
        val list: MutableList<SongsBean> = ArrayList<SongsBean>().toMutableList()
        if (cursor.moveToFirst()) {
            do {
                val musicId = cursor.getString(cursor.getColumnIndex("m_musicId"))
                val musicName = cursor.getString(cursor.getColumnIndex("m_musicName"))
                val userId = cursor.getInt(cursor.getColumnIndex("m_userId"))
                val playerName = cursor.getString(cursor.getColumnIndex("m_playerName"))
                val musicPath = cursor.getString(cursor.getColumnIndex("m_musicPath"))
                val musicUrl = cursor.getString(cursor.getColumnIndex("m_musicUrl"))
                val duration = cursor.getInt(cursor.getColumnIndex("m_duration"))
                val type = cursor.getInt(cursor.getColumnIndex("m_type")) //0--表示android自带歌曲，1--表示网络歌曲，2--表示在线歌曲，3--表示我喜欢的歌曲
                list += SongsBean(musicId.toInt(), musicName, null, null, duration, null, musicUrl, musicPath, type, playerName)
            }while (cursor.moveToNext())
        }
        cursor.close()

        return list
    }

    /**
     * 获取用户喜欢的歌曲的数量
     */
    fun getLikeMusicCountByUserId(userId: Int):Int{
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(), " m_userId=? and m_type=? ", arrayOf(userId.toString(), "3"), null, null, null)
        val count = cursor.count
        cursor.close()
        return count
    }



    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TBL)
        Log.d("数据库初始化：", TABLE_NAME+"表创建成功")
        Toast.makeText(context, "music表创建成功", Toast.LENGTH_SHORT).show()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //如果存在表则删除
        db?.execSQL("drop table if exists $TABLE_NAME")
        onCreate(db)
    }
}