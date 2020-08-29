package com.dragontelnet.text2speech.repository

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dragontelnet.text2speech.BuildConfig
import com.dragontelnet.text2speech.api.Text2AudioApi
import com.dragontelnet.text2speech.retrofit.RetrofitService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class MainActivityRepository {

    private var mp: MediaPlayer? = null
    private val TAG = "Text2AudioRepository"
    fun getResult(fieldMap: Map<String, String>): LiveData<String> {
        val liveData = MutableLiveData<String>()
        val api: Text2AudioApi = RetrofitService.getInstance()
        api.get(fieldMap, BuildConfig.TEXT_TO_AUDIO_API_KEY)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    liveData.value = t.message
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            Thread()
                            {
                                liveData.postValue(writeResponseBodyToDisk(it))
                            }.start()
                        } ?: run { liveData.value = response.message() }
                        return
                    }
                    liveData.value = response.message()
                }
            })
        return liveData
    }

    private fun writeResponseBodyToDisk(body: ResponseBody): String {
        return try {
            val folder =
                File(Environment.getExternalStorageDirectory().toString() + "/T2A")
            if (!folder.exists()) {
                folder.mkdir()
            }
            val futureStudioIconFile = File(folder, "t2a.mp3")
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(body.contentLength().toInt())
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                }
                outputStream.flush()
                "File saved to ext dir T2A/t2a.mp3"
            } catch (e: IOException) {
                //error in saving file
                e.message.toString()
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            //"Error in saving file!!!"
            e.message.toString()
        }
    }

    fun playAudio(): MutableLiveData<Boolean> {
        val playAudioLiveData = MutableLiveData<Boolean>()
        val file = File(Environment.getExternalStorageDirectory().toString() + "/T2A/t2a.mp3")
        mp = MediaPlayer()
        mp?.apply {
            //playing
            setOnCompletionListener {
                playAudioLiveData.value = true
                stopPlaying()
            }
            try {
                setDataSource(file.toString())
                prepare()
                start()
            } catch (e: Exception) {
                //Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        return playAudioLiveData
    }

    fun stopPlaying() {
        mp?.apply {
            stop()
            release()
        }
        mp = null
    }

    fun shareFile(): MutableLiveData<Intent?> {
        val intentLiveData = MutableLiveData<Intent?>()
        //choosing app to send audio file
        val fileWithinMyDir = File(
            Environment.getExternalStorageDirectory().toString() + "/T2A/t2a.mp3"
        )
        val intentShareFile = Intent(Intent.ACTION_SEND)
        if (fileWithinMyDir.exists()) {
            intentLiveData.value = intentShareFile.apply {
                type = "audio/mpeg"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(fileWithinMyDir.toString()))
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Sharing Audio File..."
                )
                putExtra(Intent.EXTRA_TEXT, "Sharing Audio File...")
            }
        } else {
            intentLiveData.value = null
        }
        return intentLiveData
    }

}