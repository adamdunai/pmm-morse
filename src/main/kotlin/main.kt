import java.io.File
import java.io.IOException
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioSystem

fun main(args: Array<String>) {
    if (!validateParameters(args)) {
        println("\nHasználati példa:\npmm-morse.jar 100 \"SOS we are sinking\"")
        return
    }

    val dotDurationInMs = args[0].toInt()
    val message = args[1]

    val morseSound = getMorseSound(dotDurationInMs, message)

    // WAV-formátumú fájlba írja a Morze hangokat
    try {
        AudioSystem.write(
            morseSound,
            AudioFileFormat.Type.WAVE,
            File("audio.wav")
        )
    } catch (ioException: IOException) {
        println("Hiba! Nem sikerült a fájlba írás")
    } catch (illegalArgumentException: IllegalArgumentException) {
        println("Hiba! A WAV formátumot nem támogatja a rendszer")
    }

}

/**
 * Validálja a bemeneti paramétereket
 */
fun validateParameters(args: Array<String>): Boolean {
    if (args.isEmpty() || args.size != 2) {
        println("Adjon meg paramétereket:\n- első paraméter: a „pont” időtartama\n- második paraméter: az üzenet sztring formátumban (csak ékezet nélküli betűk és számok)")

        return false
    } else if (args[0].toIntOrNull() == null) {
        println("Hiba! Az első paraméter szám kell legyen, a „pont” időtartama")

        return false
    }

    return true
}
