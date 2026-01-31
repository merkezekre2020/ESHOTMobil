import Foundation

struct BusDto: Codable {
    let busId: Int
    let lineNo: Int
    let lineName: String
    let remainingStops: Int
    let latitudeRaw: String?
    let longitudeRaw: String?
    
    enum CodingKeys: String, CodingKey {
        case busId = "OtobusId"
        case lineNo = "HatNumarasi"
        case lineName = "HatAdi"
        case remainingStops = "KalanDurakSayisi"
        case latitudeRaw = "KoorX" // Mapped X to Latitude per analysis
        case longitudeRaw = "KoorY"
    }
    
    var latitude: Double? {
        latitudeRaw?.replacingOccurrences(of: ",", with: ".").toDouble()
    }
    
    var longitude: Double? {
        longitudeRaw?.replacingOccurrences(of: ",", with: ".").toDouble()
    }
}

extension String {
    func toDouble() -> Double? {
        return Double(self)
    }
}
