package com.example.cooking_game

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide action bar to view the splash screen in full screen
        supportActionBar?.hide()

        // Animate the image in the splash screen
        splash_image.animate().apply {
            duration = 1000 // 1 second
            rotationY(360f) // rotate 360 degrees on Y axis
        }.start()

        // Specify a Handler with a Looper to delay 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            // Create an intent to open the MainActivity
            val myIntent = Intent(this, MainActivity::class.java)
            startActivity(myIntent)
            // Make sure to call finish(), otherwise the user would be able to go back to this SplashActivity
            finish()
        }, 2000) // 2 seconds delay
    }
}