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

import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.softminds.aislate.R
import kotlinx.android.synthetic.main.activity_faq.*

class Faq : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        val questions = resources.getStringArray(R.array.questions)
        val answers = resources.getStringArray(R.array.answers)

        for (i in 0 until questions.size) {
            val q = questions[i]
            val a = answers[i]
            val v = createViewCard(q, a)
            main_faq_container.addView(v, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }


    }

    private fun createViewCard(q: String, a: String): View {
        val card = CardView(this)
        card.isClickable = true
        card.useCompatPadding = true
        card.isFocusable = true

        val pad = resources.getDimensionPixelOffset(R.dimen.faq_card_padding)
        card.setContentPadding(pad, pad, pad, pad)

        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.VERTICAL

        val tq = TextView(this)
        tq.typeface = Typeface.DEFAULT_BOLD
        tq.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        tq.text = q

        ll.addView(tq, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val thickness = resources.getDimensionPixelOffset(R.dimen.thickness_faq_card_hr)
        val hr = View(this)
        hr.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        val vg = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, thickness)
        vg.bottomMargin = resources.getDimensionPixelSize(R.dimen.thickness_faq_card_hr)
        ll.addView(hr, vg)

        val ta = TextView(this)
        ta.text = a

        ll.addView(ta, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        card.addView(ll, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        return card
    }
}
