package com.example.financetrackerapplication.viewmodel

import com.example.financetrackerapplication.model.ExpenseModel
import com.example.financetrackerapplication.repository.ExpenseRepository

class ExpenseViewModel(private val repo: ExpenseRepository) {
    fun addExpense(expenseModel: ExpenseModel, callback: (Boolean, String) -> Unit) {
        repo.addExpense(expenseModel, callback)
    }
}
