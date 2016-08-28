L.MapControl = L.Class.extend(
{
    options:
    {

    },

    _map: null,
    _controls: {},

    // constructor
    initialize: function(options)
    {
        this._setViewport();

        L.Util.setOptions(this, options);

        var center = JSON.parse(MainControlHandler.getCenter());
        var maxBounds = JSON.parse(MainControlHandler.getMaxBounds());

        this.options.maxBounds = new L.LatLngBounds(
                                            [maxBounds[0][0],
                                            maxBounds[0][1]],
                                            [maxBounds[1][0],
                                            maxBounds[1][1]]
                                    );

        this.options.center = new L.LatLng(center[0], center[1]);
        this.options.zoom = MainControlHandler.getZoom();
        this.options.minZoom = MainControlHandler.getMinZoom();
        this.options.maxZoom = MainControlHandler.getMaxZoom();
        this.options.zooms = JSON.parse(MainControlHandler.getZooms());

        var mapOptions = {
            center: this.options.center,
            zoom: this.options.zoom,
            minZoom: this.options.minZoom,
            maxZoom: this.options.maxZoom,
            maxBounds: this.options.maxBounds,
            touchZoom: true,
            zoomControl: false,
            validZoomLevels: this.options.zooms
        };

        var crsSettings = MainControlHandler.getCRS();

        if (crsSettings)
        {
            var crsSettingsAsJson = JSON.parse(crsSettings);

            // maximum resolution in meters per pixel (max area size / tile size)
            var maxResolution = Math.max(
                                    crsSettingsAsJson.bbox[2] - crsSettingsAsJson.bbox[0],
                                    crsSettingsAsJson.bbox[3] - crsSettingsAsJson.bbox[1]
                                ) / 256;

            // official Spatial Reference from http://www.spatialreference.org/ref/epsg/2154/
            var crs = new L.Proj.CRS(
                                crsSettingsAsJson.code,
                                crsSettingsAsJson.def,
                                {
                                    // Coordinate to grid transformation matrix
                                    transformation: new L.Transformation(
                                                                1,
                                                                -crsSettingsAsJson.bbox[0],
                                                                -1,
                                                                crsSettingsAsJson.bbox[3]
                                                        )
                                });

            // required by Leaflet 0.4+
            crs.scale = function(zoom)
            {
                return 1 / (maxResolution / Math.pow(2, zoom));
            };

            L.Util.extend(
                mapOptions,
                {
                    crs: crs,
                    scale: crs.scale,
                });
        }

        console.log("LM.initialize, map options: " + JSON.stringify(mapOptions));

        this._map = new L.LimitedZoomMap("map", mapOptions);
        this._map.once("viewreset", this._onMapLoaded, this);
        this._map.attributionControl.setPrefix("");

        if (MainControlHandler.displayScale())
        {
            L.control.scale().addTo(this._map);
        }

        // adding all tiles sources
        this._addTilesSourcesLayer();

        this._map.setView(this.options.center, this.options.zoom,
        {
            reset: true
        });
    },

    addControl: function(controlHandlerName, control)
    {
        console.log("LM.addControl " + controlHandlerName);

        if (this._controls.hasOwnProperty(controlHandlerName))
        {
            this.removeControl(controlHandlerName);
        }

        this._controls[controlHandlerName] = control;
        this._controls[controlHandlerName].addTo(this._map);
    },

    removeControl: function(controlHandlerName)
    {
        console.log("LM.removeControl " + controlHandlerName);

        if (this._controls.hasOwnProperty(controlHandlerName))
        {
            this._controls[controlHandlerName].removeFrom(this._map);
            delete this._controls[controlHandlerName];
        }
    },

    getControl: function(controlHandlerName)
    {
        if (this._controls.hasOwnProperty(controlHandlerName))
        {
            return this._controls[controlHandlerName];
        }
        else
        {
            console.log("LM.getControl : no such control '" + controlHandlerName + "'");
        }
    },

    clearControls: function()
    {
        for (var controlHandlerName in this._controls)
        {
            this.removeControl(controlHandlerName);
        }
    },

    _onMapLoaded: function()
    {
        console.log("LM._onMapLoaded : map loaded");

        MainControlHandler.setMapInitialized();
    },

    _setViewport: function()
    {
        var viewport = document.querySelector("meta[name=viewport]");
        viewport.setAttribute("content", "target-densitydpi=" + MainControlHandler.getDensityDpi() + ", user-scalable=no");
    },

    _addTilesSourcesLayer: function()
    {
        var layer = new L.TileLayer.TilesSources(
        {
            minZoom: this.options.minZoom,
            maxZoom: this.options.maxZoom,
            continuousWorld: true // very important
        })

        this._map.addLayer(layer);
    }
});