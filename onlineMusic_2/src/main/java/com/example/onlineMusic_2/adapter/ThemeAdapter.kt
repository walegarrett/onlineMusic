package com.example.onlineMusic_2.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.bean.ThemeInfo
import com.example.onlineMusic_2.fragment.ThemeFragment
import com.example.onlineMusic_2.receiver.FragmentBroadCastReceiver

class ThemeAdapter(val context: Context, val themeInfoList:List<ThemeInfo>, val selectTheme:Int): RecyclerView.Adapter<ThemeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var relativeLayout: RelativeLayout = view.findViewById(R.id.theme_item_rl)
        var circleIv: ImageView = view.findViewById(R.id.theme_iv)
        var nameTv: TextView = view.findViewById(R.id.theme_name_tv)
        var selectBtn: Button = view.findViewById(R.id.theme_select_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //注意这里需要设置子项的布局文件，否则报错
        val view= LayoutInflater.from(parent.context).inflate(R.layout.theme_item, parent,false)
        val viewHolder=ViewHolder(view)

        //注册子项的点击事件
        viewHolder.selectBtn.setOnClickListener {
            val position=viewHolder.adapterPosition
            //发送打开item的广播
            val intent = Intent(FragmentBroadCastReceiver.ACTION_FRAGMENT_THEME_CLICKED)
            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            intent.putExtra("position", position)
            context.sendBroadcast(intent)
        }
        return viewHolder
    }

    override fun getItemCount(): Int= themeInfoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val themeInfo = themeInfoList[position]
        if (selectTheme == ThemeFragment.THEME_SIZE - 1) {
            holder.relativeLayout.setBackgroundResource(R.drawable.selector_layout_night)
            holder.selectBtn.setBackgroundResource(R.drawable.shape_theme_btn_night)
        } else {
            holder.relativeLayout.setBackgroundResource(R.drawable.selector_layout_day)
            holder.selectBtn.setBackgroundResource(R.drawable.shape_theme_btn_day)
        }
        holder.selectBtn.setPadding(0, 0, 0, 0)
        if (themeInfo.isSelect) {
            holder.circleIv.setImageResource(R.drawable.tick)
            holder.selectBtn.text = "使用中"
            holder.selectBtn.setTextColor(context.getResources().getColor(themeInfo.color))
        } else {
            holder.circleIv.setImageBitmap(null)
            holder.selectBtn.text = "使用"
            holder.selectBtn.setTextColor(context.getResources().getColor(R.color.grey500))
        }
        holder.circleIv.setBackgroundResource(themeInfo.color)
        holder.nameTv.setTextColor(context.getResources().getColor(themeInfo.color))
        holder.nameTv.setText(themeInfo.name)
    }
}