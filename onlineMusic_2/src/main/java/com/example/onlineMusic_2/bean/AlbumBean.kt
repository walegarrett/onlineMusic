package com.example.onlineMusic_2.bean

class AlbumBean {
    /**
     * id : 72363263
     * name : 囍（Chinese Wedding）
     * artist : {"id":0,"name":"","picUrl":null,"alias":[],"albumSize":0,"picId":0,"img1v1Url":"https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg","img1v1":0,"trans":null}
     * publishTime : 1579536000000
     * size : 2
     * copyrightId : -1
     * status : 0
     * picId : 109951163472855060
     * mark : 0
     */
    var id = 0
    var name: String? = null
    var picUrl:String?= null
    var pic_str:String?= null
    var artist: ArtistBean? = null

    class ArtistBean {
        /**
         * id : 0
         * name :
         * picUrl : null
         * alias : []
         * albumSize : 0
         * picId : 0
         * img1v1Url : https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg
         * img1v1 : 0
         * trans : null
         */
        var id = 0
        var name: String? = null
        var picUrl: Any? = null
        var picId = 0
    }
}