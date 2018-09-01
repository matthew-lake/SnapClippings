package com.mgtlake.snapclippings

import com.wonderkiln.camerakit.Size

import java.io.File

object ResultHolder {

    var image: ByteArray? = null
    var video: File? = null
    var nativeCaptureSize: Size? = null
    var timeToCallback: Long = 0

    fun dispose() {
        image = null
        nativeCaptureSize = null
        timeToCallback = 0
    }

}
