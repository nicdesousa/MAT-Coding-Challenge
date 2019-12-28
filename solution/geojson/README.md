# GeoJSON Distance Calculation

## Overview

The `GPS Source` service uses a GeoJSON file with `LineString` coordinates to represent a lap around the Silverstone Circuit.

The source data (`silverstone.json`) results in a circuit length of `~5.099 km`, while the lap distance calculation of Car's in the `Telemetry Solution` (i.e. `silverstone_closed.json`) is `~5.119 km`. 

This is because the Haversine distance calculation uses two Location updates that, in this case, closes the polygon:

![](../../images/Silverstone-geojson.png)

```console
$ node calcDistance.js 
Silverstone.geojson        distance: 5.099633744757203 km
Silverstone_closed.geojson distance: 5.119771376975699 km
```

The current circuit length for [Silverstone](https://live.planetf1.com/british-grand-prix/silverstone-circuit/10/41146/circuit-info) is `5.901 km`.

Using the current circuit length would result in an incorrect finishing line being used for updates/events that are shown in the Webapp.

For this reason the Telemetry Solution uses `~5.119 km` as the circuit length to try and keep everything synchronized.
