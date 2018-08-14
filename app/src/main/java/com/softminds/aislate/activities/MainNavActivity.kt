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

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.softminds.aislate.R
import com.softminds.aislate.AlphanetBase
import com.softminds.aislate.BuildConfig
import com.softminds.aislate.fragments.*
import com.softminds.aislate.utils.Constants
import kotlinx.android.synthetic.main.activity_main_slate.*
import kotlinx.android.synthetic.main.activity_slate_view.*
import kotlinx.android.synthetic.main.app_bar_main_slate.*
import kotlinx.android.synthetic.main.content_main_slate.*
import java.util.*


class MainNavActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private var lastSelected = R.id.nav_feed_forward

    private val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_slate)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.setCheckedItem(R.id.nav_feed_forward)
        nav_view.menu!!.findItem(R.id.nav_publish_models).isEnabled = BuildConfig.DEBUG
        loadConfigs()
        showInformation()
        sendTokenToServer()
        initiateNavHeader()
        supportFragmentManager.beginTransaction().add(R.id.main_fragment_container, FeedForward()).commit()
        supportActionBar!!.title = getString(R.string.download_feed)
    }

    private fun initiateNavHeader() {

        val mainImage = nav_view.getHeaderView(0).findViewById<ImageView>(R.id.imageView_header)
        val nameNav = nav_view.getHeaderView(0).findViewById<TextView>(R.id.nav_header_name)

        if (FirebaseAuth.getInstance().currentUser != null) {
            Glide.with(this).applyDefaultRequestOptions(RequestOptions.circleCropTransform()).load(FirebaseAuth.getInstance().currentUser!!.photoUrl).into(mainImage)
            val name = "Welcome ${FirebaseAuth.getInstance().currentUser!!.displayName}\n${FirebaseAuth.getInstance().currentUser!!.email}"
            nameNav?.text = name

        }
    }

    private fun sendTokenToServer() {
        val token = getSharedPreferences(Constants.ClientConfigConstants.TEMP_PREFS_NAME, Context.MODE_PRIVATE).getString(Constants.ClientConfigConstants.FirebaseTokenizerBuffer, "null")
        if (token == "null" || FirebaseAuth.getInstance().currentUser == null) {
            Log.e("TokenUploader", "Cannot upload token. Users token is null")
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("/root/tokens/${FirebaseAuth.getInstance().currentUser!!.email!!.replace('.', '-').replace('@', '-')}")
            ref.setValue(token)
        }
    }

    private fun showInformation() {
        val sharedPreferences = getSharedPreferences(Constants.ClientConfigConstants.TEMP_PREFS_NAME, Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean(Constants.ClientConfigConstants.FirstInstallStatus, false)) {
            AlertDialog.Builder(this@MainNavActivity)
                    .setTitle(getString(R.string.important_info))
                    .setIcon(ContextCompat.getDrawable(this@MainNavActivity, R.mipmap.ic_launcher_round))
                    .setMessage(getString(R.string.info_body))
                    .setPositiveButton(getString(R.string.agree)) { dialog: DialogInterface?, _: Int ->
                        dialog?.dismiss()
                        getSharedPreferences(Constants.ClientConfigConstants.TEMP_PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(Constants.ClientConfigConstants.FirstInstallStatus, true).apply()
                    }
                    .setCancelable(false)
                    .show()
        }

    }

    private fun loadConfigs() {
        val map = HashMap<String, Boolean>()
        map["load_advt"] = false
        map["allow_publish"] = false
        mFirebaseRemoteConfig.setDefaults(map as Map<String, Boolean>?)

        mFirebaseRemoteConfig.fetch(if (BuildConfig.DEBUG) 0 else  60 * 1000)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase Fetch : ", "Fetch Succeeded")
                        mFirebaseRemoteConfig.activateFetched()
                        updateState()
                    } else {
                        Log.d("Firebase Fetch : ", "Failed to fetch from Firebase Remote Config")
                    }
                }

    }

    private fun updateState() {
        val showAdvt = mFirebaseRemoteConfig.getBoolean("load_advt")
        (application as AlphanetBase).showAdvt = showAdvt
        nav_view.menu!!.findItem(R.id.nav_publish_models).isEnabled = mFirebaseRemoteConfig.getBoolean("allow_publish")

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_res_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (R.id.sign_out == item!!.itemId) {
            AuthUI.getInstance().signOut(this).addOnSuccessListener {
                Toast.makeText(applicationContext, getString(R.string.signed_out), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        if (R.id.report == item.itemId) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.my_email)))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_subject))
            if (intent.resolveActivity(packageManager) != null)
                startActivity(intent)
            else
                Toast.makeText(applicationContext, getString(R.string.no_email_app), Toast.LENGTH_SHORT).show()
        }
        if (R.id.remove_model == item.itemId) {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.removal_models_request))
                    .setMessage(getString(R.string.model_removal_request))
                    .setPositiveButton("Proceed") { dialog, _ ->
                        dialog.dismiss()
                        val intent = Intent(Intent.ACTION_SENDTO)
                        intent.data = Uri.parse("mailto:")
                        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.my_email)))
                        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_subject))
                        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.remove_published_model_extra_message))
                        if (intent.resolveActivity(packageManager) != null)
                            startActivity(intent)
                        else
                            Toast.makeText(applicationContext, getString(R.string.no_email_app), Toast.LENGTH_SHORT).show()
                    }
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_feed_forward -> {
                setLoadingStatus(true) //the fragment attached should set this to false when data is loaded
                main_content_loading_bar.visibility = View.VISIBLE
                lastSelected = R.id.nav_feed_forward
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, FeedForward()).commit()
                supportActionBar!!.title = getString(R.string.download_feed)

            }
            R.id.nav_convolutional -> {
                setLoadingStatus(true)
                main_content_loading_bar.visibility = View.VISIBLE
                lastSelected = R.id.nav_convolutional
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, Convolution()).commit()
                supportActionBar!!.title = getString(R.string.download_conv)

            }
            R.id.nav_recurrent -> {
                setLoadingStatus(true)
                main_content_loading_bar.visibility = View.VISIBLE
                lastSelected = R.id.nav_recurrent
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, Recurrent()).commit()
                supportActionBar!!.title = getString(R.string.download_recurrent)

            }

            R.id.nav_downloaded_models -> {
                setLoadingStatus(false)
                main_content_loading_bar.visibility = View.INVISIBLE
                lastSelected = R.id.nav_downloaded_models
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, Downloaded()).commit()
                supportActionBar!!.title = getString(R.string.downloaded)
            }

            R.id.nav_publish_models -> {
                setLoadingStatus(false)
                main_content_loading_bar.visibility = View.INVISIBLE
                lastSelected = R.id.nav_publish_models
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, PublishModels()).commit()
                supportActionBar!!.title = getString(R.string.publish_model)
            }

            R.id.nav_run_local_models -> {
                setLoadingStatus(false)
                main_content_loading_bar.visibility = View.INVISIBLE
                lastSelected = R.id.nav_publish_models
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, RunLocally()).commit()
                supportActionBar!!.title = getString(R.string.local_models)
            }


//            R.id.nav_contribute -> {
//                //todo(coder3101) : Include this in next version
//                AlertDialog.Builder(this)
//                        .setTitle("Coming Soon..")
//                        .setMessage("Hey this feature will be released publicly soon. \n\nThere is an announcement regarding this idea. Please check the announcement section of the app for more detail\n\nThanks for Showing your interest")
//                        .setPositiveButton("Okay", { dialog, _ -> dialog.dismiss(); nav_view.setCheckedItem(lastSelected) })
//                        .setOnCancelListener { nav_view.setCheckedItem(lastSelected) }
//                        .show()
//            }

            R.id.nav_announcements -> {
                startActivity(Intent(this, Announcements::class.java))
            }


            R.id.nav_about -> {
                LibsBuilder()
                        .withAboutDescription(getString(R.string.app_description))
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .withAboutAppName(getString(R.string.app_name))
                        .withLicenseDialog(true)
                        .withActivityTitle(getString(R.string.about))
                        .withAutoDetect(true)
                        .start(this)
            }

            R.id.nav_share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
                startActivity(intent)
            }
            R.id.nav_support -> {
                startActivity(Intent(this, SupportActivity::class.java))
            }
            R.id.nav_faq -> {
                startActivity(Intent(this, Faq::class.java))
            }


        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun setLoadingStatus(isLoading: Boolean) {
        if (isLoading) {
            main_content_loading_bar.visibility = View.VISIBLE
            main_fragment_container.visibility = View.GONE
        } else {
            main_content_loading_bar.visibility = View.GONE
            main_fragment_container.visibility = View.VISIBLE
        }
    }

}
