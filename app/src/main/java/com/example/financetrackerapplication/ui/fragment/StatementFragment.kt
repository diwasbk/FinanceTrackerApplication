package com.example.financetrackerapplication.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financetrackerapplication.adapter.StatementAdapter
import com.example.financetrackerapplication.databinding.FragmentStatementBinding
import com.example.financetrackerapplication.model.ExpenseModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StatementFragment : Fragment() {

    private lateinit var binding: FragmentStatementBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StatementAdapter
    private lateinit var dataList: ArrayList<ExpenseModel>
    private lateinit var databaseReference: DatabaseReference
    private var swipedPosition: Int = -1  // Store the position of the swiped item

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        binding = FragmentStatementBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize RecyclerView
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize Adapter and Data List
        dataList = ArrayList()
        adapter = StatementAdapter(dataList, requireContext())
        recyclerView.adapter = adapter

        fetchDataFromFirebase()

        // Swipe-to-delete functionality
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // No movement handling needed
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Store the position of the swiped item
                swipedPosition = viewHolder.adapterPosition

                // Get the expenseId of the swiped item
                val expenseId = adapter.getExpenseId(viewHolder.adapterPosition)

                // Show a confirmation dialog before deleting
                showDeleteConfirmationDialog(expenseId)
            }
        })

        // Attach the ItemTouchHelper to the RecyclerView
        itemTouchHelper.attachToRecyclerView(recyclerView)

        return view
    }

    private fun showDeleteConfirmationDialog(expenseId: String) {
        // Create an AlertDialog to confirm deletion
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Delete Expense")
            .setMessage("Do you want to delete this statement?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Proceed with deleting the expense
                deleteExpense(expenseId)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                // If "No" is clicked, restore the item in the RecyclerView
                adapter.notifyItemChanged(swipedPosition)
                dialog.dismiss()
            }

        // Show the dialog
        dialogBuilder.create().show()
    }

    private fun deleteExpense(expenseId: String) {
        // Get the current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Get the reference to the expense in Firebase
        val expenseRef = FirebaseDatabase.getInstance().reference
            .child("records").child(userId).child("expenses").child(expenseId)

        // Fetch the expense details to calculate the balance adjustment
        expenseRef.get().addOnSuccessListener { snapshot ->
            val expense = snapshot.getValue(ExpenseModel::class.java)
            expense?.let {
                // Adjust balance after expense deletion
                updateBalanceOnExpenseDeletion(it.amount, it.type == "Expense")
            }

            // Delete the expense from Firebase
            expenseRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Expense Deleted Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error Deleting Expense", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateBalanceOnExpenseDeletion(amount: Double, isExpense: Boolean) {
        // Get current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Reference to balance in Firebase
        val balanceRef = FirebaseDatabase.getInstance().reference
            .child("records").child(userId).child("balance")

        balanceRef.get().addOnSuccessListener { snapshot ->
            val currentIncome = snapshot.child("totalIncome").getValue(Double::class.java) ?: 0.0
            val currentExpense = snapshot.child("totalExpense").getValue(Double::class.java) ?: 0.0

            val newIncome = if (!isExpense) currentIncome - amount else currentIncome
            val newExpense = if (isExpense) currentExpense - amount else currentExpense
            val remainingBalance = newIncome - newExpense

            // Update the balance in Firebase
            balanceRef.setValue(mapOf(
                "totalIncome" to newIncome,
                "totalExpense" to newExpense,
                "remainingBalance" to remainingBalance
            ))
        }
    }

    private fun fetchDataFromFirebase() {

        // Get current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance()
            .reference.child("records").child(userId).child("expenses")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataList.clear()
                for (dataSnap in snapshot.children) {
                    val expense = dataSnap.getValue(ExpenseModel::class.java)
                    expense?.let { dataList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
