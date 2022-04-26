package com.example.onlineMusic_2.bean

class SongsBean {
    /**
     * id : 1303289043
     * name : 囍（Chinese Wedding）
     * artists : [{"id":12136078,"name":"葛东琪","picUrl":null,"alias":[],"albumSize":0,"picId":0,"img1v1Url":"https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg","img1v1":0,"trans":null}]
     * album : {"id":72363263,"name":"囍（Chinese Wedding）","artist":{"id":0,"name":"","picUrl":null,"alias":[],"albumSize":0,"picId":0,"img1v1Url":"https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg","img1v1":0,"trans":null},"publishTime":1579536000000,"size":2,"copyrightId":-1,"status":0,"picId":109951163472855060,"mark":0}
     * duration : 216705
     * copyrightId : 0
     * status : 0
     * alias : []
     * rtype : 0
     * ftype : 0
     * mvid : 0
     * fee : 8
     * rUrl : null
     * mark : 8192
     */
    var id = 0
    //歌曲名
    var name: String? = null

    //歌曲专辑信息---搜索中自带的专辑
    var album: AlbumBean? = null

    //歌曲专辑信息---歌曲详情记录了专辑封面
    var al: AlbumBean? = null
    //歌曲的时长
    var duration = 0

    //演唱者的所有信息---搜索中自带的信息
    var artists: List<ArtistsBean>? = null
    //所有演唱者的信息----歌曲详情api中带有的信息
    var ar: List<ArtistsBean>? = null

    //歌曲图片的url地址
    var musicUrl:String?=null

    //歌曲的地址
    var musicPath:String?=null
    //歌曲的类型：0--表示android自带歌曲，1--表示网络歌曲，2--表示在线歌曲，3--表示我喜欢的歌曲
    var musicType:Int?= 0

    //演唱者姓名
    var playerName:String? = null

    constructor()
    constructor(
        id: Int, name: String?, album: AlbumBean?, al: AlbumBean?, duration: Int, artists: List<ArtistsBean>?, musicUrl: String?, musicPath: String?, musicType: Int?, playerName: String?) {
        this.id = id
        this.name = name
        this.album = album
        this.al = al
        this.duration = duration
        this.artists = artists
        this.musicUrl = musicUrl
        this.musicPath = musicPath
        this.musicType = musicType
        this.playerName = playerName
    }
}