package medina.juanantonio.mplayer.common.extensions

import android.content.Context
import android.content.Intent
import medina.juanantonio.mplayer.features.dialog.DialogActivity
import medina.juanantonio.mplayer.features.dialog.DialogActivity.Companion.DIALOG_DESCRIPTION
import medina.juanantonio.mplayer.features.dialog.DialogActivity.Companion.DIALOG_NEGATIVE_BUTTON
import medina.juanantonio.mplayer.features.dialog.DialogActivity.Companion.DIALOG_POSITIVE_BUTTON
import medina.juanantonio.mplayer.features.dialog.DialogActivity.Companion.DIALOG_TITLE

fun Context.createDialogIntent(
    title: String,
    description: String = "",
    positiveButton: String = getString(android.R.string.ok),
    negativeButton: String = getString(android.R.string.cancel)
): Intent {
    return Intent(this, DialogActivity::class.java).apply {
        putExtra(DIALOG_TITLE, title)
        putExtra(DIALOG_DESCRIPTION, description)
        putExtra(DIALOG_POSITIVE_BUTTON, positiveButton)
        putExtra(DIALOG_NEGATIVE_BUTTON, negativeButton)
    }
}