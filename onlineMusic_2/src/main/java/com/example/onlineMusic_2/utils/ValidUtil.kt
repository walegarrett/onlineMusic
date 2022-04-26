package com.example.onlineMusic_2.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

object ValidUtil {
    // 校验电话号码不能为空且必须是中国大陆手机号（宽松模式匹配）
    fun isPhoneValid(phone: String?): String {
        if (phone == null || phone.equals("")) {
            return "电话号码不能为空！"
        }
        // 首位为1, 第二位为3-9, 剩下九位为 0-9, 共11位数字
        val pattern = "^[1]([3-9])[0-9]{9}$"
        val r: Pattern = Pattern.compile(pattern)
        val m: Matcher = r.matcher(phone)
        if(!m.matches())
            return "电话号码必须为11位纯数字且首位为1, 第二位为3-9！"
        return "true"
    }
    // 性别只能是男或者女
    fun isGenderValid(gender: String): String {
        if(gender == "男" || gender == "女")
            return "true"
        return "性别只能是男或者女！"
    }
    /**
     * 账号：
     * 登录账号不能为空
     * 账号只能包含大写、小写、数字和下划线
     * 账号长度必须在4到20位之间
     */
    fun isAccountValid(account:String?):String{
        if(account == null || account.equals("")){
            return "账号不能为空！"
        }
        val pattern = "^[a-zA-Z0-9_]{4,20}$"
        val r: Pattern = Pattern.compile(pattern)
        val m: Matcher = r.matcher(account)
        if(!m.matches()){
            return "账号长度必须在4-20位之间且只能包含大写、小写、数字和下划线！"
        }
        return "true"
    }
    //真实姓名只能是2-10位的中英文的组合
    fun isRealNameValid(realName:String?):String{
        if(realName==null || realName.equals(""))
            return "真实名字不能为空！"
        val pattern = "^[\\u4e00-\\u9fa5_a-zA-Z]{2,10}\$"
        val r: Pattern = Pattern.compile(pattern)
        val m: Matcher = r.matcher(realName)
        if(!m.matches()){
            return "真实姓名只能是2-10位的中英文的组合！"
        }
        return "true"
    }
    // 校验密码不少于6位
    fun isPasswordValid(password: String?): String {
        if(password == null || password.equals(""))
            return "密码不能为空！"
        if(password.trim { it <= ' ' }.length <= 5){
            return "密码必须大于等于6位！"
        }
        return "true"
    }
    //确认密码必须和密码一致
    fun isConfirmPasswordValid(password:String, confirm:String):String{
        if(password.equals(confirm))
            return "true"
        return "确认密码必须和密码一致！"
    }
    //年龄必须符合真实情况
    fun isAgeValid(age:String):String{
        if(age.equals("") || !Pattern.compile("^[0-9]+.?[0-9]*\$").matcher(age).matches())
            return "年龄不能为空且必须是数字！"
        val agei = age.toInt()
        if(agei > 120 || agei <= 0)
            return "请输入准确的年龄"
        else return "true"
    }
}