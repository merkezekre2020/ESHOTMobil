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
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty()) {
                    line = reader.readLine()
                    continue
                }
                
                val parts = line.split(delimiter).map { it.trim() }
                // Ensure we have enough parts for the critical indices
                val maxRequiredIndex = maxOf(idIndex, latIndex, lonIndex)
                
                if (parts.size > maxRequiredIndex) {
                   try {
                       val lat = parts.getOrNull(latIndex)?.replace(",", ".")?.toDoubleOrNull()
                       val lon = parts.getOrNull(lonIndex)?.replace(",", ".")?.toDoubleOrNull()
                       
                       if (lat != null && lon != null) {
                           stops.add(Stop(
                               id = parts.getOrNull(idIndex) ?: "Unknown",
                               name = if (nameIndex != -1) parts.getOrNull(nameIndex) ?: "Unknown Stop" else "Unknown Stop",
                               latitude = lat,
                               longitude = lon,
                               lineIds = if (linesIndex != -1) parts.getOrNull(linesIndex) ?: "" else ""
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
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty()) {
                    line = reader.readLine()
                    continue
                }
                
                val parts = line.split(delimiter).map { it.trim() }
                 if (parts.size > idIndex) {
                    lines.add(Line(
                        id = parts.getOrNull(idIndex) ?: "",
                        name = if (nameIndex != -1) parts.getOrNull(nameIndex) ?: "" else "",
                        description = if (descIndex != -1) parts.getOrNull(descIndex) ?: "" else "",
                        startStop = if (startIndex != -1) parts.getOrNull(startIndex) ?: "" else "",
                        endStop = if (endIndex != -1) parts.getOrNull(endIndex) ?: "" else ""
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
        try {
            val decoder = charset.newDecoder()
            decoder.decode(java.nio.ByteBuffer.wrap(buffer, 0, read))
        } catch (e: Exception) {
             // Fallback to Turkish Windows-1254 if UTF-8 fails
             try {
                 charset = Charset.forName("windows-1254")
             } catch (unsupported: Exception) {
                 // If 1254 is not supported on this device, fallback to ISO-8859-9 or default
                 charset = Charset.defaultCharset()
             }
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
