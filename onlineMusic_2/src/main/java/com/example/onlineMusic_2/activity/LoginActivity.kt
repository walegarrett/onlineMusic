package com.example.onlineMusic_2.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.db.UserDB
import com.example.onlineMusic_2.utils.ValidUtil
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.et_account
import kotlinx.android.synthetic.main.activity_login.et_password


class LoginActivity : BaseActivity(){
    companion object{
        //供别的activity传递参数到本activity提供参考
        fun actionStart(context: Context){
            val intent= Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }

    }
    @SuppressLint("CommitPrefEdits", "ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //点击注册
        tv_to_register.setOnClickListener {
            RegisterActivity.actionStart(this)
        }

        //点击登录
        bt_login.setOnClickListener {
            //首先判断是否有用户登录了
            if(UserDB.getUserDB(this)?.isLogined()!!){
                Log.d("LoginActivity", "已经有用户登录了，请退出登录后重新登录！")
                et_account.error = "已经有账号登录了，请退出当前账号后重新登录！"
                Toast.makeText(applicationContext, "已经有用户登录了，请退出登录后重新登录！",Toast.LENGTH_SHORT).show()
            }else{
                val storeUser = getSharedPreferences("userData", Context.MODE_PRIVATE).edit()
                val account = et_account.text.toString()
                val password = et_password.text.toString()

                if(testLoginValidate(account, password)){
                    val user = UserDB.getUserDB(this)?.getUserByUserName(account)
                    if(user != null){
                        //将该用户以登录的状态设置为1
                        UserDB.getUserDB(this)?.updateUserStatus(account, 1)
                        storeUser.putInt("u_id", user.id)
                        storeUser.putString("u_userName", user.userName)
                        storeUser.putString("u_realName", user.realName)
                        storeUser.putString("u_phone", user.phone)
                        storeUser.putString("u_gender", user.gender)
                        storeUser.putString("u_imagePath", user.imagePath)
                        storeUser.putInt("u_age", user.age)
                        storeUser.putString("u_info", user.info)
                        storeUser.apply()
                        //跳转到MainActivity
                        MainActivity.actionStart(this)
                    }
                }
            }
        }
    }
    //校验用户输入的账号和密码信息是否和数据库中的一一对应
    fun testLoginValidate(account:String, password:String):Boolean{
        var isAllValidated:Boolean = true
        //验证账号
        if(!ValidUtil.isAccountValid(account = account).equals("true")){
            isAllValidated = false
            et_account.error = ValidUtil.isAccountValid(account = account)
        }else if(!UserDB.getUserDB(this)?.isExistUserByUserName(account)!!){
            et_account.error = "该账号未注册！！！"
            return false
        }
        //验证密码
        if(!ValidUtil.isPasswordValid(password).equals("true")){
            isAllValidated = false
            et_password.error = ValidUtil.isPasswordValid(password)
        }else if(!UserDB.getUserDB(this)?.isPasswordCorrectByUserName(account, password)!!){
            isAllValidated = false
            et_password.error = "密码输入错误！！！"
        }
        return isAllValidated
    }
}