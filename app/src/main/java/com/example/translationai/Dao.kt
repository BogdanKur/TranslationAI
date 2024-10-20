package com.example.translationai

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface Dao {
    @Update
    suspend fun update(resultOfTranslate: ResultOfTranslate)
    @Delete
    suspend fun delete(resultOfTranslate: ResultOfTranslate)
    @Insert
    suspend fun insert(resultOfTranslate: ResultOfTranslate)

    @Query("SELECT * FROM result_table WHERE id = :id")
    fun get(id: Long):LiveData<ResultOfTranslate>
    @Query("SELECT * FROM result_table ORDER BY id DESC")
    fun getAll():LiveData<List<ResultOfTranslate>>
}