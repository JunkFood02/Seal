package com.junkfood.seal.dot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.junkfood.seal.R
import com.junkfood.seal.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, _, _ ->
            binding.toolbar.title = navController.currentDestination?.label
        }
        with(binding.toolbar) {
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.more -> {
                        findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_global_settingsFragment)
                        title = resources.getString(R.string.settings)
                        true
                    }
                    else -> {
                        true
                    }
                }

            }
        }
    }






    companion object {
        private const val TAG = "MainActivity"
    }
}