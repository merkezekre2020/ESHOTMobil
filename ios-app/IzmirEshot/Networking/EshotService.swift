import Foundation

enum APIError: Error {
    case invalidURL
    case noData
    case decodingError
}

class EshotService {
    static let shared = EshotService()
    
    private init() {}
    
    func fetchApproachingBuses(stopId: String) async throws -> [BusDto] {
        guard let url = URL(string: "https://openapi.izmir.bel.tr/api/iztek/duragayaklasanotobusler/\(stopId)") else {
            throw APIError.invalidURL
        }
        
        let (data, _) = try await URLSession.shared.data(from: url)
        
        let decoder = JSONDecoder()
        // Lenient decoding handled in DTO properties (String -> Double)
        return try decoder.decode([BusDto].self, from: data)
    }
    
    // Using CSV for Nearby finding logic as per Plan (reliable WGS84)
    // But if we want to use the API:
    func fetchNearbyStops(lat: Double, lon: Double) async throws -> Data {
        guard let url = URL(string: "https://openapi.izmir.bel.tr/api/ibb/cbs/noktayayakinduraklar?x=\(lon)&y=\(lat)&inCoordSys=WGS84&outCoordSys=WGS84") else {
             throw APIError.invalidURL
        }
        let (data, _) = try await URLSession.shared.data(from: url)
        return data
    }
}
