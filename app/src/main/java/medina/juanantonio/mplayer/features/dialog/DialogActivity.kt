package medina.juanantonio.mplayer.features.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.leanback.app.GuidedStepSupportFragment

class DialogActivity : AppCompatActivity() {

    companion object {
        const val DIALOG_TITLE = "DialogTitle"
        const val DIALOG_DESCRIPTION = "DialogDescription"
        const val DIALOG_POSITIVE_BUTTON = "DialogPositiveButton"
        const val DIALOG_NEGATIVE_BUTTON = "DialogNegativeButton"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.parseColor("#21272A")))

        if (savedInstanceState == null) {
            GuidedStepSupportFragment.addAsRoot(
                this,
                DialogFragment(),
                android.R.id.content
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}