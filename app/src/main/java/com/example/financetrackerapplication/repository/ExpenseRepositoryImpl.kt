package com.example.financetrackerapplication.repository

import com.example.financetrackerapplication.model.ExpenseModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class ExpenseRepositoryImpl : ExpenseRepository {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    // Fetch the current user's ID (from Firebase Authentication)
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUserId"

    // References to the user's expenses and balance node in Firebase (balance under records)
    private val expensesRef: DatabaseReference = database.reference.child("records").child(userId).child("expenses")
    private val balanceRef: DatabaseReference = database.reference.child("records").child(userId).child("balance")

    override fun addExpense(expenseModel: ExpenseModel, callback: (Boolean, String) -> Unit) {
        // Generate a unique ID for each expense
        val id = expensesRef.push().key.toString()
        expenseModel.expenseId = id  // Assign the generated ID
        // Save the expense data under the user's expenses node
        expensesRef.child(id).setValue(expenseModel).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // After adding the expense, update the balance
                updateBalance(expenseModel.amount, expenseModel.type == "Expense")
                callback(true, "Expense Added Successfully")
            } else {
                callback(false, "Error: ${task.exception?.message}")
            }
        }
    }

    private fun updateBalance(amount: Double, isExpense: Boolean) {
        // Get the current balance information from Firebase
        balanceRef.get().addOnSuccessListener { snapshot ->
            val currentIncome = snapshot.child("totalIncome").getValue(Double::class.java) ?: 0.0
            val currentExpense = snapshot.child("totalExpense").getValue(Double::class.java) ?: 0.0
            // Update income or expense based on the type
            val newIncome = if (!isExpense) currentIncome + amount else currentIncome
            val newExpense = if (isExpense) currentExpense + amount else currentExpense
            val remainingBalance = newIncome - newExpense
            // Update the balance node in Firebase under records > userId > balance
            balanceRef.setValue(mapOf(
                "totalIncome" to newIncome,
                "totalExpense" to newExpense,
                "remainingBalance" to remainingBalance
            ))
        }
    }

    // Update the expense if the amount or type has changed (e.g., from Income to Expense)
    fun updateExpense(expenseId: String, newExpense: ExpenseModel, callback: (Boolean, String) -> Unit) {
        val expenseRef = expensesRef.child(expenseId)
        // Fetch the current expense data to determine if the type has changed
        expenseRef.get().addOnSuccessListener { snapshot ->
            val currentExpense = snapshot.getValue(ExpenseModel::class.java)
            currentExpense?.let { oldExpense ->
                val oldType = oldExpense.type
                val oldAmount = oldExpense.amount
                // Update the expense details in Firebase
                val updatedData = mapOf(
                    "amount" to newExpense.amount,
                    "category" to newExpense.category,
                    "type" to newExpense.type,
                    "remarks" to newExpense.remarks,
                    "date" to ServerValue.TIMESTAMP // Update to the current server timestamp
                )
                // Update the expense record in Firebase
                expenseRef.updateChildren(updatedData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // If the type has changed, update the balance accordingly
                        if (oldType != newExpense.type) {
                            updateBalanceOnTypeChange(oldAmount, newExpense.amount, oldType == "Income", newExpense.type == "Expense")
                        }
                        callback(true, "Expense Updated Successfully")
                    } else {
                        callback(false, "Failed to Update Expense: ${task.exception?.message}")
                    }
                }
            }
        }
    }

    // Update the balance after a type change (Income to Expense or vice versa)
    private fun updateBalanceOnTypeChange(oldAmount: Double, newAmount: Double, wasIncome: Boolean, isExpense: Boolean) {
        balanceRef.get().addOnSuccessListener { snapshot ->
            val currentIncome = snapshot.child("totalIncome").getValue(Double::class.java) ?: 0.0
            val currentExpense = snapshot.child("totalExpense").getValue(Double::class.java) ?: 0.0
            // Adjust totalIncome and totalExpense based on type changes
            var newIncome = currentIncome
            var newExpense = currentExpense
            // If the old type was "Income", we subtract it from totalIncome
            if (wasIncome) {
                newIncome -= oldAmount
            }
            // If the new type is "Expense", we add it to totalExpense
            if (isExpense) {
                newExpense += newAmount
            }
            // Update the remaining balance
            val remainingBalance = newIncome - newExpense
            // Update the balance in Firebase
            balanceRef.setValue(mapOf(
                "totalIncome" to newIncome,
                "totalExpense" to newExpense,
                "remainingBalance" to remainingBalance
            ))
        }
    }

    // Delete the expense and adjust the balance
    fun deleteExpense(expenseId: String, callback: (Boolean, String) -> Unit) {
        val expenseRef = expensesRef.child(expenseId)
        // Fetch the current expense to determine the type before deleting
        expenseRef.get().addOnSuccessListener { snapshot ->
            val expense = snapshot.getValue(ExpenseModel::class.java)
            expense?.let {
                val isExpense = it.type == "Expense"
                val amount = it.amount
                // Delete the expense from Firebase
                expenseRef.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // After deleting, update the balance
                        updateBalance(amount, isExpense)
                        callback(true, "Expense Deleted Successfully")
                    } else {
                        callback(false, "Failed to Delete Expense: ${task.exception?.message}")
                    }
                }
            }
        }
    }

    // Fetch expenses from Firebase (for the given user)
    fun getExpenses(callback: (List<ExpenseModel>?, String?) -> Unit) {
        expensesRef.get().addOnSuccessListener { snapshot ->
            val expenseList = mutableListOf<ExpenseModel>()
            for (dataSnap in snapshot.children) {
                val expense = dataSnap.getValue(ExpenseModel::class.java)
                expense?.let { expenseList.add(it) }
            }
            callback(expenseList, null)
        }.addOnFailureListener { exception ->
            callback(null, "Error fetching expenses: ${exception.message}")
        }
    }
}
