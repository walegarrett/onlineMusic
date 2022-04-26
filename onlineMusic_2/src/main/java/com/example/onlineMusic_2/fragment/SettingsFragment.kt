package com.example.onlineMusic_2.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.db.UserDB
import kotlin.properties.Delegates

class SettingsFragment : PreferenceFragmentCompat() {

    lateinit var loginedUser:SharedPreferences
    lateinit var loginedUserName:String
    lateinit var loginedNickName:String
    var loginedUserId by Delegates.notNull<Int>()
    lateinit var loginedInfo:String
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        //初始化控件和相关的视图
        initViews()
    }
    fun initViews(){
        //点击选择主题
        val theme = findPreference<Preference>("theme")
        theme?.setOnPreferenceClickListener {
            replaceFragment(ThemeFragment())
            false
        }
        //点击切换字体大小
        val textsizeList = findPreference<ListPreference>("textSize")
        val settings = context?.getSharedPreferences("settingData", Context.MODE_PRIVATE)
        val defaultValue = settings?.getString("textSize", "中号字体")
        textsizeList?.setDefaultValue(defaultValue)
        textsizeList?.setOnPreferenceChangeListener { preference, newValue ->
            Log.d("SettingsFragment", newValue.toString())
            val settingData = context?.getSharedPreferences("settingData", Context.MODE_PRIVATE)?.edit()
            settingData?.putString("textSize", newValue.toString())
            settingData?.apply()
            preference.setDefaultValue(newValue)
            //重启
            activity?.recreate()
            true
        }
        //点击修改昵称
        val setNickName = findPreference<EditTextPreference>("setNickName")
        //获取已经登录的用户
        loginedUser = activity?.getSharedPreferences("userData", Context.MODE_PRIVATE)!!
        loginedNickName = loginedUser?.getString("u_realName", "未登录").toString()
        loginedUserId = loginedUser?.getInt("u_id", -1)
        setNickName?.setDefaultValue(loginedNickName)
        setNickName?.summary = loginedNickName
        setNickName?.setOnPreferenceChangeListener { preference, newValue ->
            val nickName = newValue.toString()
            //修改数据库中的数据
            if(loginedUserId != -1)
                context?.let { UserDB.getUserDB(it)?.updateNickName(loginedUserId, nickName) }
            setNickName?.summary = nickName
            val storeUser = activity?.getSharedPreferences("userData", Context.MODE_PRIVATE)?.edit()
            storeUser?.putString("u_realName", nickName)
            storeUser?.apply()
            true
        }
        //点击修改个人简介
        loginedInfo = loginedUser.getString("u_info", "未登录").toString()
        val userInfo = findPreference<EditTextPreference>("userInfo")
        userInfo?.summary = loginedInfo
        userInfo?.setDefaultValue(loginedInfo)
        userInfo?.setOnPreferenceChangeListener { preference, newValue ->
            val info = newValue.toString()
            //修改数据库中的数据
            if(loginedUserId != -1)
                context?.let { UserDB.getUserDB(it)?.updateUserInfo(loginedUserId, info) }
            userInfo?.summary = info
            val storeUser = activity?.getSharedPreferences("userData", Context.MODE_PRIVATE)?.edit()
            storeUser?.putString("u_info", info)
            storeUser?.apply()
            true
        }
        //点击帮助信息，查看帮助信息
        val help = findPreference<Preference>("help")
        help?.setOnPreferenceClickListener {
            replaceFragment(HelpFragment())
            false
        }
        //点击新增帮助信息，查看帮助信息
        val setHelp = findPreference<Preference>("setHelp")
        setHelp?.setOnPreferenceClickListener {
            replaceFragment(AddHelpFragment())
            false
        }
    }
    //碎片的替换
    fun replaceFragment(fragment: Fragment){
        //初始往frameLayout中添加一个新的fragment
        val manager: FragmentManager? = activity?.supportFragmentManager
        val fragmentTransaction = manager?.beginTransaction()

        fragmentTransaction?.replace(R.id.settings_replace_layout, fragment)
        fragmentTransaction?.addToBackStack(fragment.tag)
        fragmentTransaction?.commit()
    }
}