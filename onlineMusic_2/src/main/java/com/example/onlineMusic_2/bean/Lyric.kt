package com.example.onlineMusic_2.bean

class Lyric {
    var sgc = false
    var sfy = false
    var qfy = false
    var lrc: LrcBean? = null
    var klyric: KlyricBean? = null
    var tlyric: TlyricBean? = null
    var code = 0

    class LrcBean {
        var version = 0
        var lyric: String? = null
    }

    class KlyricBean {
        var version = 0
        var lyric: String? = null
    }

    class TlyricBean {
        /**
         * version : 0
         * lyric :
         */
        var version = 0
        var lyric: String? = null
    }
}