package com.dragontelnet.text2speech.viewmodel

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dragontelnet.text2speech.repository.MainActivityRepository

class MainActivityViewModel : ViewModel() {
    private val repo = MainActivityRepository()

    fun getResult(fieldMap: Map<String, String>): LiveData<String> = repo.getResult(fieldMap)

    fun playAudio(): LiveData<Boolean> = repo.playAudio()

    fun stopPlaying() = repo.stopPlaying()

    fun shareFile(): LiveData<Intent?> = repo.shareFile()

}