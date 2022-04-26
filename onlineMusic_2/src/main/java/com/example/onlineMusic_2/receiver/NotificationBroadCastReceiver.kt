package com.example.onlineMusic_2.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Message
import android.util.Log

class NotificationBroadCastReceiver(val mContext: Context) {
    var broadcastReceiver: BroadcastReceiver? = null
    var intentFilter:IntentFilter = IntentFilter()
    var notificationReceiverListener: NotificationReceiverListener? = null
    var isRegisterSuccess:Boolean = false

    init {
        intentFilter.addAction(ACTION_NOTIFICATION_SUCCESS)
        intentFilter.addAction(ACTION_NOTIFICATION_PLAYBTN)
        intentFilter.addAction(ACTION_NOTIFICATION_PREBTN)
        intentFilter.addAction(ACTION_NOTIFICATION_NEXTBTN)
    }
    companion object{
        val ACTION_NOTIFICATION_SUCCESS:String = "registerSuccess"
        val ACTION_NOTIFICATION_PLAYBTN:String = "playBtn"
        val ACTION_NOTIFICATION_PREBTN:String = "preBtn"
        val ACTION_NOTIFICATION_NEXTBTN:String = "nextBtn"

    }

    @SuppressLint("HandlerLeak")
    val handler = object: Handler(){
        override fun handleMessage(msg: Message) {
            if(notificationReceiverListener != null){

                val intent:Intent = msg.obj as Intent
                if(intent.action.equals(ACTION_NOTIFICATION_SUCCESS)){
                    isRegisterSuccess = true
                }else{
                    //接收器接收到广播
                    notificationReceiverListener?.onReceive(mContext, intent)
                }
            }
        }
    }

    //注册广播
    @SuppressLint("LongLogTag")
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
            val successIntent = Intent(ACTION_NOTIFICATION_SUCCESS)
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
    fun addNotificationReceiverListener(notificationReceiverListener: NotificationReceiverListener){
        this.notificationReceiverListener = notificationReceiverListener
    }

    //通知栏通知接收器
    interface NotificationReceiverListener{
        fun onReceive(context: Context?, intent: Intent?)
    }
}