package com.example.onlineMusic_2.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Message

class FragmentBroadCastReceiver(val mContext: Context) {
    companion object{
        val ACTION_FRAGMENT_SUCCESS:String = "registerSuccess"
        val ACTION_FRAGMENT_OPEN_RANKSONG:String = "openRankSong"
        val ACTION_FRAGMENT_PLAY_DETAILSONG:String = "playDetailSong"
        //点击收藏图片
        val ACTION_FRAGMENT_LIKE_CLICKED:String = "likeClicked"
        //点击选择主题
        val ACTION_FRAGMENT_THEME_CLICKED:String = "themeClicked"
    }

    var broadcastReceiver: BroadcastReceiver? = null
    var intentFilter: IntentFilter = IntentFilter()
    var fragmentReceiverListener: FragmentReceiverListener? = null
    var isRegisterSuccess:Boolean = false

    init {
        intentFilter.addAction(ACTION_FRAGMENT_SUCCESS)
        intentFilter.addAction(ACTION_FRAGMENT_OPEN_RANKSONG)
        intentFilter.addAction(ACTION_FRAGMENT_PLAY_DETAILSONG)
        intentFilter.addAction(ACTION_FRAGMENT_LIKE_CLICKED)
        intentFilter.addAction(ACTION_FRAGMENT_THEME_CLICKED)
    }

    @SuppressLint("HandlerLeak")
    val handler = object: Handler(){
        override fun handleMessage(msg: Message) {
            if(fragmentReceiverListener != null){
                val intent: Intent = msg.obj as Intent
                if(intent.action.equals(ACTION_FRAGMENT_SUCCESS)){
                    isRegisterSuccess = true
                }else{
                    //接收器接收到广播
                    fragmentReceiverListener?.onReceive(mContext, intent)
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
            val successIntent = Intent(ACTION_FRAGMENT_SUCCESS)
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
    fun addFragmentReceiverListener(fragmentReceiverListener: FragmentReceiverListener){
        this.fragmentReceiverListener = fragmentReceiverListener
    }

    //通知栏通知接收器
    interface FragmentReceiverListener{
        fun onReceive(context: Context?, intent: Intent?)
    }
}