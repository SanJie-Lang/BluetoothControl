package com.cbsd.libra.model

import java.io.Serializable

data class Position(var translationX: Float, var translationY: Float, var scale: Float, var width: Int, var height: Int):
    Serializable