package com.example.onlineMusic_2.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.bean.User
import com.example.onlineMusic_2.db.UserDB
import com.example.onlineMusic_2.utils.ValidUtil
import kotlinx.android.synthetic.main.activity_register.*



class RegisterActivity : BaseActivity() {
    companion object{
        //供别的activity传递参数到本activity提供参考
        fun actionStart(context: Context){
            val intent= Intent(context, RegisterActivity::class.java)
            context.startActivity(intent)
        }

    }
    @SuppressLint("CommitPrefEdits", "ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //点击注册按钮
        bt_submit_register.setOnClickListener {
            //性别
            var checkedRadioButtonId = et_gender.checkedRadioButtonId
            val gender = findViewById<RadioButton>(checkedRadioButtonId).text.toString()
            val account = et_account.text.toString()
            val phone = et_telephone.text.toString()
            val password = et_password.text.toString()
            val confirPassword = et_password2.text.toString()
            val age = et_age.text.toString()
            val realName = et_realname.text.toString()
            //所有字段校验成功后跳转到登录页面
            if(testRegisterValidate(account, phone, password, confirPassword, gender, age, realName)){
                UserDB.getUserDB(context = this)?.add(User(-1, account, realName, password, phone, "", "", gender, age.toInt(), "", 0, "个人简介为空，赶紧写点什么介绍下自己吧！！"))
                LoginActivity.actionStart(this)
            }else{
                Toast.makeText(applicationContext, "注册失败！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 测试表单的字段准确性
     */
    fun testRegisterValidate(account:String, phone:String, password:String, confirPassword:String, gender:String, age:String, realName:String): Boolean{
        var isAllValidated:Boolean = true
        //验证账号
        if(!ValidUtil.isAccountValid(account = account).equals("true")){
            isAllValidated = false
            et_account.error = ValidUtil.isAccountValid(account = account)
        }
        //验证手机号
        if(!ValidUtil.isPhoneValid(phone).equals("true")){
            isAllValidated = false
            et_telephone.error = ValidUtil.isPhoneValid(phone)
        }
        //验证密码
        if(!ValidUtil.isPasswordValid(password).equals("true")){
            isAllValidated = false
            et_password.error = ValidUtil.isPasswordValid(password)
        }
        //验证确认密码
        if(!ValidUtil.isConfirmPasswordValid(password, confirPassword).equals("true")){
            isAllValidated = false
            et_password2.error = ValidUtil.isConfirmPasswordValid(password, confirPassword)
        }
        //验证年龄
        if(!ValidUtil.isAgeValid(age).equals("true")){
            isAllValidated = false
            et_age.error = ValidUtil.isAgeValid(age)
        }
        //真实名字
        if(!ValidUtil.isRealNameValid(realName).equals("true")){
            isAllValidated = false
            et_realname.error = ValidUtil.isRealNameValid(realName)
        }
        return isAllValidated
    }
}