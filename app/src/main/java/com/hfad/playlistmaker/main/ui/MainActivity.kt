package com.hfad.playlistmaker.main.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupEdgeToEdge()
        setupWindowInsets()

        checkAndRequestNotificationPermission()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            val navigationBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = navigationBar.bottom)
            insets
        }
    }

    private fun setupWindowInsets() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.createPlaylistFragment,
                R.id.playlistDetailsFragment,
                R.id.editPlaylistFragment,
                R.id.mediaFragment -> {
                    applyInsetsToRoot(true)
                }
                R.id.searchFragment,
                R.id.libraryFragment,
                R.id.settingsFragment -> {
                    applyInsetsToRoot(false)
                }
                else -> {
                    applyInsetsToRoot(false)
                }
            }
        }
    }

    private fun applyInsetsToRoot(apply: Boolean) {
        if (apply) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
                val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
                val navigationBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

                view.updatePadding(
                    top = statusBar.top,
                    bottom = navigationBar.bottom
                )
                insets
            }
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(binding.root, null)
            binding.root.updatePadding(top = 0, bottom = 0)
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.searchFragment,
                R.id.libraryFragment,
                R.id.settingsFragment
            )
        )

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.searchFragment, R.id.libraryFragment, R.id.settingsFragment -> {
                    binding.bottomNavigation.isVisible = true
                }
                else -> {
                    binding.bottomNavigation.isVisible = false
                }
            }
        }
    }
}