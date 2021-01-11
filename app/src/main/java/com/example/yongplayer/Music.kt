package com.example.yongplayer

import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Music(var id: String, var artist: String, var albumId: String, var title: String, var duration: Long) :
    Parcelable {

    fun getMusicUri() : Uri {
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id)
    }

    fun getAlbumUri() : Uri {
        return Uri.parse("content://media/external/audio/albumart/$albumId")
    }
}