package com.example.financetrackerapplication.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.financetrackerapplication.R
import com.example.financetrackerapplication.databinding.ActivityEditStatementBinding
import com.example.financetrackerapplication.model.ExpenseModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class EditStatementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditStatementBinding
    private var expenseId: String = ""
    private lateinit var incomeCategories: Array<String>
    private lateinit var expenseCategories: Array<String>

    private lateinit var loadingLayout: LinearLayout
    private lateinit var animationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEditStatementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Loading Layout and Animation View
        loadingLayout = binding.loadingLayout
        animationView = binding.animationView // Link LottieAnimationView

        // Handle back arrow click
        binding.backArrow.setOnClickListener {
            finish() // This will finish the activity and navigate back
        }
        // Load categories from resources
        incomeCategories = resources.getStringArray(R.array.income_categories)
        expenseCategories = resources.getStringArray(R.array.expense_categories)

        // Retrieve data passed from the adapter
        expenseId = intent.getStringExtra("expenseId") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val date = intent.getLongExtra("date", 0L)
        val amount = intent.getDoubleExtra("amount", 0.0)
        val remarks = intent.getStringExtra("remarks") ?: ""
        val type = intent.getStringExtra("type") ?: ""

        // Populate UI fields with the received data
        binding.amount.setText(amount.toString())
        binding.remarks.setText(remarks)

        // Set type (Income/Expense)
        if (type.equals("income", ignoreCase = true)) {
            binding.radioIncome.isChecked = true
            updateSpinnerOptions(incomeCategories)
        } else {
            binding.radioExpense.isChecked = true
            updateSpinnerOptions(expenseCategories)
        }

        // Set the selected category in Spinner
        binding.categorySpinner.setSelection(getCategoryIndex(category, type))

        // Handle type change (Income/Expense)
        binding.typeGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedType = if (checkedId == R.id.radioIncome) "income" else "expense"
            updateSpinnerOptions(if (selectedType == "income") incomeCategories else expenseCategories)
        }

        // Handle update button click
        binding.updateStatementButton.setOnClickListener {
            updateExpenseInFirebase()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun updateSpinnerOptions(categories: Array<String>) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter
    }

    private fun getCategoryIndex(category: String, type: String): Int {
        val categories = if (type.equals("income", ignoreCase = true)) incomeCategories else expenseCategories
        return categories.indexOf(category).takeIf { it >= 0 } ?: 0
    }

    private fun updateExpenseInFirebase() {
        val updatedAmount = binding.amount.text.toString().toDoubleOrNull()
        val updatedRemarks = binding.remarks.text.toString()
        val selectedType = findViewById<RadioButton>(binding.typeGroup.checkedRadioButtonId)?.text.toString()
        val selectedCategory = binding.categorySpinner.selectedItem.toString()

        if (updatedAmount == null || updatedAmount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Get user ID and Firebase reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId.isNullOrEmpty() || expenseId.isEmpty()) {
            Toast.makeText(this, "Unable to update the expense", Toast.LENGTH_SHORT).show()
            return
        }
        val expenseRef = FirebaseDatabase.getInstance()
            .reference.child("records").child(userId).child("expenses").child(expenseId)

        // Get the current expense details to check if the type has changed
        expenseRef.get().addOnSuccessListener { snapshot ->
            val currentExpense = snapshot.getValue(ExpenseModel::class.java)
            currentExpense?.let { oldExpense ->
                val oldType = oldExpense.type
                val oldAmount = oldExpense.amount
                // Update expense in Firebase
                val updatedData = mapOf(
                    "amount" to updatedAmount,
                    "category" to selectedCategory,
                    "type" to selectedType,
                    "remarks" to updatedRemarks,
                    "date" to ServerValue.TIMESTAMP // Update to the current server timestamp
                )
                // Show loading
                showLoading()
                expenseRef.updateChildren(updatedData).addOnCompleteListener { task ->
                    // Hide loading
                    hideLoading()
                    if (task.isSuccessful) {
                        // If the type has changed, update the balance accordingly
                        if (oldType != selectedType) {
                            updateBalanceOnTypeChange(oldAmount, updatedAmount, oldType == "Income", selectedType == "Expense")
                        }
                        Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity after updating
                    } else {
                        Toast.makeText(this, "Failed to update expense", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateBalanceOnTypeChange(oldAmount: Double, newAmount: Double, wasIncome: Boolean, isExpense: Boolean) {
        // Get user ID and Firebase reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val balanceRef = FirebaseDatabase.getInstance().reference.child("records").child(userId).child("balance")
        // Get the current balance
        balanceRef.get().addOnSuccessListener { snapshot ->
            val currentIncome = snapshot.child("totalIncome").getValue(Double::class.java) ?: 0.0
            val currentExpense = snapshot.child("totalExpense").getValue(Double::class.java) ?: 0.0
            var newIncome = currentIncome
            var newExpense = currentExpense
            // Adjust the totalIncome and totalExpense based on the type changes
            if (wasIncome) {
                // If the old type was "Income", subtract the old amount from totalIncome
                newIncome -= oldAmount
            }
            if (isExpense) {
                // If the new type is "Expense", add the new amount to totalExpense
                newExpense += newAmount
            } else if (!isExpense && !wasIncome) {
                // If the new type is "Income" and was previously "Expense", subtract the old amount from totalExpense and add the new amount to totalIncome
                newExpense -= oldAmount
                newIncome += newAmount
            }
            // Update the remaining balance
            val remainingBalance = newIncome - newExpense
            // Update the balance node in Firebase under records > userId > balance
            balanceRef.setValue(mapOf(
                "totalIncome" to newIncome,
                "totalExpense" to newExpense,
                "remainingBalance" to remainingBalance
            ))
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