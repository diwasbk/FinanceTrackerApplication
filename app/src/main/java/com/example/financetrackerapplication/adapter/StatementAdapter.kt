package com.example.financetrackerapplication.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financetrackerapplication.R
import com.example.financetrackerapplication.model.ExpenseModel
import com.example.financetrackerapplication.ui.activity.EditStatementActivity
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class StatementAdapter(

    private val dataList: ArrayList<ExpenseModel>,
    private val context: Context
) : RecyclerView.Adapter<StatementAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val category: TextView = view.findViewById(R.id.statementCategory)
        val date: TextView = view.findViewById(R.id.categoryDate)
        val amount: TextView = view.findViewById(R.id.transactionAmount)
        val remarks: TextView = view.findViewById(R.id.remarks)
        val editStatementButton: ImageView = view.findViewById(R.id.editStatement) // Added editStatement ImageView
        val indicator: View = view.findViewById(R.id.transactionIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.statement_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = dataList[position]
        // Bind data to views
        holder.category.text = expense.category
        holder.amount.text = "${expense.amount}"
        // Format the date if it's a Long, otherwise display "N/A"
        val date = if (expense.date is Long) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(expense.date as Long))
        } else {
            "N/A"
        }

        holder.date.text = date
        // Bind the remarks
        holder.remarks.text = "Remarks: ${expense.remarks}"
        // Change the indicator color based on the transaction type
        val color = if (expense.type.equals("income", ignoreCase = true)) {
            holder.itemView.context.getColor(R.color.green) // Use green for income
        } else {
            holder.itemView.context.getColor(R.color.red) // Use red for expense
        }

        holder.indicator.setBackgroundColor(color)

        // Handle click on the edit button
        holder.editStatementButton.setOnClickListener {
            val intent = Intent(context, EditStatementActivity::class.java)
            // Pass data to EditStatementActivity
            intent.putExtra("expenseId", expense.expenseId)
            intent.putExtra("category", expense.category)
            intent.putExtra("date", expense.date as Long) // Pass date as Long
            intent.putExtra("amount", expense.amount)
            intent.putExtra("remarks", expense.remarks)
            intent.putExtra("type", expense.type)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    // Function to get the expenseId of an item at a given position (used for deleting the statement)
    fun getExpenseId(position: Int): String {
        return dataList[position].expenseId
    }
}
