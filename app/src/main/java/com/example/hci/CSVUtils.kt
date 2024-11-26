package com.example.hci

import android.content.Context
import android.os.Environment
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader

object CSVUtils {
    fun readCSV(context: Context, fileName: String): List<Map<String, String>> {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (!file.exists()) {
            throw FileNotFoundException("File not found in Downloads directory: $fileName")
        }

        val reader = BufferedReader(FileReader(file))
        val result = mutableListOf<Map<String, String>>()

        val headers = reader.readLine()?.split(",") ?: return emptyList() // 헤더 읽기
        reader.forEachLine { line ->
            val values = line.split(",")
            val row = headers.zip(values).toMap()
            result.add(row)
        }
        reader.close()
        return result
    }
}
