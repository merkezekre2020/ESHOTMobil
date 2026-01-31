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
        
        let criticalMaxIndex = [idIndex, latIndex, lonIndex].max() ?? 0

        for line in lines {
            let trimmedLine = line.trimmingCharacters(in: .whitespacesAndNewlines)
            if trimmedLine.isEmpty { continue }
            
            let parts = line.components(separatedBy: delimiter).map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            if parts.count > criticalMaxIndex {
                // indices are valid
                let latStr = parts[latIndex].replacingOccurrences(of: ",", with: ".")
                let lonStr = parts[lonIndex].replacingOccurrences(of: ",", with: ".")
                
                if let lat = latStr.toDouble(), let lon = lonStr.toDouble() {
                    
                    let name = (nameIndex != -1 && nameIndex < parts.count) ? parts[nameIndex] : "Unknown"
                    let lineIds = (linesIndex != -1 && linesIndex < parts.count) ? parts[linesIndex] : ""
                    
                    stops.append(Stop(
                        id: parts[idIndex],
                        name: name,
                        latitude: lat,
                        longitude: lon,
                        lineIds: lineIds
                    ))
                }
            }
        }
        return stops
    }

    func parseLines(data: Data) -> [Line] {
        guard let content = decodeData(data) else { return [] }
        var lines = content.components(separatedBy: .newlines)
        if lines.isEmpty { return [] }
        
        let headerLine = lines.removeFirst()
        let delimiter = detectDelimiter(headerLine)
        
        let headers = headerLine.components(separatedBy: delimiter).map { $0.trimmingCharacters(in: .whitespacesAndNewlines).uppercased() }
        
        guard let idIndex = headers.firstIndex(where: { $0 == "HAT_NO" || $0.contains("ID") }) else { return [] }
        
        let nameIndex = headers.firstIndex(where: { $0 == "HAT_ADI" || $0.contains("NAME") }) ?? -1
        let descIndex = headers.firstIndex(where: { $0.contains("GUZERGAH") }) ?? -1
        let startIndex = headers.firstIndex(where: { $0.contains("BASLANGIC") }) ?? -1
        let endIndex = headers.firstIndex(where: { $0.contains("BITIS") }) ?? -1
        
        var lineList: [Line] = []
        
        for row in lines {
            let trimmedRow = row.trimmingCharacters(in: .whitespacesAndNewlines)
            if trimmedRow.isEmpty { continue }
            
            let parts = row.components(separatedBy: delimiter).map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            if parts.count > idIndex {
                 let id = parts[idIndex]
                 let name = (nameIndex != -1 && nameIndex < parts.count) ? parts[nameIndex] : ""
                 let desc = (descIndex != -1 && descIndex < parts.count) ? parts[descIndex] : ""
                 let start = (startIndex != -1 && startIndex < parts.count) ? parts[startIndex] : ""
                 let end = (endIndex != -1 && endIndex < parts.count) ? parts[endIndex] : ""
                 
                 lineList.append(Line(id: id, name: name, description: desc, startStop: start, endStop: end))
            }
        }
        return lineList
    }
    
    private func decodeData(_ data: Data) -> String? {
        if let utf8 = String(data: data, encoding: .utf8) {
            return utf8
        }
        // Fallback for Windows-1254 (Turkish)
        // Using ISOLatin1 as a poor man's fallback if 1254 isn't available easily
        // In a real iOS app, we would use CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingWindowsLatin5)
        if let latin1 = String(data: data, encoding: .isoLatin1) {
            return latin1 
        }
        return String(data: data, encoding: .windowsCP1252)
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
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.decimalSeparator = "."
        formatter.groupingSeparator = ","
        
        // First try standard parsing
        if let double = Double(self) {
            return double
        }
        
        // Then try formatter
        if let number = formatter.number(from: self) {
            return number.doubleValue
        }
        
        // Try replacing commas with periods
        let normalizedString = self.replacingOccurrences(of: ",", with: ".")
        return Double(normalizedString)
    }
}