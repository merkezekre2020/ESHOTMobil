package com.eshot.app.data.csv

import android.util.Log
import com.eshot.app.data.model.Line
import com.eshot.app.data.model.Stop
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

class CsvParser {

    fun parseStops(inputStream: InputStream): List<Stop> {
        val (reader, delimiter) = createReaderAndDetectDelimiter(inputStream)
        val stops = mutableListOf<Stop>()
        
        try {
            val headers = reader.readLine()?.split(delimiter)?.map { it.trim().uppercase() } ?: return emptyList()
            // Map header indices
            val idIndex = headers.indexOfFirst { it.contains("ID") }
            val nameIndex = headers.indexOfFirst { it.contains("ADI") || it.contains("NAME") }
            val latIndex = headers.indexOfFirst { it.contains("ENLEM") || it.contains("LAT") }
            val lonIndex = headers.indexOfFirst { it.contains("BOYLAM") || it.contains("LON") }
            val linesIndex = headers.indexOfFirst { it.contains("HATLAR") }

            if (idIndex == -1 || latIndex == -1 || lonIndex == -1) {
                Log.e("CsvParser", "Missing critical columns in Stops CSV. Headers: $headers")
                return emptyList()
            }

            var line = reader.readLine()
            while (line != null) {
                val parts = line.split(delimiter).map { it.trim() }
                if (parts.size >= headers.size) { // Lenient check, need enough parts for required fields
                   try {
                       val lat = parts.getOrNull(latIndex)?.replace(",", ".")?.toDoubleOrNull()
                       val lon = parts.getOrNull(lonIndex)?.replace(",", ".")?.toDoubleOrNull()
                       
                       if (lat != null && lon != null) {
                           stops.add(Stop(
                               id = parts.getOrNull(idIndex) ?: "Unknown",
                               name = parts.getOrNull(nameIndex) ?: "Unknown Stop",
                               latitude = lat,
                               longitude = lon,
                               lineIds = parts.getOrNull(linesIndex) ?: ""
                           ))
                       }
                   } catch (e: Exception) {
                       // Skip malformed row
                   }
                }
                line = reader.readLine()
            }
        } catch (e: Exception) {
            Log.e("CsvParser", "Error parsing stops", e)
        } finally {
            reader.close()
        }
        return stops
    }

    fun parseLines(inputStream: InputStream): List<Line> {
        val (reader, delimiter) = createReaderAndDetectDelimiter(inputStream)
        val lines = mutableListOf<Line>()
        
        try {
            val headers = reader.readLine()?.split(delimiter)?.map { it.trim().uppercase() } ?: return emptyList()
            
            val idIndex = headers.indexOfFirst { it == "HAT_NO" || it.contains("ID") }
            val nameIndex = headers.indexOfFirst { it == "HAT_ADI" || it.contains("NAME") }
            val descIndex = headers.indexOfFirst { it.contains("GUZERGAH") }
            val startIndex = headers.indexOfFirst { it.contains("BASLANGIC") }
            val endIndex = headers.indexOfFirst { it.contains("BITIS") }

            if (idIndex == -1) return emptyList()

            var line = reader.readLine()
            while (line != null) {
                val parts = line.split(delimiter).map { it.trim() }
                 if (parts.isNotEmpty()) {
                    lines.add(Line(
                        id = parts.getOrNull(idIndex) ?: "",
                        name = parts.getOrNull(nameIndex) ?: "",
                        description = parts.getOrNull(descIndex) ?: "",
                        startStop = parts.getOrNull(startIndex) ?: "",
                        endStop = parts.getOrNull(endIndex) ?: ""
                    ))
                 }
                line = reader.readLine()
            }
        } catch (e: Exception) {
             Log.e("CsvParser", "Error parsing lines", e)
        } finally {
            reader.close()
        }
        return lines
    }

    private fun createReaderAndDetectDelimiter(inputStream: InputStream): Pair<BufferedReader, Char> {
        val bufferedIn = inputStream.buffered()
        bufferedIn.mark(4096)
        
        // Peek
        val buffer = ByteArray(2048)
        val read = bufferedIn.read(buffer)
        bufferedIn.reset()
        
        // Detect Encoding
        var charset = Charset.forName("UTF-8")
        val previewString = String(buffer, 0, read, charset)
        
        if (previewString.contains("")) {
             charset = Charset.forName("windows-1254")
        }
        
        // Detect Delimiter from preview
        val text = String(buffer, 0, read, charset)
        val firstLine = text.lines().firstOrNull() ?: ""
        
        val semicolons = firstLine.count { it == ';' }
        val commas = firstLine.count { it == ',' }
        val tabs = firstLine.count { it == '\t' }
        
        val delimiter = when {
            semicolons >= commas && semicolons >= tabs -> ';'
            tabs > commas -> '\t'
            else -> ','
        }
        
        return Pair(BufferedReader(InputStreamReader(bufferedIn, charset)), delimiter)
    }
}
