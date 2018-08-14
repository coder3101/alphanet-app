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


import android.app.AlertDialog
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.softminds.aislate.R
import com.softminds.aislate.ai.ModelParams
import com.softminds.aislate.ai.ModelRunner
import com.softminds.aislate.ai.Prediction
import com.softminds.aislate.ai.PredictionResulted
import com.softminds.aislate.utils.Constants
import kotlinx.android.synthetic.main.activity_raw_input.*

class RawInputActivity : RunnerUIBase(), OnPredictClicked, PredictionResulted {


    private val startID = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        metaData = intent.getParcelableExtra(Constants.ExtraIntentConstants.MODEL_META_DATA)
        uri = intent.getParcelableExtra(Constants.ExtraIntentConstants.LOCAL_RUN_PATH)
        setContentView(R.layout.activity_raw_input)


        buildView(metaData.inputProperties)
        setPredictButton(findViewById(R.id.raw_input_run_botton))
        setPredictCallback(this)


    }

    private fun extractAsVector(): FloatArray {
        // inputProperties Size must match inputDimensions
        val result = FloatArray(metaData.inputProperties.size)
        for (i in startID until startID + metaData.inputProperties.size)
            result[i - startID] = findViewById<EditText>(i).text.toString().toFloat()
        return result
    }

    private fun validated(): Boolean {
        for (i in startID until startID + metaData.inputProperties.size) {
            if (findViewById<EditText>(i).text.toString().isEmpty()) {
                findViewById<EditText>(i).requestFocus()
                findViewById<EditText>(i).error = "Missing"
                return false
            }
        }
        return true
    }

    private fun buildView(list: ArrayList<String>) {
        var temp = startID
        for (elem in list) {
            val editText = EditText(this)
            editText.id = temp++
            editText.hint = elem
            editText.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && editText.text.toString().isEmpty()) editText.error = "Missing" }
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
            val layout = TextInputLayout(this)
            val param  = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            layout.addView(editText,param)
            raw_input_linear_layout.addView(layout,param)
        }

    }


    override fun predictButtonClicked() {
        if (validated()) {
            val inputs = extractAsVector()
            val param = ModelParams(metaData, uri, inputs)
            ModelRunner(param, this).execute(this)
        }
    }


    override fun onPredictionResulted(response: FloatArray?) {
        val first = Prediction(-1, -1F)
        val second = Prediction(-1, -1F)
        val third = Prediction(-1, -1F)


        for (i in 0 until response!!.size)
            if (response[i] > first.confidence) {
                first.confidence = response[i]
                first.index = i
            }

        for (i in 0 until response.size)
            if (response[i] < first.confidence && response[i] > second.confidence) {
                second.confidence = response[i]
                second.index = i
            }

        for (i in 0 until response.size)
            if (response[i] < second.confidence && response[i] > third.confidence) {
                third.confidence = response[i]
                third.index = i
            }

        try {
            val allRes = "1 => ${metaData.outputLabels[first.index]} Confidence : ${first.confidence * 100}%\n" +
                    "2 => ${metaData.outputLabels[second.index]} Confidence : ${second.confidence * 100}%\n" +
                    "3 => ${metaData.outputLabels[third.index]} Confidence : ${third.confidence * 100}%"

            val completeRes = AlertDialog.Builder(this).setTitle("Response")
                    .setMessage(allRes)
                    .setPositiveButton(R.string.okay) { dialog, _ -> dialog.dismiss() }.create()

            AlertDialog.Builder(this).setTitle(metaData.outputLabels[first.index])
                    .setMessage("The model says it is ${metaData.outputLabels[first.index]} with a confidence of ${first.confidence * 100}%")
                    .setPositiveButton(getString(R.string.more_info)
                    ) { dialog, _ ->
                        dialog.dismiss()
                        completeRes.show()
                    }
                    .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                    .show()
        }catch(e: ArrayIndexOutOfBoundsException){
            Toast.makeText(this,R.string.garbage_run,Toast.LENGTH_SHORT).show()
        }

    }

    override fun onEmptyPredictCalled() {
        //will not be called if empty calls allowed is set to true
        Toast.makeText(this, R.string.all_empty, Toast.LENGTH_SHORT).show()
    }

    override fun onErrorEncountered() {
        Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
    }
}
