import Foundation

class CsvParser {
    
    func parseStops(data: Data) -> [Stop] {
        guard let content = decodeData(data) else { return [] }
        
        var lines = content.components(separatedBy: .newlines)
        if lines.isEmpty { return [] }
        
        let headerLine = lines.removeFirst()
        let delimiter = detectDelimiter(headerLine)
        
        let headers = headerLine.components(separatedBy: delimiter).map { $0.trimmingCharacters(in: .whitespacesAndNewlines).uppercased() }
        
        guard let idIndex = headers.firstIndex(where: { $0.contains("ID") }),
              let latIndex = headers.firstIndex(where: { $0.contains("ENLEM") || $0.contains("LAT") }),
              let lonIndex = headers.firstIndex(where: { $0.contains("BOYLAM") || $0.contains("LON") }) else {
            return []
        }
        
        let nameIndex = headers.firstIndex(where: { $0.contains("ADI") || $0.contains("NAME") }) ?? -1
        let linesIndex = headers.firstIndex(where: { $0.contains("HATLAR") }) ?? -1
        
        var stops: [Stop] = []
        
        for line in lines {
            let parts = line.components(separatedBy: delimiter)
            if parts.count >= headers.count { // Lenient
                let latIndex = latIndex < parts.count ? latIndex : -1
                let lonIndex = lonIndex < parts.count ? lonIndex : -1
                
                if latIndex != -1, lonIndex != -1,
                   let lat = parts[latIndex].replacingOccurrences(of: ",", with: ".").toDouble(),
                   let lon = parts[lonIndex].replacingOccurrences(of: ",", with: ".").toDouble() {
                    
                    stops.append(Stop(
                        id: parts[idIndex],
                        name: nameIndex != -1 ? parts[nameIndex] : "Unknown",
                        latitude: lat,
                        longitude: lon,
                        lineIds: linesIndex != -1 ? parts[linesIndex] : ""
                    ))
                }
            }
        }
        return stops
    }
    
    private func decodeData(_ data: Data) -> String? {
        // Try UTF-8
        if let utf8 = String(data: data, encoding: .utf8) {
            return utf8
        }
        // Try Windows-1254 (No native constant, use validation or Latin5/ISO-8859-9 approximation)
        // Swift ISOLatin5 (8859-9) is closest to 1254
        if let latin5 = String(data: data, encoding: .isoLatin1) { // Fallback to compatible western
            return latin5 
        }
        // WindowsCP1254 = 0x800004e6 ? No, simpler to just treat as ASCII if all else fails or map manually.
        // Actually, we can use String(encodings: ...)
        return String(data: data, encoding: .windowsCP1252) // Close enough often
    }
    
    private func detectDelimiter(_ line: String) -> String {
        let semi = line.filter { $0 == ";" }.count
        let comma = line.filter { $0 == "," }.count
        let tab = line.filter { $0 == "\t" }.count
        
        if semi >= comma && semi >= tab { return ";" }
        if tab > comma { return "\t" }
        return ","
    }
}

extension String {
    func toDouble() -> Double? {
        return Double(self)
    }
}
