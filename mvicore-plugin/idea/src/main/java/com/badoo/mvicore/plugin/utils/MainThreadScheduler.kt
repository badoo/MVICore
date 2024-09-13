package com.badoo.mvicore.plugin.utils

import io.reactivex.rxjava3.schedulers.Schedulers
import javax.swing.SwingUtilities

val mainThreadScheduler = Schedulers.from {
    SwingUtilities.invokeLater { it.run() }
}
