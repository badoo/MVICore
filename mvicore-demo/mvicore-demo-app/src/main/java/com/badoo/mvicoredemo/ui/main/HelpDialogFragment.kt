package com.badoo.mvicoredemo.ui.main

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.badoo.mvicoredemo.R

class HelpDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireActivity())
            .setMessage(
                Html.fromHtml(resources.getString(R.string.help_dialog_main))
            )
            .setPositiveButton(R.string.action_ok) { dialog, _ -> dialog.dismiss() }
            .create()
}
