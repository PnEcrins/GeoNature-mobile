L.Control.Main = L.Control.extend(
{
	_currentLocation: null,
	
	onAdd: function(map)
	{
		this._map = map;
		
		var container = L.DomUtil.create("div", "mainControlHandler");
		
		this._map.on("zoomend", this._onZoomEvent, this);
		this._map.on("moveend", this._onCenterEvent, this);
		
		MainControlHandler.setControlInitialized();
		
		return container;
	},
	
	onRemove: function(map)
	{
		map.off("zoomend", this._onZoomEvent, this);
		map.off("moveend", this._onCenterEvent, this);
	},
	
	showCurrentLocation: function(latitude, longitude, accuracy, bearing)
	{
		console.log("L.Control.Main.showCurrentLocation [" + latitude + ", " + longitude + "]");
		
		if (this._currentLocation == null)
		{
			var marker = new L.Marker([latitude, longitude],
			{
				icon: L.icon(
				{
					iconUrl: L.Icon.Default.imagePath + "/marker-location.png",
					iconSize: [25, 25],
					className: "marker-location"
				})
			});
			marker.bindPopup(MainControlHandler.getLocalizedMessage("message_current_location"));
			
			var circle = new L.Circle([latitude, longitude], accuracy,
			{
				color: "#ff0000",
				fillColor: "#ff0000",
				weight: 2,
				fillOpacity: 0.1
			});
			
			this._currentLocation = new L.LayerGroup([circle, marker]);
			this._map.addLayer(this._currentLocation);
		}
		else
		{
			/*
			this._map.off("zoomend", this._onZoomEvent, this);
			this._map.off("moveend", this._onCenterEvent, this);
			*/
			this._currentLocation.eachLayer(function(layer)
			{
				layer.setLatLng([latitude, longitude]);
				
				if (layer instanceof L.Circle)
				{
					layer.setRadius(accuracy);
				}
			});
			/*
			this._map.on("zoomend", this._onZoomEvent, this);
			this._map.on("moveend", this._onCenterEvent, this);
			*/
		}
		
		//$(".marker-location").css("-webkit-transform", $(".marker-location").css("-webkit-transform") + " rotate(" + bearing + "deg)");
		//$(".marker-location").css("transition", "all 100ms");
	},
	
	hideCurrentLocation: function()
	{
		if (this._currentLocation != null)
		{
			this._map.removeLayer(this._currentLocation);
			this._currentLocation = null;
		}
	},
	
	_onZoomEvent: function(e)
	{
		//$(".marker-location").css("-webkit-transform", $(".marker-location").css("-webkit-transform") + " rotate(" + MapFragment.getBearing() + "deg)");
		MainControlHandler.setZoom(this._map.getZoom());
	},
	
	_onCenterEvent: function(e)
	{
		MainControlHandler.setCenter(this._map.getCenter().lat, this._map.getCenter().lng);
	}
});