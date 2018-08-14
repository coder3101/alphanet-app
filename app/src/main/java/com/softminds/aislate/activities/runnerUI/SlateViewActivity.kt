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

package com.softminds.aislate.activities.runnerUI

import android.annotation.SuppressLint
import android.graphics.PointF
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.softminds.aislate.BuildConfig
import com.softminds.aislate.R
import com.softminds.aislate.ai.*
import com.softminds.aislate.AlphanetBase
import com.softminds.aislate.slate.SlateComponent
import com.softminds.aislate.utils.Constants
import kotlinx.android.synthetic.main.activity_slate_view.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

class SlateViewActivity : RunnerUIBase(), View.OnTouchListener, OnPredictClicked, PredictionResulted {

    private var showingIndex = 0
    private var prediction0: Prediction = Prediction(0, -1F)
    private var prediction1: Prediction = Prediction(0, -1F)
    private var prediction2: Prediction = Prediction(0, -1F)

    private var pointF = PointF()
    private var lastX: Float = 0F
    private var lastY: Float = 0F

    private lateinit var slateHelper: SlateComponent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        metaData = intent.getParcelableExtra(Constants.ExtraIntentConstants.MODEL_META_DATA)
        uri = intent.getParcelableExtra(Constants.ExtraIntentConstants.LOCAL_RUN_PATH)

        setContentView(R.layout.activity_slate_view)

        slateHelper = SlateComponent(metaData.width, metaData.height)

        slateView2.setModel(slateHelper)
        slateView2.setOnTouchListener(this)

        setClickListeners()
        next_result.hide()

        setPredictButton(findViewById(R.id.predict))
        setPredictCallback(this)

        last_result.hide()
    }

    override fun onResume() {
        slateView2.onResume()
        super.onResume()
    }

    override fun onPause() {
        slateView2.onPause()
        super.onPause()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val action = event?.action!! and MotionEvent.ACTION_MASK

        return when (action) {
            MotionEvent.ACTION_DOWN -> {
                processTouchDown(event)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                processTouchMove(event)
                true
            }
            MotionEvent.ACTION_UP -> {
                processTouchUp()
                true
            }
            else -> false
        }
    }

    private fun processTouchUp() {
        slateHelper.endLine()
        slateView2.invalidate()
    }

    private fun processTouchMove(event: MotionEvent) {
        slateView2.calcPos(event.x, event.y, pointF)
        slateHelper.addLineElem(pointF.x, pointF.y)
        slateView2.invalidate()
        lastY = event.y
        lastX = event.x
    }

    private fun processTouchDown(event: MotionEvent) {
        lastX = event.x
        lastY = event.y
        slateView2.calcPos(lastX, lastY, pointF)
        slateHelper.startLine(pointF.x, pointF.y)
        slateView2.invalidate()
    }


    private fun setClickListeners() {
        clear.setOnClickListener {
            slateView2.reset()
            slateHelper.clear()
            slateView2.invalidate()
            next_result.hide()
            last_result.hide()
            result.text = ""
        }


        next_result.setOnClickListener {
            last_result.show()
            if (showingIndex + 1 == 2) {
                next_result.hide()
                showResult(2)
                showingIndex++
            } else {
                showResult(showingIndex + 1)
                showingIndex++
            }

        }

        last_result.setOnClickListener {
            next_result.show()
            if (showingIndex == 1) {
                last_result.hide()
                showResult(0)
                showingIndex--
            } else {
                showResult(showingIndex - 1)
                showingIndex--
            }

        }
    }

    private fun showResult(index: Int) {
        val prediction: Prediction = when (index) {
            0 -> prediction0
            1 -> prediction1
            else -> prediction2
        }
        val stringBuilder = StringBuilder()
        stringBuilder.append("Using : ").append(metaData.name).append("\n")
        stringBuilder.append("Prediction is : ")
                .append(metaData.outputLabels[prediction.index])
                .append(" ")
                .append("\nConfidence is : ")
                .append(prediction.confidence * 100)
        result.text = stringBuilder.toString()
    }


    override fun predictButtonClicked() {
        Log.d("predictButtonClicked", true.toString())
        progress.visibility = View.VISIBLE
        predict.isEnabled = false
        clear.isEnabled = false
        result.text = ""

        val inputs = slateView2.pixelData
        if (metaData.name != null && inputs != null) {
            val params = ModelParams(metaData, uri, inputs)
            val runner = ModelRunner(params, this)
            runner.execute(this)
        }
    }

    override fun onPredictionResulted(response: FloatArray?) {
        Log.d("predictResulted", response?.contentToString())
        clear.isEnabled = true
        predict.isEnabled = true
        progress.visibility = View.GONE

        prediction0.index = 0
        prediction0.confidence = -1F

        prediction1.index = 0
        prediction1.confidence = -1F

        prediction2.index = 0
        prediction2.confidence = -1F


        if (response != null) {

            for (i in 0 until response.size) {
                val newConfidence = response[i]
                if (newConfidence >= prediction0.confidence ||
                        newConfidence >= prediction1.confidence ||
                        newConfidence >= prediction2.confidence) {
                    when {
                        newConfidence >= prediction0.confidence -> {
                            prediction2.confidence = prediction1.confidence
                            prediction2.index = prediction1.index
                            prediction1.confidence = prediction0.confidence
                            prediction1.index = prediction0.index
                            prediction0.confidence = newConfidence
                            prediction0.index = i

                        }
                        newConfidence >= prediction1.confidence -> {
                            prediction2.confidence = prediction1.confidence
                            prediction2.index = prediction1.index
                            prediction1.confidence = newConfidence
                            prediction1.index = i
                        }
                        newConfidence >= prediction2.confidence -> {
                            prediction2.confidence = newConfidence
                            prediction2.index = i
                        }
                    }
                }
            }
            showingIndex = 0
            showResult(0)
            next_result.show()
        }
    }

    override fun onEmptyPredictCalled() {
        predict.isEnabled = true
        clear.isEnabled = true
        progress.visibility = View.GONE
        clear.performClick()
        Toast.makeText(applicationContext, getString(R.string.must_draw_something_to_slate), Toast.LENGTH_SHORT).show()
    }

    override fun onErrorEncountered() {
        predict.isEnabled = true
        clear.isEnabled = true
        progress.visibility = View.GONE
        clear.performClick()
        Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
        Log.e("Error : ", "Something went wrong while inference")
    }

}
