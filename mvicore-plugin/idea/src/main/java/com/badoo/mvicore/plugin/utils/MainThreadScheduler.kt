package com.badoo.mvicore.plugin.utils

import io.reactivex.schedulers.Schedulers
import javax.swing.SwingUtilities

val mainThreadScheduler = Schedulers.from {
    SwingUtilities.invokeLater { it.run() }
}
