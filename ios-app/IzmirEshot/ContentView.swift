import SwiftUI
import MapKit

struct ContentView: View {
    @StateObject private var repo = Repository.shared
    @State private var selectedStop: Stop?
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 38.4192, longitude: 27.1287),
        span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
    )
    
    var body: some View {
        ZStack {
            MapViewWrapper(stops: $repo.stops, selectedStop: $selectedStop, region: $region)
                .edgesIgnoringSafeArea(.all)
                .sheet(item: $selectedStop) { stop in
                    StopDetailView(stop: stop)
                        .presentationDetents([.medium, .large])
                }
            
            if repo.stops.isEmpty {
                 VStack {
                     ProgressView("Duraklar Yükleniyor...")
                         .padding()
                         .background(.ultraThinMaterial)
                         .cornerRadius(10)
                 }
            }
        }
        .task {
            await repo.loadStops()
        }
    }
}

#Preview {
    ContentView()
}
