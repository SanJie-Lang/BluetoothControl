package com.cbsd.libra.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cbsd.libra.R
import com.cbsd.libra.click
import com.cbsd.libra.model.Color
import kotlinx.android.synthetic.main.item_popup.view.*

class PopupAdapter(private val mContext: Context, private val colors: ArrayList<Color>): RecyclerView.Adapter<PopupAdapter.ColorHolder>() {

    private lateinit var onColorSelectedListener: OnColorSelectedListener

    fun setOnColorSelectedListener(s: (color: Color, position: Int) -> Unit){
        onColorSelectedListener = object : OnColorSelectedListener {
            override fun onColorSelected(color: Color, position: Int) {
                s(color, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorHolder {
        return ColorHolder(LayoutInflater.from(mContext).inflate(R.layout.item_popup, parent, false))
    }

    override fun onBindViewHolder(holder: ColorHolder, position: Int) {
        holder.itemView.itemPopupColorHint.setBackgroundColor(ContextCompat.getColor(mContext, colors[position].colorRes))
        holder.itemView.itemPopupColorNameTv.text = colors[position].name

        holder.itemView.click {
            onColorSelectedListener.onColorSelected(colors[position], position)
        }
    }

    override fun getItemCount(): Int = colors.size

    inner class ColorHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface OnColorSelectedListener {
        fun onColorSelected(color: Color, position: Int)
    }
}