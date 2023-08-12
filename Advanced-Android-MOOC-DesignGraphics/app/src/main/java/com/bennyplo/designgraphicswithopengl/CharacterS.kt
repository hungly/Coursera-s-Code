package com.bennyplo.designgraphicswithopengl

import android.opengl.GLES32
import com.bennyplo.designgraphicswithopengl.MyRenderer.Companion.checkGlError
import com.bennyplo.designgraphicswithopengl.MyRenderer.Companion.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.pow

class CharacterS {

    private val colorBuffer: FloatBuffer
    private val colorStride = COLOR_PER_VERTEX * Float.SIZE_BYTES //4 bytes per vertex

    private val fragmentShaderCode = "precision mediump float;" +  //define the precision of float
            "varying vec4 vColor;" +  //variable from the vertex shader
            "void main() {" +
            "gl_FragColor = vColor;" +
            "}" //change the colour based on the variable from the vertex shader

    private val indexBuffer: IntBuffer
    private val mColorHandle: Int
    private val mMVPMatrixHandle: Int
    private val mPositionHandle: Int
    private val mProgram: Int
    private val vertexBuffer: FloatBuffer

    private val vertexCount // number of vertices
            : Int

    private val vertexShaderCode = "attribute vec3 aVertexPosition;" +  //vertex of an object
            "attribute vec4 aVertexColor;" +  //the colour  of the object
            "uniform mat4 uMVPMatrix;" +  //model view  projection matrix
            "varying vec4 vColor;" +  //variable to be accessed by the fragment shader
            "void main() {" +
            "gl_Position = uMVPMatrix* vec4(aVertexPosition, 1.0);" +  //calculate the position of the vertex
            "vColor=aVertexColor;" +
            "}" //get the colour from the application program

    private val vertexStride = COORDS_PER_VERTEX * Float.SIZE_BYTES // 4 bytes per vertex

    private var charSVertex = floatArrayOf()
    private var charIndex = intArrayOf()
    private var charSColor = floatArrayOf()

    private fun createCurve(controlePtsP: FloatArray, controlePtsQ: FloatArray) {
        val vertices = arrayListOf<Float>()
        val color = arrayListOf<Float>()
        val index = arrayListOf<Int>()
        var vi = 0
        var cIndX = 0
        var indx = 0
        var px = 0
        var x = 0f
        var y = 0f
        var z = .2f
        var centroidX = 0f
        var centroidY = 0f
        val noSegments = controlePtsP.size / 2 / 3
        for (i in controlePtsP.indices step 2) {
            centroidX += controlePtsP[i]
            centroidY += controlePtsP[i + 1]
        }
        centroidX /= controlePtsP.size / 2F
        centroidY /= controlePtsP.size / 2F
        for (segment in 0 until noSegments) {
            for (temp in 0 until 10) {
                val t = temp / 10.0
                x =
                    ((1 - t).pow(3) * controlePtsP[px + 0] + controlePtsP[px + 2] * 3 * t * (1 - t).pow(
                        2
                    ) + controlePtsP[px + 4] * 3 * t * t * (1 - t) + controlePtsP[px + 6] * t.pow(
                        3
                    )).toFloat()
                y =
                    ((1 - t).pow(3) * controlePtsP[px + 1] + controlePtsP[px + 3] * 3 * t * (1 - t).pow(
                        2
                    ) + controlePtsP[px + 5] * 3 * t * t * (1 - t) + controlePtsP[px + 7] * t.pow(
                        3
                    )).toFloat()
                vertices.add(vi++, x - centroidX)
                vertices.add(vi++, y - centroidY)
                vertices.add(vi++, z)
                color.add(cIndX++, 1F)
                color.add(cIndX++, 1F)
                color.add(cIndX++, 0F)
                color.add(cIndX++, 1F)
            }
            px += 6
        }
        px = 0
        var vj = vi
        for (segment in 0 until noSegments) {
            for (temp in 0 until 10) {
                val t = temp / 10.0
                x =
                    ((1 - t).pow(3) * controlePtsQ[px + 0] + controlePtsQ[px + 2] * 3 * t * (1 - t).pow(
                        2
                    ) + controlePtsQ[px + 4] * 3 * t * t * (1 - t) + controlePtsQ[px + 6] * t.pow(
                        3
                    )).toFloat()
                y =
                    ((1 - t).pow(3) * controlePtsQ[px + 1] + controlePtsQ[px + 3] * 3 * t * (1 - t).pow(
                        2
                    ) + controlePtsQ[px + 5] * 3 * t * t * (1 - t) + controlePtsQ[px + 7] * t.pow(
                        3
                    )).toFloat()
                vertices.add(vj++, x - centroidX)
                vertices.add(vj++, y - centroidY)
                vertices.add(vj++, z)
                color.add(cIndX++, 1F)
                color.add(cIndX++, 1F)
                color.add(cIndX++, 0F)
                color.add(cIndX++, 1F)
            }
            px += 6
        }
        var noVertices = vj
        var v0 = 0
        var v1 = 1
        var v2 = vi / 3
        var v3 = vi / 3 + 1
        while (v3 < noVertices / 3) {
            index.add(indx++, v0)
            index.add(indx++, v1)
            index.add(indx++, v2)
            index.add(indx++, v1)
            index.add(indx++, v2)
            index.add(indx++, v3)
            v0++
            v1++
            v2++
            v3++
        }
        var vk = noVertices
        var i = 0
        while (i < noVertices) {
            vertices.add(vk++, vertices[i++])
            vertices.add(vk++, vertices[i++])
            vertices.add(vk++, -vertices[i++])
            color.add(cIndX++, 1F)
            color.add(cIndX++, 0F)
            color.add(cIndX++, 0F)
            color.add(cIndX++, 1F)
        }
        noVertices = vk
        v0 = vj / 3
        v1 = vj / 3 + 1
        v2 = (vj + vi) / 3
        v3 = (vj + vi) / 3 + 1
        while (v3 < noVertices / 3) {
            index.add(indx++, v0)
            index.add(indx++, v1)
            index.add(indx++, v2)
            index.add(indx++, v1)
            index.add(indx++, v2)
            index.add(indx++, v3)
            v0++
            v1++
            v2++
            v3++
        }
        v0 = 0
        v1 = 1
        v2 = vj / 3
        v3 = vj / 3 + 1
        while (v3 < (vi + vj) / 3) {
            index.add(indx++, v0)
            index.add(indx++, v1)
            index.add(indx++, v2)
            index.add(indx++, v1)
            index.add(indx++, v2)
            index.add(indx++, v3)
            v0++
            v1++
            v2++
            v3++
        }
        v0 = vi / 3
        v1 = vi / 3 + 1
        v2 = (vj + vi) / 3
        v3 = (vj + vi) / 3 + 1
        while (v3 < noVertices / 3) {
            index.add(indx++, v0)
            index.add(indx++, v1)
            index.add(indx++, v2)
            index.add(indx++, v1)
            index.add(indx++, v2)
            index.add(indx++, v3)
            v0++
            v1++
            v2++
            v3++
        }
        charSVertex = vertices.toFloatArray()
        charIndex = index.toIntArray()
        charSColor = color.toFloatArray()
    }

    init {
        createCurve(P, Q)
        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
            charSVertex.size * Float.SIZE_BYTES
        ) // (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(charSVertex)
        vertexBuffer.position(0)
        vertexCount = charSVertex.size / COORDS_PER_VERTEX
        val cb = ByteBuffer.allocateDirect(
            charSColor.size * Float.SIZE_BYTES
        ) // (# of coordinate values * 4 bytes per float)
        cb.order(ByteOrder.nativeOrder())
        colorBuffer = cb.asFloatBuffer()
        colorBuffer.put(charSColor)
        colorBuffer.position(0)
        val ib = IntBuffer.allocate(charIndex.size)
        indexBuffer = ib
        indexBuffer.put(charIndex)
        indexBuffer.position(0)
        // prepare shaders and OpenGL program
        val vertexShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)
        mProgram = GLES32.glCreateProgram() // create empty OpenGL Program
        GLES32.glAttachShader(mProgram, vertexShader) // add the vertex shader to program
        GLES32.glAttachShader(mProgram, fragmentShader) // add the fragment shader to program
        GLES32.glLinkProgram(mProgram) // link the  OpenGL program to create an executable
        GLES32.glUseProgram(mProgram) // Add program to OpenGL environment
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "aVertexPosition")
        // Enable a handle to the triangle vertices
        GLES32.glEnableVertexAttribArray(mPositionHandle)
        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor")
        // Enable a handle to the  colour
        GLES32.glEnableVertexAttribArray(mColorHandle)
        // Prepare the colour coordinate data
        GLES32.glVertexAttribPointer(
            mColorHandle,
            COLOR_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            colorStride,
            colorBuffer
        )
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix")
        checkGlError("glGetUniformLocation")
    }

    fun draw(mvpMatrix: FloatArray?) {
        GLES32.glUseProgram(mProgram) //use the object's shading programs
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        checkGlError("glUniformMatrix4fv")
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        GLES32.glVertexAttribPointer(
            mColorHandle,
            COORDS_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            colorStride,
            colorBuffer
        )
        // Draw the 3D character A
        GLES32.glDrawElements(
            GLES32.GL_TRIANGLES,
            charIndex.size,
            GLES32.GL_UNSIGNED_INT,
            indexBuffer
        )
//        GLES32.glDrawElements(
//            GLES32.GL_LINES,
//            charIndex.size,
//            GLES32.GL_UNSIGNED_INT,
//            indexBuffer
//        )
    }

    companion object {
        // number of coordinates per vertex in this array
        private const val COORDS_PER_VERTEX = 3
        private const val COLOR_PER_VERTEX = 4
        private val P = floatArrayOf(
            2f, 0f, 3.2f, 0f, 4f, 0.8f, 2.8f, 1.3f, 2f, 1.5f, 2f, 2f, 3.2f, 2f
        )
        private val Q = floatArrayOf(
            2f, 0.2f, 2.2f, 0.2f, 3.6f, 0.4f, 2.8f, 1f, 1.5f, 1.5f, 1.6f, 2.2f, 3.2f, 2.2f
        )
    }

}