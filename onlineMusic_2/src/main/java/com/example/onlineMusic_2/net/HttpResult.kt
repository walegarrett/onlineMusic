package com.example.onlineMusic_2.net

/**
 * @author zhangliangming
 */
class HttpResult {
    /**
     * http状态码
     */
    var status = 0

    /***
     * http返回的结果
     */
    var result: Any? = null

    /**
     * 错误信息
     */
    var errorMsg: String? = null

    companion object {
        /**
         *
         */
        const val STATUS_SUCCESS = 0
        const val STATUS_ERROR = -1
        const val STATUS_NONET = -2
        const val STATUS_NOWIFI = -3
    }
}