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

package com.softminds.aislate.activities

import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.softminds.aislate.R
import com.softminds.aislate.ai.ModelMetaData
import com.softminds.aislate.utils.Constants
import kotlinx.android.synthetic.main.activity_log_viewer.*
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

class LogViewer : AppCompatActivity(), OnResult {

    override fun resulted(reason: String) {
        progress_bar_log.visibility = View.GONE
        log.visibility = View.VISIBLE
        log.text = reason
    }

    class AsyncWorker(private val modelMetaData: ModelMetaData, private val randomInputs: FloatArray, private val res : OnResult) : AsyncTask<InputStream, Void, String>() {

        override fun onPostExecute(result: String?) {
            res.resulted(result!!)
        }


        override fun doInBackground(vararg params: InputStream?): String {
            val i = params[0]
            try {
                val tensorFlowInferenceInterface = TensorFlowInferenceInterface(i)
                tensorFlowInferenceInterface.feed(Constants.NeuralNetworkParams.DEFAULT_INPUT_NODE, randomInputs, 1, modelMetaData.inputDimension.toLong())
                if (modelMetaData.isExtraRequired)
                    tensorFlowInferenceInterface.feed(Constants.NeuralNetworkParams.DEFAULT_DROPOUT_NAME, floatArrayOf(0F), 1)
                tensorFlowInferenceInterface.run(arrayOf(Constants.NeuralNetworkParams.DEFAULT_OUTPUT_NAME))
                val f = FloatArray(modelMetaData.outputDimension)
                tensorFlowInferenceInterface.fetch(Constants.NeuralNetworkParams.DEFAULT_OUTPUT_NAME, f)
            } catch (e: Exception) {
                val stringBuilder = StringBuilder()
                stringBuilder.append("Message : ${e.message}\n\n")
                        .append("Localized Message : ${e.localizedMessage}\n\n")
                        .append("************* STACK TRACES ******************\n\n")
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))

                stringBuilder.append(sw.toString())
                return stringBuilder.toString()
            }
            return "No Errors were encountered while running the model."
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_viewer)
        val metaData = intent.getParcelableExtra<ModelMetaData>(Constants.ExtraIntentConstants.MODEL_META_DATA)
        val targetUri = intent.getParcelableExtra<Uri>(Constants.ExtraIntentConstants.LOCAL_RUN_PATH)
        if (metaData == null || targetUri == null) {
            Log.d("LogViewer", "meta data was null : ${metaData == null} \nuri was null : ${targetUri == null}")
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
            finish()
        } else
            runModel(generatedInputs(metaData), metaData, targetUri)
    }

    private fun runModel(randomInputs: FloatArray, metaData: ModelMetaData, uri: Uri) {
        AsyncWorker(metaData, randomInputs, this).execute(contentResolver.openInputStream(uri))
    }

    private fun generatedInputs(modelMetaData: ModelMetaData): FloatArray {
        val res = FloatArray(modelMetaData.inputDimension)
        for (i in 0 until modelMetaData.inputDimension)
            res[i] = Random().nextFloat()
        return res
    }


}

interface OnResult {
    fun resulted(reason: String)
}
