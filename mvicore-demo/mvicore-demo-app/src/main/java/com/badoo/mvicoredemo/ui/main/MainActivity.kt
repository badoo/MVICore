package com.badoo.mvicoredemo.ui.main

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.view.MenuItem
import android.view.View
import com.badoo.mvicoredemo.R
import com.badoo.mvicoredemo.auth.logout
import com.badoo.mvicoredemo.glide.GlideApp
import com.badoo.mvicoredemo.ui.common.ObservableSourceActivity
import com.badoo.mvicoredemo.ui.lifecycle.LifecycleDemoActivity
import com.badoo.mvicoredemo.ui.main.analytics.FakeAnalyticsTracker
import com.badoo.mvicoredemo.ui.main.di.component.MainScreenInjector
import com.badoo.mvicoredemo.ui.main.event.UiEvent
import com.badoo.mvicoredemo.ui.main.event.UiEvent.ButtonClicked
import com.badoo.mvicoredemo.ui.main.event.UiEvent.ImageClicked
import com.badoo.mvicoredemo.ui.main.event.UiEvent.PlusClicked
import com.badoo.mvicoredemo.ui.main.viewmodel.ViewModel
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_main.*
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
        showToasts.setOnCheckedChangeListener { _, v -> analyticsTracker.showToasts = v }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView.apply {
            setCheckedItem(0)
            setNavigationItemSelectedListener { item ->
                item.isChecked = true
                drawerLayout.closeDrawers()

                when (item.itemId) {
                    R.id.drawer_lifecycle -> LifecycleDemoActivity.start(this@MainActivity)
                }

                true
            }
        }
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
