import Foundation

@MainActor
class Repository: ObservableObject {
    static let shared = Repository()
    
    @Published var stops: [Stop] = []
    @Published var lines: [Line] = []
    
    private let parser = CsvParser()
    private let fileManager = FileManager.default
    
    private var cacheDir: URL {
        fileManager.urls(for: .cachesDirectory, in: .userDomainMask)[0]
    }
    
    func loadStops() async {
        let fileURL = cacheDir.appendingPathComponent("stops.csv")
        if !fileManager.fileExists(atPath: fileURL.path) {
            await downloadFile(url: "https://openfiles.izmir.bel.tr/211488/docs/eshot-otobus-duraklari.csv", to: fileURL)
        }
        
        // Reload even if failed to DL (use stale if exists)
        if let data = try? Data(contentsOf: fileURL) {
            self.stops = parser.parseStops(data: data)
        }
    }
    
    func loadLines() async {
         let fileURL = cacheDir.appendingPathComponent("lines.csv")
         if !fileManager.fileExists(atPath: fileURL.path) {
             await downloadFile(url: "https://openfiles.izmir.bel.tr/211488/docs/eshot-otobus-hatlari.csv", to: fileURL)
         }
         
         if let data = try? Data(contentsOf: fileURL) {
            self.lines = parser.parseLines(data: data)
         }
    }
    
    func getApproachingBuses(stopId: String) async -> [BusDto] {
        do {
            return try await EshotService.shared.fetchApproachingBuses(stopId: stopId)
        } catch {
            print("Error fetching buses: \(error)")
            return []
        }
    }
    
    private func downloadFile(url: String, to destination: URL) async {
        guard let urlObj = URL(string: url) else { return }
        var request = URLRequest(url: urlObj)
        request.setValue("Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1", forHTTPHeaderField: "User-Agent")
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            if let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) {
                 try data.write(to: destination)
            } else {
                print("Server returned error for \(url)")
            }
        } catch {
            print("Download error: \(error)")
        }
    }
}
