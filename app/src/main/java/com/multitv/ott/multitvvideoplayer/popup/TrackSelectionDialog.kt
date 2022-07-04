package com.multitv.ott.multitvvideoplayer.popup

import android.R.attr.left
import android.R.attr.right
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.multitv.ott.multitvvideoplayer.R
import com.multitv.ott.multitvvideoplayer.custom.ToastMessage
import com.multitv.ott.multitvvideoplayer.listener.OnTrackSelected
import com.multitv.ott.multitvvideoplayer.models.TrackResolution


class TrackSelectionDialog {
    private var index = 0

    fun showTrackSelectionDialog(
        context: Context, onTrackSelected: OnTrackSelected,
        trackList: List<TrackResolution>
    ) {

        var resolution: TrackResolution? = null

        var layoutInflater = LayoutInflater.from(context);
        var view = layoutInflater.inflate(R.layout.track_selection_dialog_layout, null)

        var builder = AlertDialog.Builder(context)
        var alertDialog = builder.create()
        alertDialog.setView(view)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val qualityRadioGroup: RadioGroup = view.findViewById(R.id.quality_radio_group)
        val cancelButton: TextView = view.findViewById(R.id.cancel_btn)
        val okayButton: TextView = view.findViewById(R.id.okay_btn)

        for (item in trackList) {
            val radioButton = RadioButton(context)
            if (index>0)
            radioButton.text = item.heightStr + "p"
            else
                radioButton.text = item.heightStr

            radioButton.textSize = 16F

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(20, 10, 20, 20)
            radioButton.layoutParams = params

            qualityRadioGroup.addView(radioButton)
            index = index + 1
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        okayButton.setOnClickListener {
            alertDialog.dismiss()

            if (index == 0)
                alertDialog.dismiss()
            else {
                ToastMessage.showLogs(ToastMessage.LogType.DEBUG,"Selected item:::",""+trackList.get(index).heightStr)
                resolution = trackList.get(index)
                onTrackSelected.onTrackSelected(resolution!!)
                alertDialog.dismiss()
            }

        }


    }


}