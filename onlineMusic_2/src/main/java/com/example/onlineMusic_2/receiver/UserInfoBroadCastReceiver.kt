package com.example.onlineMusic_2.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Message

class UserInfoBroadCastReceiver(val mContext: Context) {
    companion object{
        val ACTION_USER_SUCCESS:String = "registerSuccess"
        val ACTION_USER_IMAGE_LOGINED:String = "logined"
        val ACTION_USER_IMAGE_EXITED:String = "exited"
        //更换头像
        val ACTION_USER_IMAGE_CHANGED_CLICKED:String = "imageChanged"
    }

    var broadcastReceiver: BroadcastReceiver? = null
    var intentFilter: IntentFilter = IntentFilter()
    var userInfoReceiverListener: UserInfoReceiverListener? = null
    var isRegisterSuccess:Boolean = false

    init {
        intentFilter.addAction(ACTION_USER_SUCCESS)
        intentFilter.addAction(ACTION_USER_IMAGE_LOGINED)
        intentFilter.addAction(ACTION_USER_IMAGE_EXITED)
        intentFilter.addAction(ACTION_USER_IMAGE_CHANGED_CLICKED)
    }

    @SuppressLint("HandlerLeak")
    val handler = object: Handler(){
        override fun handleMessage(msg: Message) {
            if(userInfoReceiverListener != null){
                val intent: Intent = msg.obj as Intent
                if(intent.action.equals(ACTION_USER_SUCCESS)){
                    isRegisterSuccess = true
                }else{
                    //接收器接收到广播
                    userInfoReceiverListener?.onReceive(mContext, intent)
                }
            }
        }
    }

    //注册广播
    fun registerReceiver(context: Context){
        if(broadcastReceiver == null){
            broadcastReceiver = object: BroadcastReceiver(){
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent != null) {
                        val msg = Message()
                        msg.obj = intent
                        handler.sendMessage(msg)
                    }
                }
            }
            //注册广播
            mContext.registerReceiver(broadcastReceiver, intentFilter)

            //发送注册成功广播
            val successIntent = Intent(ACTION_USER_SUCCESS)
            successIntent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            mContext.sendBroadcast(successIntent)
        }
    }
    //取消注册广播
    fun unRegisterReceiver(context: Context){
        if(broadcastReceiver != null && isRegisterSuccess){

            mContext.unregisterReceiver(broadcastReceiver)
        }
    }

    /**
     * 增加接收器
     */
    fun addUserInfoReceiverListener(userInfoReceiverListener: UserInfoReceiverListener){
        this.userInfoReceiverListener = userInfoReceiverListener
    }

    //通知栏通知接收器
    interface UserInfoReceiverListener{
        fun onReceive(context: Context?, intent: Intent?)
    }
}