package com.example.onlineMusic_2.bean

class MusicUrl {
    /**
     * data : [{"id":33894312,"url":"http://m7.music.126.net/20201220133621/5b890c0af2055fbbbec1dbbc9967f7c8/ymusic/0fd6/4f65/43ed/a8772889f38dfcb91c04da915b301617.mp3","br":320000,"size":10691439,"md5":"a8772889f38dfcb91c04da915b301617","code":200,"expi":1200,"type":"mp3","gain":0,"fee":0,"uf":null,"payed":0,"flag":0,"canExtend":false,"freeTrialInfo":null,"level":"exhigh","encodeType":"mp3","freeTrialPrivilege":{"resConsumable":false,"userConsumable":false},"urlSource":0}]
     * code : 200
     */
    var code = 0
    var data: List<DataBean>? = null

    class DataBean {
        /**
         * id : 33894312
         * url : http://m7.music.126.net/20201220133621/5b890c0af2055fbbbec1dbbc9967f7c8/ymusic/0fd6/4f65/43ed/a8772889f38dfcb91c04da915b301617.mp3
         * br : 320000
         * size : 10691439
         * md5 : a8772889f38dfcb91c04da915b301617
         * code : 200
         * expi : 1200
         * type : mp3
         * gain : 0
         * fee : 0
         * uf : null
         * payed : 0
         * flag : 0
         * canExtend : false
         * freeTrialInfo : null
         * level : exhigh
         * encodeType : mp3
         * freeTrialPrivilege : {"resConsumable":false,"userConsumable":false}
         * urlSource : 0
         */
        var id = 0
        var url: String? = null

    }
}