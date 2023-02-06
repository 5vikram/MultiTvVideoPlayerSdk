package com.multitv.ott.multitvvideoplayer.utils

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.multitv.ott.multitvvideoplayer.database.SharedPreferencePlayer
import com.multitv.ott.multitvvideoplayer.popup.TrackSelectionDialog

class SpeedControllUttils {
    private var dialog: AlertDialog? = null
    private var isTrackSelectionDialog = false;

    fun showTraksDailog(
        context: AppCompatActivity,
        trackSelector: DefaultTrackSelector,
        isShowingTrackSelectionDialog: Boolean
    ): Boolean {

        this.isTrackSelectionDialog = isShowingTrackSelectionDialog;
        val trackSelectionDialog = TrackSelectionDialog.createForTrackSelector(
            trackSelector
        )  /* onDismissListener= */
        { dismissedDialog: DialogInterface? -> isTrackSelectionDialog = false }
        trackSelectionDialog.show(context.supportFragmentManager,  /* tag= */null)
        return isTrackSelectionDialog;
    }

    fun hideSpeedDailog() {
        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
    }

    fun showSpeedControlDailog(
        context: AppCompatActivity,
        sharedPreferencePlayer: SharedPreferencePlayer,
        mMediaPlayer: ExoPlayer
    ) {
        // setup the alert builder
        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
        val position =
            sharedPreferencePlayer.getPreferencesInt(context, "player_playback_position", 2)
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Speed Control")
        val animals = arrayOf("0.25x", "0.5x", "Normal", "1.5x", "2x")
        builder.setSingleChoiceItems(animals, position) { dialog, which ->
            var speed = 1f
            var pitch = 1f
            when (which) {
                0 -> {
                    speed = 0.25f
                    pitch = 0.25f
                    sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 0)
                }
                1 -> {
                    speed = 0.5f
                    pitch = 0.5f
                    sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 1)
                }
                2 -> {
                    speed = 1f
                    pitch = 1f
                    sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 2)
                }
                3 -> {
                    speed = 1.5f
                    pitch = 1.5f
                    sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 3)
                }
                4 -> {
                    speed = 2f
                    pitch = 2f
                    sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 4)
                }
            }
            val param = PlaybackParameters(speed, pitch)
            mMediaPlayer!!.playbackParameters = param
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog?.show()
    }

}