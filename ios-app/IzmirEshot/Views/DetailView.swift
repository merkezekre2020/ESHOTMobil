import SwiftUI

struct StopDetailView: View {
    let stop: Stop
    @State private var buses: [BusDto] = []
    
    var body: some View {
        VStack(alignment: .leading) {
            Text(stop.name)
                .font(.title)
                .padding(.bottom)
            
            Text("Yaklaşan Otobüsler")
                .font(.headline)
            
            List(buses, id: \.busId) { bus in
                HStack {
                    Text("\(bus.lineNo)")
                        .fontWeight(.bold)
                        .frame(width: 50)
                    Text(bus.lineName)
                    Spacer()
                    Text("\(bus.remainingStops) durak")
                        .foregroundStyle(.secondary)
                }
            }
        }
        .padding()
        .task {
            // Load
            buses = await Repository.shared.getApproachingBuses(stopId: stop.id)
        }
    }
}
