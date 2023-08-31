package com.bennyplo.virtualreality

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private var mZoom = 0f //zoom factor

    private val mCharA by lazy {
        CharacterA()
    }

    private val mCharS by lazy {
        CharacterS()
    }

    private val mSphere by lazy {
        Sphere(context)
    }

    private val mMVMatrix = FloatArray(16) //model view matrix
    private val mMVPMatrix = FloatArray(16) //model view projection matrix
    private val mModelMatrix = FloatArray(16) //model  matrix
    private val mProjectionMatrix = FloatArray(16) //projection mastrix
    private val mViewMatrix = FloatArray(16) //view matrix
    var xAngle = 0f //x-rotation angle

    //set the rotational angles and zoom factors
    var yAngle = 0f //y-rotation angle
    var zAngle = 0f //y-rotation angle

    private var viewPortWidth: Int = 0
    private var viewPortHeight: Int = 0
    private var mSphericalMirror: FrameBufferDisplay? = null

    override fun onDrawFrame(unused: GL10) {
        val mRotationMatrixX = FloatArray(16)
        val mRotationMatrixY = FloatArray(16)
        val mRotationMatrixZ = FloatArray(16)

        GLES32.glViewport(0, 0, viewPortWidth, viewPortHeight)

        // Draw background color
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)
        GLES32.glClearDepthf(1.0f) //set up the depth buffer
        GLES32.glEnable(GLES32.GL_DEPTH_TEST) //enable depth test (so, it will not look through the surfaces)
        GLES32.glDepthFunc(GLES32.GL_LEQUAL) //indicate what type of depth test
        Matrix.setIdentityM(
            mMVPMatrix,
            0
        ) //set the model view projection matrix to an identity matrix
        Matrix.setIdentityM(mMVMatrix, 0) //set the model view  matrix to an identity matrix
        Matrix.setIdentityM(mModelMatrix, 0) //set the model matrix to an identity matrix
        Matrix.setRotateM(mRotationMatrixX, 0, xAngle, 1f, 0f, 0f) //rotate around the x-axis
        Matrix.setRotateM(mRotationMatrixY, 0, yAngle, 0f, 1f, 0f) //rotate around the y-axis
        Matrix.setRotateM(mRotationMatrixZ, 0, zAngle, 0f, 0f, 1f) //rotate around the y-axis

        // Set the camera position (View matrix)
        Matrix.setLookAtM(
            mViewMatrix, 0,
            0.0f, 0f, 1.0f,  //camera is at (0,0,1)
            0f, 0f, 0f,  //looks at the origin
            0f, 1f, 0.0f
        ) //head is down (set to (0,1,0) to look from the top)
//        Matrix.scaleM(mModelMatrix, 0, 1f * SCALE_FACTOR, 1 * SCALE_FACTOR, 1 * SCALE_FACTOR)
        // mirror effect
        Matrix.scaleM(mModelMatrix, 0, MIRROR_SCALE_FACTOR, MIRROR_SCALE_FACTOR, MIRROR_SCALE_FACTOR)

        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5f + mZoom) //move backward for 5 units
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrixX, 0)
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrixY, 0)
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrixZ, 0)
        // Calculate the projection and view transformation
        //calculate the model view matrix
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0)

        // mirror effect
//        mSphere.setLightLocation(-10f, -10f, -10f)

//        mCharA.draw(mMVPMatrix)
//        mCharS.draw(mMVPMatrix)
        mSphere.draw(mMVPMatrix)

        mSphericalMirror?.let {
            GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, it.frameBuffer[0])
            GLES32.glViewport(0, 0, it.width, it.height)
            GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)

            // mirror effect
//            val pViewMatrix = FloatArray(16)
//            Matrix.setLookAtM(
//                pViewMatrix, 0,
//                0.0f, 0f, -9f,
//                0f, 0f, 0f,
//                0f, 1f, 0.0f
//            )
//            Matrix.scaleM(
//                mModelMatrix,
//                0,
//                1f / SCALE_FACTOR / SCALE_FACTOR,
//                1 / SCALE_FACTOR / SCALE_FACTOR,
//                1 / SCALE_FACTOR / SCALE_FACTOR
//            )

//            Matrix.multiplyMM(mMVMatrix, 0, pViewMatrix, 0, mModelMatrix, 0)
            Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
//            Matrix.multiplyMM(mMVPMatrix, 0, it.mProjMatrix, 0, mMVMatrix, 0)
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0)

//            mSphere.setLightLocation(2f, 2f, 0f)
            mSphere.draw(mMVPMatrix)
            GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, 0)
        }

        GLES32.glViewport(0, 0, viewPortWidth, viewPortHeight)
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.scaleM(
            mModelMatrix,
            0,
            (mSphericalMirror?.width?.toFloat() ?: 1f) / (mSphericalMirror?.height?.toFloat()
                ?: 1f),
            1f,
            1f
        )

        // mirror effect
        Matrix.setRotateM(mRotationMatrixX, 0, 65f, 1f, 0f, 0f) //rotate around the x-axis
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.05f, 0f)
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrixX, 0)

        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0)
        mSphericalMirror?.draw(mMVPMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        // Adjust the view based on view window changes, such as screen rotation
        GLES32.glViewport(0, 0, width, height)
        var ratio = width.toFloat() / height
        val left = -ratio

        viewPortWidth = width
        viewPortHeight = height

//        Matrix.frustumM(mProjectionMatrix, 0, left, ratio, -1.0f, 1.0f, 1.0f, 8.0f)
        if (width > height) {
            ratio = (width.toFloat() / height)
            Matrix.orthoM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, -10f, 200f)

//            mSphericalMirror = FrameBufferDisplay(height, width)

            // mirror effect
            mSphericalMirror = FrameBufferDisplay(height, width)
        } else {
            ratio = (height.toFloat() / width)
            Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -ratio, ratio, -10f, 200f)

//            mSphericalMirror = FrameBufferDisplay(width, height)

            // mirror effect
            mSphericalMirror = FrameBufferDisplay(width * 2, height)
        }
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color to black
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mZoom = 1.0f
    }

    fun setZoom(zoom: Float) {
        mZoom = zoom
    }

    companion object {

        private const val SCALE_FACTOR = 1.2f
        private const val MIRROR_SCALE_FACTOR = 0.3f

        fun checkGlError(glOperation: String) {
            var error: Int
            if (GLES32.glGetError().also { error = it } != GLES32.GL_NO_ERROR) {
                Log.e("MyRenderer", "$glOperation: glError $error")
            }
        }

        fun loadShader(type: Int, shaderCode: String?): Int {
            // create a vertex shader  (GLES32.GL_VERTEX_SHADER) or a fragment shader (GLES32.GL_FRAGMENT_SHADER)
            val shader = GLES32.glCreateShader(type)
            GLES32.glShaderSource(
                shader,
                shaderCode
            ) // add the source code to the shader and compile it
            GLES32.glCompileShader(shader)
            return shader
        }
    }

}
