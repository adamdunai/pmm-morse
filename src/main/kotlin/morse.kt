import extension.nextIsNotBlank
import java.io.ByteArrayInputStream
import java.io.SequenceInputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import kotlin.system.exitProcess

// https://hu.wikipedia.org/wiki/Morzekód#Időz%C3%ADtés
private const val DOT_MARK_MULTIPLIER = 1
private const val DASH_MARK_MULTIPLIER = 3
private const val INTER_ELEMENT_GAP_MULTIPLIER = 1
private const val SHORT_GAP_MULTIPLIER = 3
private const val MEDIUM_GAP_MULTIPLIER = 7

/**
 * A kapott szöveget morzekóddá alakítja, amiből a megfelelő időzítéssel (jelköz, betűköz és szóköz)
 * AudioInputStream-eket állít elő, végül pedig egy AudioInputStream-et ad vissza
 */
fun getMorseSound(dotDurationInMs: Int, message: String): AudioInputStream {
    val soundList = mutableListOf<AudioInputStream>()
    val morseCodeList = convertToMorseCodeList(message)

    morseCodeList.forEachIndexed { morseCodeIndex, morseCode ->
        morseCode.forEachIndexed { charIndex, char ->
            soundList.add(
                createSingleMarkSound(
                    getMarkDuration(char, dotDurationInMs),
                    morseCode.isBlank()
                )
            )

            if (!morseCode.isBlank() && charIndex != morseCode.lastIndex) {
                soundList.add(
                    createSingleMarkSound(
                        INTER_ELEMENT_GAP_MULTIPLIER * dotDurationInMs,
                        true
                    )
                )
            }
        }

        if (!morseCode.isBlank() && morseCodeList.nextIsNotBlank(morseCodeIndex)) {
            soundList.add(
                createSingleMarkSound(
                    SHORT_GAP_MULTIPLIER * dotDurationInMs,
                    true
                )
            )
        }
    }

    return getMergedSounds(soundList)
}

/**
 * A kapott szöveget morzekóddá alakítja, amiket egy listában ad vissza
 */
private fun convertToMorseCodeList(message: String): List<String> {
    return message.map {
        if (!morseCodes.containsKey(it.toLowerCase())) {
            println("Ismeretlen karakter található az üzenetben: $it")
            exitProcess(1)
        }

        morseCodes[it.toLowerCase()]!!
    }
}

/**
 * Visszaadja a pont, vonal vagy szóköz hosszúságát a pont hossza alapján
 */
private fun getMarkDuration(mark: Char, dotDurationInMs: Int): Int =
    when (mark) {
        '.' -> DOT_MARK_MULTIPLIER * dotDurationInMs
        '-' -> DASH_MARK_MULTIPLIER * dotDurationInMs
        ' ' -> MEDIUM_GAP_MULTIPLIER * dotDurationInMs
        else -> dotDurationInMs
    }

/**
 * Egy pont, vonal, vagy szóközhöz tartozó AudioInputStream-et állít elő
 */
fun createSingleMarkSound(durationInMillis: Int, isSilent: Boolean = false): AudioInputStream {
    val soundBuffer = ByteArray(durationInMillis * 8)
    for (i in soundBuffer.indices) {
        soundBuffer[i] = if (isSilent) {
            0
        } else {
            (Math.sin(i / 8.0 * 2.0 * Math.PI) * 80.0).toByte()
        }
    }

    val audioFormat = AudioFormat(
        8000F,
        8,
        1,
        true,
        false
    )

    return AudioInputStream(
        ByteArrayInputStream(soundBuffer),
        audioFormat,
        soundBuffer.size.toLong()
    )
}

/**
 * A paraméterként kapott AudioInputStream listát összerakja egy AudioInputStream-be
 */
fun getMergedSounds(audioInputStreams: List<AudioInputStream>): AudioInputStream {
    if (audioInputStreams.size == 1) {
        return audioInputStreams[0]
    }

    val audioFormat = audioInputStreams[0].format

    var mergedAudioInputStream = AudioInputStream(
        SequenceInputStream(audioInputStreams[0], audioInputStreams[1]),
        audioFormat,
        audioInputStreams[0].frameLength + audioInputStreams[1].frameLength
    )

    if (audioInputStreams.size == 2) {
        return mergedAudioInputStream
    }

    for (i in 2 until audioInputStreams.size) {
        mergedAudioInputStream = AudioInputStream(
            SequenceInputStream(mergedAudioInputStream, audioInputStreams[i]),
            audioFormat,
            mergedAudioInputStream.frameLength + audioInputStreams[i].frameLength
        )
    }

    return mergedAudioInputStream
}
