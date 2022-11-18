package com.multitv.ott.multitvvideoplayer.download

import android.content.Context
import android.net.Uri
import android.os.StatFs
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.offline.*
import com.google.android.exoplayer2.offline.DownloadHelper.LiveContentUnsupportedException
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.multitv.ott.multitvvideoplayer.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.IOException
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet


private const val TAG = "DownloadTracker"

/** Tracks media that has been downloaded.  */
class DownloadTracker(
    context: Context,
    private val httpDataSourceFactory: HttpDataSource.Factory,
    private val downloadManager: DownloadManager
) {
    /**
     * Listens for changes in the tracked downloads.
     */
    interface Listener {
        /**
         * Called when the tracked downloads changed.
         */
        fun onDownloadsChanged(download: Download)

    }

    private val applicationContext: Context = context.applicationContext
    private val listeners: CopyOnWriteArraySet<Listener> = CopyOnWriteArraySet()
    private val downloadIndex: DownloadIndex = downloadManager.downloadIndex
    private var startDownloadDialogHelper: StartDownloadDialogHelper? = null
    private var availableBytesLeft: Long =
        StatFs(DownloadUtil.getDownloadDirectory(context).path).availableBytes

    val downloads: HashMap<Uri, Download> = HashMap()

    private var isTrackDailogShowing = false

    init {
        downloadManager.addListener(DownloadManagerListener())
        loadDownloads()
    }

    fun addListener(listener: Listener) {
        Assertions.checkNotNull(listener)
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun isDownloaded(mediaItem: MediaItem): Boolean {
        val download = downloads[mediaItem.playbackProperties?.uri]
        return download != null && download.state == Download.STATE_COMPLETED
    }

    fun hasDownload(uri: Uri?): Boolean = downloads.keys.contains(uri)

    fun getDownloadRequest(uri: Uri?): DownloadRequest? {
        uri ?: return null
        val download = downloads[uri]
        return if (download != null && download.state != Download.STATE_FAILED) download.request else null
    }

    fun toggleDownloadDialogHelper(
        context: Context, mediaItem: MediaItem, dailogCallbackListener: DailogCallbackListener,
        positiveCallback: (() -> Unit)? = null, dismissCallback: (() -> Unit)? = null
    ) {
        startDownloadDialogHelper?.release()
        startDownloadDialogHelper =
            StartDownloadDialogHelper(
                context,
                getDownloadHelper(mediaItem),
                mediaItem,
                positiveCallback,
                dismissCallback, dailogCallbackListener
            )
    }


    fun getTrackDailogStatus(): Boolean {
        return isTrackDailogShowing
    }

    fun setTrackDailogStatus(isTrackDailogShowing: Boolean) {
        this.isTrackDailogShowing = isTrackDailogShowing
    }

    fun toggleDownloadPopupMenu(context: Context, anchor: View, uri: Uri?) {
        val popupMenu = PopupMenu(context, anchor).apply { inflate(R.menu.popup_menu) }
        val download = downloads[uri]
        download ?: return

        popupMenu.menu.apply {
            findItem(R.id.cancel_download).isVisible =
                listOf(
                    Download.STATE_DOWNLOADING,
                    Download.STATE_STOPPED,
                    Download.STATE_QUEUED,
                    Download.STATE_FAILED
                ).contains(download.state)
            findItem(R.id.delete_download).isVisible = download.state == Download.STATE_COMPLETED
            findItem(R.id.resume_download).isVisible =
                listOf(Download.STATE_STOPPED, Download.STATE_FAILED).contains(download.state)
            findItem(R.id.pause_download).isVisible = download.state == Download.STATE_DOWNLOADING
        }

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.cancel_download, R.id.delete_download -> removeDownload(download.request.uri)
                R.id.resume_download -> {
                    DownloadService.sendSetStopReason(
                        context,
                        MyDownloadService::class.java,
                        download.request.id,
                        Download.STOP_REASON_NONE,
                        true
                    )
                }
                R.id.pause_download -> {
                    DownloadService.sendSetStopReason(
                        context,
                        MyDownloadService::class.java,
                        download.request.id,
                        Download.STATE_STOPPED,
                        false
                    )
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    fun removeDownload(uri: Uri?) {
        val download = downloads[uri]
        download?.let {
            DownloadService.sendRemoveDownload(
                applicationContext,
                MyDownloadService::class.java,
                download.request.id,
                false
            )
        }
    }

    private fun loadDownloads() {
        try {
            downloadIndex.getDownloads().use { loadedDownloads ->
                while (loadedDownloads.moveToNext()) {
                    val download = loadedDownloads.download
                    downloads[download.request.uri] = download
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Failed to query downloads", e)
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getAllDownloadProgressFlow(): Flow<List<Download>> = callbackFlow {
        while (coroutineContext.isActive) {
            offer(downloads.values.toList())
            delay(1000)
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getCurrentProgressDownload(uri: Uri?): Flow<Float?> {
        var percent: Float? =
            downloadManager.currentDownloads.find { it.request.uri == uri }?.percentDownloaded
        return callbackFlow {
            while (percent != null) {
                percent =
                    downloadManager.currentDownloads.find { it.request.uri == uri }?.percentDownloaded
                offer(percent)
                withContext(Dispatchers.IO) {
                    delay(1000)
                }
            }
        }
    }


    private fun getDownloadHelper(mediaItem: MediaItem): DownloadHelper {
        return when (mediaItem.playbackProperties?.mimeType) {
            MimeTypes.APPLICATION_MPD, MimeTypes.APPLICATION_M3U8, MimeTypes.APPLICATION_SS -> {
                DownloadHelper.forMediaItem(
                    applicationContext,
                    mediaItem,
                    DefaultRenderersFactory(applicationContext),
                    httpDataSourceFactory
                )
            }
            else -> DownloadHelper.forMediaItem(applicationContext, mediaItem)
        }
    }

    private inner class DownloadManagerListener : DownloadManager.Listener {
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            downloads[download.request.uri] = download
            for (listener in listeners) {
                listener.onDownloadsChanged(download)
            }
            if (download.state == Download.STATE_COMPLETED) {
                // Add delta between estimation and reality to have a better availableBytesLeft
                availableBytesLeft += Util.fromUtf8Bytes(download.request.data)
                    .toLong() - download.bytesDownloaded
            }
        }

        override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
            downloads.remove(download.request.uri)
            for (listener in listeners) {
                listener.onDownloadsChanged(download)
            }

            // Add the estimated or downloaded bytes to the availableBytes
            availableBytesLeft += if (download.percentDownloaded == 100f) {
                download.bytesDownloaded
            } else {
                Util.fromUtf8Bytes(download.request.data).toLong()
            }
        }
    }


    // Can't use applicationContext because it'll result in a crash, instead
    // Use context of the activity calling for the AlertDialog
    private inner class StartDownloadDialogHelper(
        private val context: Context,
        private val downloadHelper: DownloadHelper,
        private val mediaItem: MediaItem,
        private val positiveCallback: (() -> Unit)? = null,
        private val dismissCallback: (() -> Unit)? = null,
        private val dailogCallbackListener: DailogCallbackListener
    ) : DownloadHelper.Callback {

       // private var trackSelectionDialog: AlertDialog? = null

        init {
            downloadHelper.prepare(this)
        }

        fun release() {
            downloadHelper.release()
            //trackSelectionDialog?.dismiss()
        }

     //commit--

        override fun onPrepared(helper: DownloadHelper) {
            if (helper.periodCount == 0) {
                Log.d(TAG, "No periods found. Downloading entire stream.")
                downloadHelper.release()
                return
            }

            setTrackDailogStatus(true)

            val deleteDialogView = LayoutInflater.from(context).inflate(R.layout.alert_download_dialog, null)
            val deleteDialog = AlertDialog.Builder(context).create()
            deleteDialog.setView(deleteDialogView)
            //  val dialogView: View = LayoutInflater.from(context).inflate(R.layout.alert_download_dialog, null, false)
            //    val dialogBuilder = AlertDialog.Builder(context)
//     dialogBuilder.setView(dialogView)
            //   val alertDialog = dialogBuilder.create()
            //   alertDialog.show()

            val done = deleteDialogView.findViewById<AppCompatTextView>(R.id.done)
            val hd_720 = deleteDialogView.findViewById<AppCompatTextView>(R.id.hd_quality)
            val sd_420 = deleteDialogView.findViewById<AppCompatTextView>(R.id.sd_quality)

            val formatDownloadable: MutableList<Format> = mutableListOf()
            var qualitySelected: DefaultTrackSelector.Parameters
            val mappedTrackInfo = downloadHelper.getMappedTrackInfo(0)

            for (i in 0 until mappedTrackInfo.rendererCount) {
                if (C.TRACK_TYPE_VIDEO == mappedTrackInfo.getRendererType(i)) {
                    val trackGroups: TrackGroupArray = mappedTrackInfo.getTrackGroups(i)
                    for (j in 0 until trackGroups.length) {
                        val trackGroup: TrackGroup = trackGroups[j]
                        for (k in 0 until trackGroup.length) {
                            formatDownloadable.add(trackGroup.getFormat(k))
                        }
                    }
                }
            }

            if (formatDownloadable.isEmpty()) {

                return
            }

            // We sort here because later we use formatDownloadable to select track
            formatDownloadable.sortBy { it.height }
            val mediaItemTag: MediaItemTag = mediaItem.playbackProperties?.tag as MediaItemTag
            val optionsDownload: List<String> = formatDownloadable.map {
                context.getString(
                    R.string.dialog_option, it.height,
                    (it.bitrate * mediaItemTag.duration).div(8000).formatFileSize()
                )
            }

            //Default quality download
            qualitySelected = DefaultTrackSelector(context).buildUponParameters()
                .setMinVideoSize(formatDownloadable[0].width, formatDownloadable[0].height)
                .setMinVideoBitrate(formatDownloadable[0].bitrate)
                .setMaxVideoSize(formatDownloadable[0].width, formatDownloadable[0].height)
                .setMaxVideoBitrate(formatDownloadable[0].bitrate)
                .build()


            hd_720?.text = qualitySelected.toString()

            hd_720?.setOnClickListener {
                for (item in formatDownloadable.indices) {
                    val format = formatDownloadable[item]
                    qualitySelected = DefaultTrackSelector(context).buildUponParameters()
                        .setMinVideoSize(format.width, format.height)
                        .setMinVideoBitrate(format.bitrate)
                        .setMaxVideoSize(format.width, format.height)
                        .setMaxVideoBitrate(format.bitrate)
                        .build()
                    when (formatDownloadable[item].height) {
                        720 -> {
                            //   hd_720.text = formatDownloadable[item].height.toString()
                            hd_720.text = qualitySelected.toString()
                        }
                        480 -> {
                            //   hd_720.text = formatDownloadable[item].height.toString()
                            hd_720.text = qualitySelected.toString()
                        }
                        360 -> {
                            //  hd_720.text = formatDownloadable[item].height.toString()
                            hd_720.text = qualitySelected.toString()
                        }
                        else -> {
                            // hd_720.text = formatDownloadable[item].height.toString()
                            hd_720.text = qualitySelected.toString()
                        }
                    }
                }
            }

            sd_420?.setOnClickListener {
                for (item in formatDownloadable.indices) {
                    val format = formatDownloadable[item]
                    qualitySelected = DefaultTrackSelector(context).buildUponParameters()
                        .setMinVideoSize(format.width, format.height)
                        .setMinVideoBitrate(format.bitrate)
                        .setMaxVideoSize(format.width, format.height)
                        .setMaxVideoBitrate(format.bitrate)
                        .build()
                    when (formatDownloadable[item].height) {
                        480 -> {
                            //  sd_420.text = formatDownloadable[item].height.toString()
                            sd_420.text = qualitySelected.toString()
                        }
                        360 -> {
                            // sd_420.text = formatDownloadable[item].height.toString()
                            sd_420.text = qualitySelected.toString()
                        }
                        else -> {
                            //  sd_420.text = formatDownloadable[item].height.toString()
                            sd_420.text = qualitySelected.toString()
                        }
                    }
                }
            }

            done?.setOnClickListener {
                helper.clearTrackSelections(0)
                helper.addTrackSelection(0, qualitySelected)
                val estimatedContentLength: Long =
                    (qualitySelected.maxVideoBitrate * mediaItemTag.duration).div(C.MILLIS_PER_SECOND)
                        .div(C.BITS_PER_BYTE)
                if (availableBytesLeft > estimatedContentLength) {
                    val downloadRequest: DownloadRequest = downloadHelper.getDownloadRequest(
                        (mediaItem.playbackProperties?.tag as MediaItemTag).title,
                        Util.getUtf8Bytes(estimatedContentLength.toString())
                    )
                    startDownload(downloadRequest)
                    availableBytesLeft -= estimatedContentLength
                    Log.e(TAG, "availableBytesLeft after calculation: $availableBytesLeft")
                } else {
                    Toast.makeText(
                        context,
                        "Not enough space to download this file",
                        Toast.LENGTH_LONG
                    ).show()
                }
                // dailogCallbackListener.trackDailogStatus(false)
                positiveCallback?.invoke()
            }


            /*dialogBuilder.setTitle("Select Download Format")
                .setSingleChoiceItems(optionsDownload.toTypedArray(), 0) { _, which ->
                    val format = formatDownloadable[which]
                    qualitySelected = DefaultTrackSelector(context).buildUponParameters()
                        .setMinVideoSize(format.width, format.height)
                        .setMinVideoBitrate(format.bitrate)
                        .setMaxVideoSize(format.width, format.height)
                        .setMaxVideoBitrate(format.bitrate)
                        .build()
                    Log.e(TAG, "format Selected= width: ${format.width}, height: ${format.height}")
                }.setPositiveButton("Download") { _, _ ->
                    helper.clearTrackSelections(0)
                    helper.addTrackSelection(0, qualitySelected)
                    val estimatedContentLength: Long =
                        (qualitySelected.maxVideoBitrate * mediaItemTag.duration).div(C.MILLIS_PER_SECOND)
                            .div(C.BITS_PER_BYTE)
                    if (availableBytesLeft > estimatedContentLength) {
                        val downloadRequest: DownloadRequest = downloadHelper.getDownloadRequest(
                            (mediaItem.playbackProperties?.tag as MediaItemTag).title,
                            Util.getUtf8Bytes(estimatedContentLength.toString())
                        )
                        startDownload(downloadRequest)
                        availableBytesLeft -= estimatedContentLength
                        Log.e(TAG, "availableBytesLeft after calculation: $availableBytesLeft")
                    } else {
                        Toast.makeText(
                            context,
                            "Not enough space to download this file",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    // dailogCallbackListener.trackDailogStatus(false)
                    positiveCallback?.invoke()
                }.setOnDismissListener {
                    trackSelectionDialog = null

                    downloadHelper.release()
                    dismissCallback?.invoke()
                    // dailogCallbackListener.trackDailogStatus(false)
                }.setNegativeButton("Cancel") { _, _ ->
                    trackSelectionDialog = null
                    downloadHelper.release()
                    dismissCallback?.invoke()
                    dailogCallbackListener.trackDailogStatus(false)
                }*/
            deleteDialog.show()

            //dailogCallbackListener.trackDailogStatus(true)

        }

        override fun onPrepareError(helper: DownloadHelper, e: IOException) {
            dailogCallbackListener.trackDailogStatus(false)
            Toast.makeText(applicationContext, R.string.download_start_error, Toast.LENGTH_LONG)
                .show()
            Log.e(
                TAG,
                if (e is LiveContentUnsupportedException) "Downloading live content unsupported" else "Failed to start download",
                e
            )
        }

        // Internal methods.
        private fun startDownload(downloadRequest: DownloadRequest = buildDownloadRequest()) {
            DownloadService.sendAddDownload(
                applicationContext,
                MyDownloadService::class.java,
                downloadRequest,
                true
            )

        }

        private fun buildDownloadRequest(): DownloadRequest {
            return downloadHelper.getDownloadRequest(
                (mediaItem.playbackProperties?.tag as MediaItemTag).title,
                Util.getUtf8Bytes(mediaItem.playbackProperties?.uri.toString())
            )
        }
    }
}
