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

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint


object SlateRenderer {
    fun renderModel(canvas: Canvas, model: SlateComponent, paint: Paint,
                    startLineIndex: Int) {
        paint.isAntiAlias = true

        val lineSize = model.lineSize
        for (i in startLineIndex until lineSize) {
            val line = model.getLine(i)
            paint.color = Color.BLACK
            val elemSize = line.elemSize
            if (elemSize < 1) {
                continue
            }
            var elem: SlateComponent.LineElem = line.getElem(0)
            var lastX = elem.x
            var lastY = elem.y

            for (j in 0 until elemSize) {
                elem = line.getElem(j)
                val x = elem.x
                val y = elem.y
                canvas.drawLine(lastX, lastY, x, y, paint)
                lastX = x
                lastY = y
            }
        }
    }
}