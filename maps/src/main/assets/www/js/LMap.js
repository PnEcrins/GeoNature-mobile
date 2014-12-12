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

		this.options.bbox = JSON.parse(MainControlHandler.getBounds());
		this.options.maxBounds = new L.LatLngBounds(
		                                    [maxBounds[0][0],
		                                    maxBounds[0][1]],
		                                    [maxBounds[1][0],
		                                    maxBounds[1][1]]
                                    );
		this.options.center = new L.LatLng(
		                                center[0],
		                                center[1]
                                    );
		this.options.zoom = MainControlHandler.getZoom();
		this.options.minZoom = MainControlHandler.getMinZoom();
		this.options.maxZoom = MainControlHandler.getMaxZoom();
		this.options.zooms = JSON.parse(MainControlHandler.getZooms());

		console.log("LM.initialize " + JSON.stringify(this.options));

		// maximum resolution in meters per pixel (max area size / tile size).
		var maxResolution = Math.max(
		                        this.options.bbox[2] - this.options.bbox[0],
		                        this.options.bbox[3] - this.options.bbox[1]
                            ) / 256;

		// official Spatial Reference from http://www.spatialreference.org/ref/epsg/2154/
		var crs = new L.Proj.CRS(
                            "EPSG:2154",
                            "+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
		{
			// Coordinate to grid transformation matrix
			transformation: new L.Transformation(
                                        1,
                                        -this.options.bbox[0],
                                        -1,
                                        this.options.bbox[3]
                                )
		});

		// required by Leaflet 0.4+
		crs.scale = function(zoom)
		{
			return 1 / (maxResolution / Math.pow(2, zoom));
		};

		this._map = new L.LimitedZoomMap("map",
		{
			crs: crs,
			scale: crs.scale,
			center: this.options.center,
			zoom: this.options.zoom,
			minZoom: this.options.minZoom,
			maxZoom: this.options.maxZoom,
			maxBounds: this.options.maxBounds,
			touchZoom: true,
			zoomControl: false,
			validZoomLevels: this.options.zooms
		});

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