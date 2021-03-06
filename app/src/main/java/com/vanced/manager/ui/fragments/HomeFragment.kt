package com.vanced.manager.ui.fragments

import android.content.*
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.vanced.manager.R
import com.vanced.manager.adapter.SectionPageAdapter
import com.vanced.manager.adapter.SectionPageRootAdapter
import com.vanced.manager.databinding.FragmentHomeBinding
import com.vanced.manager.ui.dialogs.DialogContainer.installAlertBuilder
import com.vanced.manager.ui.viewmodels.HomeViewModel
import com.vanced.manager.utils.AppUtils.installing

open class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var variant: String
    private var isExpanded: Boolean = false
    private val viewModel: HomeViewModel by viewModels()
    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(requireActivity()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().title = getString(R.string.title_home)
        setHasOptionsMenu(true)
        variant = if (requireActivity().findViewById<TabLayout>(R.id.main_tablayout).selectedTabPosition == 1) "root" else "nonroot"
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.viewModel = viewModel
        viewModel.variant = variant
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        with(binding) {
            includeChangelogsLayout.changelogButton.setOnClickListener(this@HomeFragment)

            includeVancedLayout.vancedCard.setOnLongClickListener {
                versionToast("Vanced", viewModel?.vanced?.get()?.getInstalledVersionName()!!)
                true
            }
            
            includeMusicLayout.musicCard.setOnLongClickListener {
                versionToast("Music", viewModel?.music?.get()?.getInstalledVersionName()!!)
                true
            }

            includeMicrogLayout.microgCard.setOnLongClickListener {
                versionToast("MicroG", viewModel?.microg?.get()?.getInstalledVersionName()!!)
                true
            }
        }

        with(binding.includeChangelogsLayout) {
            viewpager.adapter = 
            if (variant == "root") 
                SectionPageRootAdapter(this@HomeFragment)
            else
                SectionPageAdapter(this@HomeFragment)

            TabLayoutMediator(tablayout, viewpager) { tab, position ->
                tab.text = 
                if (variant == "root") {
                    when (position) {
                        0 -> "Vanced"
                        else -> "Manager"
                    }
                } else {
                    when (position) {
                        0 -> "Vanced"
                        1 -> "Music"
                        2 -> "MicroG"
                        else -> "Manager"
                    }
                }
            }.attach()
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.changelog_button -> cardExpandCollapse()
        }
    }

    private fun versionToast(name: String, app: String?) {
        val clip = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clip.setPrimaryClip(ClipData.newPlainText(name, app))
        Toast.makeText(activity, getString(R.string.version_toast, name), Toast.LENGTH_LONG).show()
    }

    private fun cardExpandCollapse() {
        with(binding.includeChangelogsLayout) {
            viewpager.visibility = if (isExpanded) View.GONE else View.VISIBLE
            tablayout.visibility = if (isExpanded) View.GONE else View.VISIBLE
            changelogButton.animate().apply {
                rotation(if (isExpanded) 0F else 180F)
                interpolator = AccelerateDecelerateInterpolator()
            }
            isExpanded = !isExpanded
        }
    }
    
    override fun onPause() {
        localBroadcastManager.unregisterReceiver(broadcastReceiver)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        registerReceivers()
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                INSTALL_FAILED -> {
                    with(binding) {
                        includeMicrogLayout.microgInstalling.visibility = View.GONE
                        includeVancedLayout.vancedInstalling.visibility = View.GONE
                        includeMusicLayout.musicInstalling.visibility = View.GONE
                    }
                    installAlertBuilder(intent.getStringExtra("errorMsg") as String, requireActivity())
                    installing = false
                }
                REFRESH_HOME -> viewModel.fetchData()
            }
        }
    }

    private fun registerReceivers() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(INSTALL_FAILED)
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    companion object {      
        const val INSTALL_FAILED = "install_failed"
        const val REFRESH_HOME = "refresh_home"
    }
}

