package com.travelme.driver.activities.extensions

import android.view.View

fun View.visible(){
    visibility = View.VISIBLE
}

fun View.gone(){
    visibility = View.GONE
}

fun View.invisible(){
    visibility = View.INVISIBLE
}

fun View.notfocusable(){
    isFocusableInTouchMode = false
}