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

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.softminds.aislate.R

class ExploreHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title  = view.findViewById<TextView>(R.id.adapter_title)!!
    val size = view.findViewById<TextView>(R.id.adapter_size_info)!!
    val upvotes = view.findViewById<TextView>(R.id.adapter_upvotes)!!
    val runnerUI = view.findViewById<TextView>(R.id.adapter_runnerUI)!!
    val rootContainer = view.findViewById<RelativeLayout>(R.id.root_adapter_view)!!
}