package com.example.financetrackerapplication.viewmodel

import com.example.financetrackerapplication.model.UserModel
import com.example.financetrackerapplication.repository.UserRepository
import com.google.firebase.auth.FirebaseUser

class UserViewModel(private val repo: UserRepository) {

    // Calls the repository's login method
    fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        repo.login(email, password, callback)
    }

    // Calls the repository's signup method
    fun signup(email: String, password: String, callback: (Boolean, String, String) -> Unit) {
        repo.signup(email, password, callback)
    }

    // Calls the repository's method to add user data to the database
    fun addUserToDatabase(userId: String, userModel: UserModel, callback: (Boolean, String) -> Unit) {
        repo.addUserToDatabase(userId, userModel, callback)
    }

    // Calls the repository's forgot password method
    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        repo.forgetPassword(email, callback)
    }

    // Calls the repository's method to get the currently logged-in user
    fun getCurrentUser(): FirebaseUser? {
        return repo.getCurrentUser()
    }
}
