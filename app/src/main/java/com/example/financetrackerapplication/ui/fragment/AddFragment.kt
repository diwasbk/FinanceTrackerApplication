package com.example.financetrackerapplication.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.financetrackerapplication.R
import com.example.financetrackerapplication.databinding.FragmentAddBinding
import com.example.financetrackerapplication.model.ExpenseModel
import com.example.financetrackerapplication.repository.ExpenseRepositoryImpl
import com.example.financetrackerapplication.viewmodel.ExpenseViewModel

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeCategories: Array<String>
    private lateinit var expenseCategories: Array<String>
    private lateinit var expenseViewModel: ExpenseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize ViewModel
        expenseViewModel = ExpenseViewModel(ExpenseRepositoryImpl())
        // Load Categories
        incomeCategories = resources.getStringArray(R.array.income_categories)
        expenseCategories = resources.getStringArray(R.array.expense_categories)
        // Set default spinner options and select "Income" by default
        binding.typeGroup.check(R.id.radioIncome) // Default select "Income"
        updateSpinnerOptions(incomeCategories)
        // RadioGroup change listener
        binding.typeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioIncome -> updateSpinnerOptions(incomeCategories)
                R.id.radioExpense -> updateSpinnerOptions(expenseCategories)
            }
        }

        // Add button click listener
        binding.addButton.setOnClickListener {
            val amountText = binding.amount.text.toString()
            val amount = amountText.toDoubleOrNull()
            val category = binding.categorySpinner.selectedItem.toString()
            val type = if (binding.typeGroup.checkedRadioButtonId == R.id.radioIncome) "Income" else "Expense"
            val remarks = binding.remarks.text.toString()
            // Validation to check if all fields are filled
            if (amountText.isEmpty() || category.isEmpty() || remarks.isEmpty()) {
                Toast.makeText(requireContext(), "All fields must be filled!", Toast.LENGTH_SHORT).show()
            } else if (amount == null || amount <= 0) {
                Toast.makeText(requireContext(), "Please enter a valid amount!", Toast.LENGTH_SHORT).show()
            } else {
                val expense = ExpenseModel("", amount, category, type, remarks)
                expenseViewModel.addExpense(expense) { success, message ->
                    if (success) {
                        val successMessage = if (type == "Income") {
                            "Income Added Successfully"
                        } else {
                            "Expense Added Successfully"
                        }
                        Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()

                        // Clear the fields
                        clearFields()
                    } else {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return view
    }

    private fun updateSpinnerOptions(categories: Array<String>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter
    }

    private fun clearFields() {
        binding.amount.text.clear()
        binding.remarks.text.clear()
        binding.typeGroup.clearCheck()
        binding.typeGroup.check(R.id.radioExpense) // Reset to "Expense" by default
        updateSpinnerOptions(expenseCategories)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
