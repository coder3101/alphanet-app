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

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.softminds.aislate.R
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*
import com.firebase.ui.auth.IdpResponse


class SplashActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 1234
    private var alert: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (FirebaseAuth.getInstance().currentUser != null) {

            Handler().postDelayed({
                startActivity(Intent(this, MainNavActivity::class.java))
                this.finish()
            }, 1500)
        } else {
            val alphaAnimation = AlphaAnimation(1.0F, 0.0F)
            alphaAnimation.interpolator = AccelerateDecelerateInterpolator()
            alphaAnimation.duration = 1500

            central_title.startAnimation(alphaAnimation)
            central_image.startAnimation(alphaAnimation)
            central_description.startAnimation(alphaAnimation)

            Handler().postDelayed({

                central_description.visibility = View.INVISIBLE
                central_image.visibility = View.INVISIBLE
                central_title.visibility = View.INVISIBLE

                alert = AlertDialog.Builder(this@SplashActivity)
                        .setTitle(getString(R.string.login_required))
                        .setIcon(R.mipmap.ic_launcher_round)
                        .setMessage(getString(R.string.login_desc_required))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.login), { dialog: DialogInterface?, _: Int ->
                            dialog!!.dismiss()

                            // Choose authentication providers
                            val providers = Arrays.asList(
                                    AuthUI.IdpConfig.EmailBuilder().build(),
                                    //AuthUI.IdpConfig.PhoneBuilder().build(), //Removed phone auth as email is required for us
                                    AuthUI.IdpConfig.GoogleBuilder().build(),
                                    AuthUI.IdpConfig.FacebookBuilder().build())
                            //AuthUI.IdpConfig.TwitterBuilder().build()) //removed as email is required

                            startActivityForResult(
                                    AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            .setAvailableProviders(providers)
                                            .build(),
                                    RC_SIGN_IN)


                        })
                        .setNegativeButton(getString(R.string.exit), { dialog, _ ->
                            dialog.dismiss()
                            Toast.makeText(applicationContext, getString(R.string.sign_in_required), Toast.LENGTH_SHORT).show()
                            finish()
                        })
                        .create()

                alert!!.setCanceledOnTouchOutside(false)
                alert!!.show()

            }, 1500)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            Log.d("Login", response.toString())
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                Toast.makeText(applicationContext, "Welcome ${user!!.displayName}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainNavActivity::class.java))
                this.finish()
                // ...
            } else {
                Toast.makeText(applicationContext, getString(R.string.failed_login), Toast.LENGTH_LONG).show()
                Toast.makeText(applicationContext, getString(R.string.retry_login), Toast.LENGTH_SHORT).show()
                alert!!.show()
            }
        }
    }
}
