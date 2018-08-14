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

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.softminds.aislate.utils.Constants

class FirebaseTokenizer : FirebaseMessagingService() {

    override fun onNewToken(p0: String?) {
        Log.d("Tokenizer", "Refreshed the token : $p0")
        if(p0 !=null){
            sentToServer(p0)

        }
    }

    private fun sentToServer(newToken: String) {
        Log.d("Tokenizer", "Uploading the $newToken")
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            Log.e("Tokenizer ", "Cannot write token to server unless user is not signed in. Saving to preferences")
            getSharedPreferences(Constants.ClientConfigConstants.TEMP_PREFS_NAME, Context.MODE_PRIVATE).edit().putString(Constants.ClientConfigConstants.FirebaseTokenizerBuffer, newToken).apply()
            return
        }
        getSharedPreferences(Constants.ClientConfigConstants.TEMP_PREFS_NAME, Context.MODE_PRIVATE).edit().putString(Constants.ClientConfigConstants.FirebaseTokenizerBuffer, newToken).apply()
        val databaseRef = FirebaseDatabase.getInstance().getReference("${Constants.FirebaseStorageConstants.ROOT}/${Constants.FirebaseStorageConstants.TOKEN}/$uid")
        databaseRef.setValue(newToken).addOnSuccessListener { Log.d("Tokenizer", "Successfully written the token to sever. Client will get notifications") }.addOnFailureListener { Log.e("Tokenizer", "Failed to write the values to server : ${it.localizedMessage}") }

    }
}