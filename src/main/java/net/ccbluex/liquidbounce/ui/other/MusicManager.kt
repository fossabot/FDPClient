package net.ccbluex.liquidbounce.ui.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.FileUtils
import java.io.File

class MusicManager {
    var enableSound : MusicPlayer
    var disableSound : MusicPlayer

    init {
        val enableSoundFile=File(LiquidBounce.fileManager.soundsDir,"enable.wav")
        val disableSoundFile=File(LiquidBounce.fileManager.soundsDir,"disable.wav")

        FileUtils.unpackFile(enableSoundFile,"sounds/enable.wav")
        FileUtils.unpackFile(disableSoundFile,"sounds/disable.wav")

        enableSound=MusicPlayer(enableSoundFile)
        disableSound= MusicPlayer(disableSoundFile)
    }
}