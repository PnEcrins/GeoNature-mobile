L.Control.Unities = L.Control.extend(
{
	_marker: null,
	_unitiesLayers: null,
	_selectedUnityLayer: null,
	
	// constructor
	initialize : function(options)
	{
		L.Util.setOptions(this, options);
	},
	
	onAdd: function(map)
	{
		this._map = map;
		
		var container = L.DomUtil.create("div", "menuUnitiesControlHandler");
		
		map.on("zoomend", this._onZoomEvent, this);
		
		MenuUnitiesControlHandler.setControlInitialized();
		
		if (this.options.hasUnityLayerSource)
		{
			this._unitiesLayers = new L.LayerGroup();
			this._addTilesSourceLayer(MenuUnitiesControlHandler.getUnityLayerSource());
			this._addSelectedUnityLayer(JSON.parse(MenuUnitiesControlHandler.getSelectedUnity()));
			
			if (MenuUnitiesControlHandler.showUnitiesLayer())
			{
				this._map.addLayer(this._unitiesLayers);
			}
		}
		
		var pointing = JSON.parse(MenuUnitiesControlHandler.getMarkerPosition());
		
		if ((pointing != null) && (pointing.length == 2))
		{
			this._showMarker(new L.LatLng(pointing[0], pointing[1]));
		}
		
		return container;
	},
	
	onRemove: function(map)
	{
		map.off("zoomend", this._onZoomEvent, this);
		map.off("click", this._onClickEvent, this);
		
		if (this.options.hasUnityLayerSource)
		{
			map.removeLayer(this._unitiesLayers);
			this._unitiesLayers.clearLayers();
		}
	},
	
	addOnClickEvent: function()
	{
		this._map.on("click", this._onClickEvent, this);
	},
	
	removeOnClickEvent: function()
	{
		this._map.off("click", this._onClickEvent, this);
	},
	
	addMarker: function(latitude, longitude, accuracy)
	{
		this._addSelectedUnityLayer(JSON.parse(MenuUnitiesControlHandler.getUnityFromLocation(latitude, longitude, accuracy)));
		this._showMarker(new L.LatLng(latitude, longitude));
		this.removeOnClickEvent();
		MenuUnitiesControlHandler.setMarkerPosition(latitude, longitude, accuracy);
	},
	
	showOrHideUnitiesLayers: function()
	{
		if (this.options.hasUnityLayerSource)
		{
			if (this._map.hasLayer(this._unitiesLayers))
			{
				this._map.removeLayer(this._unitiesLayers);
			}
			else
			{
				this._map.addLayer(this._unitiesLayers);
			}
			
			MenuUnitiesControlHandler.toggleUnitiesLayer(this._map.hasLayer(this._unitiesLayers));
		}
	},
	
	_onClickEvent: function(e)
	{
		console.log("L.Control.Unities.onClickEvent " + JSON.stringify(e.latlng));
		
		this.addMarker(e.latlng.lat, e.latlng.lng, 0);
	},
	
	_showMarker: function(latlng)
	{
		if (this._marker == null)
		{
			this._marker = new L.Marker(latlng,
			{
				icon: L.icon(
				{
					iconUrl: L.Icon.Default.imagePath + "/marker2-blue.png",
					shadowUrl: L.Icon.Default.imagePath + "/marker2-shadow.png",
					iconSize: [51, 82],
					shadowSize: [51, 37],
					iconAnchor: [25, 82],
					shadowAnchor: [0, 37],
					className: "marker2"
				}),
				draggable: true
			});
			
			this._marker.on("dragend", this._moveMarker, this);
		}
		else
		{
			this._marker.setLatLng(latlng);
		}
		
		if (this._map.hasLayer(this._marker))
		{
			this._map.removeLayer(this._marker);
		}
		
		this._map.addLayer(this._marker);
	},
	
	_moveMarker: function(e)
	{
		console.log("L.Control.Unities.moveMarker " + JSON.stringify(e.target.getLatLng()));
		
		this._addSelectedUnityLayer(JSON.parse(MenuUnitiesControlHandler.getUnityFromLocation(e.target.getLatLng().lat, e.target.getLatLng().lng, 0)));
		MenuUnitiesControlHandler.setMarkerPosition(e.target.getLatLng().lat, e.target.getLatLng().lng, 0);
	},
	
	_addTilesSourceLayer: function(tilesSource)
	{
		var layer = new L.TileLayer.TilesSource(tilesSource,
		{
			minZoom: this.options.minZoom,
			maxZoom: this.options.maxZoom,
			continuousWorld: true // very important
		});
		
		this._unitiesLayers.addLayer(layer);
	},
	
	_addSelectedUnityLayer: function(geoJson)
	{
		// only if geoJson parameter is a valid GeoJSON
		if (geoJson.type && (geoJson.type === "FeatureCollection"))
		{
			// remove previously added selected unity
			if (this._selectedUnityLayer != null)
			{
				this._unitiesLayers.removeLayer(this._selectedUnityLayer);
			}
			
			this._selectedUnityLayer = new L.GeoJSON(geoJson,
			{
				style:
				{
					color: "red",
					weight: 2,
					opacity: 0.75,
					fill: false
					//fillOpacity: 0.04
				}
			});
			
			this._unitiesLayers.addLayer(this._selectedUnityLayer);
		}
		else
		{
			// remove previously added selected unity
			if (this._selectedUnityLayer != null)
			{
				this._unitiesLayers.removeLayer(this._selectedUnityLayer);
			}
			
			this._selectedUnityLayer = null;
		}
	},
	
	_onZoomEvent: function(e)
	{
		MenuUnitiesControlHandler.setZoom(this._map.getZoom());
		
		// sets the draggable property to false on added marker if the current zoom is less than minimum zoom for pointing
		if (this._marker != null)
		{
			if (MenuUnitiesControlHandler.getMinimumZoomPointing() > this._map.getZoom())
			{
				this._marker.dragging.disable();
			}
			else
			{
				this._marker.dragging.enable();
			}
		}
	}
});