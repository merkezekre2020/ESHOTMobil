package com.eshot.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API Response DTO for Approaching Buses
 * "KoorX": "38,403..." -> Latitude
 * "KoorY": "27,136..." -> Longitude
 */
@Serializable
data class BusDto(
    @SerialName("OtobusId") val busId: Int,
    @SerialName("HatNumarasi") val lineNo: Int,
    @SerialName("HatAdi") val lineName: String,
    @SerialName("KoorX") val latitudeRaw: String?, // String because of comma
    @SerialName("KoorY") val longitudeRaw: String?,
    @SerialName("KalanDurakSayisi") val remainingStops: Int,
    @SerialName("HattinYonu") val direction: Int
) {
    val latitude: Double?
        get() = latitudeRaw?.replace(",", ".")?.toDoubleOrNull()
        
    val longitude: Double?
        get() = longitudeRaw?.replace(",", ".")?.toDoubleOrNull()
}
