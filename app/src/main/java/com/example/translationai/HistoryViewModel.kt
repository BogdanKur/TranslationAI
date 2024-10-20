package com.example.translationai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HistoryViewModel(val dao: Dao): ViewModel() {
    val listOfResults = dao.getAll()

    fun insertNew(resultOfTranslate: ResultOfTranslate) {
        viewModelScope.launch {
            dao.insert(resultOfTranslate)
        }
    }
}