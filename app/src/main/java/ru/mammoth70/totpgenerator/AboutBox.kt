package ru.mammoth70.totpgenerator

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class AboutBox : DialogFragment() {
    // Диалоговое окно About.

    companion object {
        const val ABOUT_MESSAGE = "ABOUT_MESSAGE"
        const val ABOUT_TITLE = "ABOUT_TITLE"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity(),
        R.style.AboutDialogStyle)
        builder.setIcon(R.mipmap.ic_launcher_round)
        builder.setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int -> }
        builder.setView(R.layout.dialog_about)
        builder.setTitle(requireArguments().getString(ABOUT_TITLE))
        builder.setMessage(requireArguments().getString(ABOUT_MESSAGE))
        return builder.create()
    }

}