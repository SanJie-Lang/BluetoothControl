package com.cbsd.libra.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cbsd.libra.R
import com.cbsd.libra.click
import kotlinx.android.synthetic.main.item_video.view.*

class VideoAdapter(private val mContext: Context): RecyclerView.Adapter<VideoAdapter.VideoHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        return VideoHolder(LayoutInflater.from(mContext).inflate(R.layout.item_video, parent, false))
    }

    override fun getItemCount(): Int = 4

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        holder.itemView.itemVideoCoverIv.setImageResource(R.mipmap.ic_launcher_round)
        holder.itemView.itemVideoNameTv.text = "第${position + 1}个视频"
        holder.itemView.click {
            onItemClickListener?.onItemClick(position)
        }
    }

    inner class VideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun addOnItemClickListener(cc:(position: Int) -> Unit){
        if(onItemClickListener == null)
            onItemClickListener = object :
                OnItemClickListener {
                override fun onItemClick(position: Int) {
                    cc(position)
                }
            }
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}