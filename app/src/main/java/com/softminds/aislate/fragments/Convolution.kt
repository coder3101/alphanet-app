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


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.softminds.aislate.R
import com.softminds.aislate.activities.MainNavActivity
import com.softminds.aislate.ai.ModelMetaData
import com.softminds.aislate.ai.ModelType
import com.softminds.aislate.firebase.FetcherListener
import com.softminds.aislate.firebase.MetaDataFetcher
import com.softminds.aislate.ui.ExploreAdapter
import kotlinx.android.synthetic.main.fragment_convolution.*
import kotlinx.android.synthetic.main.fragment_feed_forward.*

class Convolution : Fragment(), FetcherListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_convolution, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //getActivity is now available
        MetaDataFetcher(this).fetch(ModelType.CONVOLUTION)
    }

    override fun onFetchCompleted(metaDataList: List<ModelMetaData>) {
        if(activity !=null)
        (activity as MainNavActivity).setLoadingStatus(false)
        if(conv_recycler !=null) {
            conv_recycler.setHasFixedSize(true)
            conv_recycler.layoutManager = LinearLayoutManager(context)
            conv_recycler.adapter = ExploreAdapter(metaDataList, context!!)
        }
    }

    override fun onNothingFound() {
    }

    override fun onFetchFailed(string: String) {

    }


}
