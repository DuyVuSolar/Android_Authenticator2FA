package com.kuemiin.reversevoice.utils

import java.util.regex.Pattern

object FileNameValidator {

    private const val MAX_FILE_NAME_LENGTH = 255

    // Regular expression to match special characters (excluding alphanumeric, underscore, and hyphen)
    private val SPECIAL_CHARACTERS_REGEX = Pattern.compile("[^a-zA-Z0-9_-]")

    fun isValidFileName(fileName: String): Boolean {
        // Check for null or empty
        if (fileName.isBlank()) {
            return false
        }

        // Check for reserved names
        if (fileName == "." || fileName == "..") {
            return false
        }

        // Check for special characters
        if (SPECIAL_CHARACTERS_REGEX.matcher(fileName).find()) {
            return false
        }

        // Check for length
        if (fileName.length > MAX_FILE_NAME_LENGTH) {
            return false
        }

        return true
    }

    fun sanitizeFileName(fileName: String): String {
        var sanitizedFileName = fileName
        // Remove special characters
        sanitizedFileName = SPECIAL_CHARACTERS_REGEX.matcher(sanitizedFileName).replaceAll("_")
        // Truncate if too long
        if (sanitizedFileName.length > MAX_FILE_NAME_LENGTH) {
            sanitizedFileName = sanitizedFileName.substring(0, MAX_FILE_NAME_LENGTH)
        }
        return sanitizedFileName
    }
}