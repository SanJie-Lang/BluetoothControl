package com.cbsd.libra

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

fun View.click(c: () -> Unit){
    setOnClickListener {
        c()
    }
}

fun View.longClick(longClick: () -> Unit){
    setOnLongClickListener {
        longClick()
        false
    }
}


@SuppressLint("ClickableViewAccessibility")
fun View.touch(touch: (view: View, event: MotionEvent) -> Unit){
    setOnTouchListener { view, motionEvent ->
        touch(view, motionEvent)
        false
    }

}

@SuppressLint("ClickableViewAccessibility")
fun View.touchUp(up: () -> Unit){
    setOnTouchListener { view, motionEvent ->
        when(motionEvent.action){
            MotionEvent.ACTION_UP -> {
                up()
            }
        }
        false
    }
}

@SuppressLint("ClickableViewAccessibility")
fun View.touchDown(down: () -> Unit){
    setOnTouchListener { view, motionEvent ->
        when(motionEvent.action){
            MotionEvent.ACTION_DOWN -> {
                down()
            }
        }
        false
    }
}

@SuppressLint("ClickableViewAccessibility")
fun View.touchMove(move: () -> Unit){
    setOnTouchListener { view, motionEvent ->
        when(motionEvent.action){
            MotionEvent.ACTION_MOVE -> {
                move()
            }
        }
        false
    }
}


@SuppressLint("ClickableViewAccessibility")
fun View.doubleClick(dl:() -> Unit){

}