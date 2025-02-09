package com.example.financetrackerapplication.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.financetrackerapplication.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the background color of the edges (status bar and navigation bar) to green
        setEdgeColor()

        setContentView(R.layout.activity_intro)
        // Delays transition to the next activity by 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@IntroActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        },2000)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    // Function to set the color of the edges (status and navigation bars)
    private fun setEdgeColor() {
        // Set the status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.green) // Use your custom green color

        // Set the navigation bar color
        window.navigationBarColor = ContextCompat.getColor(this, R.color.green) // Use your custom green color
    }
}