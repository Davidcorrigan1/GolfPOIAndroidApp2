package org.wit.golfpoi.helpers

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import org.wit.golfpoi.R

fun showImagePicker(intentLauncher : ActivityResultLauncher<Intent>) {
    var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
    chooseFile.type = "image/*"
    chooseFile = Intent.createChooser(chooseFile, R.string.select_golfPOI_image.toString())
    intentLauncher.launch(chooseFile)
}

fun readImageFromPath(context: Context, path: String) : Bitmap? {
    var bitmap : Bitmap? = null
    val uri = Uri.parse(path)

    if(uri != null) {
        try{
            val parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r")
            val fileDescriptor = parcelFileDescriptor?.getFileDescriptor()
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor?.close()
        } catch (e: Exception){

        }
    }
    return bitmap
}