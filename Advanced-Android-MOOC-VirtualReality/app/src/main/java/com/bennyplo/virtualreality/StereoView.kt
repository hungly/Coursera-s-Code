package com.bennyplo.virtualreality

import android.opengl.GLES32
import android.opengl.Matrix
import com.bennyplo.virtualreality.MyRenderer.Companion.checkGlError
import com.bennyplo.virtualreality.MyRenderer.Companion.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class StereoView(pHeight: Int, pWidth: Int, isLeft: Boolean) {

    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0
    private var mTextureCoordHandle = 0
    private var mTextureHandle = 0
    private var mHeightHandle = 0
    private var mFactorHandle = 0

    private val frameBufferTextureId by lazy {
        IntArray(1)
    }

    private val renderBuffer by lazy {
        IntArray(1)
    }

    private val indexBuffer: IntBuffer
    private val program: Int
    private val textureBuffer: FloatBuffer
    private val vertexBuffer: FloatBuffer

    val frameBuffer by lazy {
        IntArray(1)
    }

    val mMVMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)
    private val mModelMatrix = FloatArray(16)
    val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)

    private var modelTranslation: Float = 0f

    val mFrameViewMatrix = FloatArray(16)
    private val mFrameModelMatrix = FloatArray(16)

    val height: Int
    val width: Int
    private val aspect: Float
        get() = width.toFloat() / height.toFloat()

    init {
        val bb = ByteBuffer.allocateDirect(DISPLAY_VERTEX.size * Float.SIZE_BYTES)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(DISPLAY_VERTEX)
        vertexBuffer.position(0)

        val ib = IntBuffer.allocate(DISPLAY_INDEX.size)
        indexBuffer = ib
        indexBuffer.put(DISPLAY_INDEX)
        indexBuffer.position(0)

        val tb = ByteBuffer.allocateDirect(DISPLAY_TEXTURE_COORDS.size * Float.SIZE_BYTES)
        tb.order(ByteOrder.nativeOrder())
        textureBuffer = tb.asFloatBuffer()
        textureBuffer.put(DISPLAY_TEXTURE_COORDS)
        textureBuffer.position(0)

        val vertexShader = loadShader(GLES32.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        program = GLES32.glCreateProgram()
        GLES32.glAttachShader(program, vertexShader)
        GLES32.glAttachShader(program, fragmentShader)
        GLES32.glLinkProgram(program)
        GLES32.glUseProgram(program)

        mPositionHandle = GLES32.glGetAttribLocation(program, "aVertexPosition")
        GLES32.glEnableVertexAttribArray(mPositionHandle)
        mTextureCoordHandle = GLES32.glGetAttribLocation(program, "aTextureCoord")
        GLES32.glEnableVertexAttribArray(mTextureCoordHandle)
        mTextureHandle = GLES32.glGetUniformLocation(program, "uSampler")
        mMVPMatrixHandle = GLES32.glGetUniformLocation(program, "uMVPMatrix")
        checkGlError("Base get")

        width = pWidth / 2
        height = pHeight

        if (pWidth > pHeight) {
            val pRatio = pWidth.toFloat() / pHeight.toFloat()
            Matrix.orthoM(mProjectionMatrix, 0, -pRatio, pRatio, -1f, 1f, -10f, 200f)
        } else {
            val pRatio = pHeight.toFloat() / pWidth.toFloat()
            Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -pRatio, pRatio, -10f, 200f)
        }
        Matrix.setLookAtM(
            mViewMatrix,
            0,
            0f,
            0f,
            0.1f,
            0f,
            0f,
            0f,
            0f,
            1f,
            0f
        )
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.scaleM(mModelMatrix, 0, width.toFloat() / height.toFloat(), 1f, 1f)
        if (isLeft) {
            Matrix.translateM(mModelMatrix, 0, -1f, 0f, 0f)
        } else {
            Matrix.translateM(mModelMatrix, 0, 1f, 0f, 0f)
        }
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0)
        if (isLeft) {
            Matrix.frustumM(
                mProjectionMatrix,
                0,
                FRUSTUM_SHIFT - aspect,
                FRUSTUM_SHIFT - aspect,
                -1f,
                1f,
                NEAR_Z,
                FAR_Z
            )
            modelTranslation = IOD / 2
            Matrix.setLookAtM(
                mFrameViewMatrix,
                0,
                -IOD / 2,
                0f,
                0.1f,
                0f,
                0f,
                SCREEN_Z,
                0f,
                1f,
                0f
            )
        } else {
            Matrix.frustumM(
                mProjectionMatrix,
                0,
                -aspect - FRUSTUM_SHIFT,
                -aspect - FRUSTUM_SHIFT,
                -1f,
                1f,
                NEAR_Z,
                FAR_Z
            )
            modelTranslation = -IOD / 2
            Matrix.setLookAtM(
                mFrameViewMatrix,
                0,
                IOD / 2,
                0f,
                0.1f,
                0f,
                0f,
                SCREEN_Z,
                0f,
                1f,
                0f
            )
        }
        Matrix.setIdentityM(mFrameModelMatrix, 0)
        Matrix.translateM(mFrameModelMatrix, 0, modelTranslation, 0f, DEPTH_Z)

        createFrameBuffer(width, height)
    }

    fun getModelMatrix(rotateX: Float, rotateY: Float, rotateZ: Float): FloatArray {
        val pModelMatrix = FloatArray(16)
        val mRotationMatrixX = FloatArray(16)
        val mRotationMatrixY = FloatArray(16)
        val mRotationMatrixZ = FloatArray(16)

        Matrix.setIdentityM(pModelMatrix, 0)
        Matrix.setRotateM(mRotationMatrixX, 0, rotateX, 1f, 0f, 0f)
        Matrix.setRotateM(mRotationMatrixY, 0, rotateY, 0f, 1f, 0f)
        Matrix.setRotateM(mRotationMatrixZ, 0, rotateZ, 0f, 0f, 1f)
        Matrix.multiplyMM(pModelMatrix, 0, mFrameModelMatrix, 0, mRotationMatrixX, 0)
        Matrix.multiplyMM(pModelMatrix, 0, pModelMatrix, 0, mRotationMatrixX, 0)
        Matrix.multiplyMM(pModelMatrix, 0, pModelMatrix, 0, mRotationMatrixX, 0)
        return pModelMatrix
    }

    fun draw() {
        GLES32.glUseProgram(program)

        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        checkGlError("Base set 1")
        GLES32.glActiveTexture(GLES32.GL_TEXTURE1)
        checkGlError("Base set 2")
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, frameBufferTextureId[0])
        checkGlError("Base set 3")
        GLES32.glUniform1i(mTextureHandle, 1)
        checkGlError("Base set 4")

        GLES32.glVertexAttribPointer(
            mTextureCoordHandle,
            TEXTTURE_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            TEXTURE_STRIDE,
            textureBuffer
        )
        GLES32.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            VERTEX_STRIDE,
            vertexBuffer
        )

        GLES32.glDrawElements(
            GLES32.GL_TRIANGLES,
            DISPLAY_INDEX.size,
            GLES32.GL_UNSIGNED_INT,
            indexBuffer
        )
    }

    private fun createFrameBuffer(width: Int, height: Int) {
        GLES32.glGenFramebuffers(1, frameBuffer, 0)
        GLES32.glGenTextures(1, frameBufferTextureId, 0)
        initializeTexture(
            GLES32.GL_TEXTURE1,
            frameBufferTextureId[0],
            width,
            height,
            GLES32.GL_RGBA,
            GLES32.GL_UNSIGNED_BYTE
        )
        GLES32.glBindFramebuffer(GLES32.GL_DRAW_FRAMEBUFFER, frameBuffer[0])
        GLES32.glFramebufferTexture2D(
            GLES32.GL_FRAMEBUFFER,
            GLES32.GL_COLOR_ATTACHMENT0,
            GLES32.GL_TEXTURE_2D,
            frameBufferTextureId[0],
            0
        )
        GLES32.glGenRenderbuffers(1, renderBuffer, 0)
        GLES32.glBindRenderbuffer(GLES32.GL_RENDERBUFFER, renderBuffer[0])
        GLES32.glRenderbufferStorage(
            GLES32.GL_RENDERBUFFER,
            GLES32.GL_DEPTH_COMPONENT24,
            width,
            height
        )
        GLES32.glFramebufferRenderbuffer(
            GLES32.GL_FRAMEBUFFER,
            GLES32.GL_DEPTH_ATTACHMENT,
            GLES32.GL_RENDERBUFFER,
            renderBuffer[0]
        )
        val status = GLES32.glCheckFramebufferStatus(GLES32.GL_FRAMEBUFFER)
        if (status != GLES32.GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Failed to initialize framebuffer object $status")
        }
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0)
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, 0)
    }

    private fun initializeTexture(
        whichTexture: Int,
        textureId: Int,
        width: Int,
        height: Int,
        pixelFormat: Int,
        type: Int
    ) {
        GLES32.glActiveTexture(whichTexture)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureId)
        GLES32.glTexParameterf(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_MIN_FILTER,
            GLES32.GL_NEAREST.toFloat()
        )
        GLES32.glTexParameterf(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_MAG_FILTER,
            GLES32.GL_NEAREST.toFloat()
        )
        GLES32.glTexParameterf(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_WRAP_S,
            GLES32.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES32.glTexParameterf(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_WRAP_T,
            GLES32.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES32.glTexImage2D(
            GLES32.GL_TEXTURE_2D,
            0,
            pixelFormat,
            width,
            height,
            0,
            pixelFormat,
            type,
            null
        )
    }

    companion object {

        private const val COORDS_PER_VERTEX = 3
        private const val TEXTTURE_PER_VERTEX = 2

        private const val VERTEX_STRIDE = COORDS_PER_VERTEX * Float.SIZE_BYTES
        private const val TEXTURE_STRIDE = TEXTTURE_PER_VERTEX * Float.SIZE_BYTES

        private const val VERTEX_SHADER_CODE =
            "attribute vec3 aVertexPosition;" +
                    "uniform mat4 uMVPMatrix;" +
                    "attribute vec2 aTextureCoord;" +
                    "varying vec2 vTextureCoord;" +
                    "void main() {" +
                    "   gl_Position = uMVPMatrix * vec4(aVertexPosition, 1.0);" +
                    "   vTextureCoord = aTextureCoord;" +
                    "}"

        private const val FRAGMENT_SHADER_CODE =
            "precision lowp float;" +
                    "varying vec2 vTextureCoord;" +
                    "uniform sampler2D uSampler;" +
                    "void main() {" +
                    "   vec4 fragmentColor = texture2D(uSampler, vec2(vTextureCoord.s, vTextureCoord.t));" +
                    "   gl_FragColor = vec4(fragmentColor.rgb, fragmentColor.a);" +
                    "}"

        private val DISPLAY_VERTEX = floatArrayOf(
            -1f, -1f, 1f,
            1f, -1f, 1f,
            1f, 1f, 1f,
            -1f, 1f, 1f
        )

        private val DISPLAY_INDEX = intArrayOf(
            0, 1, 2,
            0, 2, 3
        )

        //        private val DISPLAY_TEXTURE_COORDS = floatArrayOf(
//            0f, 0f,
//            1f, 0f,
//            1f, 1f,
//            0f, 1f
//        )
        // mirror effect
        private val DISPLAY_TEXTURE_COORDS = floatArrayOf(
            0f, 1f,
            1f, 1f,
            1f, 0f,
            0f, 0f
        )

        private const val DEPTH_Z = -1f
        private const val NEAR_Z = 1f
        private const val FAR_Z = 8f
        private const val SCREEN_Z = -10f
        private const val IOD = 0.8f
        private const val FRUSTUM_SHIFT = -(IOD / 2) * NEAR_Z / SCREEN_Z
    }

}