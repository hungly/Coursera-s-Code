package com.bennyplo.animation

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyRenderer(private val context:Context?) : GLSurfaceView.Renderer {
    private val mMVPMatrix = FloatArray(16) //model view projection matrix
    private val mProjectionMatrix = FloatArray(16) //projection mastrix
    private val mViewMatrix = FloatArray(16) //view matrix
    private val mMVMatrix = FloatArray(16) //model view matrix
    private val mModelMatrix = FloatArray(16) //model  matrix
    private var mCharA: CharacterA? = null
    private var mCharS: CharacterS? = null
    private var mSphere: Sphere? = null
    private var mArbitrary: ArbitraryShape? = null
    private var mMySphere: MySphere? = null
    private var mMyArbitrary: MyArbitraryShape? = null
    private var mMyPyramid: MyPyramid? = null
    private var mFlatSurface: FlatSurface? = null
    private var mMyCube: MyCube? = null

    private var mAngleX: Float = 0F
    private var mAngleY: Float = 0F

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color to black
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mCharA = CharacterA()
        mCharS = CharacterS()
        mSphere = Sphere()
        mArbitrary = ArbitraryShape()
//        mMySphere = MySphere(context)
        mMyArbitrary = MyArbitraryShape(context)
        mMyPyramid = MyPyramid(context)
        mFlatSurface = FlatSurface(context)
        mMyCube = MyCube(context)

        //-------
        GLES32.glCullFace(GLES32.GL_BACK) //don't draw back faces
        GLES32.glEnable(GLES32.GL_CULL_FACE) //enable culling
        //-------
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        // Adjust the view based on view window changes, such as screen rotation
        GLES32.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        val left = -ratio
        Matrix.frustumM(mProjectionMatrix, 0, left, ratio, -1.0f, 1.0f, 1.0f, 8.0f)
    }

    override fun onDrawFrame(unused: GL10) {
        val mRotationMatrixX = FloatArray(16)
        val mRotationMatrixY = FloatArray(16)
        val mRotationMatrixZ = FloatArray(16)
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
        Matrix.setRotateM(mRotationMatrixX, 0, mAngleX, 1f, 0f, 0f) //rotate around the x-axis
        Matrix.setRotateM(mRotationMatrixY, 0, mAngleY, 0f, 1f, 0f) //rotate around the y-axis
        Matrix.setRotateM(mRotationMatrixZ, 0, 0f, 0f, 0f, 1f) //rotate around the Z-axis
        // Set the camera position (View matrix)
        Matrix.setLookAtM(
            mViewMatrix, 0,
            0.0f, 0f, 1.0f,  //camera is at (0,0,1)
            0f, 0f, 0f,  //looks at the origin
            0f, 1f, 0.0f
        ) //head is down (set to (0,1,0) to look from the top)
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5f) //move backward for 5 units
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrixX, 0)
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrixY, 0)
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrixZ, 0)
        // Calculate the projection and view transformation
        // Calculate the model view matrix
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0)

        GLES32.glDisable(GLES32.GL_BLEND)
//        mCharA?.draw(mMVPMatrix)
//        mCharS?.draw(mMVPMatrix)
//        mSphere?.draw(mMVPMatrix)
//        mArbitrary?.draw(mMVPMatrix)
//        mMySphere?.draw(mMVPMatrix)
//        mMyArbitrary?.draw(mMVPMatrix)
        mMyPyramid?.draw(mMVPMatrix)

//        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -0.6f) //move backward for 5 units
//        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0)
//        GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE_MINUS_CONSTANT_ALPHA)
//        GLES32.glBlendColor(1f, 0f, 0f, 1f)
//        GLES32.glDisable(GLES32.GL_CULL_FACE)
//        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
//        GLES32.glBlendEquation(GLES32.GL_FUNC_ADD)
//        GLES32.glEnable(GLES32.GL_BLEND)

        GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE_MINUS_CONSTANT_ALPHA)
        GLES32.glBlendEquation(GLES32.GL_FUNC_ADD)
        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glDisable(GLES32.GL_CULL_FACE) //enable culling

//        mFlatSurface?.draw(mMVPMatrix)
//        mMyArbitrary?.draw(mMVPMatrix)
        mMyCube?.draw(mMVPMatrix)
    }

    fun setAngleX(angle: Float) {
        mAngleX = angle
    }

    fun getAngleX() = mAngleX

    fun setAngleY(angle: Float) {
        mAngleY = angle
    }

    fun getAngleY() = mAngleY

    fun setLightLocation(pX:Float, pY:Float, pZ:Float) {
//        mArbitrary?.setLightLocation(pX, pY, pZ)
    }

    companion object {
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