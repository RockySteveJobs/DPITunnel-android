package ru.evgeniy.dpitunnelcli.ui.activity.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.evgeniy.dpitunnelcli.databinding.ActivitySettingsBinding
import ru.evgeniy.dpitunnelcli.ui.settings.SettingsFragment

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.settingsToolbar)

        supportFragmentManager
            .beginTransaction()
            .replace(binding.settingsFragmentContainer.id, SettingsFragment())
            .commit()
    }
}