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

package com.softminds.aislate

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class AlphanetBase : Application() {

    //todo : Must change before the release version
    var showAdvt = BuildConfig.DEBUG

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(false)
        //FirebaseDatabase.getInstance().setPersistenceCacheSizeBytes(1024 * 1024 * 5) // 5MB
    }
}