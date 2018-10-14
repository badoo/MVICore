package com.badoo.mvicoredemo.ui.main

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Html
import com.badoo.mvicoredemo.R

class HelpDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity!!)
            .setMessage(
                Html.fromHtml(resources.getString(R.string.help_dialog_main)))
            .setPositiveButton(R.string.action_ok) { dialog, _ -> dialog.dismiss() }
            .create()
}
