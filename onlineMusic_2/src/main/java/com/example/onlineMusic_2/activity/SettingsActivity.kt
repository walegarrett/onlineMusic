package com.example.onlineMusic_2.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.fragment.MainFragment
import com.example.onlineMusic_2.fragment.SettingsFragment
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    companion object{
        //供别的activity传递参数到本activity提供参考
        fun actionStart(context: Context){
            val intent= Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //初始化页面
        initViews()
    }

    /**
     * 初始化页面布局以及控件的点击事件
     */
    fun initViews(){
        //让toolbar的外观和功能与actionbar都相似
        setSupportActionBar(toolbar)
        //添加一个返回按钮
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            //系统自带图标
            val attrs = intArrayOf(android.R.attr.homeAsUpIndicator)
            val ta = theme.obtainStyledAttributes(attrs)
            val indicator = ta.getDrawable(0)
            it.setHomeAsUpIndicator(indicator)
        }

        //初始往frameLayout中添加一个新的fragment
        val manager: FragmentManager = getSupportFragmentManager()
        val fragmentTransaction = manager.beginTransaction()
        fragmentTransaction.add(
            R.id.settings_replace_layout,
            SettingsFragment()
        )
        fragmentTransaction.commit()

    }
    //创建菜单选项
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_settings,menu)
        return true
    }
    //菜单项的选中
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->finish()
        }
        return true
    }

    //碎片的替换
    fun replaceFragment(fragment: Fragment){
        //初始往frameLayout中添加一个新的fragment
        val manager: FragmentManager = getSupportFragmentManager()
        val fragmentTransaction = manager.beginTransaction()
        fragmentTransaction.replace(R.id.settings_replace_layout, fragment)
        fragmentTransaction.commit()
    }
}