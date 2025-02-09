package com.example.financetrackerapplication.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.financetrackerapplication.R
import com.example.financetrackerapplication.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var mAuth: FirebaseAuth

    private lateinit var loadingLayout: LinearLayout
    private lateinit var animationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the background color of the edges (status bar and navigation bar) to green
        setEdgeColor()

        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Loading Layout and Animation View
        loadingLayout = binding.loadingLayout
        animationView = binding.animationView // Link LottieAnimationView

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        // Navigate to Login Activity
        binding.backArrow.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Handle reset password button click
        binding.resetPasswordBtn.setOnClickListener {
            val email = binding.email.text.toString().trim()
            if (email.isEmpty()) {
                binding.email.error = "Email is required"
                binding.email.requestFocus()
                return@setOnClickListener
            }
            // Send password reset email
            sendPasswordResetEmail(email)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Function to send password reset email using Firebase Auth
    private fun sendPasswordResetEmail(email: String) {
        showLoading()  // Show the loading animation
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            hideLoading()  // Hide the loading animation once the request is completed
            if (task.isSuccessful) {
                Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading() {
        loadingLayout.visibility = View.VISIBLE
        animationView.playAnimation()  // Start Lottie animation
    }

    private fun hideLoading() {
        loadingLayout.visibility = View.GONE
        animationView.cancelAnimation()  // Stop Lottie animation
    }

    // Function to set the color of the edges (status and navigation bars)
    private fun setEdgeColor() {
        // Set the status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.green) // Use your custom green color

        // Set the navigation bar color
        window.navigationBarColor = ContextCompat.getColor(this, R.color.green) // Use your custom green color
    }
}