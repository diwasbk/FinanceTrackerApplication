package com.example.financetrackerapplication.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.financetrackerapplication.R
import com.example.financetrackerapplication.databinding.ActivityRegisterBinding
import com.example.financetrackerapplication.model.UserModel
import com.example.financetrackerapplication.repository.UserRepositoryImpl
import com.example.financetrackerapplication.viewmodel.UserViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    // Lazy initialization of UserViewModel
    private val userViewModel: UserViewModel by lazy {
        UserViewModel(UserRepositoryImpl())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // References to UI components
        val emailInput = binding.registerNewEmail
        val passwordInput = binding.registerNewPassword
        val confirmPasswordInput = binding.confirmNewPassword

        // Register button click listener
        binding.registerBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()
            // Validate input
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Extract the username from email
            val username = email.substringBefore("@")
            // Call ViewModel to handle registration logic
            userViewModel.signup(email, password) { isSuccess, message, userId ->
                if (isSuccess) {
                    val userModel = UserModel(userId = userId, email = email, username = username) // Pass username
                    userViewModel.addUserToDatabase(userId, userModel) { dbSuccess, dbMessage ->
                        if (dbSuccess) {
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                            // Navigate to login activity
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, dbMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Navigate to Login Activity
        binding.navigateLogin.setOnClickListener {
            val explicitIntent = Intent(this, LoginActivity::class.java)
            startActivity(explicitIntent)
            finish()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}