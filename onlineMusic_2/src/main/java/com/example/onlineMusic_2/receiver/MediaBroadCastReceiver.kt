package com.example.onlineMusic_2.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Message

class MediaBroadCastReceiver(val mContext: Context) {
    companion object{
        val ACTION_MEDIA_SUCCESS:String = "registerSuccess"
        val ACTION_MEDIA_PLAYING:String = "isPlaying"
        val ACTION_MEDIA_PAUSED:String = "isPaused"
        val ACTION_MEDIA_FINISHED:String = "finished"

    }

    var broadcastReceiver: BroadcastReceiver? = null
    var intentFilter: IntentFilter = IntentFilter()
    var mediaReceiverListener: MediaReceiverListener? = null
    var isRegisterSuccess:Boolean = false

    init {
        intentFilter.addAction(ACTION_MEDIA_SUCCESS)
        intentFilter.addAction(ACTION_MEDIA_PLAYING)
        intentFilter.addAction(ACTION_MEDIA_PAUSED)
        intentFilter.addAction(ACTION_MEDIA_FINISHED)
    }

    @SuppressLint("HandlerLeak")
    val handler = object: Handler(){
        override fun handleMessage(msg: Message) {
            if(mediaReceiverListener != null){

                val intent: Intent = msg.obj as Intent
                if(intent.action.equals(ACTION_MEDIA_SUCCESS)){
                    isRegisterSuccess = true
                }else{
                    //接收器接收到广播
                    mediaReceiverListener?.onReceive(mContext, intent)
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
            val successIntent = Intent(ACTION_MEDIA_SUCCESS)
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
    fun addNotificationReceiverListener(mediaReceiverListener: MediaReceiverListener){
        this.mediaReceiverListener = mediaReceiverListener
    }

    //通知栏通知接收器
    interface MediaReceiverListener{
        fun onReceive(context: Context?, intent: Intent?)
    }
}