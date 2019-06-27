package com.example.takeidlibrary

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import com.dex.toolslibrary.copyAssetToSDFileByCheck
import com.dex.toolslibrary.findBestSize
import com.dex.toolslibrary.findScreenSize
import com.dex.toolslibrary.scaleBetween
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.Serializable
import java.util.concurrent.Executors


class TakeIDV2Activity : Activity(), SurfaceHolder.Callback, EasyPermissions.PermissionCallbacks {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent().apply {
                setClass(context, TakeIDV2Activity::class.java)
            })
        }
    }

    private var camera: Camera? = null
    private val singleExecutor = Executors.newSingleThreadExecutor()
    private var isDataInited: Boolean = false

    private fun requestPermission() {
        EasyPermissions.requestPermissions(this, "", 11, *TakeIdConst.permissions)
    }

    private fun createActivity() {
        initData()
        setContentView(ConstraintLayout(this).apply {
            val surfaceView = generateSurfaceView()
            val lightButton = generateLightPictureButton()
            val takePicButton = generateTakePictureButton()
            val borderView = generateBorderView()
            val drawSurfaceHandler = Handler {
                if (it.what == 0) {
                    @Suppress("UNCHECKED_CAST") val allSize = it.obj as Map<String, *>
                    @Suppress("UNCHECKED_CAST") val surfaceViewSize = allSize["surfaceViewSize"] as Array<Int>
                    val toolSize = (allSize["toolSize"] as Double).toInt()
                    @Suppress("UNCHECKED_CAST") val borderSize = allSize["borderSize"] as Array<Int>
                    addView(surfaceView, ConstraintLayout.LayoutParams(surfaceViewSize[0], surfaceViewSize[1]))
                    addView(lightButton, ConstraintLayout.LayoutParams(toolSize, toolSize).apply {
                        leftToLeft = surfaceView.id
                        topToTop = surfaceView.id
                        bottomToBottom = surfaceView.id
                    })
                    addView(takePicButton, ConstraintLayout.LayoutParams(toolSize, toolSize).apply {
                        rightToRight = surfaceView.id
                        topToTop = surfaceView.id
                        bottomToBottom = surfaceView.id
                    })
                    addView(borderView, ConstraintLayout.LayoutParams(borderSize[0], borderSize[1]).apply {
                        leftToRight = lightButton.id
                        topToTop = surfaceView.id
                        rightToLeft = takePicButton.id
                        bottomToBottom = surfaceView.id
                    })
                }
                true
            }
            singleExecutor.execute {
                //                计算布局等的各个参数
                val bestSize = generateCamera().parameters.supportedPreviewSizes.findBestSize()
                val screenSize = this@TakeIDV2Activity.findScreenSize()
                val toolSize = screenSize[0] * TakeIdConst.ICON_RATE
                val surfaceViewSize =
                    (bestSize[1].toDouble() / bestSize[0].toDouble()).scaleBetween(screenSize[0], screenSize[1])
                val outerW = (surfaceViewSize[0].toDouble() * TakeIdConst.SURFACE_RATE).toInt()
                val outerH = surfaceViewSize[1]
                val borderSize = 0.63.scaleBetween(outerW, outerH)
                drawSurfaceHandler.sendMessage(Message().apply {
                    what = 0
                    obj = mapOf(
                        "surfaceViewSize" to surfaceViewSize,
                        "toolSize" to toolSize,
                        "borderSize" to borderSize
                    )
                })
            }


        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        setContentView(LinearLayout(this))
        requestPermission()


    }

    private val initDataHandler = Handler { msg ->
        isDataInited = (msg.obj as Boolean)
        Toast.makeText(this@TakeIDV2Activity, "数据同步完成", Toast.LENGTH_SHORT).show()
        true
    }

    //    初始化数据,tesstwo数据复制到sd卡中
    private fun initData() {
        singleExecutor.execute {
            this.copyAssetToSDFileByCheck(
                "dic/chi_sim.traineddata",
                File("${Environment.getExternalStorageDirectory().path}${File.separator}tessdata"),
                "chi_sim.traineddata"
            )
            initDataHandler.sendMessage(Message().apply { obj = true })
        }

    }


    private val takePhotoHandler = Handler {
        if (it.what != TakeIdConst.ORIGINAL_PIC_TAG && it.what != TakeIdConst.CUT_PIC_TAG) {
            val takeIDRes = it.obj as TakeIDRes
            sendBroadcast(Intent().apply {
                action = TakeIdConst.ACTION
                putExtra(TakeIdConst.TAKEID_EXTRA_NAME, takeIDRes)
            })
        }

        true
    }

    //拍照按钮
    private fun generateTakePictureButton(): Button = Button(this).apply {
        this.text = "拍照"
        id = View.generateViewId()
        setOnClickListener {
            if (!isDataInited) {
                Toast.makeText(this@TakeIDV2Activity, "数据同步中...请稍后再试", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //            生成照片
            generateCamera().takePicture(null, null, null) { data, _ ->
                //                generatePic(data)
                finish()
                singleExecutor.execute(
                    PictureDealRunnable(
                        takePhotoHandler,
                        data,
                        this@TakeIDV2Activity.externalCacheDir?.path ?: ""
                    )
                )
//                finish()
            }
        }
    }

    //身份证的对准框
    private fun generateBorderView(): View = View(this).apply {
        background = GradientDrawable().apply {
            setStroke(2, Color.parseColor("#DC143C"))
            setColor(Color.TRANSPARENT)
        }
    }


    //打开灯
    private fun generateLightPictureButton(): Button = Button(this).apply {
        this.text = "手电"
        id = View.generateViewId()
        setOnClickListener { switchFlashLight() }
    }

    //摄像头初始化
    private fun generateCamera(): Camera {
        if (camera == null) {
            camera = Camera.open(0)
            val tempParameters = camera?.parameters
//            调节曝光度
//            tempParameters?.exposureCompensation = -4
            val bestSize = tempParameters?.supportedPreviewSizes?.findBestSize()
            tempParameters?.setPreviewSize(bestSize!![0], bestSize[1])
            tempParameters?.setPictureSize(bestSize!![0], bestSize[1])
            camera?.parameters = tempParameters
        }

        return camera!!
    }

    //拍照预览初始化
    private fun generateSurfaceView(): SurfaceView {
        return SurfaceView(this).apply {
            this.holder.addCallback(this@TakeIDV2Activity)
            this.id = View.generateViewId()
            setOnClickListener {
                generateCamera().autoFocus(null)
            }
        }
    }

    //    申请权限后的回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }

    //        拒绝的权限
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {

    }

    //        同意的权限
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (perms.size != TakeIdConst.permissions.size) {
            finish()
        } else {
            createActivity()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        generateCamera().apply {
            setPreviewDisplay(holder)
            startPreview()
        }
    }

    override fun finish() {
        super.finish()
        try {
            generateCamera().apply {
                stopPreview()
                release()
            }
            camera = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 开关闪光灯
     *
     * @return 闪光灯是否开启
     */
    private fun switchFlashLight(): Boolean {
        if (camera != null) {
            val parameters = camera?.parameters
            return if (parameters?.flashMode == Camera.Parameters.FLASH_MODE_OFF) {
                parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                camera?.parameters = parameters
                true
            } else {
                parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
                camera?.parameters = parameters
                false
            }
        }
        return false
    }


}






//class TessReceiverBroadCast : BroadcastReceiver() {
//    override fun onReceive(context: Context?, intent: Intent?) {
//        val extra = intent?.getSerializableExtra(TakeIdConst.TAKEID_EXTRA_NAME) as TakeIDRes
//        val content = "${extra.picExtraName}==${extra.txt}==${extra.path}"
//        Toast.makeText(context, content, Toast.LENGTH_LONG).show()
//    }
//
//}
