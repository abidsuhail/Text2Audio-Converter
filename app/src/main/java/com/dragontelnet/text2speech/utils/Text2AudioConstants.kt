package com.dragontelnet.text2speech.utils

class Text2AudioConstants {

    interface Voices {

        interface EnglishIndian {
            companion object {
                const val EKA_FEMALE: String = "Eka"
                const val JAI_FEMALE: String = "Jai"
                const val AJIT_MALE: String = "Ajit"
                val arrayOfEngIndVoices = arrayOf(EKA_FEMALE, JAI_FEMALE, AJIT_MALE)
            }
        }

        interface EnglishUS {
            companion object {
                const val LINDA_FEMALE: String = "Linda"
                const val AMY_FEMALE: String = "Amy"
                const val MARY_FEMALE: String = "Mary"
                const val JOHN_MALE: String = "John"
                const val MIKE_MALE: String = "Mike"

                val arrayOfEnUsVoices = arrayOf(
                    LINDA_FEMALE,
                    AMY_FEMALE,
                    MARY_FEMALE,
                    JOHN_MALE,
                    MIKE_MALE
                )

            }
        }
    }

    interface Languages {
        companion object {
            const val ENGLISH_US = "en-us"
            const val ENGLISH_INDIAN = "en-in"
            val arrayOfLanguages = arrayOf(
                ENGLISH_US,
                ENGLISH_INDIAN
            )
        }
    }
}