package com.example.financetrackerapplication.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.airbnb.lottie.LottieAnimationView
import com.example.financetrackerapplication.databinding.FragmentAccountBinding
import com.example.financetrackerapplication.ui.activity.ChangePasswordActivity
import com.example.financetrackerapplication.ui.activity.EditProfileActivity
import com.example.financetrackerapplication.ui.activity.IntroActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileNameTextView: TextView
    private lateinit var profileEmailTextView: TextView
    private lateinit var totalBalanceTextView: TextView

    private lateinit var loadingLayout: LinearLayout
    private lateinit var animationView: LottieAnimationView

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUserId"
    private val userRef: DatabaseReference = database.reference.child("users").child(userId)
    private val balanceRef: DatabaseReference = database.reference.child("records").child(userId).child("balance")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)

        // Initialize UI elements
        profileNameTextView = binding.profileName
        profileEmailTextView = binding.profileEmail
        totalBalanceTextView = binding.totalBalance
        // Fetch and display user data
        fetchUserData()
        // Fetch and display the user's balance from the balance node
        fetchRemainingBalance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Loading and Animation View
        loadingLayout = binding.loadingLayout
        animationView = binding.animationView // Link LottieAnimationView

        // Show loading animation
        showLoading()
        // Handle "Edit Profile" click
        binding.editProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }
        // Handle "Change Password" click
        binding.changePassword.setOnClickListener {
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }
        // Handle "Logout" click
        binding.logout.setOnClickListener {
            // Create a confirmation dialog
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Are you sure you want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    // Proceed with logout
                    val intent = Intent(requireContext(), IntroActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish() // Use requireActivity() to access the parent activity
                }
                .setNegativeButton("No") { dialog, id ->
                    // Dismiss the dialog, do nothing
                    dialog.dismiss()
                }
            // Show the dialog
            builder.create().show()
        }
    }

    private fun fetchUserData() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Fetch and update email
                val email = snapshot.child("email").getValue(String::class.java)
                profileEmailTextView.text = email ?: "No Email"
                // Optionally fetch and display user's name
                val fullName = snapshot.child("fullName").getValue(String::class.java)
                profileNameTextView.text = fullName ?: "Full Name"
                // Fetch and display username in uppercase
                val username = snapshot.child("username").getValue(String::class.java)
                profileNameTextView.text = username?.toUpperCase() ?: "Username"  // Convert username to uppercase
                // Hide loading animation after data is loaded
                hideLoading()
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                hideLoading()
            }
        })
    }

    private fun fetchRemainingBalance() {
        balanceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Retrieve the balance information from the balance node
                val totalIncome = snapshot.child("totalIncome").getValue(Double::class.java) ?: 0.0
                val totalExpense =
                    snapshot.child("totalExpense").getValue(Double::class.java) ?: 0.0
                val remainingBalance =
                    snapshot.child("remainingBalance").getValue(Double::class.java) ?: 0.0
                // Update UI
                totalBalanceTextView.text = "%.2f".format(remainingBalance)
                // Hide loading animation after data is loaded
                hideLoading()
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun showLoading() {
        loadingLayout.visibility = View.VISIBLE
        animationView.playAnimation()  // Start Lottie animation
    }

    private fun hideLoading() {
        loadingLayout.visibility = View.GONE
        animationView.cancelAnimation()  // Stop Lottie animation
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}