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

package com.softminds.aislate.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.softminds.aislate.R
import com.softminds.aislate.activities.runnerUI.RawInputActivity
import com.softminds.aislate.activities.runnerUI.SlateViewActivity
import com.softminds.aislate.ai.*
import com.softminds.aislate.firebase.MetaDataFetcher
import com.softminds.aislate.utils.Constants
import com.softminds.aislate.utils.ModelManipulator

class ExploreAdapter(private var modelMetaDataList: List<ModelMetaData>, private val context: Context, private val isOfflineView: Boolean = false) : RecyclerView.Adapter<ExploreHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.explore_adapter_view, parent, false)
        return ExploreHolder(view)
    }

    override fun getItemCount(): Int {
        return modelMetaDataList.size
    }

    override fun onBindViewHolder(holder: ExploreHolder, position: Int) {
        holder.title.text = modelMetaDataList[position].name

        val s = String.format("%.2f", (modelMetaDataList[position].fileSize).toFloat() / (1024F * 1024F))

        val s1 = "$s MB"


        holder.size.text = s1
        val pp = if (modelMetaDataList[position].upvote!! > 1000)
            "Likes : ${modelMetaDataList[position].upvote!! / 1000} K"
        else
            "Likes : ${modelMetaDataList[position].upvote!!}"

        holder.upvotes.text = pp
        holder.runnerUI.text = when (modelMetaDataList[position].runnerUi) {

            RunnerUI.RAW_INPUT -> context.getString(R.string.raw_input_type_sheet)
            RunnerUI.SLATE_VIEW -> context.getString(R.string.slate_input_type_sheet)
            RunnerUI.CAMERA_VIEW -> context.getString(R.string.camera_type_input)
            null -> "Runner UI : unknown"
        }
        holder.rootContainer.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(context)
            bottomSheetDialog.setCancelable(true)
            bottomSheetDialog.setCanceledOnTouchOutside(true)
            val view = View.inflate(context, R.layout.model_data_sheet, null)
            setUpView(view, modelMetaDataList[position], bottomSheetDialog)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

        }

    }

    private fun setUpView(view: View?, modelMetaData: ModelMetaData, bottomSheetDialog: BottomSheetDialog) {

        setBasicInfo(view!!, modelMetaData)

        //set UpVote Marker
        view.findViewById<ImageView>(R.id.bottom_sheet_likes_icon).setOnClickListener {
            Toast.makeText(context, context.getString(R.string.sending_vote), Toast.LENGTH_SHORT).show()
            MetaDataFetcher.markUpVote(modelMetaData.self!!, modelMetaData.upvote!!, modelMetaData.type!!)
            bottomSheetDialog.dismiss()
            //fixme(coder3101) : may cause multiple up votes
        }

        if (isOfflineView)
            setOfflineControls(view, modelMetaData, bottomSheetDialog)
        else if (!ModelManipulator(context).getLocalModels().isEmpty())
            setOfflineOnline(view, modelMetaData, bottomSheetDialog)
        else
            view.findViewById<Button>(R.id.bottom_sheet_download).setOnClickListener {
                ModelManipulator(context).startDownload(modelMetaData)
                bottomSheetDialog.dismiss()
            }
    }

    private fun setOfflineOnline(view: View, modelMetaData: ModelMetaData, bottomSheetDialog: BottomSheetDialog) {
        view.findViewById<Button>(R.id.bottom_sheet_remove_offline).visibility = View.GONE
        val downloadButton = view.findViewById<Button>(R.id.bottom_sheet_download)
        downloadButton.visibility = View.VISIBLE
        val runButton = view.findViewById<Button>(R.id.bottom_sheet_run_model)
        runButton.visibility = View.GONE
        for (m in ModelManipulator(context).getLocalModels())
            if (m.id == modelMetaData.id) {
                Log.d("ExploreAdapter", "Offline Adapter item found")
                downloadButton.text = context.getString(R.string.alredy_downloaded)
                downloadButton.isEnabled = false
                runButton.visibility = View.VISIBLE
                runButton.setOnClickListener {
                    launchActivity(modelMetaData)
                }

            } else downloadButton.setOnClickListener {
                ModelManipulator(context).startDownload(modelMetaData)
                bottomSheetDialog.dismiss()
            }
    }

    private fun launchActivity(modelMetaData: ModelMetaData) {
        val i = Intent(context, when (modelMetaData.runnerUi) {
            RunnerUI.RAW_INPUT -> RawInputActivity::class.java
            RunnerUI.SLATE_VIEW -> SlateViewActivity::class.java
            RunnerUI.CAMERA_VIEW -> RawInputActivity::class.java
            else -> null
        })
        i.putExtra(Constants.ExtraIntentConstants.MODEL_META_DATA, modelMetaData as Parcelable)
        i.putExtra(Constants.ExtraIntentConstants.LOCAL_RUN_PATH, ModelManipulator(context).getUriFromLocalModel(modelMetaData))
        context.startActivity(i)
    }

    private fun setOfflineControls(view: View, modelMetaData: ModelMetaData, bottomSheetDialog: BottomSheetDialog) {
        view.findViewById<Button>(R.id.bottom_sheet_download).visibility = View.GONE
        val removeButton = view.findViewById<Button>(R.id.bottom_sheet_remove_offline)
        val runButton = view.findViewById<Button>(R.id.bottom_sheet_run_model)
        runButton.visibility = View.VISIBLE
        runButton.setOnClickListener { launchActivity(modelMetaData) }
        removeButton.visibility = View.VISIBLE
        removeButton.setOnClickListener {
            AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.sure_to_remove))
                    .setMessage("Do you really want to remove this ${modelMetaData.name} from local storage.")
                    .setPositiveButton(R.string.yes) { dialog, _ ->
                        dialog.dismiss()
                        if (ModelManipulator(context).deregisterLocally(modelMetaData)) {
                            bottomSheetDialog.dismiss()
                            modelMetaDataList = ModelManipulator(context).getLocalModels()
                            notifyDataSetChanged()
                            Toast.makeText(context, "Successfully removed ${modelMetaData.name} from local storage", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.removal_model_failed), Toast.LENGTH_SHORT).show()
                            bottomSheetDialog.dismiss()
                        }

                    }.setNegativeButton(R.string.no) { dialog, _ ->
                        dialog.dismiss()
                        bottomSheetDialog.dismiss()
                    }.show()
        }

    }

    private fun setBasicInfo(view: View, modelMetaData: ModelMetaData) {
        view.findViewById<TextView>(R.id.bottom_sheet_title).text = modelMetaData.name
        val ss = String.format("%.2f", modelMetaData.fileSize / (1024F * 1024F)) + " MB"
        view.findViewById<TextView>(R.id.bottom_sheet_size).text = ss
        view.findViewById<TextView>(R.id.bottom_sheet_likes).text = modelMetaData.upvote?.toString()
        var zzp = "by : " + modelMetaData.publisher + "<${modelMetaData.publisherEmail}>"
        if (zzp.length > 40)
            zzp = "by : " + modelMetaData.publisherEmail
        view.findViewById<TextView>(R.id.bottom_sheet_author).text = zzp
        view.findViewById<TextView>(R.id.bottom_sheet_desc).text = modelMetaData.description
        view.findViewById<TextView>(R.id.bottom_sheet_input_props).text = if (modelMetaData.type == ModelType.CONVOLUTION) "Pixel values of dimensions ${modelMetaData.width} x ${modelMetaData.height} drawn on the slate view." else fromMetaGetFeatures(modelMetaData)
        view.findViewById<TextView>(R.id.bottom_sheet_output_props).text = fromMetaGetLabels(modelMetaData)

        view.findViewById<TextView>(R.id.bottom_sheet_runnerUI).text = when (modelMetaData.runnerUi) {
            RunnerUI.RAW_INPUT -> context.getString(R.string.raw_input_type_sheet)
            RunnerUI.SLATE_VIEW -> context.getString(R.string.slate_input_type_sheet)
            RunnerUI.CAMERA_VIEW -> context.getString(R.string.camera_type_input)
            null -> "Runner UI : unknown"
        }
    }

    private fun fromMetaGetLabels(modelMetaData: ModelMetaData): String {
        val builder = StringBuilder()
        for (s in modelMetaData.outputLabels) {
            builder.append(context.getString(R.string.bullet_dot))
                    .append("   ")
                    .append(context.getString(R.string.probability_of))
                    .append(" ")
                    .append(s)
                    .append("\n")
        }
        return builder.toString()
    }

    private fun fromMetaGetFeatures(modelMetaData: ModelMetaData): String {
        val result = StringBuilder()
        for (s in modelMetaData.inputProperties)
            result.append(context.getString(R.string.bullet_dot))
                    .append("  ")
                    .append(s)
                    .append("\n")

        return result.toString()

    }


}