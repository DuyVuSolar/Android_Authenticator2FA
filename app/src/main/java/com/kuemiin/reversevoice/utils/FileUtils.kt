package com.kuemiin.reversevoice.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.StatFs
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.kuemiin.reversevoice.BaseApplication
import com.kuemiin.reversevoice.utils.extension.getApplication
import com.kuemiin.reversevoice.utils.extension.isFileExcel
import com.kuemiin.reversevoice.utils.extension.isFilePDF
import com.kuemiin.reversevoice.utils.extension.isFileTXT
import com.kuemiin.reversevoice.utils.extension.isFileWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.kuemiin.reversevoice.BuildConfig
import com.kuemiin.reversevoice.model.AppAudio
import com.kuemiin.reversevoice.model.DateGroupMyAudio
import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap

object FileUtils {

    const val folderConvert = "PDFConvert"
    const val folderDefault = "PdfViewer"
    const val folderSignature = "Signature"
    const val folderHTML = "HtmlData"
    const val folderTemp = "TempData"
    const val folderWidgetCorner = "WidgetCorner"
    var FOLDER_NAME: String = "ReverseVoice"

    const val EXTENSION_RECORD = "wav"
    const val REVERSE_AUDIO = "reverse_audio"


    fun checkAvailableStoreToRecordDownload() : Boolean{
        return getAvailableInternalMemorySizeLong() > 1024 * 1024 * 200//200MB
    }

    private fun getAvailableInternalMemorySizeLong(): Long {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return (availableBlocks * blockSize)
    }

    fun getRootFolder(context: Context): String {
        val path: String = getInternalFolder(context)
        return path
    }

    private fun getInternalFolder(context: Context): String {
        return context.filesDir.absolutePath + File.separator + FOLDER_NAME
    }

    fun getFolderTemp(): File {
        val folder = File(BaseApplication.getAppInstance().filesDir, folderTemp)

        if(!folder.exists()){
            folder.mkdirs()
        }
        return folder
    }

    fun getFolderTempReverse(): File {
        val folder = File(BaseApplication.getAppInstance().filesDir, REVERSE_AUDIO)

        if(!folder.exists()){
            folder.mkdirs()
        }
        return folder
    }

    fun clearCacheTemp(){
        getFolderTemp().listFiles()?.forEach {
            it.delete()
        }
    }

    fun saveBitmapToTempFile(context: Context, bitmap: Bitmap, filename: String, quality : Int = 80, compress : CompressFormat = CompressFormat.JPEG, callback : () -> Unit = {}): String {//filename ex : "${System.currentTimeMillis()}.png"
        val file = File(getFolderTemp(), filename)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            bitmap.compress(compress, quality, fos)
            fos.flush()
            bitmap.recycle()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            callback.invoke()
        }
        return file.absolutePath
    }




    private fun InputStream.toFile(path: String) {
        use { input ->
            File(path).outputStream().use { input.copyTo(it) }
        }
    }

    fun getUriFromFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    }

    fun sendFileToEmail(context: Context, file : File){
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = when {
                file.absolutePath.isFilePDF() -> "application/pdf"
                file.absolutePath.isFileWord() -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                file.absolutePath.isFileExcel() -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                file.absolutePath.isFilePDF() -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                file.absolutePath.isFileTXT() -> "text/plain"
                else -> "*/*"
            }
        }
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("recipient@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")

        emailIntent.putExtra(Intent.EXTRA_STREAM, getUriFromFile(context, file))
        context.startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"))
    }

    fun String.getTypeFromPathFile() : String{
        return when {
            this.isFilePDF() -> "application/pdf"
            this.isFileWord() -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            this.isFileExcel() -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            this.isFilePDF() -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            this.isFileTXT() -> "text/plain"
            else -> "*/*"
        }
    }

    fun shareFileDoc(context : Context, file : File){
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = file.absolutePath.getTypeFromPathFile()
            putExtra(Intent.EXTRA_STREAM, getUriFromFile(context, file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share File"))
    }


    fun shareImage(context : Context, file : File){
        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/*"
         share.putExtra(Intent.EXTRA_STREAM, getUriFromFile(context, file))
        share.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.startActivity(Intent.createChooser(share, "Share image"))
    }

    fun shareVideo(context : Context, file : File){
        val share = Intent(Intent.ACTION_SEND)
        share.type = "video/*"
         share.putExtra(Intent.EXTRA_STREAM, getUriFromFile(context, file))
        share.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.startActivity(Intent.createChooser(share, "Share video"))
    }

    fun shareMultiVideo(context: Context, files: List<String>){
        val share = Intent(Intent.ACTION_SEND)
        share.type = "video/*"
        share.setAction(Intent.ACTION_SEND_MULTIPLE)
        share.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val listUri = arrayListOf<Uri>()
        for (path in files) {
            val uri = getUriFromFile(context, File(path))
            listUri.add(uri)
        }
        share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, listUri)
        context.startActivity(Intent.createChooser(share, "Share videos"))
    }

    fun shareAudio(context : Context, file : File){
        val share = Intent(Intent.ACTION_SEND)
        share.type = "audio/*"
         share.putExtra(Intent.EXTRA_STREAM, getUriFromFile(context, file))
        share.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.startActivity(Intent.createChooser(share, "Share audio"))
    }

    fun shareAudio(context : Context, uri : Uri){
        val share = Intent(Intent.ACTION_SEND)
        share.type = "audio/mp3"
        share.putExtra(Intent.EXTRA_STREAM, uri)
        share.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.startActivity(Intent.createChooser(share, "Share audio"))
    }


    fun convertsTime(diff: Long, minHourFormat: Boolean = false, isCanBeZero: Boolean = true): String {
        val diffMilliseconds = (diff % 1000).toInt()
        var diffSeconds = (diff / 1000 % 60).toInt()
        val diffMinutes = (diff / (60 * 1000) % 60).toInt()
        val diffHours = (diff / (60 * 60 * 1000) % 24).toInt()
        val diffDays = (diff / (24 * 60 * 60 * 1000)).toInt()
        var str = ""
        if (!isCanBeZero && diffDays == 0 && diffHours == 0 && diffMinutes == 0 && diffSeconds == 0 && diff > 0) {
            diffSeconds = 1;
        }
        str = if (diffDays > 0) Integer.toString(diffDays) + "d" + " " +
                formatTime(diffHours) + ":" + formatTime(
            diffMinutes
        ) + ":" +
                formatTime(diffSeconds) else if (diffHours > 0 || minHourFormat) formatTime(
            diffHours
        ) + ":" + formatTime(
            diffMinutes
        ) + ":" +
                formatTime(diffSeconds) else formatTime(
            diffMinutes
        ) + ":" + formatTime(
            diffSeconds
        )
        return str
    }

    private fun formatTime(tt: Int): String? {
        return String.format("%02d", tt)
    }


    private fun getDirPath(context : Context): String {
        var dirPath = ""
        var imageDir: File? = null
        val extStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (extStorageDir?.canWrite() == true) {
            imageDir = File(extStorageDir.path + "/$folderDefault")
        }
        if (imageDir != null) {
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }
            if (imageDir.canWrite()) {
                dirPath = imageDir.path
            }
        }
        return dirPath
    }

    fun createNewUri(context: Context, format: CompressFormat?): Uri? {
        val currentTimeMillis = System.currentTimeMillis()
        val today = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        val title = dateFormat.format(today)
        val dirPath: String = getDirPath(context)
        val fileName = "scv$title.png"
        val path = "$dirPath/$fileName"
        val file = File(path)
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, title)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(
            MediaStore.Images.Media.MIME_TYPE,
            "image/" + "png"
        )
        values.put(MediaStore.Images.Media.DATA, path)
        val time = currentTimeMillis / 1000
        values.put(MediaStore.MediaColumns.DATE_ADDED, time)
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time)
        if (file.exists()) {
            values.put(MediaStore.Images.Media.SIZE, file.length())
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        return uri
    }

    fun deleteMediaFile(context: Context, file: File, callback: (Boolean) -> Unit) {
        if (isAndroidQ()) {
            val contentResolver: ContentResolver = context.contentResolver
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(file.name)
            val collection: Uri = MediaStore.Files.getContentUri("external")

            try{
                val cursor = contentResolver.query(collection, null, selection, selectionArgs, null) ?: return
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    val id = cursor.getLong(idColumn)
                    val deleteUri = Uri.withAppendedPath(collection, id.toString())
                    val rowsDeleted = contentResolver.delete(deleteUri, null, null)
                    if (rowsDeleted > 0) {
                        loge("File deleted successfully")
//                    file.delete()
                        callback.invoke(true)
                    } else {
                        loge("Failed to delete file")
                        callback.invoke(false)
                    }
                } else {
                    loge("File not found in MediaStore")
                    callback.invoke(false)
                }
                cursor.close()
            }catch (e : Exception){}

        }
    }

    fun renameMediaFile(context: Context, oldFile: File, newFile: String, callback: (Boolean) -> Unit) {
        runBlocking {
            if (oldFile.exists()) {
                if (oldFile.renameTo(File(newFile))) {
                    try {
                        saveFileToSharedStorage(context, newFile)
                        File(newFile).setLastModified(System.currentTimeMillis())
                    } catch (e: Exception) {

                    }
                    callback.invoke(true)
                } else {
                    callback.invoke(false)
                }
            } else {
                callback.invoke(false)
            }
        }
    }

    fun saveFileToSharedStorage(context: Context, filePath: String) {
        if(isAndroidQ()){
            val file = File(filePath)
            val resolver: ContentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DATA, file.absolutePath)
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Or your file's MIME type
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
            }

            try {
                val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    try {
                        Log.e("SaveFile", "Error opening output stream")
//                    val outputStream: OutputStream? = resolver.openOutputStream(uri)
//                    if (outputStream != null) {
//                        FileInputStream(filePath).copyTo(outputStream)
//                        outputStream.close()
//                        Log.d("SaveFile", "File saved to shared storage: $uri")
//                    } else {
//                    }
                    } catch (e: IOException) {
                        Log.e("SaveFile", "Error saving file: ${e.message}")
                    }
                } else {
                    Log.e("SaveFile", "Error creating MediaStore entry")
                }
            }catch (e : Exception){}

        }else{
            MediaScannerConnection.scanFile(
                context,
                arrayOf(filePath),
                null
            ) { path, uri ->
                // This callback is called when the scan is complete
                if (uri != null) {
                    Log.d("MediaScanner", "Scanned file: $path, URI: $uri")
                } else {
                    Log.e("MediaScanner", "Failed to scan file: $path")
                }
            }
        }
    }

    fun deleteFileInternalAudio(audio: AppAudio){
        try {
            File(audio.path).delete()
        } catch (e: Exception) {
            throw e
        }
        try {
            File(audio.getPathReverse()).delete()
        } catch (e: Exception) {
            throw e
        }

    }

    private fun uriImageToFile(context: Context, uri: Uri): String? = runBlocking{
        val file = File(context.filesDir, "${System.currentTimeMillis()}.png")
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream: OutputStream = withContext(Dispatchers.IO) { FileOutputStream(file) }
            inputStream?.use { input ->
                outputStream.use { output ->
                    copyToWithAwait(input, output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@runBlocking null
        }
        return@runBlocking file.absolutePath
    }

    private suspend fun copyToWithAwait(input: InputStream, output: OutputStream) {
        withContext(Dispatchers.IO) {
            input.copyTo(output)
        }
    }

    private fun getPath(context: Context, uri: Uri): String? {
        var uri = uri
        val needToCheckUri = true
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.applicationContext, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                uri = ContentUris.withAppendedId("content://downloads/public_downloads".toUri(), id.toLong())
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("image" == type) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            var cursor: Cursor? = null
            try {
                cursor =
                    context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index)
                }
            } catch (e: Exception) {
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }


    fun reverseAudioBytesManual(
        inputFile: String,
        outputFile: String,
        headerSize: Int = 44, // e.g., 44
        sampleSize: Int = 2, // e.g., 2 for 16-bit
    ) {
        val inFile = File(inputFile)
        val outFile = File(outputFile)

        try {
            val allBytes = inFile.readBytes()
            if (allBytes.size < headerSize) {
                println("Error: File size too small for header.")
                return
            }

            // 1. Separate Header and Payload
            val header = allBytes.copyOfRange(0, headerSize)
            val payload = allBytes.copyOfRange(headerSize, allBytes.size)
            val payloadSize = payload.size

            if (payloadSize % sampleSize != 0) {
                println("Error: Payload size is not a multiple of the sample size. Data is corrupt or format is complex.")
                return
            }

            val numSamples = payloadSize / sampleSize
            val reversedPayload = ByteArray(payloadSize)

            // 2. Reverse Samples (in chunks of sampleSize)
            // We reverse the order of the SAMPLES, not the raw bytes
            for (i in 0 until numSamples) {
                // Calculate indices for the current sample chunk
                val originalStart = i * sampleSize
                val reversedStart = (numSamples - 1 - i) * sampleSize

                // Copy the sample chunk (e.g., 2 bytes for 16-bit)
                // from the original position to the reversed position
                System.arraycopy(
                    payload, originalStart,
                    reversedPayload, reversedStart,
                    sampleSize
                )
            }

            // 3. Combine Header and Reversed Payload, then Write
            val outputStream = FileOutputStream(outFile)
            outputStream.write(header) // Write the original header
            outputStream.write(reversedPayload) // Write the reversed audio data
            outputStream.close()

            println("Successfully reversed audio data and saved to: $outputFile")

        } catch (e: Exception) {
            e.printStackTrace()
            println("An error occurred: ${e.message}")
        }
    }


    fun getListDateGroupAudio(pathFolder: String): ArrayList<AppAudio> {
        val list = ArrayList<AppAudio>()
        val file = File(pathFolder)
        val mapGroup = HashMap<String, ArrayList<AppAudio>>()
        val listGroup = ArrayList<DateGroupMyAudio>()
        val formatDate = "dd/MM/yyyy"
        val toDayString = Date().toFormat(formatDate)
        val yesterdayString = Calendar.getInstance().let {
            it.timeInMillis -= 86400000
            it.timeInMillis
        }.toStringDateFormat(formatDate)

        if (file.exists()) {
            val listFile = file.listFiles()
            if (listFile != null) {
                listFile.sortByDescending { it.lastModified() }
                for (childFile in listFile) {
                    val path = childFile.absolutePath
                    if (!childFile.extension.equals("raw", true)) {
                        val duration = getAudioDuration(path)
                        if (duration > 0) {
                            val date = childFile.lastModified()
                            val appAudio = AppAudio(childFile.lastModified(), path, duration, date)
                            var sDate = date.toStringDateFormat(formatDate)
                            if (sDate.equals(toDayString, true)) {
                                sDate = Constant.TODAY
                            } else if (sDate.equals(yesterdayString, true)) {
                                sDate = Constant.YESTERDAY
                            }
                            if (mapGroup[sDate] == null) {
                                val listAppAudio = ArrayList<AppAudio>()
                                mapGroup[sDate] = listAppAudio
                                listGroup.add(DateGroupMyAudio(sDate, date, listAppAudio))
                            }
                            mapGroup[sDate]!!.add(appAudio)
                            //loge(childFile.name)
                        }
                    }
                }
            }
        }
        listGroup.sortByDescending { it.timeStamp }
        val listAppAudio = arrayListOf<AppAudio>()
        listGroup.forEach {
            it.list.forEach {
                listAppAudio.add(it)
            }
        }
        return listAppAudio
    }

    fun getAudioDuration(path: String): Long {
        val mmr = MediaMetadataRetriever()
        var duration = 0L
        try {
            mmr.setDataSource(path)
            val durationStr =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = java.lang.Long.parseLong(durationStr)
        } catch (e: Exception) {

        }
        mmr.release()
        return duration
    }

    fun shareMultiplePath(listPath: List<String>, activity: Activity?) {
        val uris = ArrayList<Uri>()
        for (path in listPath) {
            val file = File(path)
            val uri =
                FileProvider.getUriForFile(
                    getApplication(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
            uris.add(uri)
        }
        val intent = Intent()
        intent.action = Intent.ACTION_SEND_MULTIPLE
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share audio files.")
        intent.type = "audio/*"
        intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        activity?.startActivity(intent)
    }


}