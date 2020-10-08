package com.badoo.mvicoredemo.ui.main

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.view.MenuItem
import android.view.View
import com.badoo.mvicoredemo.R
import com.badoo.mvicoredemo.auth.logout
import com.badoo.mvicoredemo.glide.GlideApp
import com.badoo.mvicoredemo.ui.common.ObservableSourceActivity
import com.badoo.mvicoredemo.ui.main.analytics.FakeAnalyticsTracker
import com.badoo.mvicoredemo.ui.main.di.component.MainScreenInjector
import com.badoo.mvicoredemo.ui.main.event.UiEvent
import com.badoo.mvicoredemo.ui.main.event.UiEvent.ButtonClicked
import com.badoo.mvicoredemo.ui.main.event.UiEvent.ImageClicked
import com.badoo.mvicoredemo.ui.main.event.UiEvent.PlusClicked
import com.badoo.mvicoredemo.ui.main.viewmodel.ViewModel
import init
import io.reactivex.rxjava3.functions.Consumer
import kotlinx.android.synthetic.main.activity_main.button0
import kotlinx.android.synthetic.main.activity_main.button1
import kotlinx.android.synthetic.main.activity_main.button2
import kotlinx.android.synthetic.main.activity_main.button3
import kotlinx.android.synthetic.main.activity_main.counter
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.help
import kotlinx.android.synthetic.main.activity_main.image
import kotlinx.android.synthetic.main.activity_main.imageProgress
import kotlinx.android.synthetic.main.activity_main.navigationView
import kotlinx.android.synthetic.main.activity_main.showToasts
import kotlinx.android.synthetic.main.activity_main.signOut
import kotlinx.android.synthetic.main.activity_main.toolbar
import javax.inject.Inject

class MainActivity : ObservableSourceActivity<UiEvent>(), Consumer<ViewModel> {

    @Inject lateinit var bindings: MainActivityBindings
    @Inject lateinit var analyticsTracker: FakeAnalyticsTracker
    private lateinit var buttons: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainScreenInjector.get(this).inject(this)
        setContentView(R.layout.activity_main)
        setupViews()
        setupDebugDrawer()
        bindings.setup(this)
    }

    private fun setupViews() {
        buttons = listOf(button0, button1, button2, button3)
        buttons.forEachIndexed { idx, button -> button.setOnClickListener { onNext(ButtonClicked(idx)) } }
        image.setOnClickListener { onNext(ImageClicked) }
        fab.setOnClickListener { onNext(PlusClicked) }
        signOut.setOnClickListener { logout() }
        showToasts.setOnClickListener {
            // Only for debugging purposes, otherwise should be part of the state!
            analyticsTracker.showToasts = !analyticsTracker.showToasts
            showToasts.toggle(analyticsTracker.showToasts)
        }

        help.setOnClickListener {
            HelpDialogFragment().show(supportFragmentManager, "help")
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView.init(drawerLayout, 0)
    }

    override fun accept(vm: ViewModel) {
        counter.text = vm.counter.toString()
        buttons.forEachIndexed { idx, button -> button.setBackgroundColor(resources.getColor(vm.buttonColors[idx]))}
        imageProgress.visibility = if (vm.imageIsLoading) View.VISIBLE else View.GONE
        loadImage(vm.imageUrl)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun loadImage(url: String?) {
        if (url != null) {
            GlideApp.with(this)
                .load(url)
                .centerCrop()
                .into(image)
        }
    }
}
