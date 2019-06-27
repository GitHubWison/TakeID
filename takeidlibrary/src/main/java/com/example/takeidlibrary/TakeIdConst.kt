package com.example.takeidlibrary

import android.Manifest

class TakeIdConst {
    companion object {
        //        action
        const val ACTION = "com.dex.takeid.result"
        const val TAKEID_EXTRA_NAME = "TAKEID_EXTRA_NAME"

        //        工具栏显示的比例
        const val ICON_RATE = 1.toDouble() / 6.toDouble()
        //        拍照框长度显示的最大比例
        const val SURFACE_RATE = 1.toDouble() - 2.toDouble() * ICON_RATE

        const val ORIGINAL_PIC_PATH: String = "ORIGINAL_PIC_PATH"
        const val CUT_PIC_PATH: String = "CUT_PIC_PATH"
        const val NAME_PIC_PATH: String = "NAME_PIC_PATH"
        const val GENDER_PIC_PATH: String = "GENDER_PIC_PATH"
        const val NATION_PIC_PATH: String = "NATION_PIC_PATH"
        const val YEAR_PIC_PATH: String = "YEAR_PIC_PATH"
        const val MONTH_PIC_PATH: String = "MONTH_PIC_PATH"
        const val DAY_PIC_PATH: String = "DAY_PIC_PATH"
        const val ADDRESS_PIC_PATH: String = "ADDRESS_PIC_PATH"
        const val NO_PIC_PATH: String = "NO_PIC_PATH"

        const val ORIGINAL_PIC_TAG: Int = 999
        const val CUT_PIC_TAG: Int = 888

        const val NAME_PIC_TAG: Int = 0
        const val GENDER_PIC_TAG: Int = 1
        const val NATION_PIC_TAG: Int = 2
        const val YEAR_PIC_TAG: Int = 3
        const val MONTH_PIC_TAG: Int = 4
        const val DAY_PIC_TAG: Int = 5
        const val ADDRESS_PIC_TAG: Int = 6
        const val NO_PIC_TAG: Int = 7

//         const val RESULT_CODE = 1001

        //////////////////left//////////////////////////////
        const val id_info_left = 0.17//****************
        const val id_name_gender_year_address_left = id_info_left
        const val id_nation_left = 0.37
        const val id_month_left = 0.33
        const val id_day_left = 0.42
        const val id_no_left = 0.31
        //////////////////left-end//////////////////////////////

        //////////////////top//////////////////////////////
        const val id_height_except_address = 0.13//*********
        //        姓名
        const val id_name_top = 0.10//********************
        //        性别/ 民族
        const val id_gender_nation_top = id_name_top + id_height_except_address
        //       出生年/月/日
        const val id_birthday_year_month_day_top = id_name_top + 2.toDouble() * id_height_except_address
        //        地址
        const val id_address_top = id_name_top + 3.toDouble() * id_height_except_address
        //        身份证号
        const val id_no_top = 0.79
        //////////////////top-end//////////////////////////////

        //////////////////width//////////////////////////////
        const val id_name_width = 0.18//************************
        const val id_gender_width = 0.06
        const val id_nation_width = 0.05
        const val id_year_width = 0.11
        const val id_month_width = 0.04
        const val id_day_width = 0.04
        const val id_address_width = 0.42
        const val id_no_width = 0.56
        //////////////////width-end//////////////////////////////

        //////////////////height-end//////////////////////////////
        const val id_address_height = 0.21
        //////////////////height-end//////////////////////////////
//        身份证的宽/长比
        const val ID_CARD_RATE = 0.63

        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )


    }
}