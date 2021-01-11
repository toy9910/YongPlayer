package com.example.yongplayer

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    val REQUEST_CODE = 99
    val CHANNEL_ID = "YONG"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(checkPermission())
            showList()
        else
            ActivityCompat.requestPermissions(this,permissions,REQUEST_CODE)

    }

    private fun showList() {
        val adapter = MusicAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.musicList.addAll(getMusicList())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            REQUEST_CODE -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showList()
                }
                else {
                    Toast.makeText(this,"권한 요청을 승인해야 실행할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun checkPermission() : Boolean{
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun getMusicList(): List<Music> {
        val musicListUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        var proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )

        val cursor = contentResolver.query(musicListUri,proj,null,null,null)
        val musicList = mutableListOf<Music>()
        while(cursor?.moveToNext() ?: false) {
            val id = cursor!!.getString(0)
            val title = cursor!!.getString(1)
            val albumId = cursor!!.getString(2)
            val artist = cursor!!.getString(3)
            val duration = cursor!!.getLong(4)
            val music = Music(id,artist,albumId,title,duration)
            musicList.add(music)
        }
        return musicList
    }
}