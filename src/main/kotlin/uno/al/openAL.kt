package uno.al

import kool.adr
import kool.free
import kool.remSize
import org.lwjgl.openal.ALC10.*
import org.lwjgl.openal.ALC10.alcMakeContextCurrent
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10.*
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALCCapabilities
import org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename
import org.lwjgl.system.MemoryStack.stackGet
import org.lwjgl.system.MemoryStack.stackPush
import uno.glfw.stak
import java.nio.Buffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

object alc {

    val currentContext: ALCcontext
        get() = ALCcontext(alcGetCurrentContext())
}

object al {

    enum class Format(val i: Int) {
        UNDEFINED(-1),
        MONO8(0x1100),
        MONO16(0x1101),
        STEREO8(0x1102),
        STEREO16(0x1103)
    }

    fun genBuffer() = ALBuffer(alGenBuffers())
    fun genSource() = ALSource(alGenSources())
}

inline class ALBuffer(val i: Int) {

    fun data(format: al.Format, data: Buffer, frequency: Int) = nalBufferData(i, format.i, data.adr, data.remSize, frequency)

    fun delete() = alDeleteBuffers(i)
}

inline class ALSource(val i: Int) {

    var buffer: ALBuffer
        get() = ALBuffer(alGetSourcei(i, AL_BUFFER))
        set(value) = alSourcei(i, AL_BUFFER, value.i)

    fun play() = alSourcePlay(i)

    fun delete() = alDeleteSources(i)
}

object stbVorbis {

    fun decodeFilename(filename: String): Triple<ShortBuffer, Int, Int> = stak {
        val channels = it.mallocInt(1)
        val sampleRate = it.mallocInt(1)
        val buf = stb_vorbis_decode_filename(filename, channels, sampleRate)
        Triple(buf!!, channels[0], sampleRate[0])
    }
}

inline class ALCdevice(val L: Long) {

    fun createContext() = createContext(stackGet().callocInt(1))
    fun createContext(attrList: IntBuffer) = ALCcontext(alcCreateContext(L, attrList))

    fun close() = alcCloseDevice(L)

    infix fun isExtensionPresent(extName: CharSequence) = alcIsExtensionPresent(L, extName)

    fun createCapabilities(): ALCCapabilities = ALC.createCapabilities(L)

    companion object {
        fun open(deviceSpecifier: String? = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER)) = ALCdevice(alcOpenDevice(deviceSpecifier))
    }
}

inline class ALCcontext(val L: Long) {

    fun makeCurrent() = alcMakeContextCurrent(L)

    fun process() = alcProcessContext(L)

    fun suspend() = alcSuspendContext(L)

    fun destroy() = alcDestroyContext(L)

    val device: ALCdevice
        get() = ALCdevice(alcGetContextsDevice(L))
}