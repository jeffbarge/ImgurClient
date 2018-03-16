package com.barger.sofichallenge

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        val caption = intent.getStringExtra(EXTRA_CAPTION)
        val url = intent.getStringExtra(EXTRA_URL)
        caption_text.text = caption
        caption_text.visibility = if (!TextUtils.isEmpty(caption)) View.VISIBLE else View.GONE

        Picasso.get()
                .load(url)
                .into(full_image)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val EXTRA_URL = "extra_url"
        private val EXTRA_CAPTION = "extra_caption"
        fun createIntent(context: Context, imgUrl: String, caption: String): Intent {
            val intent = Intent(context, ImageActivity::class.java)
            intent.putExtra(EXTRA_URL, imgUrl)
            intent.putExtra(EXTRA_CAPTION, caption)
            return intent
        }
    }
}