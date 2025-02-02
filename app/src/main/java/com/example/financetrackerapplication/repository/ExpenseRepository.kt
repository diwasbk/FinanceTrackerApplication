package com.example.financetrackerapplication.repository

import com.example.financetrackerapplication.model.ExpenseModel

interface ExpenseRepository {
    fun addExpense(expenseModel: ExpenseModel, callback: (Boolean, String) -> Unit)
}
