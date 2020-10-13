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

class VideoAdapter(private val mContext: Context) :
    RecyclerView.Adapter<VideoAdapter.VideoHolder>() {

    private val imageList = arrayListOf(
        R.mipmap.img_video_1,
        R.mipmap.img_video_2,
        R.mipmap.img_video_3,
        R.mipmap.img_video_4,
        R.mipmap.img_video_5
    )

    private val videoNameList = arrayListOf("时尚运动 健身塑型", "实景时间流", "网课开学啦", "城市立交夜景", "慢速火车实拍")

    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        return VideoHolder(
            LayoutInflater.from(mContext).inflate(R.layout.item_video, parent, false)
        )
    }

    override fun getItemCount(): Int = imageList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        holder.itemView.itemVideoCoverIv.setImageResource(imageList[position])
        holder.itemView.itemVideoNameTv.text = videoNameList[position]
        holder.itemView.click {
            onItemClickListener?.onItemClick(position)
        }
    }

    inner class VideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun addOnItemClickListener(cc: (position: Int) -> Unit) {
        if (onItemClickListener == null)
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