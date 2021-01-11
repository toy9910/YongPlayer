package com.example.yongplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_music.*
import kotlinx.android.synthetic.main.item_layout.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer

class MusicActivity : AppCompatActivity() {
    var music: Music? = null
    val musicList = arrayListOf<Music>()
    var isPlaying = false
    var curPos = 0
    var yongPlayer = MediaPlayer()
    val mBroadcastReceiver = MusicBroadcastReceiver()
    var duration = "00:00"
    var curTime = -1
    var timerTask: Timer? = null

    inner class MusicBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                "NOTI_PLAY" -> {
                    yongPlayer.start()
                    timerStart()
                    isPlaying = true
                    play_play.setImageResource(R.drawable.pause)
                }
                "NOTI_PAUSE" -> {
                    yongPlayer.pause()
                    timerPause()
                    isPlaying = false
                    play_play.setImageResource(R.drawable.play)
                }
                "NOTI_PLAY_NEXT" -> {
                    if(curPos == musicList.size - 1) {
                        curPos = 0
                    }
                    else
                        curPos++
                    music = musicList.get(curPos)
                    timerReset()
                    playMusic(music)
                }
                "NOTI_PLAY_PREV" -> {
                    if(curPos == 0)
                        curPos = musicList.size-1
                    else
                        curPos--
                    music = musicList.get(curPos)
                    timerReset()
                    playMusic(music)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)

        val intentFilter = IntentFilter()
        intentFilter.addAction("NOTI_PLAY")
        intentFilter.addAction("NOTI_PAUSE")
        intentFilter.addAction("NOTI_PLAY_NEXT")
        intentFilter.addAction("NOTI_PLAY_PREV")
        registerReceiver(mBroadcastReceiver,intentFilter)

        musicList.addAll(getMusicList())

        music = intent.getParcelableExtra("music")
        duration = intent.getStringExtra("duration")!!
        curPos = musicList.indexOf(music)
        music_album_img.setImageURI(music?.getAlbumUri())

        val intent2 = Intent(this,MusicService::class.java)
        intent2.setAction("CREATE_NOTIFICATION")
        intent2.putExtra("music",music)
        intent2.putExtra("musiclist",musicList)
        startService(intent2)

        isPlaying = true
        playMusic(music)

        yongPlayer.setOnCompletionListener {
            moveNextMusic()
        }
    }

    fun onClick(view: View) {
        when(view.id) {
            R.id.play_play -> {
                if(isPlaying) {
                    play_play.setImageResource(R.drawable.play)
                    val intent = Intent(this,MusicService::class.java)
                    intent.setAction("ACTION_PAUSE")
                    startService(intent)
                    yongPlayer.pause()
                    timerPause()
                    isPlaying = false
                }
                else {
                    play_play.setImageResource(R.drawable.pause)
                    val intent = Intent(this,MusicService::class.java)
                    intent.setAction("ACTION_PLAY")
                    startService(intent)
                    yongPlayer.start()
                    timerStart()
                    isPlaying = true
                }
            }
            R.id.play_next -> {
                moveNextMusic()
            }
            R.id.play_prev -> {
                movePrevMusic()
            }
        }
    }

    fun timerStart() {
        timerTask = timer(period = 1000) {
            curTime++

            val min = curTime / 60
            val sec = curTime % 60

            progress_bar.post(object : Runnable {
                override fun run() {
                    progress_bar.setProgress(curTime)
                }
            })

            runOnUiThread {
                time.setText(String.format("%02d:%02d",min,sec) + " / $duration")

            }
        }
    }

    fun timerPause() {
        timerTask?.cancel()
    }

    fun timerReset() {
        timerTask?.cancel()
        curTime = -1
        val sdf = SimpleDateFormat("mm:ss")
        duration = sdf.format(music?.duration)
        time.setText("00:00 / $duration")
        progress_bar.progress = 0
    }

    fun playMusic(curMusic: Music?) {
            val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music?.id)
            music_album_img.setImageURI(music?.getAlbumUri())
            val sdf = SimpleDateFormat("mm:ss")
            duration = sdf.format(music?.duration)
            progress_bar.max = (music?.duration!! / 1000).toInt()

            yongPlayer.reset()
            yongPlayer.setDataSource(this, uri)
            yongPlayer.prepare()
            yongPlayer.start()
            timerStart()

            isPlaying = true
            play_play.setImageResource(R.drawable.pause)
    }

    fun movePrevMusic() {
        if(curPos == 0)
            curPos = musicList.size-1
        else
            curPos--
        music = musicList.get(curPos)
        timerReset()
        val intent = Intent(this,MusicService::class.java)
        intent.setAction("ACTION_PLAY_PREV")
        intent.putExtra("music",music)
        startService(intent)
        playMusic(music)
    }

    fun moveNextMusic() {
        if(curPos == musicList.size - 1) {
            curPos = 0
        }
        else
            curPos++
        music = musicList.get(curPos)
        timerReset()
        val intent = Intent(this,MusicService::class.java)
        intent.setAction("ACTION_PLAY_NEXT")
        intent.putExtra("music",music)
        startService(intent)
        playMusic(music)
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
