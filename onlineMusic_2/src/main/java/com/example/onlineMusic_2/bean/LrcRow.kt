package com.example.onlineMusic_2.bean

import android.text.TextUtils
import android.util.Log


/**
 * 每行歌词的实体类，实现了Comparable接口，方便List<LrcRow>的sort排序
 *
 * @author Ligang  2014/8/19
</LrcRow> */
class LrcRow : Comparable<LrcRow?> {
    /**
     * 开始时间 为00:10:00
     */
    var timeStr: String? = null

    /**
     * 开始时间 毫米数  00:10:00  为10000
     */
    var time = 0

    /**
     * 歌词内容
     */
    var content: String? = null

    /**
     * 该行歌词显示的总时间
     */
    private var totalTime = 0
    fun getTotalTime(): Long {
        return totalTime.toLong()
    }

    fun setTotalTime(totalTime: Int) {
        this.totalTime = totalTime
    }

    constructor() : super() {}
    constructor(timeStr: String?, time: Int, content: String?) : super() {
        this.timeStr = timeStr
        this.time = time
        this.content = content
    }

    override fun toString(): String {
        return ("LrcRow [timeStr=" + timeStr + ", time=" + time + ", content="
                + content + "]")
    }

    companion object {
        /**
         * 将歌词文件中的某一行 解析成一个List<LrcRow>
         * 因为一行中可能包含了多个LrcRow对象
         * 比如  [03:33.02][00:36.37]当鸽子不再象征和平  ，就包含了2个对象
         *
         * @param lrcLine
         * @return
        </LrcRow> */
        fun createRows(lrcLine: String): List<LrcRow>? {
            if (!lrcLine.startsWith("[")) {
                return null
            }
            //最后一个"]"
            val lastIndexOfRightBracket = lrcLine.lastIndexOf("]")
            //歌词内容
            val content = lrcLine.substring(lastIndexOfRightBracket + 1, lrcLine.length)
            //截取出歌词时间，并将"[" 和"]" 替换为"-"   [offset:0]
//            Log.e("歌词", "lrcLine=$lrcLine")
            // -03:33.02--00:36.37-
            val times = lrcLine.substring(0, lastIndexOfRightBracket + 1).replace("[", "-").replace("]", "-")

            val timesArray = times.split("-".toRegex()).toTypedArray()
            val lrcRows: MutableList<LrcRow> = ArrayList()
            for (tem in timesArray) {
                if (TextUtils.isEmpty(tem.trim { it <= ' ' })) {
                    continue
                }
                //
                try {
                    val lrcRow = LrcRow(tem, formatTime(tem), content)
                    lrcRows.add(lrcRow)
                } catch (e: Exception) {
//                    e.message?.let { Log.w("LrcRow", it) }
                }
            }
            return lrcRows
        }

        /****
         * 把歌词时间转换为毫秒值  如 将00:10.00  转为10000
         *
         * @param timeStr
         * @return
         */
        private fun formatTime(timeStr: String): Int {
            var timeStr = timeStr
            if(!timeStr.contains(".")){
                timeStr += ".00"
            }
            timeStr = timeStr.replace('.', ':')
            val times = timeStr.split(":".toRegex()).toTypedArray()
            return times[0].toInt() * 60 * 1000 + times[1].toInt() * 1000 + times[2].toInt()
        }
    }

    override fun compareTo(other: LrcRow?): Int {
        if (other != null) {
            return (time - other.time)
        }else return 0
    }
}