package com.example.financetrackerapplication.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.financetrackerapplication.R
import com.example.financetrackerapplication.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    lateinit var binding: ActivityChangePasswordBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle back arrow click
        binding.backArrow.setOnClickListener {
            finish()
        }

        // Handle the change password button click
        binding.changePasswordButton.setOnClickListener {
            changePassword()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Function to change the password
    private fun changePassword() {
        val currentPassword = binding.currentPassword.text.toString().trim()
        val newPassword = binding.newPassword.text.toString().trim()
        val confirmPassword = binding.confirmNewPassword.text.toString().trim()

        // Validate the input fields
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword != confirmPassword) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        // Get the current user
        val user = auth.currentUser
        if (user != null) {
            // Re-authenticate the user before changing the password
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                if (reAuthTask.isSuccessful) {
                    // Now that the user is re-authenticated, change the password
                    user.updatePassword(newPassword).addOnCompleteListener { updatePasswordTask ->
                        if (updatePasswordTask.isSuccessful) {
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            finish() // Close the activity after password change
                        } else {
                            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Failed to re-authenticate the user
                    Toast.makeText(this, "Incorrect current password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}