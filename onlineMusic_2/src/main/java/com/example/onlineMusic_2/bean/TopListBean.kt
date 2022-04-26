package com.example.onlineMusic_2.bean

/**
 * 歌曲排行榜
 */
class TopListBean {
    var code = 0
    var artistToplist: ArtistToplistBean? = null
    //排行表列表
    var list: List<ListBean>? = null

    class ArtistToplistBean {
        var coverUrl: String? = null
        var name: String? = null
        var upateFrequency: String? = null
        var position = 0
    }

    class ListBean {
        var coverImgUrl: String? = null
        var playCount: Long = 0
        var description: String? = null
        var status = 0
        var name: String? = null
        var id:String? = null
        var coverImgId_str: String? = null
        var ToplistType: String? = null

        //前三个歌曲
        var top3MusicList:List<TopListItemBean.PlaylistBean.TracksBean>? = null
        override fun toString(): String {
            return "ListBean(coverImgUrl=$coverImgUrl, playCount=$playCount, description=$description, status=$status, name=$name, id=$id, coverImgId_str=$coverImgId_str, ToplistType=$ToplistType, top3MusicList=$top3MusicList)"
        }

    }

    override fun toString(): String {
        return "TopListBean(code=$code, artistToplist=$artistToplist, list=$list)"
    }

}