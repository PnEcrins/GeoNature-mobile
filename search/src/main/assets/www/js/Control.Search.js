L.Control.Search = L.Control.extend(
{
	_marker: null,

	// constructor
	initialize : function(options)
	{
		L.Util.setOptions(this, options);
	},

	onAdd: function(map)
	{
		this._map = map;

		var container = L.DomUtil.create("div", "searchControlHandler");

		SearchControlHandler.setControlInitialized();

		var pointing = JSON.parse(SearchControlHandler.getMarkerPosition());

		if ((pointing != null) && (pointing.length == 2))
		{
			this._showMarker(new L.LatLng(pointing[0], pointing[1]));
		}

		return container;
	},

	onRemove: function(map)
	{
		map.off("click", this._onClickEvent, this);
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
		this._showMarker(new L.LatLng(latitude, longitude));
		this.removeOnClickEvent();
		SearchControlHandler.setMarkerPosition(latitude, longitude, accuracy);
	},

	_onClickEvent: function(e)
	{
		console.log("L.Control.Search.onClickEvent " + JSON.stringify(e.latlng));

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

			this._marker.on("click dragend", this._moveMarker, this);
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
		console.log("L.Control.Search.moveMarker " + JSON.stringify(e.target.getLatLng()));

		SearchControlHandler.setMarkerPosition(e.target.getLatLng().lat, e.target.getLatLng().lng, 0);
	}
});