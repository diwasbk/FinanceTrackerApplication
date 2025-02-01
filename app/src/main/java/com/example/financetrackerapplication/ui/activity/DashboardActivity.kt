package com.example.financetrackerapplication.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.financetrackerapplication.R
import com.example.financetrackerapplication.databinding.ActivityDashboardBinding
import com.example.financetrackerapplication.ui.fragment.AccountFragment
import com.example.financetrackerapplication.ui.fragment.AddFragment
import com.example.financetrackerapplication.ui.fragment.StatementFragment
import com.example.financetrackerapplication.ui.fragment.StatisticFragment

class DashboardActivity : AppCompatActivity() {

    lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure no background highlight
        binding.bottomNavigationView.itemActiveIndicatorColor = null
        
        // To show Statement Fragment by default
        replaceFragment(AddFragment())
        binding.bottomNavigationView.setOnItemSelectedListener {menu->
            when(menu.itemId){
                R.id.navStatement -> replaceFragment(StatementFragment())
                R.id.navAdd -> replaceFragment(AddFragment())
                R.id.navStatistic -> replaceFragment(StatisticFragment())
                R.id.navAccount -> replaceFragment(AccountFragment())
                else -> {}
            }
            true
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    // Function to implement Fragments
    fun replaceFragment(fragment: Fragment) {
        val fragmentManager : FragmentManager = supportFragmentManager
        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameNavigation,fragment)
        fragmentTransaction.commit()
    }
}