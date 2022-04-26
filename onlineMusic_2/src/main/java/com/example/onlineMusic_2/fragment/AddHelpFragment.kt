package com.example.onlineMusic_2.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.bean.Help
import com.example.onlineMusic_2.db.HelpDB
import kotlinx.android.synthetic.main.fragment_add_help.*

class AddHelpFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_help, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
    }

    /**
     * 初始化页面控件，并且监听提交按钮的点击事件，如果通过字段验证则提交自定义帮助信息
     */
    @SuppressLint("ShowToast")
    fun initViews(){
        submitHelp.setOnClickListener {
            val question = questionView.text.toString()
            val answer = answerView.text.toString()
            if(question != "" && answer != ""){
                mActivity?.let { it1 -> HelpDB.getHelpDB(it1)?.add(Help(-1, question, answer)) }
                getFragmentManager()?.popBackStack()
            }else{
                Toast.makeText(context, "文本内容不能为空哦！！！", Toast.LENGTH_SHORT).show()
            }

        }
    }
}