package com.example.currencyconverter.ui.splash

import android.content.Intent
import android.os.Bundle
import com.example.currencyconverter.R
import com.example.currencyconverter.ui.main.MainActivity
import com.example.currencyconverter.util.LOGO_IMAGE_ANIMATION_DURATION
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        titleLabel.setOnClickListener { navigateToMain() }
        setupUi()
    }

    private fun setupUi() {
        logoImage.pathAnimator
            .duration(LOGO_IMAGE_ANIMATION_DURATION)
            .listenerEnd { navigateToMain() }
            .start()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}