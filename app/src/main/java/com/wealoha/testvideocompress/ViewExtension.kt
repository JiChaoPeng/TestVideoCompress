package com.wealoha.testvideocompress

import android.view.View


class SingleOnClickListener(private val listener: ((v: View?) -> Unit)?) : View.OnClickListener {

    private var lastTime: Long = 0L

    private val INTERVAL: Long = 300

    override fun onClick(v: View?) {
        val curTime = System.currentTimeMillis()
        if (curTime - lastTime > INTERVAL) {
            listener?.invoke(v)
            lastTime = curTime
        }
    }

}

class LongClickListener(private val listener: ((v: View?) -> Unit)?) : View.OnLongClickListener {
    override fun onLongClick(v: View?): Boolean {
        listener?.invoke(v)
        return true
    }
}

fun View.onSingleClick(listener: ((v: View?) -> Unit)?) {
    setOnClickListener(SingleOnClickListener(listener))
}

fun View.onLongClick(listener: ((v: View?) -> Unit)?) {
    setOnLongClickListener(LongClickListener(listener))
}
