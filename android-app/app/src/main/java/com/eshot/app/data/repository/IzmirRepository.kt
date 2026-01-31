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
            if (!stopsCacheFile.exists() || forceRefresh) {
                downloadFile("https://openfiles.izmir.bel.tr/211488/docs/eshot-otobus-duraklari.csv", stopsCacheFile)
            }
            cachedStops = csvParser.parseStops(FileInputStream(stopsCacheFile))
            cachedStops
        }
    }

    suspend fun loadLines(forceRefresh: Boolean = false): List<Line> {
         if (cachedLines.isNotEmpty() && !forceRefresh) return cachedLines
         
         return withContext(Dispatchers.IO) {
             if (!linesCacheFile.exists() || forceRefresh) {
                 downloadFile("https://openfiles.izmir.bel.tr/211488/docs/eshot-otobus-hatlari.csv", linesCacheFile)
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

    private fun downloadFile(url: String, dest: File) {
        try {
            URL(url).openStream().use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
