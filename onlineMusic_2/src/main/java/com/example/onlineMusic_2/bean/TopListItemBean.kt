package com.example.onlineMusic_2.bean

class TopListItemBean {
    var playlist: PlaylistBean? = null
    var code = 0

    class PlaylistBean {
        var titleImage = 0
        var coverImgId: Long = 0
        var coverImgUrl: String? = null
        var playCount: Long = 0
        var description: String? = null
        var status = 0
        var name: String? = null
        var id:String? = null
        var shareCount = 0
        var coverImgId_str: String? = null
        var ToplistType: String? = null
        var commentCount = 0

        //该榜单中的所有歌曲信息
        var tracks: List<TracksBean>? = null

        //歌曲的信息，歌曲名和id
        class TracksBean {
            var name: String? = null
            var id = 0
            var ar: List<ArtistsBean>? = null
        }

        override fun toString(): String {
            return "PlaylistBean(titleImage=$titleImage, coverImgId=$coverImgId, coverImgUrl=$coverImgUrl, playCount=$playCount, description=$description, status=$status, name=$name, id=$id, shareCount=$shareCount, coverImgId_str=$coverImgId_str, ToplistType=$ToplistType, commentCount=$commentCount, tracks=$tracks)"
        }
    }

    override fun toString(): String {
        return "TopListItemBean(playlist=$playlist, code=$code)"
    }

}