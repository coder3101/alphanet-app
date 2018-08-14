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

package com.softminds.aislate.ai

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.softminds.aislate.utils.Constants
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.io.InputStream

class ModelRunner(private val modelParams: ModelParams, private val listeners: PredictionResulted, private val dropOut: Float = 0F) : AsyncTask<Context, Void, FloatArray>() {

    private var stream: InputStream? = null

    override fun onPreExecute() {
        var empty = true
        for (i in modelParams.inputs) {
            if (i != 0F) {
                empty = false
                break
            }
        }
        if (empty && !modelParams.modelMeta.emptyCallsAllowed) {
            listeners.onEmptyPredictCalled()
            cancel(true)
        }
    }

    override fun onPostExecute(result: FloatArray?) {
        if (result == null)
            listeners.onErrorEncountered()
        else
            listeners.onPredictionResulted(result)
    }

    override fun doInBackground(vararg params: Context?): FloatArray? {
        val result = FloatArray(modelParams.modelMeta.outputDimension)
        Log.d("ModelRunner", "Started the execution of ${modelParams.modelMeta.name}")

        return try {
            stream = params[0]!!.contentResolver!!.openInputStream(modelParams.uri)
            val tensorFlowInferenceInterface = TensorFlowInferenceInterface(stream)
            tensorFlowInferenceInterface.feed(Constants.NeuralNetworkParams.DEFAULT_INPUT_NODE, modelParams.inputs, 1L, modelParams.modelMeta.inputDimension.toLong())
            if (modelParams.modelMeta.isExtraRequired)
                tensorFlowInferenceInterface.feed(Constants.NeuralNetworkParams.DEFAULT_DROPOUT_NAME, floatArrayOf(dropOut), 1)
            //for (i in 0 until modelParams.extraInputs!!.size)
            //   tensorFlowInferenceInterface.feed(modelParams.extraFeedNode!![i], modelParams.extraInputs!![i], *modelParams.extraDimen!![i])
            tensorFlowInferenceInterface.run(arrayOf(Constants.NeuralNetworkParams.DEFAULT_OUTPUT_NAME))
            //todo(coder3101) : You can improve this  to run multiple nodes at once
            tensorFlowInferenceInterface.fetch(Constants.NeuralNetworkParams.DEFAULT_OUTPUT_NAME, result)
            if (stream != null)
                stream!!.close()
            result
        } catch (e: Exception) {
            Log.e("ModelRunner", "Error in running graph", e)

            null //calls onError
        }
    }
}

interface PredictionResulted {
    fun onPredictionResulted(response: FloatArray?)
    fun onEmptyPredictCalled()
    fun onErrorEncountered()
}
