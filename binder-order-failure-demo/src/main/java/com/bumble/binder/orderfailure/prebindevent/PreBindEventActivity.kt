package com.bumble.binder.orderfailure.prebindevent

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumble.binder.orderfailure.R
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

class PreBindEventActivity : AppCompatActivity(), Consumer<ViewModel>, ObservableSource<UiEvent> {

    private val source = PublishSubject.create<UiEvent>()
    private lateinit var title: TextView
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = findViewById(R.id.title)
        button = findViewById(R.id.button)
        MainActivityBindings(this, Feature1(), Feature2()).setup(this)
    }

    override fun subscribe(observer: Observer<in UiEvent>) {
        source.subscribe(observer)
    }

    override fun accept(viewModel: ViewModel) {
        if (viewModel.title != null) {
            title.visibility = View.VISIBLE
            title.text = viewModel.title
            source.onNext(UiEvent.InitialEvent)
        } else {
            title.visibility = View.INVISIBLE
        }

        if (viewModel.showButton) {
            button.visibility = View.VISIBLE
            button.setOnClickListener { source.onNext(UiEvent.SecondEvent) }
        } else {
            button.visibility = View.INVISIBLE
            button.setOnClickListener(null)
        }
    }
}

sealed class UiEvent {
    object InitialEvent : UiEvent()
    object SecondEvent : UiEvent()
}

data class ViewModel(
    val title: String?,
    val showButton: Boolean = false
)