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

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponse
import com.softminds.aislate.R


@Suppress("UNUSED_PARAMETER")
class SupportActivity : AppCompatActivity(), PurchasesUpdatedListener, BillingClientStateListener {

    private var billing: BillingClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)
        billing = BillingClient.newBuilder(this).setListener(this).build()
        billing?.startConnection(this)
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (responseCode == BillingResponse.USER_CANCELED) {
            Toast.makeText(this, R.string.cancelled, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.something_went_wrong_purchase, Toast.LENGTH_SHORT).show()

        }
    }

    private fun handlePurchase(purchase: Purchase) {

        AlertDialog.Builder(this)
                .setTitle(getString(R.string.thanks_for_support))
                .setIcon(ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round))
                .setMessage(getString(R.string.support_done_message) +
                        getString(R.string.support_done_2))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.okay)) { dialog: DialogInterface?, _: Int ->
                    dialog?.dismiss()
                    finish()
                }
                .show()

    }

    override fun onBillingServiceDisconnected() {
        Toast.makeText(this, R.string.failed_to_connect_gplay, Toast.LENGTH_SHORT).show()
    }

    override fun onBillingSetupFinished(responseCode: Int) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            //query purchases here.
        }
    }

    fun purchaseSupport1(view: View) {
        val param = BillingFlowParams.newBuilder()
                .setSku(getString(R.string.support_sku_1))
                .setType(BillingClient.SkuType.INAPP)
                .build()
        billing?.launchBillingFlow(this, param)
    }

    fun purchaseSupport3(view: View) {
        val param = BillingFlowParams.newBuilder()
                .setSku(getString(R.string.support_sku_3))
                .setType(BillingClient.SkuType.INAPP)
                .build()
        billing?.launchBillingFlow(this, param)
    }

    fun purchaseSupport5(view: View) {
        val param = BillingFlowParams.newBuilder()
                .setSku(getString(R.string.support_sku_5))
                .setType(BillingClient.SkuType.INAPP)
                .build()
        billing?.launchBillingFlow(this, param)
    }

    fun btcSupport(view: View){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://coder3101.bitcoinwallet.com"))
        startActivity(i)
    }

    fun paypalSupport(view : View){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/coder3101"))
        startActivity(i)
    }

}
