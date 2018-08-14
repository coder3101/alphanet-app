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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.UploadTask
import com.softminds.aislate.R
import com.softminds.aislate.ai.*
import com.softminds.aislate.utils.Constants
import com.softminds.aislate.utils.ModelManipulator
import kotlinx.android.synthetic.main.activity_publish.*
import kotlinx.android.synthetic.main.step_1_publish_layout.*
import kotlinx.android.synthetic.main.step_2_publish_layout.*
import kotlinx.android.synthetic.main.step_3_publish_layout.*
import java.lang.Exception
import java.util.*

class PublishActivity : AppCompatActivity(), ModelManipulator.UploadListener, PredictionResulted {

    private var filled = false

    private var validFileChosen = false

    private lateinit var modelMetaData: ModelMetaData

    private lateinit var targetUri: Uri

    private val maxFeaturesAllowed = 785

    private val uploadRequestCode = 425

    private lateinit var menuBar: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
        setListeners()
    }

    private fun setListeners() {

        val alphaAnimationAppear = AlphaAnimation(0.0f, 1.0f)
        alphaAnimationAppear.duration = 1000

        val alphaAnimationDisappear = AlphaAnimation(1.0f, 0.0f)
        alphaAnimationDisappear.duration = 1000


        publish_name.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && publish_name.text.isNullOrEmpty()) publish_name.error = "Missing"; }
        publish_description.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && publish_description.text.isNullOrEmpty()) publish_description.error = "Missing"; }
        publish_conv_width.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && publish_conv_width.text.isNullOrEmpty()) publish_conv_width.error = "Missing" }
        publish_conv_height.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && publish_conv_height.text.isNullOrEmpty()) publish_conv_height.error = "Missing" }
        publish_feed_input.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && publish_feed_input.text.isNullOrEmpty()) publish_feed_input.error = "Missing" }
        publish_model_output.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && publish_model_output.text.isNullOrEmpty()) publish_model_output.error = "Missing" }


        publish_accuracy.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && publish_accuracy.text.isNullOrEmpty()) publish_accuracy.error = "Missing" }
        publish_layer_num.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && publish_layer_num.text.isNullOrEmpty()) publish_layer_num.error = "Missing" }
        publish_entropy.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && publish_entropy.text.isNullOrEmpty()) publish_entropy.error = "Missing" }
        publish_training_set.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && publish_training_set.text.isNullOrEmpty()) publish_training_set.error = "Missing" }


        publish_type.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.publish_type_conv -> {
                    publish_conv.visibility = View.VISIBLE
                    publish_feed_container.visibility = View.GONE
                    publish_feed_input.text = null
                }
                R.id.publish_type_feed -> {
                    publish_feed_container.visibility = View.VISIBLE
                    publish_conv.visibility = View.GONE
                    publish_conv_height.text = null
                    publish_conv_width.text = null

                }
                else -> {
                    //recurrent will be here someday
                }
            }
        }

        publish_next_to_2.setOnClickListener {
            if (validated()) {
                menuBar.findItem(R.id.publish_sequence_pixel).isVisible = true
                filled = true
                publish_step_one.startAnimation(alphaAnimationDisappear)

                Handler().postDelayed({
                    publish_step_one.visibility = View.GONE
                    publish_step_two.visibility = View.VISIBLE
                    try {
                        if (publish_type.checkedRadioButtonId == R.id.publish_type_feed) {
                            val i: Int = 1
                            findViewById<TextInputEditText>(i).requestFocus()

                        } else {
                            findViewById<TextInputEditText>(maxFeaturesAllowed + 1).requestFocus()
                        }
                    } catch (e: Exception) {
                        Log.d("Publisher", "Unable to draw focuses", e)
                    }
                    publish_step_two.startAnimation(alphaAnimationAppear)
                }, 1000)
                //dynamically create the views

                val m = resources.getDimensionPixelOffset(R.dimen.standard_margin);

                if (publish_type.checkedRadioButtonId == R.id.publish_type_feed) {
                    for (i in 0 until (Integer.parseInt(publish_feed_input.text.toString()))) {
                        val param = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        param.setMargins(m, m, m, m)
                        val param2 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        val layout = TextInputLayout(this)
                        val editText = TextInputEditText(this)
                        editText.hint = "Enter ${i + 1}th Feature Description"
                        editText.maxLines = 1
                        editText.id = i + 1
                        editText.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && editText.text.isNullOrEmpty()) editText.error = "Missing" }
                        layout.addView(editText, param)
                        val v = View(this)
                        v.setBackgroundColor(ContextCompat.getColor(this, R.color.browser_actions_bg_grey))
                        val param3 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.standard_view_thickness))
                        publish_step_two_root_linear_layout.addView(layout, param2)
                        publish_step_two_root_linear_layout.addView(v, param3)
                        publish_feed_feature_desc_title.visibility = View.VISIBLE
                    }
                }
                for (i in 0 until Integer.parseInt(publish_model_output.text.toString())) {
                    val param2 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    param2.setMargins(m, m, m, m)
                    val layout = TextInputLayout(this)
                    val editText = TextInputEditText(this)
                    editText.id = maxFeaturesAllowed + i + 1
                    editText.hint = "Enter ${i + 1}th Label Description"
                    editText.maxLines = 1
                    editText.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && editText.text.isNullOrEmpty()) editText.error = "Missing" }
                    layout.addView(editText, param2)
                    val v = View(this)
                    v.setBackgroundColor(ContextCompat.getColor(this, R.color.browser_actions_bg_grey))
                    val param3 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.standard_view_thickness))
                    publish_step_two_root_linear_layout2.addView(layout, param2)
                    publish_step_two_root_linear_layout2.addView(v, param3)
                }

            }
        }

        publish_next_to_3.setOnClickListener {
            if (validated2()) {
                menuBar.findItem(R.id.publish_sequence_pixel).isVisible = false
                publish_step_two.startAnimation(alphaAnimationDisappear)
                Handler().postDelayed({
                    publish_step_two.visibility = View.GONE
                    publish_step_three.visibility = View.VISIBLE
                    publish_accuracy.requestFocus()
                    publish_step_three.startAnimation(alphaAnimationAppear)
                }, 1000)

            }
        }

        publish_model_choose.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            if (intent.resolveActivity(packageManager) != null)
                startActivityForResult(intent, uploadRequestCode)
            else Toast.makeText(applicationContext, getString(R.string.no_file_explorer), Toast.LENGTH_SHORT).show()
        }

        publish_final_step.setOnClickListener {
            if (finalValidated()) {
                modelMetaData = ModelMetaData(publish_name.text.toString())
                publish_final_step.isEnabled = false

                modelMetaData.type = when (publish_type.checkedRadioButtonId) {
                    R.id.publish_type_feed -> ModelType.FEED_FORWARD
                    R.id.publish_type_conv -> ModelType.CONVOLUTION
                    else -> ModelType.RECURRENT
                }
                modelMetaData.runnerUi = when (publish_model_architecture.checkedRadioButtonId) {
                    R.id.raw_input_architecture -> RunnerUI.RAW_INPUT
                    R.id.slate_architecture -> RunnerUI.SLATE_VIEW
                    else -> null
                }

                modelMetaData.description = publish_description.text.toString()
                modelMetaData.isVerified = false
                modelMetaData.emptyCallsAllowed = false
                modelMetaData.width = if (publish_conv_width.text.isNullOrEmpty()) 0 else Integer.parseInt(publish_conv_width.text.toString())
                modelMetaData.height = if (publish_conv_height.text.isNullOrEmpty()) 0 else Integer.parseInt(publish_conv_height.text.toString())
                modelMetaData.inputDimension = if (publish_feed_input.text.isNullOrEmpty()) publish_conv_height.text.toString().toInt() * publish_conv_width.text.toString().toInt() else Integer.parseInt(publish_feed_input.text.toString())
                modelMetaData.outputDimension = Integer.parseInt(publish_model_output.text.toString())
                modelMetaData.isExtraRequired = publish_model_drop_out.isChecked


                modelMetaData.downloads = 0
                modelMetaData.version = 1.0
                modelMetaData.id = System.currentTimeMillis()
                modelMetaData.publisherEmail = FirebaseAuth.getInstance().currentUser!!.email!!
                modelMetaData.publisher = FirebaseAuth.getInstance().currentUser!!.displayName!!
                modelMetaData.publishDate = System.currentTimeMillis()
                modelMetaData.accuracy = publish_accuracy.text.toString().toFloat()
                modelMetaData.entropyLoss = publish_entropy.text.toString().toFloatOrNull()
                modelMetaData.layerNum = publish_layer_num.text.toString().toInt()
                modelMetaData.trainingSetName = publish_training_set.text.toString()

                if (!publish_feed_input.text.isNullOrEmpty()) {
                    val inputProp = ArrayList<String>()
                    for (i in 0 until Integer.parseInt(publish_feed_input.text.toString()))
                        inputProp.add(findViewById<TextInputEditText>(i + 1).text.toString())
                    modelMetaData.inputProperties = inputProp
                }

                val outputProp = ArrayList<String>()
                for (i in 0 until Integer.parseInt(publish_model_output.text.toString()))
                    outputProp.add(findViewById<TextInputEditText>(i + maxFeaturesAllowed + 1).text.toString())
                modelMetaData.outputLabels = outputProp

                validateModel()
            }
        }
    }

    private fun finalValidated(): Boolean {
        if (publish_entropy.text.isNullOrEmpty()) {
            publish_entropy.requestFocus()
            return false
        }
        if (publish_layer_num.text.isNullOrEmpty()) {
            publish_layer_num.requestFocus()
            return false
        }
        if (publish_accuracy.text.isNullOrEmpty()) {
            publish_accuracy.requestFocus()
            return false
        }
        if (publish_training_set.text.isNullOrEmpty()) {
            publish_training_set.requestFocus()
            return false
        }

        if (!validFileChosen) {
            Toast.makeText(applicationContext, getString(R.string.protobuf_missing_upload), Toast.LENGTH_SHORT).show()
            return false
        }
        if (publish_accuracy.text.toString().toFloat() >= 100) {
            Toast.makeText(applicationContext, getString(R.string.more_than_100_accuracy), Toast.LENGTH_SHORT).show()
            return false
        }

        if (publish_accuracy.text.toString().toFloat() <= 75) {
            Toast.makeText(applicationContext, getString(R.string.model_less_than_accuracy), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun validated2(): Boolean {
        val inputSize = if (publish_type.checkedRadioButtonId == R.id.publish_type_feed) Integer.parseInt(publish_feed_input.text.toString()) else maxFeaturesAllowed
        if (inputSize != maxFeaturesAllowed) {
            for (i in 0 until inputSize) {
                if (findViewById<TextInputEditText>(i + 1).text.isNullOrEmpty()) {
                    findViewById<TextInputEditText>(i + 1).requestFocus()
                    return false
                }
            }
        }
        for (i in 0 until Integer.parseInt(publish_model_output.text.toString())) {
            if (findViewById<TextInputEditText>(i + maxFeaturesAllowed + 1).text.isNullOrEmpty()) {
                findViewById<TextInputEditText>(i + maxFeaturesAllowed + 1).requestFocus()
                return false
            }
        }
        return true
    }

    private fun validated(): Boolean {

        if (publish_model_architecture.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Network Architecture is required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (publish_name.text.isNullOrEmpty()) {
            publish_next_to_2.requestFocus()
            return false
        }
        if (publish_description.text.isNullOrEmpty()) {
            publish_description.requestFocus()
            return false
        }
        if (publish_type.checkedRadioButtonId == -1) {
            Toast.makeText(applicationContext, getString(R.string.selection_required), Toast.LENGTH_SHORT).show()
            return false
        }
        if (publish_type.checkedRadioButtonId == R.id.publish_type_feed && publish_feed_input.text.isNullOrEmpty()) {
            publish_feed_input.requestFocus()
            return false
        }
        if (publish_type.checkedRadioButtonId == R.id.publish_type_conv && (publish_conv_width.text.isNullOrEmpty() || publish_conv_height.text.isNullOrEmpty())) {
            if (publish_conv_height.text.isNullOrEmpty())
                publish_conv_height.requestFocus()
            else
                publish_conv_width.requestFocus()
            return false
        }
        if (publish_model_output.text.isNullOrEmpty()) {
            publish_model_output.requestFocus()
            return false
        }
        if (!publish_feed_input.text.toString().isBlank() && publish_feed_input.text.toString().toInt() > maxFeaturesAllowed) {
            publish_feed_input.requestFocus()
            publish_feed_input.error = "Should be less than $maxFeaturesAllowed"
            return false
        }

        //fixme:Not getting called if large values are coming
        if ((!publish_feed_input.text.toString().isBlank() && publish_feed_input.text.toString().toInt() > 100) || (!publish_model_output.text.toString().isBlank() && publish_model_output.text.toString().toInt() > 100)) {
            Toast.makeText(applicationContext, getString(R.string.may_take_long), Toast.LENGTH_SHORT).show()
        }

        return true
    }

    override fun onBackPressed() {
        if (filled)
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.progress_loss_warning))
                    .setTitle(getString(R.string.sure_to_leave))
                    .setPositiveButton(getString(R.string.yes)) { dialog, _ -> dialog.dismiss(); super.onBackPressed(); }
                    .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
                    .show()
        else super.onBackPressed()
    }

    override fun onUploadCompleted(taskSnapshot: UploadTask.TaskSnapshot) {
        taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri: Uri? ->
            modelMetaData.downloadUrl = uri.toString()
            modelMetaData.fileSize = taskSnapshot.totalByteCount
            val ref = FirebaseDatabase.getInstance().reference
            publish_progress_text.text = getString(R.string.almost_done)
            val p0 = FirebaseDatabase.getInstance().reference.push().key
            modelMetaData.self = p0
            modelMetaData.upvote = 0

            ref.child(Constants.FirebaseStorageConstants.ROOT)
                    .child(Constants.FirebaseStorageConstants.MODELS_PATH)
                    .child(when (modelMetaData.type) {
                        ModelType.FEED_FORWARD -> Constants.FirebaseStorageConstants.FEED_FORWARD
                        ModelType.RECURRENT -> Constants.FirebaseStorageConstants.RECURRENT
                        ModelType.CONVOLUTION -> Constants.FirebaseStorageConstants.CONVOLUTION
                        else -> {
                            "null"
                        }
                    })
                    .child(p0!!)
                    .setValue(modelMetaData)
                    .addOnSuccessListener {

                        AlertDialog.Builder(this)
                                .setTitle(getString(R.string.model_published))
                                .setMessage("Your model ${modelMetaData.name} was published and is under review by us. \n\nYou will get a confirmation mail once your model is verified.\nThis usually takes less than 6 hours.")
                                .setPositiveButton(R.string.okay) { dialog, _ -> dialog.dismiss(); finish() }
                                .setOnCancelListener { finish() }
                                .show()
                    }
        }

    }

    override fun onUploadFailed(exception: Exception) {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.upload_failed))
                .setMessage("Error in publishing the model \nReason : ${exception.message}\nReason2 : ${exception.localizedMessage}")
                .setOnCancelListener { finish() }
                .setPositiveButton(R.string.okay) { dialog, _ -> dialog.dismiss(); finish() }
                .show()
    }

    override fun onUploadProgressUpdate(param: Int) {
        publish_upload_progress.progress = param
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(applicationContext, getString(R.string.cancelled_by_user), Toast.LENGTH_SHORT).show()
        }

        if (requestCode == uploadRequestCode && resultCode == Activity.RESULT_OK) {
            val uri = data!!.data
            if (uri == null) {
                Log.d("URI", "Uri is null and cannot upload")
                Toast.makeText(applicationContext, getString(R.string.no_readable_or_no_exist), Toast.LENGTH_SHORT).show()
            } else {
                val name = uri.lastPathSegment
                if (!name.endsWith(".pb"))
                    Toast.makeText(applicationContext, getString(R.string.not_a_proto_file), Toast.LENGTH_SHORT).show()
                else {
                    validFileChosen = true
                    targetUri = uri
                    publish_target_file_name.text = uri.lastPathSegment
                }
            }
        } else {
            Log.d("URI", "Activity not okay returned")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.publish_activity_menu, menu)
        menuBar = menu!!
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when {
            item?.itemId == R.id.publish_clear -> {
                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.sure_to_clear))
                        .setMessage(getString(R.string.clear_all_content_warn))
                        .setPositiveButton(R.string.yes) { dialog, _ ->
                            dialog.dismiss()
                            if (publish_step_one.visibility == View.VISIBLE) {
                                publish_name.text = null
                                publish_description.text = null
                                publish_model_output.text = null
                            }
                            if (publish_step_two.visibility == View.VISIBLE) {
                                val inputSize = if (publish_type.checkedRadioButtonId == R.id.publish_type_feed) Integer.parseInt(publish_feed_input.text.toString()) else maxFeaturesAllowed
                                if (inputSize != maxFeaturesAllowed) {
                                    for (i in 0 until inputSize)
                                        findViewById<TextInputEditText>(i + 1).text = null
                                }
                                for (i in 0 until Integer.parseInt(publish_model_output.text.toString()))
                                    findViewById<TextInputEditText>(i + maxFeaturesAllowed + 1).text = null
                            }

                            if (publish_step_three.visibility == View.VISIBLE) {
                                publish_accuracy.text = null
                                publish_layer_num.text = null
                                publish_entropy.text = null
                                publish_training_set.text = null
                            }
                        }
                        .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                        .show()
                true
            }
            item?.itemId == R.id.publish_sequence_pixel -> {
                if (publish_step_two.visibility == View.VISIBLE) {
                    val inputSize = if (publish_type.checkedRadioButtonId == R.id.publish_type_feed) Integer.parseInt(publish_feed_input.text.toString()) else maxFeaturesAllowed
                    if (inputSize != maxFeaturesAllowed) {
                        for (i in 0 until inputSize) {
                            val param = "Pixel ${i + 1} th is : "
                            findViewById<TextInputEditText>(i + 1).setText(param)
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun validateModel() {
        publish_progress_text.visibility = View.VISIBLE

        val inputArray = FloatArray(if (publish_type.checkedRadioButtonId == R.id.publish_type_conv) publish_conv_width.text.toString().toInt() * publish_conv_height.text.toString().toInt() else publish_feed_input.text.toString().toInt())
        for (i in 0 until inputArray.size)
            inputArray[i] = Random().nextFloat()

        publish_progress_text.text = getString(R.string.testing_model_message)
        ModelRunner(ModelParams(modelMetaData, targetUri, inputArray), this).execute(applicationContext)

    }

    override fun onPredictionResulted(response: FloatArray?) {
        if (response!!.size == publish_model_output.text.toString().toInt()) {
            publish_progress_text.text = getString(R.string.uploading_proto)
            ModelManipulator(this).publishModel(targetUri, modelMetaData.type!!, modelMetaData.name!!, this)
        } else {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.invalid_output_dimen))
                    .setMessage("Your model had to produce an output ${modelMetaData.outputDimension} dimensions but it produced output of dimension ${response.size}.")
                    .setPositiveButton(R.string.okay
                    ) { dialog, _ ->
                        dialog.dismiss()
                        publish_final_step.isEnabled = true
                        publish_progress_text.visibility = View.GONE
                    }
                    .setCancelable(false)
                    .show()

        }
    }

    override fun onEmptyPredictCalled() {
        Log.e("Publish", "Failed to load called predict without inputs")
        publish_progress_text.visibility = View.GONE
        publish_final_step.isEnabled = true

    }

    override fun onErrorEncountered() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.validation_failed))
                .setMessage("We tested you model by feeding ${modelMetaData.inputDimension} " +
                        "dimensional random floats and expected a output of ${modelMetaData.outputDimension}. " +
                        "But Your model did not produced the required results. " +
                        "Please check back by changing the values.\n\nHint : Check if your model needs drop out.")
                .setPositiveButton(R.string.okay) { dialog, _ ->
                    dialog.dismiss()
                    publish_progress_text.visibility = View.GONE
                    publish_final_step.isEnabled = true
                }
                .setCancelable(false)
                .show()
    }


}

