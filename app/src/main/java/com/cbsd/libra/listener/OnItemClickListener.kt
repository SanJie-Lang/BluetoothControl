package com.cbsd.libra.listener

interface OnItemClickListener<T> {
    fun onItemClick(t: T, position: Int)
}