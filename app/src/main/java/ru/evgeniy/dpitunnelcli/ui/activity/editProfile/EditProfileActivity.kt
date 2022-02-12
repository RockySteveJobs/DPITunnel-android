package ru.evgeniy.dpitunnelcli.ui.activity.editProfile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import ru.evgeniy.dpitunnelcli.R
import ru.evgeniy.dpitunnelcli.data.usecases.*
import ru.evgeniy.dpitunnelcli.databinding.ActivityEditProfileBinding
import ru.evgeniy.dpitunnelcli.utils.Constants
import ru.evgeniy.dpitunnelcli.utils.MinMaxFilter
import ru.evgeniy.dpitunnelcli.utils.scrollToBottom


class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var granted = true
            permissions.forEach {
                if(!it.value) granted = false
            }
            if (granted) {
                if (checkLocationEnabled())
                    editProfilesViewModel.getDefaultIfaceWifiAP(applicationContext)
            } else {
                Toast.makeText(this, R.string.location_permission_failed, Toast.LENGTH_LONG).show()
            }
        }

    private val editProfilesViewModel by viewModels<EditProfileViewModel> {
        EditProfileViewModelFactory(
            getDefaultIfaceUseCase = FetchDefaultIfaceWifiAPUseCase(),
            autoConfigUseCase = AutoConfigUseCase(),
            settingsUseCase = SettingsUseCase(applicationContext),
            saveProfileUseCase = SaveProfileUseCase(applicationContext),
            fetchProfileUseCase = FetchProfileUseCase(applicationContext),
            getStringResourceUseCase = GetStringResourceUseCase(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.editProfileToolbar)

        editProfilesViewModel.uiState.observe(this) { state ->
            when(state) {
                is EditProfileViewModel.UIState.Normal -> {}
                is EditProfileViewModel.UIState.Error -> {
                    when(state.error) {
                        EditProfileViewModel.ErrorType.ERROR_TYPE_INVALID_PROFILE_ID -> {
                            Toast.makeText(this, R.string.invalid_profile_id_failed, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                is EditProfileViewModel.UIState.Finish -> returnResult()
            }
        }

        val fabSave = binding.editProfileFab
        fabSave.setOnClickListener {
            editProfilesViewModel.save()
        }

        val butSetProfileFromCurrentSettings = binding.editProfileProfileIdButton

        butSetProfileFromCurrentSettings.setOnClickListener {
            checkLocationPermissions()
        }

        val edittextProfileId = binding.editProfileProfileIdEdit
        edittextProfileId.doAfterTextChanged {
            editProfilesViewModel.profileId = it.toString()
        }

        val spinnerZeroLevel = binding.editProfileDesyncAttacksZeroLevel
        ArrayAdapter.createFromResource(
            this,
            R.array.zero_attacks,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerZeroLevel.adapter = adapter
        }
        spinnerZeroLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                editProfilesViewModel.zeroAttack = pos
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        val spinnerFirstLevel = binding.editProfileDesyncAttacksFirstLevel
        ArrayAdapter.createFromResource(
            this,
            R.array.first_attacks,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerFirstLevel.adapter = adapter
        }
        spinnerFirstLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                editProfilesViewModel.firstAttack = pos
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        val edittextTtl = binding.editProfileDesyncAttacksTtl
        edittextTtl.filters = arrayOf(MinMaxFilter(Constants.TTL_VALUE_RANGE))
        edittextTtl.doAfterTextChanged {
            editProfilesViewModel.ttl = it.toString()
        }

        val edittextWindowSize = binding.editProfileDesyncAttacksWindowSize
        edittextWindowSize.filters = arrayOf(MinMaxFilter(Constants.TCP_WINDOW_SIZE_VALUE_RANGE))
        edittextWindowSize.doAfterTextChanged {
            editProfilesViewModel.windowSize = it.toString()
        }

        val edittextWindowScaleFactor = binding.editProfileDesyncAttacksWindowScaleFactor
        edittextWindowScaleFactor.filters = arrayOf(MinMaxFilter(Constants.TCP_WINDOW_SCALE_FACTOR_VALUE_RANGE))
        edittextWindowScaleFactor.doAfterTextChanged {
            editProfilesViewModel.windowScaleFactor = it.toString()
        }

        val edittextSplitPosition = binding.editProfileDesyncAttacksSplitPosition
        edittextSplitPosition.filters = arrayOf(MinMaxFilter(Constants.SPLIT_POSITION_VALUE_RANGE))
        edittextSplitPosition.doAfterTextChanged {
            editProfilesViewModel.splitPosition = it.toString()
        }

        val switchSplitAtSNI = binding.editProfileDesyncAttacksSplitAtSni
        switchSplitAtSNI.setOnCheckedChangeListener { _, isChecked ->
            editProfilesViewModel.splitAtSNI = isChecked
        }

        val fullLog = binding.editProfileDesyncAttacksAutoconfigFullLog
        fullLog.setOnCheckedChangeListener { _, isChecked ->
            editProfilesViewModel.showFullLog(isChecked)
        }

        val terminalScroll = binding.editProfileDesyncAttacksAutoconfigTerminalScroll
        val terminalOutput = binding.editProfileDesyncAttacksAutoconfigTerminalOutput

        val terminalInput = binding.editProfileDesyncAttacksAutoconfigTerminalInput
        terminalInput.addTextChangedListener {
            it?.let {
                val s = it.toString()
                if (s.isNotEmpty() && s[s.length - 1] == '\n') {
                    editProfilesViewModel.autoConfigSendInput(s)
                    terminalInput.setText("")
                }
            }
        }

        editProfilesViewModel.autoConfigOutput.observe(this) {
            fullLog.visibility = View.VISIBLE
            terminalOutput.visibility = View.VISIBLE
            terminalInput.visibility = View.VISIBLE
            terminalOutput.text = it
            terminalScroll.scrollToBottom()
            terminalInput.requestFocus()
        }

        val buttonAutoConfig = binding.editProfileDesyncAttacksAutoconfigButton
        buttonAutoConfig.setOnClickListener {
            editProfilesViewModel.runAutoConfig(applicationContext)
        }

        val switchDoh = binding.editProfileDnsUseDoh
        switchDoh.setOnCheckedChangeListener { _, isChecked ->
            editProfilesViewModel.doh = isChecked
        }

        val edittextDohServer = binding.editProfileDnsDohServer
        edittextDohServer.doAfterTextChanged {
            editProfilesViewModel.dohServer = it.toString()
        }

        val edittextDns = binding.editProfileDnsDnsServer
        edittextDns.doAfterTextChanged {
            editProfilesViewModel.dnsServer = it.toString()
        }

        editProfilesViewModel.profile.observe(this) { profile ->
            supportActionBar?.title = profile?.title ?: getString(R.string.unnamed_profile_name)
            edittextProfileId.setText(profile.id)
            spinnerZeroLevel.setSelection(profile.desyncZeroAttack?.ordinal?.plus(1) ?: 0)
            spinnerFirstLevel.setSelection(profile.desyncFirstAttack?.ordinal?.plus(1) ?: 0)
            edittextTtl.setText(profile.fakePacketsTtl?.toString() ?: "")
            edittextWindowSize.setText(profile.windowSize?.toString() ?: "")
            edittextWindowScaleFactor.setText(profile.windowScaleFactor?.toString() ?: "")
            edittextSplitPosition.setText(profile.splitPosition?.toString() ?: "")
            switchSplitAtSNI.isChecked = profile.splitAtSni
            switchDoh.isChecked = profile.doh
            edittextDohServer.setText(profile.dohServer ?: "")
            edittextDns.setText(profile.inBuiltDNSIP?.plus(profile.inBuiltDNSPort?.let { ":$it" } ?: "") ?: "")
        }

        editProfilesViewModel.loadProfile(intent.getStringExtra(PROFILE_ID_KEY))
    }

    private fun returnResult() {
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission.launch(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
            )
        } else {
            if (checkLocationEnabled())
                editProfilesViewModel.getDefaultIfaceWifiAP(applicationContext)
        }
    }

    private fun checkLocationEnabled(): Boolean {
        val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        var networkEnabled = false
        var locationEnabled = true

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }

        if(!gpsEnabled && !networkEnabled) {
            Toast.makeText(this, R.string.location_disabled_failed, Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

            locationEnabled = false
        }

        return locationEnabled
    }

    companion object {
        const val PROFILE_ID_KEY = "profile_id_key"
    }
}