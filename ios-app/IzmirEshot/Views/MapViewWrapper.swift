import SwiftUI
import MapKit

struct MapViewWrapper: UIViewRepresentable {
    @Binding var stops: [Stop]
    @Binding var selectedStop: Stop?
    @Binding var region: MKCoordinateRegion
    
    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true
        
        // Register standard annotation view for clustering
        mapView.register(MKMarkerAnnotationView.self, forAnnotationViewWithReuseIdentifier: "StopAnnotation")
        
        return mapView
    }
    
    func updateUIView(_ uiView: MKMapView, context: Context) {
        // Efficient update: Only update if count mismatch significantly or simplistic logic
        // Ideally checking diff, but for 5000 items, removeAll/add might flicker.
        // For MVP, we'll check if already populated.
        
        if uiView.annotations.count < stops.count {
             uiView.removeAnnotations(uiView.annotations)
             
             let annotations = stops.map { stop -> MKPointAnnotation in
                 let ann = StopAnnotation()
                 ann.coordinate = stop.coordinate
                 ann.title = stop.name
                 ann.stopData = stop
                 return ann
             }
             uiView.addAnnotations(annotations)
        }
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, MKMapViewDelegate {
        var parent: MapViewWrapper
        
        init(_ parent: MapViewWrapper) {
            self.parent = parent
        }
        
        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            if annotation is MKUserLocation { return nil }
            
            if let stopAnn = annotation as? StopAnnotation {
                let view = mapView.dequeueReusableAnnotationView(withIdentifier: "StopAnnotation", for: annotation) as? MKMarkerAnnotationView
                    ?? MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: "StopAnnotation")
                
                view.clusteringIdentifier = "stopCluster"
                view.markerTintColor = .systemBlue
                return view
            }
            return nil
        }
        
        func mapView(_ mapView: MKMapView, didSelect view: MKAnnotationView) {
            if let stopAnn = view.annotation as? StopAnnotation {
                parent.selectedStop = stopAnn.stopData
            }
        }
    }
}

class StopAnnotation: MKPointAnnotation {
    var stopData: Stop?
}
