package com.example.financetrackerapplication.ui.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.financetrackerapplication.databinding.FragmentStatisticBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StatisticFragment : Fragment() {

    private var _binding: FragmentStatisticBinding? = null
    private val binding get() = _binding!!

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUserId"
    private val balanceRef: DatabaseReference = database.reference.child("records").child(userId).child("balance")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStatisticBinding.inflate(inflater, container, false)
        // Fetch and update the data from balance node
        fetchAndUpdateData()
        return binding.root
    }

    private fun fetchAndUpdateData() {
        balanceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Retrieve the balance information from the balance node
                val totalIncome = snapshot.child("totalIncome").getValue(Double::class.java) ?: 0.0
                val totalExpense = snapshot.child("totalExpense").getValue(Double::class.java) ?: 0.0
                val remainingBalance = snapshot.child("remainingBalance").getValue(Double::class.java) ?: 0.0
                // Update the UI with the fetched balance data using ViewBinding
                binding.incomeBalance.text = "%.2f".format(totalIncome)
                binding.expenseBalance.text = "%.2f".format(totalExpense)
                binding.remainingBalance.text = "%.2f".format(remainingBalance)
                // Update the Pie Chart
                updatePieChart(totalIncome, totalExpense)
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle any database errors
            }
        })
    }

    private fun updatePieChart(income: Double, expense: Double) {
        val entries = mutableListOf<PieEntry>()
        if (income > 0) entries.add(PieEntry(income.toFloat(), "Income"))
        if (expense > 0) entries.add(PieEntry(expense.toFloat(), "Expense"))
        val colors = listOf(Color.parseColor("#4CAF50"), Color.RED)

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors)
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            valueTypeface = Typeface.DEFAULT_BOLD // Make values bold
        }

        val pieData = PieData(dataSet).apply {
            setValueTextSize(14f)
            setValueTextColor(Color.WHITE)
            setValueTypeface(Typeface.DEFAULT_BOLD)
            setValueFormatter(PercentFormatter()) // Custom formatter to show percentages
        }

        binding.pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true) // Show values in percentage
            animateY(1000) // Smooth animation
            data = pieData
            setEntryLabelColor(Color.WHITE) // Set labels to white
            setEntryLabelTypeface(Typeface.DEFAULT_BOLD) // Make labels bold
            invalidate() // Refresh chart
        }
    }

    // Custom Formatter to show percentage
    class PercentFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return "%.1f%%".format(value)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
