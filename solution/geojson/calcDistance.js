// calculates the total length for an array of coordinates
let geojsonLength = require('geojson-length');
let silverstone = require('./silverstone.json');
let silverstoneClosed = require('./silverstone_closed.json');
console.log("Silverstone.geojson        distance: %d km", geojsonLength(silverstone) / 1000);
console.log("Silverstone_closed.geojson distance: %d km", geojsonLength(silverstoneClosed) / 1000);
