package com.example.onlineMusic_2.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.onlineMusic_2.R
import java.util.*

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        //获取权限
        initPermission()
    }

    /**
     * 显示欢迎页面指定时间之后开启MainActivity
     */
    private fun checkSkip() {
        val timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                startMusicActivity()
            }
        }
        timer.schedule(task, 1000)
    }

    private fun startMusicActivity() {
        val intent = Intent()
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * 是否获取读取内存的权限，已经获取即可进入app
     */
    private fun initPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            checkSkip()
            return
        }
        var isPermissionGranted = true
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MainActivity.PERMISSON_REQUESTCODE)
            isPermissionGranted = false
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MainActivity.PERMISSON_REQUESTCODE)
            isPermissionGranted = false
        }
        if(isPermissionGranted){
            checkSkip()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MainActivity.PERMISSON_REQUESTCODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkSkip()
            } else {
                Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}