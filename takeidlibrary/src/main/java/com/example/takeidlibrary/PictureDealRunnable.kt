package com.example.takeidlibrary

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Handler
import android.os.Message
import com.dex.toolslibrary.scaleBetween
import com.dex.toolslibrary.toGray
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

//处理图片
class PictureDealRunnable(val handler: Handler, val data: ByteArray, private val folderPath: String) : Runnable {
    var tessTwo: TessBaseAPI = TessBaseAPI()

    init {
        tessTwo.init(Environment.getExternalStorageDirectory().path + File.separator, "chi_sim")
    }

    override fun run() {
        val originalPicFile = File(folderPath, "original.png")
        val cutPicFile = File(folderPath, "cuted.png")
        val os = FileOutputStream(originalPicFile)
        os.write(data)
        os.close()
        handler.sendMessage(Message().apply {
            what = TakeIdConst.ORIGINAL_PIC_TAG
            obj = TakeIDRes(originalPicFile.path, "", TakeIdConst.ORIGINAL_PIC_PATH)
        })
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        val bitmapSize = arrayOf(bitmap.width, bitmap.height)
        val toolSize = bitmapSize[0] * TakeIdConst.ICON_RATE
        val cuterW = (bitmapSize[0].toDouble() * TakeIdConst.SURFACE_RATE).toInt()
        val cuterH = bitmapSize[1]
        val borderSize = TakeIdConst.ID_CARD_RATE.scaleBetween(cuterW, cuterH)
        val top = (bitmapSize[1] - borderSize[1]).toDouble() / (2.toDouble())

        val cutBitmap = Bitmap.createBitmap(bitmap, toolSize.toInt(), top.toInt(), borderSize[0], borderSize[1])
        val bos =
            BufferedOutputStream(FileOutputStream(cutPicFile))
        cutBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        handler.sendMessage(Message().apply {
            what = TakeIdConst.CUT_PIC_TAG
            obj = TakeIDRes(cutPicFile.path, "", TakeIdConst.CUT_PIC_PATH)
        })


        /////////////裁剪信息///////////////
        //name
        handler.sendMessage(Message().apply {
            what = TakeIdConst.NAME_PIC_TAG
            obj =
                cutBitmapAndToGray(
                    cutBitmap,
                    TakeIdConst.id_name_gender_year_address_left,
                    TakeIdConst.id_name_top,
                    TakeIdConst.id_name_width,
                    TakeIdConst.id_height_except_address,
                    File(folderPath, "cut_name_pic.jpg"),
                    TakeIdConst.NAME_PIC_PATH,""
                )
        })
        //gender
        handler.sendMessage(Message().apply {
            what = TakeIdConst.GENDER_PIC_TAG
            obj = cutBitmapAndToGray(
                cutBitmap,
                TakeIdConst.id_name_gender_year_address_left,
                TakeIdConst.id_gender_nation_top,
                TakeIdConst.id_gender_width,
                TakeIdConst.id_height_except_address, File(folderPath, "cut_gender_pic.jpg"),
                TakeIdConst.GENDER_PIC_PATH,
                "男女"
            )
        })
        //nation
        handler.sendMessage(Message().apply {
            what = TakeIdConst.NATION_PIC_TAG
            obj = cutBitmapAndToGray(
                cutBitmap, TakeIdConst.id_nation_left, TakeIdConst.id_gender_nation_top,
                TakeIdConst.id_nation_width, TakeIdConst.id_height_except_address
                , File(folderPath, "cut_nation_pic.jpg"),
                TakeIdConst.NAME_PIC_PATH,
                ""
            )
        })
        //year
        handler.sendMessage(Message().apply {
            what = TakeIdConst.YEAR_PIC_TAG
            obj = cutBitmapAndToGray(
                cutBitmap,
                TakeIdConst.id_name_gender_year_address_left,
                TakeIdConst.id_birthday_year_month_day_top,
                TakeIdConst.id_year_width,
                TakeIdConst.id_height_except_address
                , File(folderPath, "cut_year_pic.jpg"),
                TakeIdConst.YEAR_PIC_PATH,
                "0123456789"
            )
        })
        //month
        handler.sendMessage(Message().apply {
            what = TakeIdConst.MONTH_PIC_TAG
            obj = cutBitmapAndToGray(
                cutBitmap, TakeIdConst.id_month_left, TakeIdConst.id_birthday_year_month_day_top,
                TakeIdConst.id_month_width, TakeIdConst.id_height_except_address
                , File(folderPath, "cut_month_pic.jpg"),
                TakeIdConst.MONTH_PIC_PATH,
                "0123456789"
            )
        })
        //day
        handler.sendMessage(Message().apply {
            what = TakeIdConst.DAY_PIC_TAG
            obj = cutBitmapAndToGray(
                cutBitmap, TakeIdConst.id_day_left, TakeIdConst.id_birthday_year_month_day_top,
                TakeIdConst.id_day_width, TakeIdConst.id_height_except_address
                , File(folderPath, "cut_day_pic.jpg"),
                TakeIdConst.DAY_PIC_PATH,
                "0123456789"
            )
        })
        //address
        handler.sendMessage(Message().apply {
            what = TakeIdConst.ADDRESS_PIC_TAG
            obj = cutBitmapAndToGray(
                cutBitmap, TakeIdConst.id_name_gender_year_address_left, TakeIdConst.id_address_top,
                TakeIdConst.id_address_width, TakeIdConst.id_address_height
                , File(folderPath, "cut_address_pic.jpg"),
                TakeIdConst.ADDRESS_PIC_PATH,
                ""
            )
        })
        //no
        handler.sendMessage(Message().apply {
            what = TakeIdConst.NO_PIC_TAG
            obj = cutBitmapAndToGray(
                cutBitmap,
                TakeIdConst.id_no_left,
                TakeIdConst.id_no_top,
                TakeIdConst.id_no_width,
                TakeIdConst.id_height_except_address
                , File(folderPath, "cut_no_pic.jpg"),
                TakeIdConst.NO_PIC_PATH,
                "0123456789xX"
            )
        })


        /////////////裁剪信息///////////////

    }

    //裁剪,识别
    private fun cutBitmapAndToGray(
        bitmap: Bitmap,
        leftRate: Double,
        topRate: Double,
        widthRate: Double,
        heightRate: Double,
        file: File,
        picExtraName: String,
        whiteList:String
    ): TakeIDRes {

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        val left: Int = (bitmapWidth.toDouble() * leftRate).toInt()
        val top: Int = (bitmapHeight.toDouble() * topRate).toInt()
        val width = (bitmapWidth.toDouble() * widthRate).toInt()
        val height = (bitmapHeight.toDouble() * heightRate).toInt()
        val resBitmapOriginal= Bitmap.createBitmap(bitmap, left, top, width, height)
        val resBitmap = resBitmapOriginal.toGray()

//            .toGray()
        resBitmap.compress(Bitmap.CompressFormat.JPEG, 100, BufferedOutputStream(FileOutputStream(file)))
//        val text = TessInstance.setImage(resBitmap)
        tessTwo.setVariable("tessedit_char_whitelist", whiteList)
        tessTwo.setImage(resBitmap)
        val text = tessTwo.utF8Text
        return TakeIDRes(file.path, text.replace("\n", "").replace(" ", ""), picExtraName)

    }


}