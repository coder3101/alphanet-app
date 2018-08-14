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

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class ModelMetaData() : Parcelable, Serializable
{

    var self: String? = null
    var name: String? = null
    var type: ModelType? = null
    var runnerUi : RunnerUI? = null
    var description: String? = null
    var isVerified = false
    var id : Long? = null
    var upvote : Long? = null
    var emptyCallsAllowed = false
    var height = 0
    var width = 0
    var inputDimension = 0
    var outputDimension = 0
    var isExtraRequired = false
    var inputProperties = ArrayList<String>()
    var outputLabels = ArrayList<String>()
    var trainingSetName = "Not Available"
    var layerNum: Int = 0
    var publisher = "SoftMinds"
    var publisherEmail = "ashar786khan@gmail.com"
    var publishDate = System.currentTimeMillis()
    var fileSize = 0L
    var version = 1.0
    var downloadUrl: String? = null
    var accuracy: Float? = null
    var entropyLoss: Float? = null
    var downloads = 0

    constructor(parcel: Parcel) : this() {
        self = parcel.readString()
        name = parcel.readString()
        type = ModelType.valueOf(parcel.readString())
        runnerUi = RunnerUI.valueOf(parcel.readString())
        description = parcel.readString()
        isVerified = parcel.readByte() != 0.toByte()
        id = parcel.readLong()
        upvote = parcel.readLong()
        emptyCallsAllowed = parcel.readByte() != 0.toByte()
        height = parcel.readInt()
        width = parcel.readInt()
        inputDimension = parcel.readInt()
        outputDimension = parcel.readInt()
        isExtraRequired = parcel.readByte() != 0.toByte()
        @Suppress("UNCHECKED_CAST")
        inputProperties = parcel.readArrayList(String::class.java.classLoader) as ArrayList<String>
        @Suppress("UNCHECKED_CAST")
        outputLabels = parcel.readArrayList(String::class.java.classLoader) as ArrayList<String>
        trainingSetName = parcel.readString()
        layerNum = parcel.readInt()
        publisher = parcel.readString()
        publisherEmail = parcel.readString()
        publishDate = parcel.readLong()
        fileSize = parcel.readLong()
        version = parcel.readDouble()
        downloadUrl = parcel.readString()
        accuracy = parcel.readValue(Float::class.java.classLoader) as? Float
        entropyLoss = parcel.readValue(Float::class.java.classLoader) as? Float
        downloads = parcel.readInt()
    }

    constructor(name: String) : this() {
        this.name = name
        this.upvote = 0
        this.id = System.currentTimeMillis()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(self)
        parcel.writeString(name)
        parcel.writeString(type!!.name)
        parcel.writeString(runnerUi!!.name)
        parcel.writeString(description)
        parcel.writeByte(if (isVerified) 1 else 0)
        parcel.writeLong(id!!)
        parcel.writeLong(upvote!!)
        parcel.writeByte(if (emptyCallsAllowed) 1 else 0)
        parcel.writeInt(height)
        parcel.writeInt(width)
        parcel.writeInt(inputDimension)
        parcel.writeInt(outputDimension)
        parcel.writeByte(if (isExtraRequired) 1 else 0)
        parcel.writeList(inputProperties)
        parcel.writeList(outputLabels)
        parcel.writeString(trainingSetName)
        parcel.writeInt(layerNum)
        parcel.writeString(publisher)
        parcel.writeString(publisherEmail)
        parcel.writeLong(publishDate)
        parcel.writeLong(fileSize)
        parcel.writeDouble(version)
        parcel.writeString(downloadUrl)
        parcel.writeValue(accuracy)
        parcel.writeValue(entropyLoss)
        parcel.writeInt(downloads)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ModelMetaData> {
        override fun createFromParcel(parcel: Parcel): ModelMetaData {
            return ModelMetaData(parcel)
        }

        override fun newArray(size: Int): Array<ModelMetaData?> {
            return arrayOfNulls(size)
        }
    }

}
