package ru.evgeniy.dpitunnelcli.ui.profiles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import ru.evgeniy.dpitunnelcli.R
import ru.evgeniy.dpitunnelcli.domain.entities.Profile

class ProfilesAdapter(val profileListener: (Profile) -> Unit,
                      val profileRenameListener: (Profile) -> Unit,
                      val profileDeleteListener: (Profile) -> Unit): RecyclerView.Adapter<ProfileViewHolder>() {
    private var profiles = listOf<Profile>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        return ProfileViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_holder_profile_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.onBind(profiles[position])
        holder.itemView.setOnClickListener {
            profileListener(profiles[position])
        }
        holder.options.setOnClickListener { view ->
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.profile_item_menu)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.profile_item_menu_rename -> {
                        profileRenameListener(profiles[position])
                        true
                    }
                    R.id.profile_item_menu_delete -> {
                        profileDeleteListener(profiles[position])
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int = profiles.size

    fun bindProfiles(newProfiles: List<Profile>?) {
        newProfiles?.let {
            profiles = it
            notifyDataSetChanged()
        }
    }
}

class ProfileViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val title: TextView = itemView.findViewById(R.id.view_holder_profile_title)
    val options: ImageButton = itemView.findViewById(R.id.view_holder_profile_options)

    fun onBind(profile: Profile) {
        title.text = profile.title ?: itemView.context.getString(R.string.unnamed_profile_name)
    }
}