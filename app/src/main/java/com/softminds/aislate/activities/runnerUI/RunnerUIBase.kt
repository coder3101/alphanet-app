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

package com.softminds.aislate.activities.runnerUI

import android.net.Uri
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.softminds.aislate.AlphanetBase
import com.softminds.aislate.BuildConfig
import com.softminds.aislate.R
import com.softminds.aislate.ai.ModelMetaData
import com.softminds.aislate.utils.Constants
import java.util.*

abstract class RunnerUIBase : AppCompatActivity() {

    lateinit var metaData: ModelMetaData
    lateinit var uri: Uri

    private var interstitialAd: InterstitialAd? = null
    private var predictButton: Button? = null
    private var predictCallback: OnPredictClicked? = null


    override fun onResume() {
        supportActionBar?.title = metaData.name!!
        if ((application as AlphanetBase).showAdvt)
            intializeAds()
        super.onResume()
    }


    private fun intializeAds() {
        MobileAds.initialize(this, "ca-app-pub-2985268992686393~1891614793")
        interstitialAd = InterstitialAd(this)
        if (BuildConfig.DEBUG)
            interstitialAd?.adUnitId = Constants.AdUnitIds.DEBUG_UNIT
        else
            interstitialAd?.adUnitId = Constants.AdUnitIds.RELEASE_UNIT
        interstitialAd?.loadAd(AdRequest.Builder().build())
        interstitialAd?.adListener = object : AdListener() {
            override fun onAdClosed() {
                interstitialAd?.loadAd(AdRequest.Builder().build())
            }
        }

    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).setPositiveButton(R.string.yes) { dialog, _ ->
            dialog.dismiss()
            super.onBackPressed()
        }
                .setTitle("Sure to leave")
                .setMessage("Are you sure you want to leave the Runner UI?")
                .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    fun setPredictButton(v: Button) {
        predictButton = v
    }

    fun setPredictCallback(predictClicked: OnPredictClicked) {
        if (predictButton == null) {
            throw RuntimeException("Predict Button must be set, Before setting its callback")
        } else {
            predictCallback = predictClicked
            predictButton!!.setOnClickListener {
                if (Random().nextInt() % 10 == 0 && interstitialAd != null && interstitialAd!!.isLoaded && (application as AlphanetBase).showAdvt)
                    interstitialAd!!.show()
                else
                    predictCallback!!.predictButtonClicked()


            }
        }
    }
}

interface OnPredictClicked {
    fun predictButtonClicked()
}