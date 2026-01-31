package com.eshot.app.data.repository

import android.content.Context
import com.eshot.app.data.api.EshotService
import com.eshot.app.data.csv.CsvParser
import com.eshot.app.data.model.BusDto
import com.eshot.app.data.model.Line
import com.eshot.app.data.model.Stop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class IzmirRepository(private val context: Context) {
    
    private val csvParser = CsvParser()
    private val stopsCacheFile = File(context.cacheDir, "stops.csv")
    private val linesCacheFile = File(context.cacheDir, "lines.csv")

    // In-memory cache
    private var cachedStops: List<Stop> = emptyList()
    private var cachedLines: List<Line> = emptyList()

    suspend fun loadStops(forceRefresh: Boolean = false): List<Stop> {
        if (cachedStops.isNotEmpty() && !forceRefresh) return cachedStops
        
        return withContext(Dispatchers.IO) {
            val fileExists = stopsCacheFile.exists()
            if (!fileExists || forceRefresh) {
                try {
                    downloadFile("https://openfiles.izmir.bel.tr/211488/docs/eshot-otobus-duraklari.csv", stopsCacheFile)
                } catch (e: Exception) {
                    // If we don't have the file and download fails, we must throw
                    if (!fileExists) throw e 
                    // If we have the file but refresh failed, ignore error and use cache
                    e.printStackTrace() 
                }
            }
            cachedStops = csvParser.parseStops(FileInputStream(stopsCacheFile))
            cachedStops
        }
    }

    suspend fun loadLines(forceRefresh: Boolean = false): List<Line> {
         if (cachedLines.isNotEmpty() && !forceRefresh) return cachedLines
         
         return withContext(Dispatchers.IO) {
             val fileExists = linesCacheFile.exists()
             if (!fileExists || forceRefresh) {
                 try {
                     downloadFile("https://openfiles.izmir.bel.tr/211488/docs/eshot-otobus-hatlari.csv", linesCacheFile)
                 } catch (e: Exception) {
                     if (!fileExists) throw e
                     e.printStackTrace()
                 }
             }
             cachedLines = csvParser.parseLines(FileInputStream(linesCacheFile))
             cachedLines
         }
    }

    suspend fun getApproachingBuses(stopId: String): List<BusDto> {
        return withContext(Dispatchers.IO) {
            EshotService.getApproachingBuses(stopId)
        }
    }

    private fun downloadFile(urlStr: String, dest: File) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            // Set User-Agent to avoid 403 Forbidden from some strict servers
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android 10; Mobile; rv:88.0) Gecko/88.0 Firefox/88.0")
            
            connection.connect()
            
            if (connection.responseCode in 200..299) {
                connection.inputStream.use { input ->
                    FileOutputStream(dest).use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                throw IOException("Server returned ${connection.responseCode} for $urlStr")
            }
        } catch (e: Exception) {
            throw IOException("Download failed: ${e.message}", e)
        } finally {
            connection?.disconnect()
        }
    }
}