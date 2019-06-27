package com.example.takeidlibrary

import java.io.Serializable

data class TakeIDRes(
    var path: String?,
    var txt: String?,
    var picExtraName: String?
) : Serializable