package ru.evgeniy.dpitunnelcli.ui.profiles

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ru.evgeniy.dpitunnelcli.R
import ru.evgeniy.dpitunnelcli.data.usecases.DeleteProfileUseCase
import ru.evgeniy.dpitunnelcli.ui.activity.editProfile.EditProfileActivity
import ru.evgeniy.dpitunnelcli.data.usecases.FetchAllProfilesUseCase
import ru.evgeniy.dpitunnelcli.data.usecases.RenameProfileUseCase
import ru.evgeniy.dpitunnelcli.data.usecases.SettingsUseCase
import ru.evgeniy.dpitunnelcli.databinding.FragmentProfilesBinding

class ProfilesFragment : Fragment() {

    private var _binding: FragmentProfilesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val profilesViewModel by viewModels<ProfilesViewModel> {
        ProfilesViewModelFactory(
            fetchAllProfilesUseCase = FetchAllProfilesUseCase(requireContext().applicationContext),
            deleteProfileUseCase = DeleteProfileUseCase(requireContext().applicationContext),
            renameProfileUseCase = RenameProfileUseCase(requireContext().applicationContext),
            settingsUseCase = SettingsUseCase(requireContext().applicationContext)
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profilesRecycler.adapter = ProfilesAdapter(
            profileListener = {
                resultLauncher.launch(Intent(context, EditProfileActivity::class.java)
                    .putExtra(EditProfileActivity.PROFILE_ID_KEY, it.id))
            },
            profileRenameListener = {
                val inputEditTextField = EditText(requireActivity())
                inputEditTextField.setText(it.title)
                inputEditTextField.maxLines = 1
                inputEditTextField.inputType = InputType.TYPE_TEXT_VARIATION_FILTER or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.dialog_profile_rename_title))
                    .setView(inputEditTextField)
                    .setPositiveButton(getString(R.string.dialog_profile_rename_positive)) { _, _ ->
                        profilesViewModel.rename(it.id!!, inputEditTextField.text.toString())
                    }
                    .setNegativeButton(getString(R.string.dialog_profile_rename_negative), null)
                    .create()
                dialog.show()
            },
            profileDeleteListener = {
                profilesViewModel.delete(it.id!!)
            },
            profileDefaultListener = {
                profilesViewModel.setDefaultProfile(it.id!!)
            }
        )

        binding.profilesRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )

        profilesViewModel.profiles.observe(viewLifecycleOwner) {
            (binding.profilesRecycler.adapter as ProfilesAdapter).bindProfiles(it)
        }

        binding.profilesAddProfileButton.setOnClickListener {
            resultLauncher.launch(Intent(context, EditProfileActivity::class.java)
                .putExtra(EditProfileActivity.PROFILE_ID_KEY, 0))
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}