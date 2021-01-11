package com.example.yongplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MusicService : Service() {
    val channelId = "HY"
    val NOTIFICATION_ID = 99
    val builder = NotificationCompat.Builder(this,channelId)
    var music: Music? = null
    var musicList = arrayListOf<Music>()
    var pos = 0
    var isPlaying = false

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            "CREATE_NOTIFICATION" -> {
                music = intent.getParcelableExtra("music")
                musicList = intent.getParcelableArrayListExtra<Music>("musiclist")!!
                pos = musicList.indexOf(music)
                createNotificationChannel(this,NotificationManagerCompat.IMPORTANCE_DEFAULT,false,getString(R.string.app_name),"App noti channel")
                setupNotification(music)
                isPlaying = true
            }
            "ACTION_PLAY" -> {
                if(!isPlaying) {
                    updateNotification("PLAY", music)
                }
                else {
                    updateNotification("PAUSE",music)
                }
            }
            "NOTI_ACTION_PLAY" -> {
                if(!isPlaying) {
                    updateNotification("PLAY", music)
                    val intent2 = Intent("NOTI_PLAY")
                    sendBroadcast(intent2)
                }
                else {
                    updateNotification("PAUSE",music)
                    val intent2 = Intent("NOTI_PAUSE")
                    sendBroadcast(intent2)
                }
            }
            "ACTION_PLAY_NEXT" -> {
                if(pos == musicList.size - 1) {
                    pos = 0
                }
                else
                    pos++
                music = musicList.get(pos)
                updateNotification("PLAY_NEXT",music)
            }
            "NOTI_ACTION_PLAY_NEXT" -> {
                if(pos == musicList.size - 1) {
                    pos = 0
                }
                else
                    pos++
                music = musicList.get(pos)
                updateNotification("PLAY_NEXT",music)
                val intent2 = Intent("NOTI_PLAY_NEXT")
                sendBroadcast(intent2)
            }
            "ACTION_PLAY_PREV" -> {
                if(pos == 0)
                    pos = musicList.size-1
                else
                    pos--
                music = musicList.get(pos)
                updateNotification("PLAY_PREV",music)
            }
            "NOTI_ACTION_PLAY_PREV" -> {
                if(pos == 0)
                    pos = musicList.size-1
                else
                    pos--
                music = musicList.get(pos)
                updateNotification("PLAY_PREV",music)
                val intent2 = Intent("NOTI_PLAY_PREV")
                sendBroadcast(intent2)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    fun createNotificationChannel(context: Context, importance: Int, showBadge: Boolean, name: String, description: String) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,name,importance)
            channel.description = description
            channel.setShowBadge(showBadge)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun setupNotification(music: Music?) {
        val title = "Don't Say a Word"
        val content = "Ellie Goulding"

        val intent1 = Intent(this,MusicActivity::class.java)
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        val pIntent1 = PendingIntent.getActivity(this,0,intent1,0)

        val intent2 = Intent(this,MusicService::class.java)
        intent2.setAction("NOTI_ACTION_PLAY")
        val pIntent2 = PendingIntent.getService(this,0,intent2,0)

        val intent3 = Intent(this,MusicService::class.java)
        intent3.setAction("NOTI_ACTION_PLAY_NEXT")
        val pIntent3 = PendingIntent.getService(this,0,intent3,0)

        val intent4 = Intent(this,MusicService::class.java)
        intent4.setAction("NOTI_ACTION_PLAY_PREV")
        val pIntent4 = PendingIntent.getService(this,0,intent4,0)


        builder.setSmallIcon(R.drawable.play)
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setContentIntent(pIntent1)


        val remoteViews = RemoteViews("com.example.yongplayer",R.layout.noti_layout)
        remoteViews.setImageViewUri(R.id.noti_album_img,music?.getAlbumUri())
        remoteViews.setImageViewResource(R.id.noti_play,R.drawable.pause)
        remoteViews.setTextViewText(R.id.noti_Id,music?.artist);
        remoteViews.setTextViewText(R.id.noti_Title,music?.title)

        remoteViews.setOnClickPendingIntent(R.id.noti_play,pIntent2)
        remoteViews.setOnClickPendingIntent(R.id.noti_play_next,pIntent3)
        remoteViews.setOnClickPendingIntent(R.id.noti_play_prev,pIntent4)


        builder.setContent(remoteViews)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID,builder.build())
    }

    fun updateNotification(actions: String, music: Music?) {
        val remoteViews = RemoteViews("com.example.yongplayer",R.layout.noti_layout)
        when(actions) {
            "PLAY" -> {
                remoteViews.setImageViewResource(R.id.noti_play, R.drawable.pause)
                builder.setSmallIcon(R.drawable.play)
                isPlaying = true
            }
            "PAUSE" -> {
                remoteViews.setImageViewResource(R.id.noti_play, R.drawable.play)
                builder.setSmallIcon(R.drawable.pause)
                isPlaying = false
            }
            else -> {
                remoteViews.setImageViewUri(R.id.noti_album_img,music?.getAlbumUri())
                remoteViews.setImageViewResource(R.id.noti_play, R.drawable.pause)
                remoteViews.setTextViewText(R.id.noti_Id,music?.artist);
                remoteViews.setTextViewText(R.id.noti_Title,music?.title)
                builder.setSmallIcon(R.drawable.play)
                isPlaying = true
            }
        }
        builder.setContent(remoteViews)
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID,builder.build())

    }
}