package com.example.financetrackerapplication.repository

import com.example.financetrackerapplication.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface UserRepository {
    // Method for logging in the user
    fun login(email: String, password: String, callback: (Boolean, String) -> Unit)

    // Method for signing up a new user
    fun signup(email: String, password: String, callback: (Boolean, String, String) -> Unit)

    // Method to add user data to Firebase Realtime Database
    fun addUserToDatabase(userId: String, userModel: UserModel, callback: (Boolean, String) -> Unit)

    // Method to handle forgotten passwords
    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit)

    // Method to get the currently logged-in user
    fun getCurrentUser(): FirebaseUser?
}
