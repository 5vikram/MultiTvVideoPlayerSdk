package com.multitv.ott.multitvvideoplayer.playerglide

import android.graphics.Bitmap
import android.service.autofill.FieldClassification.Match
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.multitv.ott.multitvvideoplayer.utils.VideoPlayerTracer
import java.nio.ByteBuffer
import java.security.MessageDigest


private const val MAX_COLUMNS = 10
private const val THUMBNAILS_EACH = 5000 // milliseconds

class GlideThumbnailTransformation(position: Long, maxLine: Int) : BitmapTransformation() {
    private var MAX_LINES = 6
    private val x: Int
    private val y: Int

    init {

        if (maxLine > 0) {
            val totalSecond = maxLine / MAX_COLUMNS / 5
            MAX_LINES = Math.round(totalSecond.toDouble()).toInt()
            MAX_LINES = MAX_LINES + 1
        } else {
            MAX_LINES = 6
        }

        VideoPlayerTracer.error("MAX LINE:::", "" + MAX_LINES)


        val square = position.toInt().div(THUMBNAILS_EACH)
        x = square % MAX_COLUMNS
        y = square / MAX_COLUMNS
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val width = toTransform.width / MAX_COLUMNS
        val height = toTransform.height / MAX_LINES

        return Bitmap.createBitmap(toTransform, x * width, 0, width, height)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        val data: ByteArray = ByteBuffer.allocate(8).putInt(x).putInt(y).array()
        messageDigest.update(data)
    }

    override fun hashCode(): Int {
        return (x.toString() + y.toString()).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is GlideThumbnailTransformation) return false
        return other.x == x && other.y == y
    }
}