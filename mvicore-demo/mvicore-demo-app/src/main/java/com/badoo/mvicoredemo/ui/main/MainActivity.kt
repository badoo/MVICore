package com.badoo.mvicoredemo.ui.main

import android.os.Bundle
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
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_main.button0
import kotlinx.android.synthetic.main.activity_main.button1
import kotlinx.android.synthetic.main.activity_main.button2
import kotlinx.android.synthetic.main.activity_main.button3
import kotlinx.android.synthetic.main.activity_main.counter
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.image
import kotlinx.android.synthetic.main.activity_main.imageProgress
import kotlinx.android.synthetic.main.activity_main.showToasts
import kotlinx.android.synthetic.main.activity_main.signOut
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
        buttons.forEachIndexed { idx, button -> button.setOnClickListener { onNext(ButtonClicked(idx))}}
        image.setOnClickListener { onNext(ImageClicked) }
        fab.setOnClickListener { onNext(PlusClicked) }
        signOut.setOnClickListener { logout() }
        showToasts.setOnCheckedChangeListener { _, v -> analyticsTracker.showToasts = v }
    }

    override fun accept(vm: ViewModel) {
        counter.text = vm.counter.toString()
        buttons.forEachIndexed { idx, button -> button.setBackgroundColor(resources.getColor(vm.buttonColors[idx]))}
        imageProgress.visibility = if (vm.imageIsLoading) View.VISIBLE else View.GONE
        loadImage(vm.imageUrl)
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
