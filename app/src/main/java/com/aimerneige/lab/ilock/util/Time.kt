package com.aimerneige.lab.ilock.util

import java.text.SimpleDateFormat
import java.util.*


/**
 * 一些用来获取时间信息的函数
 */

fun getDate(): String {
    val sdf = SimpleDateFormat("yyyy/M/dd")
    return sdf.format(Date())
}

fun getTime(): String {
    val sdf = SimpleDateFormat("hh:mm:ss")
    return sdf.format(Date())
}

fun getSystemTime(): Long {
    return System.currentTimeMillis()
}

fun getHour24(): Int {
    val sdf = SimpleDateFormat("HH")
    return sdf.format(Date()).toInt()
}

fun getHour12(): Int {
    val sdf = SimpleDateFormat("hh")
    return sdf.format(Date()).toInt()
}

fun getMinute(): Int {
    val sdf = SimpleDateFormat("mm")
    return sdf.format(Date()).toInt()
}

fun getSecond(): Int {
    val sdf = SimpleDateFormat("ss")
    return sdf.format(Date()).toInt()
}
