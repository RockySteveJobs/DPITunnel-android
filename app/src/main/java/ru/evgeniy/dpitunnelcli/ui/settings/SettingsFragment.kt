package ru.evgeniy.dpitunnelcli.ui.settings

import android.net.InetAddresses
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import ru.evgeniy.dpitunnelcli.R
import ru.evgeniy.dpitunnelcli.preferences.AppPreferences
import ru.evgeniy.dpitunnelcli.utils.Constants
import ru.evgeniy.dpitunnelcli.utils.MinMaxFilter

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = AppPreferences.SETTINGS_STORAGE_NAME
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val proxyIP = findPreference<EditTextPreference>("preference_proxy_ip")
        proxyIP?.setOnPreferenceChangeListener { _, newValue ->
            val isValid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                InetAddresses.isNumericAddress(newValue.toString())
            } else {
                Patterns.IP_ADDRESS.matcher(newValue.toString()).matches()
            }
            if (!isValid)
                Toast.makeText(requireContext(), getString(R.string.preference_proxy_ip_invalid_ip), Toast.LENGTH_SHORT).show()
            isValid
        }

        val proxyPort = findPreference<EditTextPreference>("preference_proxy_port")
        proxyPort?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
            it.filters = arrayOf(MinMaxFilter(Constants.SERVER_PORT_RANGE))
        }
    }
}