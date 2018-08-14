/*
 * Copyright (c) 2018. <ashar786khan@gmail.com>
 * This file is part of Alphanet's Android Application.
 * Alphanet 's Android Application is free software : you can redistribute it and/or modify
 * it under the terms of GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This Application is distributed in the hope that it will be useful
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General  Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this Source File.
 *  If not, see <http:www.gnu.org/licenses/>.
 */

package com.softminds.aislate.slate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View


class SlateView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val mPaint = Paint()
    private var mModel: SlateComponent? = null

    private var mOffscreenBitmap: Bitmap? = null
    private var mOffscreenCanvas: Canvas? = null

    private val mMatrix = Matrix()
    private val mInvMatrix = Matrix()
    private var mDrawnLineSize = 0
    private var mSetup = false

    private val mTmpPoints = FloatArray(2)

    val pixelData: FloatArray?
        get() {
            if (mOffscreenBitmap == null) {
                return null
            }

            val width = mOffscreenBitmap!!.width
            val height = mOffscreenBitmap!!.height
            val pixels = IntArray(width * height)
            mOffscreenBitmap!!.getPixels(pixels, 0, width, 0, 0, width, height)

            val retPixels = FloatArray(pixels.size)
            for (i in pixels.indices) {
                val pix = pixels[i]
                val b = pix and 0xff
                retPixels[i] = ((0xff - b) / 255.0).toFloat()
            }
            return retPixels
        }

    fun setModel(model: SlateComponent) {
        this.mModel = model
    }

    fun setPixelData(data: FloatArray) {
        if (data.size != height * width) {
            throw RuntimeException("The dimensions for image and pixel do not matches" + "Required : " + height * width + " found " + data.size)
        } else {

            if (mOffscreenBitmap != null) {

                val pixelInt = IntArray(data.size)
                for (i in data.indices) {
                    val pix = data[i]
                    val buffer = pix * 255
                    pixelInt[i] = buffer.toInt()
                }

                mOffscreenBitmap!!.setPixels(pixelInt, 0, width, 0, 0, width, height)
                invalidate()
            } else {
                createBitmap()
                setPixelData(data)
            }
        }
    }

    fun reset() {
        mDrawnLineSize = 0
        if (mOffscreenBitmap != null) {
            mPaint.color = Color.WHITE
            val width = mOffscreenBitmap!!.width
            val height = mOffscreenBitmap!!.height
            mOffscreenCanvas!!.drawRect(Rect(0, 0, width, height), mPaint)
        }
    }

    private fun setup() {
        mSetup = true

        val width = width.toFloat()
        val height = height.toFloat()

        val modelWidth = mModel!!.width.toFloat()
        val modelHeight = mModel!!.height.toFloat()

        val scaleW = width / modelWidth
        val scaleH = height / modelHeight

        var scale = scaleW
        if (scale > scaleH) {
            scale = scaleH
        }

        val newCx = modelWidth * scale / 2
        val newCy = modelHeight * scale / 2
        val dx = width / 2 - newCx
        val dy = height / 2 - newCy

        mMatrix.setScale(scale, scale)
        mMatrix.postTranslate(dx, dy)
        mMatrix.invert(mInvMatrix)
        mSetup = true
    }

    public override fun onDraw(canvas: Canvas) {
        if (mModel == null) {
            return
        }
        if (!mSetup) {
            setup()
        }
        if (mOffscreenBitmap == null) {
            return
        }

        var startIndex = mDrawnLineSize - 1
        if (startIndex < 0) {
            startIndex = 0
        }

        SlateRenderer.renderModel(mOffscreenCanvas!!, mModel!!, mPaint, startIndex)
        canvas.drawBitmap(mOffscreenBitmap!!, mMatrix, mPaint)

        mDrawnLineSize = mModel!!.lineSize
    }

    fun calcPos(x: Float, y: Float, out: PointF) {
        mTmpPoints[0] = x
        mTmpPoints[1] = y
        mInvMatrix.mapPoints(mTmpPoints)
        out.x = mTmpPoints[0]
        out.y = mTmpPoints[1]
    }

    fun onResume() {
        createBitmap()
    }

    fun onPause() {
        releaseBitmap()
    }

    private fun createBitmap() {
        if (mOffscreenBitmap != null) {
            mOffscreenBitmap!!.recycle()
        }
        mOffscreenBitmap = Bitmap.createBitmap(mModel!!.width, mModel!!.height, Bitmap.Config.ARGB_8888)
        mOffscreenCanvas = Canvas(mOffscreenBitmap!!)
        reset()
    }

    private fun releaseBitmap() {
        if (mOffscreenBitmap != null) {
            mOffscreenBitmap!!.recycle()
            mOffscreenBitmap = null
            mOffscreenCanvas = null
        }
        reset()
    }
}
