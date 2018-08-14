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

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.softminds.aislate.R
import com.softminds.aislate.activities.PublishActivity
import kotlinx.android.synthetic.main.fragment_publish_models.*


class PublishModels : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_publish_models, container, false)
        view.findViewById<Button>(R.id.proceed_publish_button).setOnClickListener{
            if(checkbox.isChecked){
                startActivity(Intent(context,PublishActivity::class.java))
            }
            else{
                Toast.makeText(context,getString(R.string.click_chec_to_proceed),Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
}
