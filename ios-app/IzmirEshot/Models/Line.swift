import Foundation

struct Line: Identifiable, Codable {
    let id: String
    let name: String
    let description: String
    let startStop: String
    let endStop: String
}
