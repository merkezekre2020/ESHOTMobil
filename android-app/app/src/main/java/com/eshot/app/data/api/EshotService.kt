package com.eshot.app.data.api

import com.eshot.app.data.model.BusDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object EshotService {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getApproachingBuses(stopId: String): List<BusDto> {
        return try {
            client.get("https://openapi.izmir.bel.tr/api/iztek/duragayaklasanotobusler/$stopId").body()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getNearbyStopsRaw(lat: Double, lon: Double): String {
       return client.get("https://openapi.izmir.bel.tr/api/ibb/cbs/noktayayakinduraklar?x=$lon&y=$lat&inCoordSys=WGS84&outCoordSys=WGS84").body()
    }
}
