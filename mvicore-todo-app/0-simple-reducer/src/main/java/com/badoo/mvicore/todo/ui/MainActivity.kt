package com.badoo.mvicore.todo.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.badoo.mvicore.android.AndroidTimeCapsule
import com.badoo.mvicore.android.lifecycle.CreateDestroyBinderLifecycle
import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.using
import com.badoo.mvicore.todo.R
import com.badoo.mvicore.todo.feature.TodoListFeature
import com.badoo.mvicore.todo.mapper.StateToViewModel
import com.badoo.mvicore.todo.mapper.UiEventToWish

class MainActivity: AppCompatActivity() {

    private lateinit var capsule: AndroidTimeCapsule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        capsule = AndroidTimeCapsule(savedInstanceState)
        val feature = TodoListFeature(capsule)

        val view = TodoListView(findViewById(android.R.id.content))
        Binder(CreateDestroyBinderLifecycle(lifecycle)).apply {
            bind(view to feature using UiEventToWish)
            bind(feature to view using StateToViewModel)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capsule.saveState(outState)
    }
}
