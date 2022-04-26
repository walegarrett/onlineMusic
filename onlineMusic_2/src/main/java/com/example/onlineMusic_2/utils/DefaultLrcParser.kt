package com.example.onlineMusic_2.utils

import android.util.Log
import com.example.onlineMusic_2.bean.LrcRow
import java.io.*
import kotlin.collections.ArrayList


class DefaultLrcParser{
    /***
     * 将歌词文件里面的字符串 解析成一个List<LrcRow>
     */
    fun getLrcRows(str: InputStream?): List<LrcRow>? {
        if (str==null) {
            return null
        }
        val br = BufferedReader(InputStreamReader(str))
        val lrcRows: MutableList<LrcRow> = ArrayList()
        var lrcLine: String? =null
        try {
            while (br.readLine().also { lrcLine = it } != null) {
//                lrcLine?.let { Log.d("DefaultLrcParser", it) }
                val rows = lrcLine?.let { LrcRow.createRows(it) }
                if (rows != null && rows.isNotEmpty()) {
                    lrcRows.addAll(rows)
                }
            }
            lrcRows.sort()
            val len = lrcRows.size
//            Log.d("DefaultLrcParser", len.toString())
            for (i in 0 until len - 1) {
                lrcRows[i].setTotalTime(lrcRows[i + 1].time - lrcRows[i].time)
            }
            lrcRows[len - 1].setTotalTime(5000)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return lrcRows
    }

    /**
     * 根据字符串数组解析歌词
     */
    fun getLrcRows(str: List<String>?): List<LrcRow>? {
        if (str==null || str.isEmpty()) {
            return null
        }
        val lrcRows: MutableList<LrcRow> = ArrayList()
        try {
            for(lrcLine in str){
//                Log.d("DefaultLrcParser", lrcLine)
                val rows = lrcLine.let { LrcRow.createRows(it) }
                if (rows != null && rows.isNotEmpty()) {
                    lrcRows.addAll(rows)
                }
            }
            lrcRows.sort()
            val len = lrcRows.size
//            Log.d("DefaultLrcParser", len.toString())
            for (i in 0 until len - 1) {
                lrcRows[i].setTotalTime(lrcRows[i + 1].time - lrcRows[i].time)
            }
            lrcRows[len - 1].setTotalTime(5000)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return lrcRows
    }
}