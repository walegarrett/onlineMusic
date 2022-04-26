package com.example.onlineMusic_2.db

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.example.onlineMusic_2.bean.User
import java.util.*

class UserDB(val context: Context): SQLiteOpenHelper(context, "user.db", null, 2) {
    companion object {
        /**
         * 表名
         */
        const val TABLE_NAME = "user"

        /**
         * 建表语句
         */
        const val CREATE_TBL = ("create table " + TABLE_NAME + "("
                + "id integer primary key autoincrement," + "u_userName text," + "u_realName text,"
                + "u_password text," + "u_phone text," + "u_mail text,"
                + "u_address text," + "u_gender text," + "u_age integer,"
                + "u_imagePath text," + "u_isLogined integer, " + "u_info text" +")")

        /**
         * 实例
         */
        private var userDB: UserDB? = null

        /**
         * 供外部获取UserDB
         */
        fun getUserDB(context: Context): UserDB? {
            if (userDB == null) {
                userDB = UserDB(context)
            }
            return userDB
        }
    }

    /**
     * 向数据库添加用户数据
     */
    fun add(user:User):Boolean{
        val values: MutableList<ContentValues?> = ArrayList()
        val value = getContentValues(user)
        values.add(value)

        return insert(values as List<ContentValues>)
    }
    private fun getContentValues(user: User): ContentValues? {
        val values = ContentValues()

        values.put("u_userName", user.userName)
        values.put("u_password", user.password)
        values.put("u_age", user.age)
        values.put("u_gender", user.gender)
        values.put("u_imagePath", user.imagePath)
        values.put("u_isLogined", user.isLogined)
        values.put("u_realName", user.realName)
        values.put("u_phone", user.phone)
        values.put("u_info", user.info)
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
     * 根据账号名查看是否存在该用户
     */
    fun isExistUserByUserName(userName:String):Boolean{
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(), " u_userName=? ", arrayOf(userName), null, null, null)
        if (!cursor.moveToNext()) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    /**
     * 判断密码是否输入正确
     */
    fun isPasswordCorrectByUserName(userName:String, password:String):Boolean{
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(), " u_userName=? and u_password=? ", arrayOf(userName, password), null, null, null)
        if (!cursor.moveToNext()) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    /**
     * 根据用户名查询用户
     */
    fun getUserByUserName(userName:String): User? {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(), " u_userName=? ", arrayOf(userName), null, null, null)
        if (cursor.moveToFirst()) {
            val u_id = cursor.getInt(cursor.getColumnIndex("id"))
            val u_userName = cursor.getString(cursor.getColumnIndex("u_userName"))
            val u_realName = cursor.getString(cursor.getColumnIndex("u_realName"))
            val u_phone = cursor.getString(cursor.getColumnIndex("u_phone"))
            val u_imagePath = cursor.getString(cursor.getColumnIndex("u_imagePath"))
            val u_age = cursor.getInt(cursor.getColumnIndex("u_age"))
            val u_gender = cursor.getString(cursor.getColumnIndex("u_gender"))
            val u_info = cursor.getString(cursor.getColumnIndex("u_info"))
            cursor.close()
            return User(u_id, u_userName, u_realName, "", u_phone, "", "", u_gender, u_age, u_imagePath, 1, u_info)
        }
        cursor.close()
        return null
    }

    /**
     * 设置用户登录状态为已登录
     */
    fun updateUserStatus(userName: String, status:Int){
        val db = writableDatabase
        var values = ContentValues()
        values.put("u_isLogined", 0)
        db.update(TABLE_NAME, values, "u_isLogined = ?", arrayOf("1"))
        //设置指定的用户已登录
        values = ContentValues()
        values.put("u_isLogined", status)
        db.update(TABLE_NAME, values, "u_userName = ?", arrayOf(userName))
    }

    /**
     * 判断是否有用户登录了
     */
    fun isLogined():Boolean{
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(), " u_isLogined=? ", arrayOf("1"), null, null, null)
        if (!cursor.moveToNext()) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    /**
     * 更换用户头像图片地址
     */
    fun updateImagePath(userId:Int, imagePath:String){
        val db = writableDatabase
        //设置指定的用户已登录
        val values = ContentValues()
        values.put("u_imagePath", imagePath)
        db.update(TABLE_NAME, values, "id = ?", arrayOf(userId.toString()))
    }

    /**
     * 修改用户的昵称
     */
    fun updateNickName(userId:Int, nickName:String){
        val db = writableDatabase
        //设置指定的用户已登录
        val values = ContentValues()
        values.put("u_realName", nickName)
        db.update(TABLE_NAME, values, "id = ?", arrayOf(userId.toString()))
    }
    /**
     * 修改用户的昵称
     */
    fun updateUserInfo(userId:Int, info:String){
        val db = writableDatabase
        //设置指定的用户已登录
        val values = ContentValues()
        values.put("u_info", info)
        db.update(TABLE_NAME, values, "id = ?", arrayOf(userId.toString()))
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TBL)
        Log.d("数据库初始化：", TABLE_NAME+"表创建成功")
        Toast.makeText(context, "user表创建成功", Toast.LENGTH_SHORT).show()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //如果存在表则删除
        db?.execSQL("drop table if exists $TABLE_NAME")
        onCreate(db)
    }
}