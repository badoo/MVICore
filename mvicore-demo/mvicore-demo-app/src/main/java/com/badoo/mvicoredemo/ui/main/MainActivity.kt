package com.badoo.mvicoredemo.ui.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import com.badoo.mvicoredemo.auth.logout
import com.badoo.mvicoredemo.databinding.ActivityMainBinding
import com.badoo.mvicoredemo.glide.GlideApp
import com.badoo.mvicoredemo.ui.common.ObservableSourceActivity
import com.badoo.mvicoredemo.ui.main.analytics.FakeAnalyticsTracker
import com.badoo.mvicoredemo.ui.main.event.UiEvent
import com.badoo.mvicoredemo.ui.main.event.UiEvent.ButtonClicked
import com.badoo.mvicoredemo.ui.main.event.UiEvent.ImageClicked
import com.badoo.mvicoredemo.ui.main.event.UiEvent.PlusClicked
import com.badoo.mvicoredemo.ui.main.viewmodel.ViewModel
import dagger.hilt.android.AndroidEntryPoint
import init
import io.reactivex.rxjava3.functions.Consumer
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ObservableSourceActivity<UiEvent>(), Consumer<ViewModel> {

    @Inject
    lateinit var bindings: MainActivityBindings

    @Inject
    lateinit var analyticsTracker: FakeAnalyticsTracker
    private lateinit var buttons: List<View>
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
        setupDebugDrawer()
        bindings.setup(this)
    }

    private fun setupViews() {
        buttons = listOf(binding.button0, binding.button1, binding.button2, binding.button3)
        buttons.forEachIndexed { idx, button -> button.setOnClickListener { onNext(ButtonClicked(idx)) } }
        binding.image.setOnClickListener { onNext(ImageClicked) }
        binding.fab.setOnClickListener { onNext(PlusClicked) }
        binding.signOut.setOnClickListener { logout() }
        binding.showToasts.setOnClickListener {
            // Only for debugging purposes, otherwise should be part of the state!
            analyticsTracker.showToasts = !analyticsTracker.showToasts
            binding.showToasts.toggle(analyticsTracker.showToasts)
        }

        binding.help.setOnClickListener {
            HelpDialogFragment().show(supportFragmentManager, "help")
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.navigationView.init(binding.drawerLayout, 0)
    }

    override fun accept(vm: ViewModel) {
        binding.counter.text = vm.counter.toString()
        buttons.forEachIndexed { idx, button ->
            button.setBackgroundColor(resources.getColor(vm.buttonColors[idx]))
        }
        binding.imageProgress.visibility = if (vm.imageIsLoading) View.VISIBLE else View.GONE
        loadImage(vm.imageUrl)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    private fun loadImage(url: String?) {
        if (url != null) {
            GlideApp.with(this)
                .load(url)
                .centerCrop()
                .into(binding.image)
        }
    }
}
