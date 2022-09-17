package com.toocol.termio.platform.nativefx

import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

class NativeBinding {
    internal interface IntEnum {
        fun asInt(): Int

        companion object {
            fun combine(vararg enums: IntEnum): Int {
                var result = 0
                for (e in enums) {
                    result = result or e.asInt()
                }
                return result
            }

            fun combine(combined: Int, vararg enums: IntEnum): Int {
                var result = combined
                for (e in enums) {
                    result = result or e.asInt()
                }
                return result
            }
        }
    }

    internal interface MouseBtn {
        companion object {
            const val NO_BTN = 0
            const val PRIMARY_BTN = 1
            const val SECONDARY_BTN = 2
            const val MIDDLE_BTN = 4

            fun fromEvent(ev: MouseEvent): Int {
                var result = NO_BTN
                if (ev.isPrimaryButtonDown) {
                    result = result or PRIMARY_BTN
                }
                if (ev.isSecondaryButtonDown) {
                    result = result or SECONDARY_BTN
                }
                if (ev.isMiddleButtonDown) {
                    result = result or MIDDLE_BTN
                }
                return result
            }

            fun fromEvent(ev: ScrollEvent?): Int {
                // TODO implement me 8.07.2019
                return NO_BTN
            }
        }
    }

    internal interface Modifier {
        companion object {
            const val NO_KEY = 0
            const val SHIFT_KEY = 1
            const val ALT_KEY = 2
            const val META_KEY = 4
            const val CONTROL_KEY = 8

            fun fromEvent(ev: MouseEvent): Int {
                var result = NO_KEY
                if (ev.isShiftDown) {
                    result = result or SHIFT_KEY
                }
                if (ev.isAltDown) {
                    result = result or ALT_KEY
                }
                if (ev.isMetaDown) {
                    result = result or META_KEY
                }
                if (ev.isControlDown) {
                    result = result or CONTROL_KEY
                }
                return result
            }

            fun fromEvent(ev: ScrollEvent): Int {
                var result = NO_KEY
                if (ev.isShiftDown) {
                    result = result or SHIFT_KEY
                }
                if (ev.isAltDown) {
                    result = result or ALT_KEY
                }
                if (ev.isMetaDown) {
                    result = result or META_KEY
                }
                if (ev.isControlDown) {
                    result = result or CONTROL_KEY
                }
                return result
            }
        }
    }

    init {
        throw AssertionError("Can't instantiated.")
    }

    companion object {
        private var initialized = false

        external fun nextKey(): Int
        external fun connectTo(name: String?): Int
        external fun terminate(key: Int): Boolean
        external fun isConnected(key: Int): Boolean
        external fun sendMsg(key: Int, msg: String?): String?

        external fun processNativeEvents(key: Int)
        external fun resize(key: Int, w: Int, h: Int)
        external fun isDirty(key: Int): Boolean
        external fun redraw(key: Int, x: Int, y: Int, w: Int, h: Int)
        external fun setDirty(key: Int, value: Boolean)
        external fun setBufferReady(key: Int, value: Boolean)
        external fun isBufferReady(key: Int): Boolean
        external fun getW(key: Int): Int
        external fun getH(key: Int): Int
        external fun fireMousePressedEvent(
            key: Int,
            x: Double,
            y: Double,
            buttons: Int,
            modifiers: Int,
            timestamp: Long,
        ): Boolean

        external fun fireMouseReleasedEvent(
            key: Int,
            x: Double,
            y: Double,
            buttons: Int,
            modifiers: Int,
            timestamp: Long,
        ): Boolean

        external fun fireMouseClickedEvent(
            key: Int,
            x: Double,
            y: Double,
            buttons: Int,
            modifiers: Int,
            clickCount: Int,
            timestamp: Long,
        ): Boolean

        external fun fireMouseEnteredEvent(
            key: Int,
            x: Double,
            y: Double,
            buttons: Int,
            modifiers: Int,
            timestamp: Long,
        ): Boolean

        external fun fireMouseExitedEvent(
            key: Int,
            x: Double,
            y: Double,
            buttons: Int,
            modifiers: Int,
            timestamp: Long,
        ): Boolean

        external fun fireMouseMoveEvent(
            key: Int,
            x: Double,
            y: Double,
            buttons: Int,
            modifiers: Int,
            timestamp: Long,
        ): Boolean

        external fun fireMouseWheelEvent(
            key: Int,
            x: Double,
            y: Double,
            amount: Double,
            buttons: Int,
            modifiers: Int,
            timestamp: Long,
        ): Boolean

        external fun fireKeyPressedEvent(
            key: Int,
            characters: String?,
            keyCode: Int,
            modifiers: Int,
            timestamp: Long,
        ): Boolean

        external fun fireKeyReleasedEvent(
            key: Int,
            characters: String?,
            keyCode: Int,
            modifiers: Int,
            timestamp: Long,
        ): Boolean

        external fun fireKeyTypedEvent(
            key: Int,
            characters: String?,
            keyCode: Int,
            modifiers: Int,
            timestamp: Long,
        ): Boolean

        external fun getBuffer(key: Int): ByteBuffer?
        external fun lock(key: Int): Boolean
        external fun lock(key: Int, timeout: Long): Boolean
        external fun unlock(key: Int)
        external fun waitForBufferChanges(key: Int)
        external fun hasBufferChanges(key: Int): Boolean
        external fun lockBuffer(key: Int)
        external fun unlockBuffer(key: Int)

        fun libEnding(): String {
            if (isOS("windows")) return ".dll"
            if (isOS("linux")) return ".so"
            return if (isOS("macos")) ".dylib" else ".so"
        }

        fun archName(): String {
            val osArch = System.getProperty("os.arch")
            if (osArch.lowercase(Locale.getDefault()).contains("x64")
                || osArch.lowercase(Locale.getDefault()).contains("amd64")
                || osArch.lowercase(Locale.getDefault()).contains("x86_64")
            ) {
                return "x64"
            } else if (osArch.lowercase(Locale.getDefault()).contains("x86")) {
                return "x86"
            } else if (osArch.lowercase(Locale.getDefault()).contains("aarch64")) {
                return "aarch64"
            }
            return osArch
        }

        fun osName(): String {
            val osName = System.getProperty("os.name")
            if (osName.lowercase(Locale.getDefault()).contains("windows")) {
                return "windows"
            } else if (osName.lowercase(Locale.getDefault()).contains("linux")) {
                return "linux"
            } else if (osName.lowercase(Locale.getDefault()).contains("mac")
                || osName.lowercase(Locale.getDefault()).contains("darwin")
            ) {
                return "macos"
            }
            return osName
        }

        fun isOS(os: String): Boolean {
            return os == osName()
        }

        fun init() {
            if (initialized) {
                // throw new RuntimeException("Already initialized.");
                return
            }
            initialized = true
            var path = "/eu/mihosoft/nativefx/nativelibs/"
            var vcredistPath1 = path
            var vcredistPath2 = path
            var libName = "nativefx" + libEnding()
            if (isOS("windows")) {
                path += "windows/" + archName() + "/" + libName
                vcredistPath1 += "windows/" + archName() + "/vcruntime140.dll"
                vcredistPath2 += "windows/" + archName() + "/msvcp140.dll"
            } else if (isOS("linux")) {
                libName = "lib$libName"
                path += "linux/" + archName() + "/" + libName
            } else if (isOS("macos")) {
                libName = "lib$libName"
                path += "macos/" + archName() + "/" + libName
            }
            try {
                val libPath = Files.createTempDirectory("nativefx-libs")
                if (isOS("windows")) {
                    resourceToFile(vcredistPath1, Paths.get(libPath.toFile().absolutePath, "vcruntime140.dll"), false)
                    resourceToFile(vcredistPath2, Paths.get(libPath.toFile().absolutePath, "msvcp140.dll"), false)
                    if (Paths.get(libPath.toFile().absolutePath + "/vcruntime140.dll").toFile().isFile) {
                        println("> loading " + libPath.toFile().absolutePath + "/vcruntime140.dll")
                        System.load(libPath.toFile().absolutePath + "/vcruntime140.dll")
                    }
                }
                resourceToFile(path, Paths.get(libPath.toFile().absolutePath, libName))
                println("> loading " + libPath.toFile().absolutePath + "/" + libName)
                System.load(libPath.toFile().absolutePath + "/" + libName)
            } catch (e: IOException) {
                e.printStackTrace(System.err)
            }
        }

        @Throws(IOException::class)
        private fun resourceToFile(resource: String, destination: Path, failOnError: Boolean = true) {
            try {
                NativeBinding::class.java.getResourceAsStream(resource).use { `is` ->
                    println("> unpacking resource '$resource' to file '$destination'")
                    Files.copy(`is`, destination,
                        StandardCopyOption.REPLACE_EXISTING)
                }
            } catch (e: NullPointerException) {
                if (failOnError) {
                    throw FileNotFoundException("Resource '$resource' was not found.")
                } else {
                    System.err.println("WARNING: Resource '$resource' was not found.")
                }
            }
        }

        private val listeners: MutableMap<Int, MutableList<NativeEventListener>> = HashMap()
        fun addEventListener(key: Int, l: NativeEventListener) {
            // create list if not present
            if (!listeners.containsKey(key)) {
                val list: MutableList<NativeEventListener> = ArrayList()
                listeners[key] = list
            }
            listeners[key]!!.add(l)
        }

        fun removeEventListener(key: Int, l: NativeEventListener) {
            // create list if not present
            if (!listeners.containsKey(key)) {
                val list: MutableList<NativeEventListener> = ArrayList()
                listeners[key] = list
            }
            listeners[key]!!.remove(l)
        }

        fun removeEventListeners(key: Int) {
            // create list if not present
            if (!listeners.containsKey(key)) {
                val list: MutableList<NativeEventListener> = ArrayList()
                listeners[key] = list
            }
            listeners[key]!!.clear()
        }

        /*CALLED FROM NATIVE*/
        fun fireNativeEvent(key: Int, type: String?, evt: String?) {
            // return early if not present
            if (!listeners.containsKey(key)) {
                return
            }
            val list: List<NativeEventListener> = listeners[key]!!
            for (l in list) {
                l.event(key, type, evt)
            }
        }
    }
}