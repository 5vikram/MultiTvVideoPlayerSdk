package com.multitv.ott.multitvvideoplayer.playerglide

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.ByteBuffer
import java.security.MessageDigest

private const val MAX_LINES = 26
private const val MAX_COLUMNS = 10
private const val THUMBNAILS_EACH = 1000 // milliseconds

class GlideThumbnailTransformation(position: Long,thumbnailEach:Int) : BitmapTransformation() {

    private val x: Int
    private val y: Int

    init {
        val square = position.toInt().div(thumbnailEach)
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
        if(other !is GlideThumbnailTransformation) return false
        return other.x == x && other.y == y
    }
}