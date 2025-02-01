package com.example.financetrackerapplication.repository

import com.example.financetrackerapplication.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserRepositoryImpl : UserRepository {

    // Firebase Realtime Database instance
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // Reference to the "users" node in the database
    private val ref: DatabaseReference = database.reference.child("users")

    // Firebase Authentication instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Login implementation
    override fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Login successful")
                } else {
                    callback(false, task.exception?.message.toString())
                }
            }
    }

    // Signup implementation
    override fun signup(email: String, password: String, callback: (Boolean, String, String) -> Unit) {
        val username = email.substringBefore("@") // Extract the part before the "@" symbol
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Pass the user ID from Firebase Authentication and username
                    callback(true, "Registration successful", auth.currentUser?.uid.toString())
                } else {
                    callback(false, task.exception?.message.toString(), "")
                }
            }
    }

    // Add user details to the Firebase database
    override fun addUserToDatabase(userId: String, userModel: UserModel, callback: (Boolean, String) -> Unit) {
        ref.child(userId).setValue(userModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "User added to database successfully")
                } else {
                    callback(false, task.exception?.message.toString())
                }
            }
    }

    // Forgot password implementation
    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Password reset email sent")
                } else {
                    callback(false, task.exception?.message.toString())
                }
            }
    }

    // Get the currently logged-in user
    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
