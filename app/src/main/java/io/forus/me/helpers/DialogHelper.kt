package io.forus.me.helpers

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import io.forus.me.R

interface DialogHelper {

    companion object {
        /**
         * Add a button to a generic dialog, which does action bound to listener.onClick, or will use
         * the dialog's dismiss function if null.
         */
        fun addNegativeButton(dialog:AlertDialog, textRes: Int, listener: View.OnClickListener? = null) {
            val negativeButton = dialog.findViewById<Button>(R.id.negativeButton)
            if (negativeButton != null) {
                negativeButton.visibility = View.VISIBLE
                negativeButton.setText(textRes)
                if (listener != null) {
                    negativeButton.setOnClickListener(listener)
                } else {
                    negativeButton.setOnClickListener( {
                        dialog.cancel()
                    })
                }
            }
        }

        fun addPositiveButton(dialog: AlertDialog, textRes: Int, listener: View.OnClickListener?) {
            val positiveButton = dialog.findViewById<Button>(R.id.positiveButton)
            if (positiveButton != null) {
                positiveButton.visibility = View.VISIBLE
                positiveButton.setText(textRes)
                positiveButton.setOnClickListener(listener)
            }
        }

        /**
         * Create a dialog using the default style
         * @param context The view context of the dialog
         * @param titleRes The resource number of the title text
         * @param descriptionRes The resource number of the description text, which can be null
         * ( or default) if a custom view is used inside the dialog
         * @param onCloseListener What to do if the dialog closes on default
         */
        fun createDialog(context: Context, titleRes: Int, descriptionRes: Int? = null, onCloseListener: OnCloseListener): AlertDialog {
            var dialog = AlertDialog.Builder(context, R.style.AppTheme_Dialog).create()
            dialog = stylize(dialog, titleRes, descriptionRes)
            dialog.setOnDismissListener(onCloseListener)
            dialog.setOnCancelListener(onCloseListener)
            return dialog
        }

        fun setOnCloseListener(dialog: AlertDialog,  listener:OnCloseListener): AlertDialog {
            dialog.setOnCancelListener(listener)
            dialog.setOnDismissListener(listener)
            return dialog
        }

        fun setView(dialog:AlertDialog, view:View): AlertDialog {
            val viewContainer = dialog.findViewById<LinearLayout>(R.id.contentWrapper)
            if (viewContainer != null) {
                viewContainer.removeAllViews()
                viewContainer.addView(view)
            }
            return dialog
        }

        private fun stylize(dialog: AlertDialog, titleRes: Int, descriptionRes: Int? = null): AlertDialog {
            val layout = LayoutInflater.from(dialog.context).inflate(R.layout.popup_generic, null)
            val toolbar: Toolbar = layout.findViewById(R.id.toolbar)
            toolbar.setTitle(titleRes)
            if (descriptionRes != null) {
                layout.findViewById<TextView>(R.id.titleView)?.setText(titleRes)
                layout.findViewById<TextView>(R.id.descriptionView)?.setText(descriptionRes)
            }
            dialog.setView(layout)
            return dialog
        }
    }

    abstract class OnCloseListener: DialogInterface.OnDismissListener, DialogInterface.OnCancelListener {
        abstract fun onClose(dialog: DialogInterface?)

        override fun onDismiss(dialog: DialogInterface?) {
            this.onClose(dialog)
        }

        override fun onCancel(dialog: DialogInterface?) {
            this.onClose(dialog)
        }

    }
}