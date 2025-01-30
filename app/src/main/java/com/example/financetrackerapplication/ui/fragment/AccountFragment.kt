package com.example.financetrackerapplication.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.financetrackerapplication.databinding.FragmentAccountBinding
import com.example.financetrackerapplication.ui.activity.ChangePasswordActivity
import com.example.financetrackerapplication.ui.activity.EditProfileActivity
import com.example.financetrackerapplication.ui.activity.IntroActivity

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}