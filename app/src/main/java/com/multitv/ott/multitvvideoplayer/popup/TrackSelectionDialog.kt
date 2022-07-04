package com.multitv.ott.multitvvideoplayer.popup

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.multitv.ott.multitvvideoplayer.R
import com.multitv.ott.multitvvideoplayer.adapters.ResolutionAdapter
import com.multitv.ott.multitvvideoplayer.listener.OnTrackSelected
import com.multitv.ott.multitvvideoplayer.models.TrackResolution
import org.w3c.dom.Text

object TrackSelectionDialog {

    fun showTrackSelectionDialog(context: Context, onTrackSelected: OnTrackSelected,
                    trackList: List<TrackResolution>){

        var resolution: TrackResolution? = null

        var layoutInflater = LayoutInflater.from(context);
        var view = layoutInflater.inflate(R.layout.track_selection_dialog_layout, null)

        var builder = AlertDialog.Builder(context)
        var alertDialog = builder.create()
        alertDialog.setView(view)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val qualityRecyclerView: RecyclerView = view.findViewById(R.id.quality_recycler_view)
        val cancelButton: TextView = view.findViewById(R.id.cancel_btn)
        val okayButton: TextView = view.findViewById(R.id.okay_btn)

        qualityRecyclerView.adapter = ResolutionAdapter(trackList,
                    object : ResolutionAdapter.AdapterCallbacks{
                        override fun onItemSelected(trackResolution: TrackResolution) {
                            // here you will get the selected track resolution
                            resolution = trackResolution
                        }
                    })


        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        okayButton.setOnClickListener {
            onTrackSelected.onTrackSelected(resolution!!)
            alertDialog.dismiss()
        }


    }



}