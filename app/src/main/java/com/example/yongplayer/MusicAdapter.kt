package com.example.yongplayer

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_layout.view.*
import java.text.SimpleDateFormat

class MusicAdapter : RecyclerView.Adapter<CustomViewHolder>() {
    val musicList = arrayListOf<Music>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val music = musicList.get(position)
        holder.setHolder(music)
    }
}



class CustomViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var music2 : Music? = null
    init {
        itemView.setOnClickListener {
            val intent = Intent(itemView.context,MusicActivity::class.java)
            intent.putExtra("duration",itemView.textDuration.text)
            intent.putExtra("music",music2)
            itemView.context.startActivity(intent)
        }
    }

    fun setHolder(music: Music) {
        music2 = music
        itemView.album_img.setImageURI(music.getAlbumUri())
        itemView.textId.text = music.artist
        itemView.textTitle.text = music.title
        val sdf = SimpleDateFormat("mm:ss")
        itemView.textDuration.text = sdf.format(music.duration)
    }

}