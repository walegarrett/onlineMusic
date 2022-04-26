package com.example.onlineMusic_2.db

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.example.onlineMusic_2.bean.Help
import com.example.onlineMusic_2.bean.SongsBean
import java.util.*

class HelpDB(val context: Context): SQLiteOpenHelper(context, "help.db", null, 2) {
    companion object {
        /**
         * 表名
         */
        const val TABLE_NAME = "help"

        /**
         * 建表语句
         */
        const val CREATE_TBL = ("create table " + TABLE_NAME + "("
                + "id integer primary key autoincrement," + "h_question text," + "h_answer text" +")")

        /**
         * 实例
         */
        private var helpDB: HelpDB? = null

        /**
         * 供外部获取UserDB
         */
        fun getHelpDB(context: Context): HelpDB? {
            if (helpDB == null) {
                helpDB = HelpDB(context)
            }
            return helpDB
        }
    }

    /**
     * 向数据库添加帮助信息数据
     */
    fun add(help:Help):Boolean{
        val values: MutableList<ContentValues?> = ArrayList()
        val value = getContentValues(help)
        values.add(value)

        return insert(values as List<ContentValues>)
    }
    private fun getContentValues(help: Help): ContentValues? {
        val values = ContentValues()

        values.put("h_question", help.question)
        values.put("h_answer", help.answer)
        return values
    }
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

    /**
     * 获取数据库中所有帮助信息
     */
    fun getAllHelpInfo():List<Help> {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(), null, null, null, null, null)
        val list: MutableList<Help> = ArrayList<Help>().toMutableList()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("id"))
                val question = cursor.getString(cursor.getColumnIndex("h_question"))
                val answer = cursor.getString(cursor.getColumnIndex("h_answer"))
                list += Help(id, question, answer)
            }while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TBL)
        Log.d("数据库初始化：", TABLE_NAME+"表创建成功")
        Toast.makeText(context, "help表创建成功", Toast.LENGTH_SHORT).show()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //如果存在表则删除
        db?.execSQL("drop table if exists $TABLE_NAME")
        onCreate(db)
    }
}