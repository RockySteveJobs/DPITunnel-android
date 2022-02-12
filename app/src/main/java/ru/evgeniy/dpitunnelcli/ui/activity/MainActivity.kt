package ru.evgeniy.dpitunnelcli.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.evgeniy.dpitunnelcli.R
import ru.evgeniy.dpitunnelcli.databinding.ActivityMainBinding
import ru.evgeniy.dpitunnelcli.preferences.AppPreferences
import java.io.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppPreferences.setDefaults(this)
        val appPreferences = AppPreferences.getInstance(this)
        if (appPreferences.firstRun) {
            extractAssets()
            appPreferences.caBundlePath = filesDir.absolutePath + "/ca.bundle"
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_dashboard, R.id.navigation_profiles, R.id.navigation_notifications
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun extractAssets() {
        extractFileFromAssets("ca.bundle")
    }

    private fun extractFileFromAssets(filename: String) {
        try {
            val input = assets.open(filename)
            val outFile = File(filesDir, filename)
            val out = FileOutputStream(outFile)
            copyFile(input, out)
            input.close()
            out.flush()
            out.close()
        } catch (e: IOException) {
            Log.e("tag", "Failed to copy asset file: $filename", e)
        }
    }

    @Throws(IOException::class)
    private fun copyFile(input: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }
}