var map;

function initmap() {
    // set up the map
    map = new L.Map('mainMap');

    // create the tile layer with correct attribution
    var osmUrl='http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
    var osmAttrib='Map data © <a href="http://openstreetmap.org">OpenStreetMap</a> contributors';
    var osm = new L.TileLayer(osmUrl, {minZoom: 7, maxZoom: 12, attribution: osmAttrib});        

    // start the map in South-East England
    map.setView(new L.LatLng(51.3, 0.0),8);
    map.addLayer(osm);
}
