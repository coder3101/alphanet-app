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

package com.softminds.aislate.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.softminds.aislate.R
import com.softminds.aislate.ai.ModelMetaData
import android.support.v4.app.NotificationManagerCompat
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import com.softminds.aislate.ai.ModelType
import java.io.*
import java.lang.Exception


class ModelManipulator(private val context: Context) {

    private val cancelDownload = "ACTION_CANCEL_DOWNLOAD"

    fun startDownload(modelMetaData: ModelMetaData) {

        if (!FirebaseStorage.getInstance().reference.activeDownloadTasks.isEmpty()) {
            //todo(coder3101) : Add a queue mechanism for this
            Toast.makeText(context, context.getString(R.string.already_downloading), Toast.LENGTH_SHORT).show()
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = context.getString(R.string.channel_name)
            val description = context.getString(R.string.channel_description)
            val channel = NotificationChannel("download_ID", name, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = description
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }

        val notificationID = 4522


        Log.d("startDownload", "Creating notification for download")

        val intentCancelDownloadTask = Intent(context, CancelDownloadBroadCast::class.java)
        intentCancelDownloadTask.action = cancelDownload
        intentCancelDownloadTask.putExtra("download_ID", 0)

        val pendingCancel = PendingIntent.getBroadcast(context, 0, intentCancelDownloadTask, 0)


        val builder = NotificationCompat.Builder(context, "download_ID")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Downloading ${modelMetaData.name}...")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentText(context.getString(R.string.download_progress))
                .setOngoing(true)
                .addAction(R.drawable.com_facebook_auth_dialog_cancel_background, context.getString(R.string.cancel), pendingCancel)
                .setProgress(100, 0, false)

        notificationManager.notify(notificationID, builder.build())

        Toast.makeText(context, context.getString(R.string.progress_in_bar), Toast.LENGTH_SHORT).show()


        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(modelMetaData.downloadUrl!!)

        val temp = File(context.filesDir, "models")
        if (!temp.exists()) {
            temp.mkdir()
        }

        val file = File(context.filesDir, "models/${modelMetaData.name + modelMetaData.id}.pb")
        ref.getFile(file).addOnCompleteListener { task: Task<FileDownloadTask.TaskSnapshot> ->
            if (task.isSuccessful) {
                //success
                Toast.makeText(context, context.getString(R.string.completed_download), Toast.LENGTH_SHORT).show()
                builder.setProgress(100, 100, false)
                //cancel after 1 sec
                Handler().postDelayed({ notificationManager.cancel(notificationID) }, 1000)

                registerLocally(modelMetaData)


            } else {
                Log.e("ModelManipulator", "Unable to download file", task.exception)

                Toast.makeText(context, context.getString(R.string.failed_download), Toast.LENGTH_SHORT).show()
                notificationManager.cancel(notificationID)
            }
        }.addOnProgressListener { taskSnapshot: FileDownloadTask.TaskSnapshot? ->
            val progress = (100.0 * taskSnapshot!!.bytesTransferred) / taskSnapshot.totalByteCount
            builder.setProgress(100, progress.toInt(), false)
            notificationManager.notify(notificationID, builder.build())

        }
    }

    private fun registerLocally(modelMetaData: ModelMetaData) {

        val temp = File(context.filesDir, "modelParams")
        if (!temp.exists()) {
            temp.mkdir()
        }

        //serialize the modelParam to file
        val file2 = File(context.filesDir, "modelParams/${modelMetaData.name + modelMetaData.id}.meta")
        val fos = FileOutputStream(file2)
        val ous = ObjectOutputStream(fos)
        ous.writeObject(modelMetaData)
        ous.close()
        fos.close()


        val pref = context.getSharedPreferences(Constants.ModelMappers.PREF_NAME, MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(modelMetaData.name + modelMetaData.id, true) // true means available offline
        editor.apply()
    }

    fun deregisterLocally(modelMetaData: ModelMetaData): Boolean {
        val file = File(context.filesDir, "models/${modelMetaData.name + modelMetaData.id}.pb")
        return if (file.delete()) {
            val pref = context.getSharedPreferences(Constants.ModelMappers.PREF_NAME, MODE_PRIVATE)
            val editor = pref.edit()
            editor.remove(modelMetaData.name + modelMetaData.id)
            editor.apply()

            val file2 = File(context.filesDir, "modelParams/${modelMetaData.name + modelMetaData.id}.meta")
            file2.delete()
            true

        } else {
            Log.d("Remover", "Something went wrong and remove failed")
            false
        }

    }

    fun getLocalModels(): List<ModelMetaData> {
        val pref = context.getSharedPreferences(Constants.ModelMappers.PREF_NAME, MODE_PRIVATE)
        val map = pref.all
        val allModels = ArrayList<ModelMetaData>()
        for (entry in map.entries) {
            val file = File(context.filesDir, "modelParams/${entry.key}.meta")
            val fis = FileInputStream(file)
            val ois = ObjectInputStream(fis)
            allModels.add(ois.readObject() as ModelMetaData)
            ois.close()
            fis.close()
        }
        return allModels
    }

    fun publishModel(uri: Uri, modelType: ModelType, name: String, listener: UploadListener) {
        when (modelType) {
            ModelType.FEED_FORWARD -> {
                val ref = FirebaseStorage.getInstance().reference
                ref.child(Constants.FirebaseStorageConstants.MODELS_PATH)
                        .child(Constants.FirebaseStorageConstants.FEED_FORWARD)
                        .child(name)
                        .putFile(uri,
                                StorageMetadata.Builder().setCustomMetadata("uploader", FirebaseAuth.getInstance()
                                        .currentUser!!
                                        .email!!)
                                        .build())
                        .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? -> listener.onUploadCompleted(taskSnapshot!!) }
                        .addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot? -> listener.onUploadProgressUpdate(((taskSnapshot!!.bytesTransferred * 100) / taskSnapshot.totalByteCount).toInt()) }
                        .addOnFailureListener { exception -> listener.onUploadFailed(exception) }
            }

            ModelType.CONVOLUTION -> {
                val ref = FirebaseStorage.getInstance().reference
                ref.child(Constants.FirebaseStorageConstants.MODELS_PATH)
                        .child(Constants.FirebaseStorageConstants.CONVOLUTION)
                        .child(name)
                        .putFile(uri,
                                StorageMetadata.Builder().setCustomMetadata("uploader", FirebaseAuth.getInstance()
                                        .currentUser!!
                                        .email!!)
                                        .build())
                        .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? -> listener.onUploadCompleted(taskSnapshot!!) }
                        .addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot? -> listener.onUploadProgressUpdate(((taskSnapshot!!.bytesTransferred * 100) / taskSnapshot.totalByteCount).toInt()) }
                        .addOnFailureListener { exception -> listener.onUploadFailed(exception) }
            }

            ModelType.RECURRENT -> {
                val ref = FirebaseStorage.getInstance().reference
                ref.child(Constants.FirebaseStorageConstants.MODELS_PATH)
                        .child(Constants.FirebaseStorageConstants.RECURRENT)
                        .child(name)
                        .putFile(uri,
                                StorageMetadata.Builder().setCustomMetadata("uploader", FirebaseAuth.getInstance()
                                        .currentUser!!
                                        .email!!)
                                        .build())
                        .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? -> listener.onUploadCompleted(taskSnapshot!!) }
                        .addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot? -> listener.onUploadProgressUpdate(((taskSnapshot!!.bytesTransferred * 100) / taskSnapshot.totalByteCount).toInt()) }
                        .addOnFailureListener { exception -> listener.onUploadFailed(exception) }
            }
        }
    }

    fun getUriFromLocalModel(modelMetaData: ModelMetaData): Uri? {
        Log.d("ModelManipulator", getLocalModels().size.toString())
        var flag = false
        for (e in getLocalModels())
            if (e.id == modelMetaData.id) {
                flag = true
                break
            }
        return if (flag) {
            val string = File(context.filesDir, "models/${modelMetaData.name + modelMetaData.id}.pb").toString()
            Log.d("ModelManipulator", "Serving model at $string ")
            Uri.fromFile(File(context.filesDir, "models/${modelMetaData.name + modelMetaData.id}.pb"))
        } else {
            Toast.makeText(context,context.getString(R.string.missing_or_na_model),Toast.LENGTH_SHORT).show()
            //fixme(coder3101):This is likely to throw at inference time
            Uri.fromFile(File(context.filesDir, "models/${modelMetaData.name + modelMetaData.id}.pb"))
        }
    }

    interface UploadListener {
        fun onUploadCompleted(taskSnapshot: UploadTask.TaskSnapshot)
        fun onUploadFailed(exception: Exception)
        fun onUploadProgressUpdate(param: Int)
    }


}