package com.example.conch

import android.app.Application
import android.content.Context


class MyApplication : Application() {

    companion object {
        var _context: Application? = null
        fun getContext(): Context {
            return _context!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        _context = this
    }


    /**
     * TODO Android部分
     *
     * TODO 歌单的CURD
     * TODO MainActivity中的控制器的实现
     * TODO 播放队列的更换
     * TODO 歌单播放
     * TODO 重构Track
     * TODO
     */

    /**
     * TODO 服务器部分
     *
     * TODO OSS文件的上传和下载
     * TODO
     */

}

private const val TAG = "MyApplication"
