package com.example.onlineMusic_2.bean

import android.os.Parcel
import android.os.Parcelable

class ThemeInfo : Parcelable {
    var name: String? = null
    var color = 0
    var background = 0
    var isSelect = false

    constructor() {}

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeInt(color)
        dest.writeInt(background)
        dest.writeByte(if (isSelect) 1.toByte() else 0.toByte())
    }

    protected constructor(`in`: Parcel) {
        name = `in`.readString()
        color = `in`.readInt()
        background = `in`.readInt()
        isSelect = `in`.readByte().toInt() != 0
    }

    companion object {
        val CREATOR: Parcelable.Creator<ThemeInfo?> = object : Parcelable.Creator<ThemeInfo?> {
            override fun createFromParcel(source: Parcel): ThemeInfo? {
                return ThemeInfo(source)
            }

            override fun newArray(size: Int): Array<ThemeInfo?> {
                return arrayOfNulls(size)
            }
        }
    }
}