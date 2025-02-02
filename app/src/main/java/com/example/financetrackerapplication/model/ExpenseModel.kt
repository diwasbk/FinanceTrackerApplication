package com.example.financetrackerapplication.model

import com.google.firebase.database.ServerValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExpenseModel(
    var expenseId: String = "",
    var amount: Double = 0.0,
    var category: String = "",
    var type: String = "",
    var remarks: String = "",
    var date: Any = ServerValue.TIMESTAMP // Firebase server timestamp
) {
    fun getFormattedDate(): String {
        return if (date is Long) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(date as Long))
        } else {
            "N/A"
        }
    }
}
