// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "IzmirEshot",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(name: "IzmirEshot", targets: ["IzmirEshot"]),
    ],
    targets: [
        .target(
            name: "IzmirEshot",
            path: "IzmirEshot"
        ),
        .testTarget(
            name: "IzmirEshotTests",
            dependencies: ["IzmirEshot"],
            path: "IzmirEshotTests"
        )
    ]
)
