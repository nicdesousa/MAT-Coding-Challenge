# GeoJSON Circuit Length Calculation

## Overview

The MAT `GPS Source` service uses a GeoJSON file ([silverstone.geojson](http://geojson.tools/?url=https://raw.githubusercontent.com/nicdesousa/MAT-Coding-Challenge/master/solution/geojson/silverstone.geojson)) with `LineString` coordinates to represent a lap around the Silverstone Circuit as an array of line segments.

As per the highlighted areas in the image below, the GeoJSON source file's line segments do not result in a "closed" polygon:

![](../../images/Silverstone-geojson.png)

The total length of the line segments (defined by the coordinates) can be calculated with the Haversine formula, where:

- The total length of the line segments, as shown on the left-hand side of the image, is: `5.099633744071716 km`
- If we now "close" the polygon ([silverstone_closed.geojson](http://geojson.tools/?url=https://raw.githubusercontent.com/nicdesousa/MAT-Coding-Challenge/master/solution/geojson/silverstone_closed.geojson)), as shown on right-hand side of the image, the total length is: `5.119771376289225 km`

The `Telemetry Solution` uses the latter as the circuit length, since the first `carCoordinate` sent after the completion of a loop in the `GPS Source` "closes" the polygon.

## Implementation and Testing

Please review [HaversineTest.testCircuitGeometry()](../src/test/java/com/github/nicdesousa/telemetry/util/HaversineTest.java) for the implementation and testing code.
