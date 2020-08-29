package com.dragontelnet.text2speech.utils

import com.dragontelnet.text2speech.utils.Text2AudioConstants.Voices.EnglishIndian.Companion.EKA_FEMALE

class Text2AudioMapBuilder private constructor() {
    companion object {
        class Builder {
            //default values
            private var fileExtensionType = "mp3"
            private var r = "0"
            private var languageCode = "en-in"
            private var frequency = "8khz_8bit_mono"
            private var peopleVoiceName = EKA_FEMALE
            private var text = "Hello World"
            fun addLanguage(langCode: String): Builder {
                this.languageCode = langCode
                return this
            }

            fun addVoice(voiceName: String): Builder {
                this.peopleVoiceName = voiceName
                return this
            }

            fun addText(text: String): Builder {
                this.text = text
                return this
            }

            fun build(): Map<String, String> {
                val map = HashMap<String, String>()
                map["c"] = fileExtensionType
                map["r"] = r
                map["hl"] = languageCode
                map["f"] = frequency
                map["v"] = peopleVoiceName
                map["src"] = text
                return map
            }
        }
    }
}