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

import java.util.ArrayList

class SlateComponent(val width: Int, val height: Int) {


    private var mCurrentLine: Line? = null

    private val mLines = ArrayList<Line>()

    val lineSize: Int
        get() = mLines.size

    class LineElem(var x: Float, var y: Float)

    class Line {
        private val elements = ArrayList<LineElem>()

        val elemSize: Int
            get() = elements.size

        fun addElem(elem: LineElem) {
            elements.add(elem)
        }

        fun getElem(index: Int): LineElem {
            return elements[index]
        }
    }

    fun startLine(x: Float, y: Float) {
        mCurrentLine = Line()
        mCurrentLine!!.addElem(LineElem(x, y))
        mLines.add(mCurrentLine as Line)
    }

    fun endLine() {
        mCurrentLine = null
    }

    fun addLineElem(x: Float, y: Float) {
        if (mCurrentLine != null) {
            mCurrentLine!!.addElem(LineElem(x, y))
        }
    }

    fun getLine(index: Int): Line {
        return mLines[index]
    }

    fun clear() {
        mLines.clear()
    }
}