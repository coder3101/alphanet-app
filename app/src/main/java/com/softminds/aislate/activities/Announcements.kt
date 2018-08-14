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

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.database.*
import com.softminds.aislate.R
import com.softminds.aislate.utils.Constants
import kotlinx.android.synthetic.main.activity_announcements.*

class Announcements : AppCompatActivity(), ValueEventListener {

    override fun onCancelled(p0: DatabaseError) {
        Toast.makeText(applicationContext, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onDataChange(p0: DataSnapshot) {
        if (p0.exists()) {
            val body = p0.child("body").value as String
            val date = p0.child("date").value as String + " by " + p0.child("author").value as String
            val title = p0.child("title").value as String

            announcements_progress.visibility = View.INVISIBLE
            announcements_title.text = title
            announcements_date.text = date
            announcements_body.text = body
            announcements_card.visibility = View.VISIBLE
        } else {
            if (!p0.exists()) {
                Toast.makeText(applicationContext, getString(R.string.no_announcements), Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(applicationContext, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                finish()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcements)

        announcements_card.visibility = View.INVISIBLE
        announcements_progress.visibility = View.VISIBLE

        startLoading()

    }

    private fun startLoading() {
        val ref = FirebaseDatabase.getInstance().reference
        ref.child(Constants.FirebaseStorageConstants.ROOT).child(Constants.FirebaseStorageConstants.ANNOUNCEMENTS).addListenerForSingleValueEvent(this)
    }

}
