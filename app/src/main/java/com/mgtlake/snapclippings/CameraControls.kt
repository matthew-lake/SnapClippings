package com.mgtlake.snapclippings

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout

import com.wonderkiln.camerakit.CameraKit
import com.wonderkiln.camerakit.CameraKitEventCallback
import com.wonderkiln.camerakit.CameraKitImage
import com.wonderkiln.camerakit.CameraKitVideo
import com.wonderkiln.camerakit.CameraView
import com.wonderkiln.camerakit.OnCameraKitEvent

import java.io.File

import kotlinx.android.synthetic.main.camera_controls.view.*

class CameraControls @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private var cameraViewId = -1
    private var cameraView: CameraView? = null

    private var coverViewId = -1
    private var coverView: View? = null

    private var captureDownTime: Long = 0
    private var captureStartTime: Long = 0
    private var pendingVideoCapture: Boolean = false
    private var capturingVideo: Boolean = false

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.camera_controls, this)

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.CameraControls,
                    0, 0)

            try {
                cameraViewId = a.getResourceId(R.styleable.CameraControls_camera, -1)
                coverViewId = a.getResourceId(R.styleable.CameraControls_cover, -1)
            } finally {
                a.recycle()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (cameraViewId != -1) {
            val view = rootView.findViewById<View>(cameraViewId)
            if (view is CameraView) {
                cameraView = view
                cameraView!!.bindCameraKitListener(this)
            }
        }

        if (coverViewId != -1) {
            val view = rootView.findViewById<View>(coverViewId)
            if (view != null) {
                coverView = view
                coverView!!.visibility = View.GONE
            }
        }
    }

    @OnCameraKitEvent(CameraKitImage::class)
    fun imageCaptured(image: CameraKitImage) {
        val jpeg = image.jpeg

        val callbackTime = System.currentTimeMillis()
        ResultHolder.dispose()
        ResultHolder.image = jpeg
        ResultHolder.nativeCaptureSize = cameraView!!.captureSize
        ResultHolder.timeToCallback = callbackTime - captureStartTime
        //val intent = Intent(context, PreviewActivity::class.java)
        //context.startActivity(intent)
    }

    @OnCameraKitEvent(CameraKitVideo::class)
    fun videoCaptured(video: CameraKitVideo) {
        val videoFile = video.videoFile
        if (videoFile != null) {
            ResultHolder.dispose()
            ResultHolder.video = videoFile
            ResultHolder.nativeCaptureSize = cameraView!!.captureSize
            //val intent = Intent(context, PreviewActivity::class.java)
            //context.startActivity(intent)
        }
    }

    //@OnTouch(R.id.captureButton)
    fun onTouchCapture(view: View, motionEvent: MotionEvent): Boolean {
        handleViewTouchFeedback(view, motionEvent)
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                captureDownTime = System.currentTimeMillis()
                pendingVideoCapture = true
                postDelayed({
                    if (pendingVideoCapture) {
                        capturingVideo = true
                        cameraView!!.captureVideo()
                    }
                }, 250)
            }

            MotionEvent.ACTION_UP -> {
                pendingVideoCapture = false

                if (capturingVideo) {
                    capturingVideo = false
                    cameraView!!.stopVideo()
                } else {
                    captureStartTime = System.currentTimeMillis()
                    cameraView!!.captureImage { event -> imageCaptured(event) }
                }
            }
        }
        return true
    }

    internal fun handleViewTouchFeedback(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownAnimation(view)
                return true
            }

            MotionEvent.ACTION_UP -> {
                touchUpAnimation(view)
                return true
            }

            else -> {
                return true
            }
        }
    }

    internal fun touchDownAnimation(view: View) {
        view.animate()
                .scaleX(0.88f)
                .scaleY(0.88f)
                .setDuration(300)
                .setInterpolator(OvershootInterpolator())
                .start()
    }

    internal fun touchUpAnimation(view: View) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(OvershootInterpolator())
                .start()
    }

    internal fun changeViewImageResource(imageView: ImageView, @DrawableRes resId: Int) {
        imageView.rotation = 0f
        imageView.animate()
                .rotationBy(360f)
                .setDuration(400)
                .setInterpolator(OvershootInterpolator())
                .start()

        imageView.postDelayed({ imageView.setImageResource(resId) }, 120)
    }

}
