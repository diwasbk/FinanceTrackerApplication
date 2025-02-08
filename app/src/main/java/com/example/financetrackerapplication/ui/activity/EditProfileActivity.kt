package com.example.financetrackerapplication.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.financetrackerapplication.R
import com.example.financetrackerapplication.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditProfileBinding
    
    private lateinit var loadingLayout: LinearLayout
    private lateinit var animationView: LottieAnimationView
    
    // Firebase references
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userId: String = auth.currentUser?.uid ?: "defaultUserId"
    private val userRef: DatabaseReference = database.reference.child("users").child(userId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Loading Layout and Animation View
        loadingLayout = binding.loadingLayout
        animationView = binding.animationView // Link LottieAnimationView
        
        // Handle back arrow click
        binding.backArrow.setOnClickListener {
            finish()
        }

        // Handle submit button click
        binding.submitButton.setOnClickListener {
            val newUsername = binding.changeUsername.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()
            // Validate input
            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Please enter a new username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Verify password and update username in Firebase
            verifyPasswordAndUpdateUsername(confirmPassword, newUsername)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun verifyPasswordAndUpdateUsername(password: String, newUsername: String) {
        val currentUser = auth.currentUser
        // Show loading before starting Firebase operations
        showLoading()
        // Re-authenticate the user to verify the password
        val credential = EmailAuthProvider.getCredential(currentUser?.email ?: "", password)
        currentUser?.reauthenticate(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Password is correct, update the username in Firebase
                userRef.child("username").setValue(newUsername.toLowerCase()).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        // Hide the loading once response is received
                        hideLoading()
                        // Username updated successfully
                        Toast.makeText(this, "Username changed successfully", Toast.LENGTH_SHORT).show()
                        // Close the activity after the successful update
                        finish()
                    } else {
                        // Error updating username
                        Toast.makeText(this, "Error updating username. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                hideLoading() // Hide loading progress bar if password verification fails
                // Password is incorrect
                Toast.makeText(this, "Wrong password. Please try again.", Toast.LENGTH_SHORT).show()
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
}