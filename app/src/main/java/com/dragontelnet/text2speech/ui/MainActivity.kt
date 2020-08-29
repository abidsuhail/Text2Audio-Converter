package com.dragontelnet.text2speech.ui

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dragontelnet.text2speech.R
import com.dragontelnet.text2speech.databinding.ActivityMainBinding
import com.dragontelnet.text2speech.databinding.DownloadedAudioLayoutBinding
import com.dragontelnet.text2speech.utils.Text2AudioConstants.Languages.Companion.ENGLISH_INDIAN
import com.dragontelnet.text2speech.utils.Text2AudioConstants.Languages.Companion.arrayOfLanguages
import com.dragontelnet.text2speech.utils.Text2AudioConstants.Voices.EnglishIndian.Companion.arrayOfEngIndVoices
import com.dragontelnet.text2speech.utils.Text2AudioConstants.Voices.EnglishUS.Companion.arrayOfEnUsVoices
import com.dragontelnet.text2speech.utils.Text2AudioMapBuilder
import com.dragontelnet.text2speech.viewmodel.MainActivityViewModel
import com.github.kayvannj.permission_utils.Func
import com.github.kayvannj.permission_utils.PermissionUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds


class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_STORAGE: Int = 123
    private lateinit var mRequestObject: PermissionUtil.PermissionRequestObject
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var mViewModel: MainActivityViewModel
    private lateinit var selectedVoice: String
    private lateinit var selectedLanguage: String
    private lateinit var voicesAdapter: ArrayAdapter<String>
    private lateinit var languageAdapter: ArrayAdapter<String>
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityMainBinding
    private val MAX_CHAR_IN_SENTENCE = 1000000

    private var mp: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        title = "T2A Converter"
        initAds()
        initInitialSpinnerValues()
        initSpinnerAdapterStuffs()
        initCharTextChangeListener()
        initProgressDialog()
        mViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        //on click download audio button
        binding.downloadAudioBtn.setOnClickListener {
            askPerm()  //ASKING EXT READ AND WRITE PERMISSION
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        mRequestObject.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private fun askPerm() {
        mRequestObject = PermissionUtil.with(this).request(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).onAllGranted(
            object : Func() {
                override fun call() {
                    //Happy Path
                    makeApiCall()
                }
            }).onAnyDenied(
            object : Func() {
                override fun call() {
                    //Sad Path
                    Toast.makeText(
                        this@MainActivity,
                        "Please allow the permissions to save converted audio in storage",
                        Toast.LENGTH_LONG
                    ).show()

                }
            })
            .ask(REQUEST_CODE_STORAGE) // REQUEST_CODE_STORAGE is what ever int you want (should be distinct)

    }

    private fun makeApiCall() {
        binding.speechTxtEt.text.toString().apply {
            if (isNotBlank()) {
                progressDialog.show()
                //making api call with text , voice name and t2s api key
                mViewModel.getResult(buildFieldMap(this))
                    .observe(this@MainActivity, Observer { msg ->
                        progressDialog.dismiss()
                        mInterstitialAd.loadAd(AdRequest.Builder().build())  //loading ad
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                        showCustomDialog()

                    })
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Please enter the sentence!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun showCustomDialog() {

        val downloadAudioBinding = DownloadedAudioLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
            .setTitle("Converted to Audio")
            .setMessage("File Converting success")
            .setPositiveButton("CLOSE") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
        builder.setView(downloadAudioBinding.root)
        builder.create().show()

        //play audio button clicked
        downloadAudioBinding.apply {
            playBtn.setOnClickListener {
                playAudio(playBtn)
            }
            //share button click
            shareAudioBtn.setOnClickListener {
                shareFile()
            }
        }
    }

    private fun shareFile() {
        //choosing app to send audio file
        mViewModel.shareFile().observe(this, Observer { intent ->
            intent?.let {
                startActivity(Intent.createChooser(intent, "Share this audio File"))
            } ?: Toast.makeText(this, "Unknown Error!!!", Toast.LENGTH_SHORT).show()
        })
    }

    private fun playAudio(playButton: ImageButton) {

        if (playButton.tag == "play") {
            //playing started
            playButton.tag = "stop"
            playButton.setImageResource(R.drawable.ic_stop)
            mViewModel.playAudio().observe(this, Observer { isCompleted ->
                if (isCompleted) {

                    //playing completed
                    playButton.setImageResource(R.drawable.ic_play)
                    playButton.tag = "play"
                }
            })
        } else {
            //already playing,now stopping
            mViewModel.stopPlaying()
            playButton.setImageResource(R.drawable.ic_play)
            playButton.tag = "play"
        }

    }

    private fun initProgressDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Converting online please wait");
        progressDialog.setCanceledOnTouchOutside(false)
    }

    private fun initCharTextChangeListener() {
        binding.speechTxtEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                newText?.let {
                    binding.charCounterTv.text = (MAX_CHAR_IN_SENTENCE - it.length).toString()
                    if (it.length >= MAX_CHAR_IN_SENTENCE) {
                        Toast.makeText(
                            this@MainActivity,
                            "Sentence size reached!!!!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

    }

    private fun initArrayAdapterOfVoices(voicesList: Array<String>) {
        voicesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, voicesList)
        voicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.voiceNameSpinner.adapter = voicesAdapter
    }

    private fun initArrayAdapterOfLanguages(languagesList: Array<String>) {
        languageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languagesList)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinner.adapter = languageAdapter
    }

    private fun initSpinnerAdapterStuffs() {
        //setting spinner layout and listener
        initArrayAdapterOfVoices(arrayOfEngIndVoices)
        initArrayAdapterOfLanguages(arrayOfLanguages)

        //<--------------------------------VOICE NAME SPINNER------------------------------>
        initVoiceSpinnerListener()

        //<------------------------------LANGUAGE SPINNER ------------------------------------->
        initLanguageSpinnerListener()

    }

    private fun initLanguageSpinnerListener() {
        binding.languageSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedLanguage = arrayOfLanguages[position]
                    if (selectedLanguage == ENGLISH_INDIAN) {
                        //ENG INDIAN SELECTED
                        initArrayAdapterOfVoices(arrayOfEngIndVoices)
                    } else {
                        //ENG US SELECTED
                        initArrayAdapterOfVoices(arrayOfEnUsVoices)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

    }

    private fun initVoiceSpinnerListener() {
        binding.voiceNameSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedVoice = if (selectedLanguage == ENGLISH_INDIAN) {
                        arrayOfEngIndVoices[position]
                    } else {
                        arrayOfEnUsVoices[position]
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

    }

    private fun initInitialSpinnerValues() {
        // default selected voice and language is at pos 0
        selectedLanguage = arrayOfLanguages[0]
        selectedVoice = arrayOfEngIndVoices[0]
    }

    private fun initAds() {

        MobileAds.initialize(this) {
        }

        //<------------------Banner Ad----------------------------------------->
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest) //loading banner ad

        //<-------------------Interstitial ad----------------------------------->
        mInterstitialAd = InterstitialAd(this) //init Interstitial ad
        mInterstitialAd.adUnitId = resources.getString(R.string.test_inters_ad_unit) //ad unit id
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                //ad loaded now show it
                mInterstitialAd.show()
            }
        }
    }


    private fun buildFieldMap(text: String): Map<String, String> {
        return Text2AudioMapBuilder.Companion
            .Builder()
            .addText(text)
            .addLanguage(selectedLanguage)
            .addVoice(selectedVoice)
            .build()
    }
}