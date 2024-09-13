package com.badoo.mvicore.plugin.action

import com.badoo.mvicore.plugin.iconFrom
import com.badoo.mvicore.plugin.model.ConnectionData
import com.badoo.mvicore.plugin.model.Event
import com.badoo.mvicore.plugin.ui.ConnectionList
import com.badoo.mvicore.plugin.ui.EventList
import com.badoo.mvicore.plugin.utils.showError
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.project.Project
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.text.SimpleDateFormat
import java.util.Date

class RunAction(
    private val project: Project,
    private val disposables: CompositeDisposable,
    private val connections: ConnectionList,
    private val eventsObservable: Observable<JsonElement>,
    private val events: EventList
    ) : AnAction() {
        private var isRunning = false
        private val gson = Gson()
        private val actionManager = ActionManager.getInstance()
        init {
            templatePresentation.icon = actionManager.iconFrom(IdeActions.ACTION_DEFAULT_RUNNER)
        }

        override fun actionPerformed(e: AnActionEvent) {
            if (isRunning) {
                disposables.clear()
                connections.clear()
                isRunning = false
            } else {
                isRunning = true
                disposables += eventsObservable
                    .doOnDispose { isRunning = false }
                    .doOnTerminate { isRunning = false }
                    .subscribe({
                        parseEvent(it)
                    }, {
                        if (it is Exception) {
                            project.showError("Error connecting to device:", it)
                        }
                    })
            }
            e.presentation.icon = actionManager.iconFrom(
                if (isRunning) IdeActions.ACTION_STOP_PROGRAM else IdeActions.ACTION_DEFAULT_RUNNER
            )
        }

        private fun parseEvent(it: JsonElement) {
            when (it) {
                is JsonObject -> when (it.get("type").asString) {
                    "init" -> {
                        val connections = it.get("connections").asJsonArray.mapNotNull { it.toConnection() }
                        val items = it.get("items").asJsonArray.mapNotNull { it.asJsonObject?.toItem() }
                        this.connections.clear()
                        connections.forEach {
                            this.connections.add(it)
                        }
                        this.events.clear()
                        items.forEach {
                            this.events.add(it)
                        }
                    }
                    "bind" -> {
                        val connection = it.get("connection").toConnection()
                        if (connection != null) connections.add(connection)
                    }
                    "data" -> {
                        it.asJsonObject?.toItem()?.let {
                            events.add(it)
                        }
                    }
                    "complete" -> {
                        val connection = it.get("connection").toConnection()
                        if (connection != null) connections.remove(connection)
                    }
                }
            }
        }

        private fun JsonElement.toConnection() = when (this) {
            is JsonObject -> gson.fromJson(this, ConnectionData::class.java)
            else -> null
        }

        private fun JsonObject.toItem(): Event.Item? {
            val connection = get("connection").toConnection() ?: return null
            val element = getAsJsonObject("element")
            val parsedElement = JsonObject().apply {
                val (timestamp, type, value) = element.parse()
                add("[${timestamp?.dateString()}] : $type", value)
            }
            return Event.Item(connection, parsedElement)
        }

        private fun JsonElement.parse(): Triple<Long?, String?, JsonElement> = when (this) {
            is JsonObject -> if (isWrapper) {
                val obj = this
                val type = obj.remove(TYPE).asString
                var primitive = obj.remove(VALUE)
                val timestamp = obj.remove(TIMESTAMP)?.asLong

                if (primitive == null) {
                    this.entrySet().map {
                        it to it.value.parse()
                    }.forEach { (entry, result) ->
                        val (key, _) = entry
                        val (_, type, child) = result
                        val typeDesc = type?.let {" ($it)"}.orEmpty()
                        obj.remove(key)
                        obj.add("$key$typeDesc", child)
                    }
                } else if (primitive.isJsonArray) {
                    val array = primitive.asJsonArray
                    val result = JsonObject()
                    array.forEachIndexed { i, el ->
                        val (_, type, obj) = el.parse()
                        val typeDesc = type?.let {" ($it)"}.orEmpty()
                        result.add("$i$typeDesc", obj)
                    }
                    primitive = result
                }

                Triple(timestamp, type, primitive ?: this)
            } else {
                Triple(null, null, this)
            }
            else -> Triple(null, null, this)
        }

        private val TYPE = "\$type"
        private val VALUE = "\$value"
        private val TIMESTAMP = "\$timestamp"

        private val JsonElement.isWrapper: Boolean
            get() = isJsonObject && this.asJsonObject.get(TYPE) != null

        private fun Long.dateString() =
            SimpleDateFormat("HH:mm:ss").format(Date(this))
    }
