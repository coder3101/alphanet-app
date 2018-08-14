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

package com.softminds.aislate.utils


class Constants {

    object FirebaseStorageConstants {
        const val FEED_FORWARD = "feed_forward"
        const val CONVOLUTION = "convolutional"
        const val RECURRENT = "recurrent"
        const val MODELS_PATH = "models"
        const val TOKEN = "tokens"
        const val ROOT = "root"
        const val ANNOUNCEMENTS = "announcements"
    }

    object ClientConfigConstants {
        const val FirstInstallStatus = "first_install"
        const val TEMP_PREFS_NAME = "temp"
        const val FirebaseTokenizerBuffer = "token"
    }

    object AdUnitIds {
        const val DEBUG_UNIT = "ca-app-pub-3940256099942544/1033173712"
        const val RELEASE_UNIT = "ca-app-pub-2985268992686393/9055788940"
    }

    object ModelMappers{
        const val PREF_NAME = "model_map"
        const val ALL_DOWNLOADED = "downloaded_model"
    }

    object ExtraIntentConstants{
        const val BASE = "com.softminds.aislate"
        const val WIDTH = "$BASE.width_pixel"
        const val HEIGHT = "$BASE.height_pixel"
        const val MODEL_META_DATA = "$BASE.model.full_data"
        const val LOCAL_RUN_PATH = "$BASE.local_path"
    }

    object NeuralNetworkParams{
        const val DEFAULT_INPUT_NODE = "input"
        const val DEFAULT_OUTPUT_NAME = "output"
        const val DEFAULT_DROPOUT_NAME = "dropout"
    }


}
