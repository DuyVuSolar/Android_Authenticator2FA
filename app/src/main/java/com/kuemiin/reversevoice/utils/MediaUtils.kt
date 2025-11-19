package com.kuemiin.reversevoice.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.kuemiin.reversevoice.BaseApplication
import com.kuemiin.reversevoice.model.Album
import com.kuemiin.reversevoice.model.Image
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

object MediaUtils {
    private var contentResolver = BaseApplication.getAppInstance().contentResolver

//     fun getAllImages(): ArrayList<Image> {
//        val imageList = arrayListOf<Image>()
//        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME)
//        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
//
//        cursor?.use {
//            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
//            while (it.moveToNext()) {
//                val imagePath = it.getString(columnIndex)
//                val name = it.getString(nameIndex)
//                val image = Image(Uri.EMPTY, imagePath,Uri.EMPTY, name)
//                imageList.add(image)
//            }
//        }
//        return imageList
//    }

//     fun getAllFolders(): ArrayList<Album> {
//        val folderSet = arrayListOf<Album>()
//        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        val projection = arrayOf( MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME)
//        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
//
//        cursor?.use {
//            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
//            while (it.moveToNext()) {
//                val imagePath = it.getString(columnIndex)
//                val name = it.getString(nameIndex)
//                val folderPath = File(imagePath).parent
//                if (folderPath != null) {
//                    val album = Album(0, imagePath,Uri.EMPTY, name)
//                    folderSet.add(album)
//                }
//            }
//        }
//        return folderSet
//    }

    fun getImagesFromFolder(folderPath: String): List<String> {
        val imageList = mutableListOf<String>()
        val folder = File(folderPath)

        if (folder.exists() && folder.isDirectory) {
            val files = folder.listFiles()
            if (files != null) {
                for (file in files) {
                    val filePath = file.absolutePath
                    if (filePath.endsWith(".jpg", true) || filePath.endsWith(".jpeg", true) ||
                        filePath.endsWith(".png", true) || filePath.endsWith(".gif", true)) {
                        imageList.add(filePath)
                    }
                }
            }
        }
        return imageList
    }

    fun saveBitmapToExternalStorage(bitmap: Bitmap, context: Context) : Boolean{
        val filename = "Image_${UUID.randomUUID()}.jpg"
        var fos: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val resolver = context.contentResolver
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val imageFile = File(imagesDir, filename)
                fos = FileOutputStream(imageFile)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
//                bitmap.recycle()
            }
            return true
        }catch (e : Exception){
            return false
        }finally {
            fos?.close()
        }
    }

    fun getAllFoldersAndImages(): MutableMap<Album, List<Image>> {
        val folderImagesMap = mutableMapOf<Album, List<Image>>()
        val allAlbum = Album.AllAlbum
        folderImagesMap[allAlbum] = listOf()
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, sortOrder)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            while (it.moveToNext()) {
                val imagePath = it.getString(columnIndex)
                val folderPath = File(imagePath).parent

                val name = it.getString(nameIndex)
                val image = Image(0, name, Uri.EMPTY, imagePath)

                if (folderPath != null) {
                    val album = Album(0, folderPath.split("/").last(), Uri.EMPTY, folderPath)
                    if (folderImagesMap.containsKey(album)) {
                        folderImagesMap[album] = folderImagesMap[album]!! + image
                    } else {
                        folderImagesMap[album] = listOf(image)
                    }
                    folderImagesMap[allAlbum] = folderImagesMap[allAlbum]!! + image
                }
            }
        }

        return folderImagesMap
    }

    fun isPdfLocked(filePath: String): Boolean {
        return try {
//            if(!filePath.isFilePDF() || File(filePath).length() == 0L) return false
//            if(listLocked.contains(filePath)){
//                return true
//            }
//            if(listUnLocked.contains(filePath)){
//                return false
//            }
//            val mReader = PdfReader(filePath)
//            val isEncrypted = mReader.isEncrypted
//            if(isEncrypted && !listLocked.contains(filePath)) {
//                listLocked.add(filePath)
//            }
//            else if(!isEncrypted&& !listUnLocked.contains(filePath)) listUnLocked.add(filePath)
            return true
        } catch (e: Exception) {
            // If an exception occurs, it might be due to encryption
//            if(!listLocked.contains(filePath) && e.toString().contains("password")) {
//                listLocked.add(filePath)
//            }
            true
        }
    }

    fun getFileExtension(file: File): String {
        return file.name.substringAfterLast('.', "")
    }

    fun copyFileFromAssetsToInternalStorage(
        context: Context,
        assetFileName: String,
        outputFileName: String
    ) {
        val outputFile = File(context.filesDir, outputFileName) // Get a file path in app's internal files directory
        if(outputFile.exists() && outputFile.length() > 0) {
            outputFile.delete()
        }

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            // Open the asset
            inputStream = context.assets.open(assetFileName)

            // Create the output file stream
            outputStream = FileOutputStream(outputFile)

            // Copy the contents
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            Log.d("CopyFile", "File copied successfully to: ${outputFile.absolutePath}")
        } catch (e: IOException) {
            Log.e("CopyFile", "Error copying file from assets: ${e.message}")
            e.printStackTrace()
            // Optionally delete the partially created file if an error occurs
            if (outputFile.exists()) {
                outputFile.delete()
            }
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


}