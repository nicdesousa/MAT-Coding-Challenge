// calculates the total length for an array of coordinates
var geojsonLength = require('geojson-length');
let silverstone = require('./silverstone.json');
let silverstoneClosed = require('./silverstone_closed.json');
console.log("Silverstone.geojson        distance: " + geojsonLength(silverstone));
console.log("Silverstone_closed.geojson distance: " + geojsonLength(silverstoneClosed));
