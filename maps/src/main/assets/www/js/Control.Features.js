L.Control.Features = L.Control.extend(
{
	_features: null,

	onAdd: function(map)
	{
		console.log("L.Control.Features.onAdd");

		this._map = map;

		var container = L.DomUtil.create("div", "featuresControlHandler");

		this._features = new L.FeatureGroup();

		map.addLayer(this._features);

		FeaturesControlHandler.setControlInitialized();

		return container;
	},

	onRemove: function(map)
	{
		this._clearEvents();
	},

	addFeatures: function(featuresAsString, stylesAsString, fitBounds)
	{
		var featuresGroup = new L.FeatureGroup();
		var features = JSON.parse(featuresAsString);

		for (var i = 0; i < features.features.length; i++)
		{
			var feature = features.features[i];

			if (feature.geometry.type === "Point")
			{
				var circle = new L.Circle(new L.LatLng(feature.geometry.coordinates[1], feature.geometry.coordinates[0]));
				circle.options.key = feature.id;
				circle.setRadius(10);

				featuresGroup.addLayer(circle);
			}

			if (feature.geometry.type === "LineString")
			{
				var polyline = new L.Polyline([]);
				polyline.options.key = feature.id;

				for (var j = 0; j < feature.geometry.coordinates.length; j++)
				{
					polyline.addLatLng(new L.LatLng(feature.geometry.coordinates[j][1], feature.geometry.coordinates[j][0]));
				}

				featuresGroup.addLayer(polyline);
			}

			if (feature.geometry.type === "Polygon")
			{
				var polygon = new L.Polygon([]);
				polygon.options.key = feature.id;

				for (var j = 0; j < feature.geometry.coordinates[0].length; j++)
				{
					polygon.addLatLng(new L.LatLng(feature.geometry.coordinates[0][j][1], feature.geometry.coordinates[0][j][0]));
				}

				featuresGroup.addLayer(polygon);
			}
		}

		var style = JSON.parse(stylesAsString);

		featuresGroup.setStyle(
		{
			stroke: style.stroke,
			color: style.color,
			weight: style.weight,
			opacity: style.opacity,
			fillColor: style.fillColor,
			fillOpacity: style.fillOpacity,
			clickable: false
		});

		this._features.addLayer(featuresGroup);

		if (fitBounds)
		{
			this._map.fitBounds(featuresGroup.getBounds());
		}

		this._clearEvents();
		this._moveRefreshPosition();
		this._startFindFeature();
	},

	clearFeatures: function()
	{
		this._clearEvents();
		this._map.removeLayer(this._features);
		this._features.clearLayers();
		this._map.addLayer(this._features);

		this._moveRefreshPosition();
	},

	selectFeature: function(featureId)
	{
		console.log("L.Control.Features.selectFeature '" + featureId + "'");

		this._features.eachLayer(function(featureGroup)
		{
			if (featureGroup instanceof L.FeatureGroup)
			{
				featureGroup.setStyle(JSON.parse(FeaturesControlHandler.getFeatureDefaultStyleAsString()));

				featureGroup.eachLayer(function(feature)
				{
					if (feature.options.key === featureId)
					{
						console.log("L.Control.Features.selectFeature feature found : '" + featureId + "'");

						var style = JSON.parse(FeaturesControlHandler.getFeatureSelectedStyleAsString());

						// polyline and polygon
						if (feature instanceof L.Polyline)
						{
							// adding a specific style for a polyline
							if (!(feature instanceof L.Polygon))
							{
								style.fill = false;
							}
						}

						feature.setStyle(style);
					}
				});
			}
		});
	},

	_startFindFeature: function()
	{
		console.log("L.Control.Features.startFindFeature");

		if (FeaturesControlHandler.isFeaturesClickable())
		{
			this._map.on("click", this._onMapClickEvent, this);
		}
	},

	_onMapClickEvent: function(e)
	{
		console.log("L.Control.Features._onMapClickEvent " + JSON.stringify(e.latlng));

		FeaturesControlHandler.findFeature(e.latlng.lat, e.latlng.lng);
	},

	/**
	 * Clears all used events.
	 */
	_clearEvents: function()
	{
		this._map.off("click", this._onMapClickEvent, this);
	},

	_moveRefreshPosition: function()
	{
		var currentCenter = this._map.getCenter();
		var currentCenterPoint = this._map.latLngToContainerPoint(currentCenter);
		var refreshCenter = this._map.containerPointToLatLng(currentCenterPoint);
		this._map.setView(refreshCenter, this._map.getZoom());
		this._map.setView(currentCenter, this._map.getZoom());
	}
});