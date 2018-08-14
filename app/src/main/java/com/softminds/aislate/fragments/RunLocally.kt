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

package com.softminds.aislate.fragments


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.softminds.aislate.R
import com.softminds.aislate.activities.LogViewer
import com.softminds.aislate.activities.runnerUI.RawInputActivity
import com.softminds.aislate.activities.runnerUI.SlateViewActivity
import com.softminds.aislate.ai.*
import com.softminds.aislate.utils.Constants
import kotlinx.android.synthetic.main.fragment_run_locally.*
import java.util.*
import kotlin.collections.ArrayList

class RunLocally : Fragment(), PredictionResulted {
    private var targetUri: Uri? = null

    private val uploadRequestCode = 445

    private lateinit var modelData: ModelMetaData

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_run_locally, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        local_run_model_feature_count.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && local_run_model_feature_count.text.isNullOrEmpty()) local_run_model_feature_count.error = "Missing"; }
        local_run_model_output_node_count.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && local_run_model_output_node_count.text.isNullOrEmpty()) local_run_model_output_node_count.error = "Missing"; }
        //local_run_model_dropout_value.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && local_run_model_dropout_value.text.isNullOrEmpty()) local_run_model_dropout_value.requestFocus(); local_run_model_dropout_value.error = "Missing"; }
        local_run_type_conv_inputs_height.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && local_run_type_conv_inputs_height.text.isNullOrEmpty()) local_run_type_conv_inputs_height.error = "Missing"; }
        local_run_type_conv_inputs_width.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && local_run_type_conv_inputs_width.text.isNullOrEmpty()) local_run_type_conv_inputs_width.error = "Missing"; }


        local_run_model_type.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.local_run_model_type_feed) {
                local_run_input_feature_container.visibility = View.VISIBLE
                local_run_type_conv_inputs.visibility = View.GONE
            } else {
                local_run_input_feature_container.visibility = View.GONE
                local_run_type_conv_inputs.visibility = View.VISIBLE
            }
        }

        local_run_model_dropout_required.setOnCheckedChangeListener { _, isChecked -> local_run_model_dropout_value.isEnabled = isChecked }

        run_local_choose_file.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            if (intent.resolveActivity(context!!.packageManager) != null)
                startActivityForResult(intent, uploadRequestCode)
            else Toast.makeText(context, getString(R.string.no_file_explorer), Toast.LENGTH_SHORT).show()
        }

        run_local_run_file.setOnClickListener {
            if (validated()) {
                modelData = ModelMetaData("Test Model")
                modelData.emptyCallsAllowed = true //Just allow with Zeros calls

                if (local_run_model_type.checkedRadioButtonId == R.id.local_run_model_type_feed) {
                    modelData.inputDimension = local_run_model_feature_count.text.toString().toInt()
                    modelData.runnerUi = RunnerUI.RAW_INPUT
                    modelData.type = ModelType.FEED_FORWARD
                    modelData.height = 0
                    modelData.width = 0
                    val lk = ArrayList<String>()
                    for(i in 0 until modelData.inputDimension)
                        lk.add("Feature $i")
                    modelData.inputProperties = lk
                }

                if (local_run_model_type.checkedRadioButtonId == R.id.local_run_model_type_conv) {
                    modelData.height = local_run_type_conv_inputs_height.text.toString().toInt()
                    modelData.width = local_run_type_conv_inputs_width.text.toString().toInt()
                    modelData.runnerUi = RunnerUI.SLATE_VIEW
                    modelData.type = ModelType.CONVOLUTION
                    modelData.inputDimension = modelData.height * modelData.width
                }
                modelData.isExtraRequired = local_run_model_dropout_required.isChecked
                modelData.outputDimension = local_run_model_output_node_count.text.toString().toInt()
                val list = ArrayList<String>()
                for (i in 0 until modelData.outputDimension)
                    list.add("class at $i index")
                modelData.outputLabels = list


                val randomInput = FloatArray(modelData.inputDimension)
                for (i in 0 until modelData.inputDimension)
                    randomInput[i] = Random().nextFloat()

                var drp: Float = if (local_run_model_dropout_value.text.isNullOrEmpty()) 0F else local_run_model_dropout_value.text.toString().toFloat()
                if (drp > 1 && !(local_run_model_dropout_value.text.isNullOrEmpty()) && local_run_model_dropout_required.isChecked) {
                    Toast.makeText(context, getString(R.string.invalid_dropout), Toast.LENGTH_SHORT).show()
                    local_run_model_dropout_value.setText("0")
                    drp = 0F
                }
                ModelRunner(ModelParams(modelData, targetUri!!, randomInput), this, drp).execute(context)

            }
        }


    }

    private fun validated(): Boolean {
        if (local_run_model_type.checkedRadioButtonId == -1) {
            Toast.makeText(context, getString(R.string.model_type_selection_is_missing), Toast.LENGTH_SHORT).show()
            return false
        }

        if (local_run_model_feature_count.text.isNullOrEmpty() && local_run_model_type.checkedRadioButtonId == R.id.local_run_model_type_feed) {
            local_run_model_feature_count.requestFocus()
            return false
        }

        if ((local_run_type_conv_inputs_width.text.isNullOrEmpty() || local_run_type_conv_inputs_height.text.isNullOrEmpty()) && local_run_model_type.checkedRadioButtonId == R.id.local_run_model_type_conv) {
            if (local_run_type_conv_inputs_height.text.isNullOrEmpty())
                local_run_type_conv_inputs_height.requestFocus()
            else
                local_run_type_conv_inputs_width.requestFocus()
            return false
        }

        if (local_run_model_output_node_count.text.isNullOrEmpty()) {
            local_run_model_output_node_count.requestFocus()
            return false
        }

        if (local_run_model_dropout_required.isChecked && local_run_model_dropout_value.text.toString().isEmpty()) {
            Toast.makeText(context, getString(R.string.no_dropout_runing_with_zero), Toast.LENGTH_SHORT).show()
        }

        if (targetUri == null) {
            Toast.makeText(context, R.string.protobuf_missing_upload, Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == uploadRequestCode) {
            val dataURI = data?.data
            val name = dataURI!!.lastPathSegment
            if (name.endsWith(".pb")) {
                targetUri = dataURI
                local_run_model_proto_name.text = name
            } else {
                Toast.makeText(context, R.string.not_a_proto_file, Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(context, R.string.cancelled_by_user, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPredictionResulted(response: FloatArray?) {

        val i = Intent(context, when (modelData.runnerUi) {
            RunnerUI.RAW_INPUT -> RawInputActivity::class.java
            RunnerUI.SLATE_VIEW -> SlateViewActivity::class.java
            RunnerUI.CAMERA_VIEW -> RawInputActivity::class.java
            else -> null
        })
        i.putExtra(Constants.ExtraIntentConstants.MODEL_META_DATA, modelData as Parcelable)
        i.putExtra(Constants.ExtraIntentConstants.LOCAL_RUN_PATH, targetUri)
        startActivity(i)

    }

    override fun onEmptyPredictCalled() {
        Log.d("Run Locally", "Empty array called as input")
    }

    override fun onErrorEncountered() {

        val inputDimen = when (local_run_model_type.checkedRadioButtonId) {
            R.id.local_run_model_type_feed -> local_run_model_feature_count.text.toString().toInt()
            R.id.local_run_model_type_conv -> local_run_type_conv_inputs_height.text.toString().toInt() * local_run_type_conv_inputs_width.text.toString().toInt()
            else -> -1
        }

        AlertDialog.Builder(context)
                .setNegativeButton(R.string.close, { dialog, _ -> dialog.dismiss() })
                .setPositiveButton(R.string.view_logs, { dialog, _ ->
                    dialog.dismiss()
                    val i = Intent(context, LogViewer::class.java)
                    i.putExtra(Constants.ExtraIntentConstants.MODEL_META_DATA, modelData as Parcelable)
                    i.putExtra(Constants.ExtraIntentConstants.LOCAL_RUN_PATH, targetUri)
                    startActivity(i)
                })
                .setTitle(getString(R.string.validation_local_failed))
                .setMessage("We tested your model by feeding $inputDimen dimension random float, and we expected your model to produce a ${local_run_model_output_node_count.text.toString().toInt()} dimensional output, but it didn't. Check the logs for more details.")
                .show()
    }


}
