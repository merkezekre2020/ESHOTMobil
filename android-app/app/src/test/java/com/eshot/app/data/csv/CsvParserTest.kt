package com.eshot.app.data.csv

import com.eshot.app.data.model.Stop
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class CsvParserTest {
    
    private val parser = CsvParser()

    @Test
    fun testParseStopsSemiColon() {
        val csv = "DURAK_ID;DURAK_ADI;ENLEM;BOYLAM;DURAKTAN_GECEN_HATLAR\n" +
                  "10005;Bahribaba;38.415;27.127;32"
        val stops = parser.parseStops(ByteArrayInputStream(csv.toByteArray()))
        
        assertEquals(1, stops.size)
        assertEquals("10005", stops[0].id)
        assertEquals("Bahribaba", stops[0].name)
    }

    @Test
    fun testParseStopsComma() {
        val csv = "DURAK_ID,DURAK_ADI,ENLEM,BOYLAM,DURAKTAN_GECEN_HATLAR\n" +
                  "10005,Bahribaba,38.415,27.127,32"
        val stops = parser.parseStops(ByteArrayInputStream(csv.toByteArray()))
        
        assertEquals(1, stops.size)
        assertEquals("10005", stops[0].id)
    }
}
