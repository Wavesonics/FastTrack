package com.darkrockstudios.apps.fasttrack.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Share a snapshot of the fasting progress as an image plus a caption.
 *
 * [bitmap] is already opaque (the hero paints the brand gradient into its own
 * bounds before capture), so it can be written straight to a PNG. We compress
 * it directly rather than routing through a software Canvas — that avoids the
 * "software rendering doesn't support hardware bitmaps" pitfall entirely.
 */
suspend fun shareFastImage(
	context: Context,
	bitmap: Bitmap,
	caption: String,
) = withContext(Dispatchers.IO) {
	// cacheDir root is exposed via file_paths.xml (<cache-path name="shared_files" path="."/>)
	val file = File(context.cacheDir, "fast_share.png")
	FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

	val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

	val send = Intent(Intent.ACTION_SEND).apply {
		type = "image/png"
		putExtra(Intent.EXTRA_STREAM, uri)
		putExtra(Intent.EXTRA_TEXT, caption)
		addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		// clipData ensures the read grant reaches the chosen target reliably
		clipData = ClipData.newUri(context.contentResolver, "Fast Track", uri)
	}

	withContext(Dispatchers.Main) {
		context.startActivity(Intent.createChooser(send, null))
	}
}
