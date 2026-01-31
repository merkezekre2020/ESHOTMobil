import XCTest

class CsvParserTests: XCTestCase {
    
    let parser = CsvParser()
    
    func testParseStopsSemiColon() {
        let csv = "DURAK_ID;DURAK_ADI;ENLEM;BOYLAM;DURAKTAN_GECEN_HATLAR\n10005;Bahribaba;38.415;27.127;32"
        let data = csv.data(using: .utf8)!
        let stops = parser.parseStops(data: data)
        
        XCTAssertEqual(stops.count, 1)
        XCTAssertEqual(stops[0].id, "10005")
        XCTAssertEqual(stops[0].latitude, 38.415, accuracy: 0.001)
    }
    
    func testParseStopsComma() {
        let csv = "DURAK_ID,DURAK_ADI,ENLEM,BOYLAM,DURAKTAN_GECEN_HATLAR\n10005,Bahribaba,38.415,27.127,32"
        let data = csv.data(using: .utf8)!
        let stops = parser.parseStops(data: data)
        
        XCTAssertEqual(stops.count, 1)
    }
    
    func testParseCommaDecimal() {
        let csv = "DURAK_ID;DURAK_ADI;ENLEM;BOYLAM\n10005;Bahribaba;38,415;27,127"
        let data = csv.data(using: .utf8)!
        let stops = parser.parseStops(data: data)
        
        XCTAssertEqual(stops.count, 1)
        XCTAssertEqual(stops[0].latitude, 38.415, accuracy: 0.001)
    }
}
