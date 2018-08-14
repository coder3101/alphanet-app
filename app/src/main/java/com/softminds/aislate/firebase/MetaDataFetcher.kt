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

package com.softminds.aislate.firebase

import com.google.firebase.database.*
import com.softminds.aislate.ai.ModelMetaData
import com.softminds.aislate.ai.ModelType
import com.softminds.aislate.ai.ModelType.*
import com.softminds.aislate.utils.Constants

class MetaDataFetcher(private val fetcherListener: FetcherListener) : ValueEventListener {

    companion object {
        fun markUpVote(self: String, thisValue: Long, type: ModelType) {
            FirebaseDatabase.getInstance().reference
                    .child(Constants.FirebaseStorageConstants.ROOT)
                    .child(Constants.FirebaseStorageConstants.MODELS_PATH)
                    .child(when (type) {

                        FEED_FORWARD -> Constants.FirebaseStorageConstants.FEED_FORWARD
                        CONVOLUTION -> Constants.FirebaseStorageConstants.CONVOLUTION
                        RECURRENT -> Constants.FirebaseStorageConstants.RECURRENT
                    })
                    .child(self)
                    .child("upvote").setValue(thisValue + 1)
        }
    }

    fun fetch(type: ModelType) {

        when (type) {
            FEED_FORWARD -> {
                val ref = FirebaseDatabase.getInstance().reference
                ref.child(Constants.FirebaseStorageConstants.ROOT)
                        .child(Constants.FirebaseStorageConstants.MODELS_PATH)
                        .child(Constants.FirebaseStorageConstants.FEED_FORWARD)
                        .addListenerForSingleValueEvent(this)
            }

            CONVOLUTION -> {
                val ref = FirebaseDatabase.getInstance().reference
                ref.child(Constants.FirebaseStorageConstants.ROOT)
                        .child(Constants.FirebaseStorageConstants.MODELS_PATH)
                        .child(Constants.FirebaseStorageConstants.CONVOLUTION)
                        .addListenerForSingleValueEvent(this)
            }
            RECURRENT -> {
                val ref = FirebaseDatabase.getInstance().reference
                ref.child(Constants.FirebaseStorageConstants.ROOT)
                        .child(Constants.FirebaseStorageConstants.MODELS_PATH)
                        .child(Constants.FirebaseStorageConstants.RECURRENT)
                        .addListenerForSingleValueEvent(this)
            }
        }
    }

    override fun onCancelled(p0: DatabaseError) {
        fetcherListener.onFetchFailed(p0.details)
    }

    override fun onDataChange(p0: DataSnapshot) {
        if (p0.exists()) {
            val mutableList = ArrayList<ModelMetaData>()

            for (child in p0.children) {
                if (child.exists() && child != null) {
                    mutableList.add(child.getValue(ModelMetaData::class.java)!!)
                }
            }
            if (mutableList.isEmpty())
                fetcherListener.onNothingFound()
            else
                fetcherListener.onFetchCompleted(mutableList)

        } else if (!p0.exists()) {
            fetcherListener.onNothingFound()
        } else {
            fetcherListener.onFetchFailed("Null response was received")
        }
    }
}


interface FetcherListener {
    fun onFetchCompleted(metaDataList: List<ModelMetaData>)
    fun onNothingFound()
    fun onFetchFailed(string: String)
}