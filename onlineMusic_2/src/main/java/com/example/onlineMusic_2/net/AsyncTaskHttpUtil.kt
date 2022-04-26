package com.example.onlineMusic_2.net

import android.os.AsyncTask

class AsyncTaskHttpUtil : AsyncTask<String?, Int?, HttpResult?>() {
    private var mAsyncTaskListener: AsyncTaskListener? = null
    private var sleepTime = 0
    override fun onPostExecute(httpResult: HttpResult?) {
        if (mAsyncTaskListener != null) {
            mAsyncTaskListener!!.onPostExecute(httpResult)
        }
    }

    override fun doInBackground(vararg params: String?): HttpResult? {
        try {
            Thread.sleep(sleepTime.toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return if (mAsyncTaskListener != null) {
            mAsyncTaskListener!!.doInBackground()
        } else null
    }

    interface AsyncTaskListener {
        fun doInBackground(): HttpResult?
        fun onPostExecute(httpResult: HttpResult?)
    }

    fun setAsyncTaskListener(mAsyncTaskListener: AsyncTaskListener?) {
        this.mAsyncTaskListener = mAsyncTaskListener
    }

    fun setSleepTime(sleepTime: Int) {
        this.sleepTime = sleepTime
    }
}
