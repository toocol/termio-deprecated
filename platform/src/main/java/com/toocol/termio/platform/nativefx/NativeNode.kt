package com.toocol.termio.platform.nativefx

import com.toocol.termio.platform.nativefx.NativeBinding.Modifier
import com.toocol.termio.platform.nativefx.NativeBinding.MouseBtn
import javafx.animation.AnimationTimer
import javafx.geometry.Rectangle2D
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Region
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

/**
 * This node renders native buffers. It handles the connection to a
 * shared memory object, the transfer of input events and resize requests as
 * well as native events.
 */
class NativeNode @JvmOverloads constructor(
    private var hidpiAware: Boolean = false,
    private val pixelBufferEnabled: Boolean = false,
) : Region() {
    private var serverName: String? = null
    private val formatInt = PixelFormat.getIntArgbPreInstance()
    private val formatByte = PixelFormat.getByteBgraPreInstance()
    private var img: WritableImage? = null
    private var view: ImageView? = null
    private var buffer: ByteBuffer? = null
    private var intBuf: IntBuffer? = null
    private var pixelBuffer: PixelBuffer<ByteBuffer?>? = null
    private var dimensions: Rectangle2D? = null
    private var timer: AnimationTimer? = null
    private var key = -1
    private var buttonState = 0
    private var lockingError = false
    private val numValues = 10
    private val fpsValues = DoubleArray(numValues)
    private var frameTimestamp: Long = 0
    private var fpsCounter = 0

    private var isVerbose = false

    /**
     * Constructor. Creates a new instance of this class without hidpi-awareness.
     */
    init {
        addEventHandler(MouseEvent.MOUSE_MOVED) { ev: MouseEvent ->
            val sx = if (hidpiAware) scene.window.renderScaleX else 1.0
            val sy = if (hidpiAware) scene.window.renderScaleX else 1.0
            val x = ev.x * sx
            val y = ev.y * sy
            val timestamp = System.nanoTime()
            NativeBinding.fireMouseMoveEvent(key, x, y,
                MouseBtn.fromEvent(ev), Modifier.fromEvent(ev),
                timestamp
            )
        }
        addEventHandler(MouseEvent.MOUSE_PRESSED) { ev: MouseEvent ->
            val sx = if (hidpiAware) scene.window.renderScaleX else 1.0
            val sy = if (hidpiAware) scene.window.renderScaleX else 1.0
            val x = ev.x * sx
            val y = ev.y * sy
            val timestamp = System.nanoTime()
            buttonState = MouseBtn.fromEvent(ev)
            NativeBinding.fireMousePressedEvent(key, x, y,
                MouseBtn.fromEvent(ev), Modifier.fromEvent(ev),
                timestamp
            )
        }
        addEventHandler(MouseEvent.MOUSE_RELEASED) { ev: MouseEvent ->
            val sx = if (hidpiAware) scene.window.renderScaleX else 1.0
            val sy = if (hidpiAware) scene.window.renderScaleX else 1.0
            val x = ev.x * sx
            val y = ev.y * sy
            val timestamp = System.nanoTime()
            NativeBinding.fireMouseReleasedEvent(key,
                x,
                y,  // TODO 11.07.2019 check whether we correctly detected btn up (what about multiple btns?)
                buttonState,
                Modifier.fromEvent(ev),
                timestamp
            )
        }
        addEventHandler(MouseEvent.MOUSE_DRAGGED) { ev: MouseEvent ->
            val sx = if (hidpiAware) scene.window.renderScaleX else 1.0
            val sy = if (hidpiAware) scene.window.renderScaleX else 1.0
            val x = ev.x * sx
            val y = ev.y * sy
            val timestamp = System.nanoTime()
            NativeBinding.fireMouseMoveEvent(key, x, y,
                MouseBtn.fromEvent(ev), Modifier.fromEvent(ev),
                timestamp
            )
        }
        addEventHandler(MouseEvent.MOUSE_CLICKED) { ev: MouseEvent ->
            requestFocus()
            val sx = if (hidpiAware) scene.window.renderScaleX else 1.0
            val sy = if (hidpiAware) scene.window.renderScaleX else 1.0
            val x = ev.x * sx
            val y = ev.y * sy
            val timestamp = System.nanoTime()
            NativeBinding.fireMouseClickedEvent(key, x, y,
                MouseBtn.fromEvent(ev), Modifier.fromEvent(ev),
                ev.clickCount,
                timestamp
            )
        }
        addEventHandler(ScrollEvent.SCROLL) { ev: ScrollEvent ->
            val sx = if (hidpiAware) scene.window.renderScaleX else 1.0
            val sy = if (hidpiAware) scene.window.renderScaleX else 1.0
            val x = ev.x * sx
            val y = ev.y * sy
            val timestamp = System.nanoTime()
            NativeBinding.fireMouseWheelEvent(key, x, y, ev.deltaY,
                MouseBtn.fromEvent(ev), Modifier.fromEvent(ev),
                timestamp
            )
        }

        // ---- keys
        this.isFocusTraversable = true // TODO make this optional?
        addEventHandler(KeyEvent.KEY_PRESSED) { ev: KeyEvent ->
            // System.out.println("KEY: pressed " + ev.getText() + " : " + ev.getCode());
            val timestamp = System.nanoTime()
            NativeBinding.fireKeyPressedEvent(key, ev.text, ev.code.code,  /*modifiers*/
                0,
                timestamp
            )
        }
        addEventHandler(KeyEvent.KEY_RELEASED) { ev: KeyEvent ->
            // System.out.println("KEY: released " + ev.getText() + " : " + ev.getCode());
            val timestamp = System.nanoTime()
            NativeBinding.fireKeyReleasedEvent(key, ev.text, ev.code.code,  /*modifiers*/
                0,
                timestamp
            )
        }
        addEventHandler(KeyEvent.KEY_TYPED) { ev: KeyEvent ->
            // System.out.println("KEY: typed    " + ev.getText() + " : " + ev.getCode());
            val timestamp = System.nanoTime()
            NativeBinding.fireKeyTypedEvent(key, ev.text, ev.code.code,  /*modifiers*/
                0,
                timestamp
            )
        }
        showNotConnectedText()
    }

    /**
     * Connects this node to the specified shared memory object.
     *
     * @param name name of the shared memory to connect to
     * @throws RuntimeException if the connection cannot be established
     */
    fun connect(name: String) {
        serverName = name
        NativeBinding.init()
        disconnect()
        if (key < 0 || NativeBinding.isConnected(key)) {
            key = NativeBinding.connectTo(name)
        }
        if (key < 0) {
            showErrorText()
            throw RuntimeException("[$key]> cannot connect to shared memory ''$name''.")
        }
        view = ImageView()
        view!!.isPreserveRatio = false
        view!!.fitWidthProperty().bind(widthProperty())
        view!!.fitHeightProperty().bind(heightProperty())
        val r = Runnable {
            val currentTimeStamp = System.nanoTime()

            // try to lock the shared resource
            lockingError = !NativeBinding.lock(key)
            if (lockingError) {
                showErrorText()
                timer!!.stop()
                return@Runnable
            }
            val dirty = NativeBinding.isDirty(key)
            val isReady = NativeBinding.isBufferReady(key)

            // if(!isReady) {
            //     System.out.println("["+key+"]> WARNING: buffer ready: " + isReady);
            // }
            NativeBinding.processNativeEvents(key)

            // if not dirty yet and/or not ready there's nothing
            // to do. we return early.
            if (!dirty || !isReady) {
                NativeBinding.unlock(key)
                return@Runnable
            }
            val currentW = NativeBinding.getW(key)
            val currentH = NativeBinding.getH(key)

            // create new image instance if the image doesn't exist or
            // if the dimensions do not match
            if (img == null
                || currentW.toDouble().compareTo(img!!.width) != 0
                || currentH.toDouble().compareTo(img!!.height) != 0) {
                if (isVerbose) {
                    println("[$key]> -> new img instance, resize W: $currentW, H: $currentH")
                }
                buffer = NativeBinding.getBuffer(key)
                if (pixelBufferEnabled) {
                    pixelBuffer = PixelBuffer(currentW, currentH, buffer, formatByte)
                    img = WritableImage(pixelBuffer)
                } else {
                    intBuf = buffer!!.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer()
                    img = WritableImage(currentW, currentH)
                }
                dimensions = Rectangle2D(0.0, 0.0, currentW.toDouble(), currentH.toDouble())
                view!!.image = img
            }
            if (pixelBufferEnabled) {
                pixelBuffer!!.updateBuffer { dimensions }
            } else {
                img!!.pixelWriter.setPixels(0,
                    0,
                    img!!.width.toInt(),
                    img!!.height.toInt(),
                    formatInt,
                    intBuf,
                    img!!.width.toInt())
            }

            // we updated the image, not dirty anymore
            // NativeBinding.lock(key);
            NativeBinding.setDirty(key, false)
            val w = width.toInt()
            val h = height.toInt()
            val sx = if (hidpiAware) scene.window.renderScaleX else 1.0
            val sy = if (hidpiAware) scene.window.renderScaleX else 1.0
            if ((w.toDouble() != NativeBinding.getW(key) / sx || h.toDouble() != NativeBinding.getH(key) / sy) && w > 0 && h > 0) {
                if (isVerbose) {
                    println("[$key]> requesting buffer resize W: $w, H: $h")
                }
                NativeBinding.resize(key, (w * sx).toInt(), (h * sy).toInt())
            }
            NativeBinding.unlock(key)
            if (isVerbose) {
                val duration = currentTimeStamp - frameTimestamp
                val fps = 1e9 / duration
                fpsValues[fpsCounter] = fps
                if (fpsCounter == numValues - 1) {
                    var fpsAverage = 0.0
                    for (fpsVal in fpsValues) {
                        fpsAverage += fpsVal
                    }
                    fpsAverage /= numValues.toDouble()
                    println("[$key]> fps: $fpsAverage")
                    fpsCounter = 0
                }
                fpsCounter++
                frameTimestamp = currentTimeStamp
            } // end if verbose
        }
        timer = object : AnimationTimer() {
            override fun handle(now: Long) {
                r.run()
            }
        }
        timer!!.start()
        children.add(view)
    }

    /**
     * Adds the specified listener to the native observable.
     *
     * @param l listener to add
     */
    fun addNativeEventListener(l: NativeEventListener) {
        NativeBinding.addEventListener(key, l)
    }

    /**
     * Removes the specified listener from the native observable.
     *
     * @param l listener to remove
     */
    fun removeNativeEventListener(l: NativeEventListener) {
        NativeBinding.removeEventListener(key, l)
    }

    /**
     * Disconnects this node from the connected server and removes native
     * listeners added by this node.
     */
    fun disconnect() {
        if (key < 0) return
        if (timer != null) timer!!.stop()

        // NativeBinding.terminate(key);
        NativeBinding.removeEventListeners(key)
        children.clear()
        img = null
        view = null
        buffer = null
        intBuf = null
        timer = null
    }

    /**
     * Disconnects this node and terminates the connected server. All shared
     * memory resources are released. Native listeners thar have been added
     * by this node will be removed as well.
     */
    fun terminate() {
        if (key < 0) return
        if (timer != null) timer!!.stop()
        NativeBinding.terminate(key)
        NativeBinding.removeEventListeners(key)
        children.clear()
        img = null
        view = null
        buffer = null
        intBuf = null
        timer = null
    }

    private fun showNotConnectedText() {
        children.clear()
        val label = Label("INFO, not connected to a server.")
        label.style = "-fx-text-fill: green; -fx-background-color: white; -fx-border-color: green;-fx-font-size:16"
        children.add(label)
        label.layoutXProperty().bind(widthProperty().divide(2).subtract(label.widthProperty().divide(2)))
        label.layoutYProperty().bind(heightProperty().divide(2).subtract(label.heightProperty().divide(2)))
    }

    private fun showErrorText() {
        children.clear()
        val label = Label("ERROR, cannot connect to server '$serverName'.")
        label.style = "-fx-text-fill: red; -fx-background-color: white; -fx-border-color: red;-fx-font-size:16"
        children.add(label)
        label.layoutXProperty().bind(widthProperty().divide(2).subtract(label.widthProperty().divide(2)))
        label.layoutYProperty().bind(heightProperty().divide(2).subtract(label.heightProperty().divide(2)))
    }

    override fun computePrefWidth(height: Double): Double {
        // TODO: consider insets ect...
        return 0.0
    }

    override fun computePrefHeight(width: Double): Double {
        // TODO: consider insets ect...
        return 0.0
    }
}