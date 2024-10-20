package com.example.translationai

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("result_table")
data class ResultOfTranslate(
    @PrimaryKey(true)
    var id: Long = 0L,
    @ColumnInfo("targetAbbrCode")
    var targetAbbrCode: String = "",
    @ColumnInfo("currentText")
    var currentText: String = "",
    @ColumnInfo("translatedText")
    var translateText: String = ""
)
