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
        guard let url = URL(string: url) else { return }
        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            try data.write(to: destination)
        } catch {
            print("Download error: \(error)")
        }
    }
}
