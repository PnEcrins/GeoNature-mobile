L.Control.NativeDraw = L.Control.extend(
{
	_features: null,
	_currentFeature: null,
	_markers: null,
	_mode: null,
	_savingQueued: null,

	onAdd: function(map)
	{
		this._map = map;

		var container = L.DomUtil.create("div", "drawControlHandler");

		map.on("zoomend", this._onZoomEvent, this);

		this._features = new L.FeatureGroup();
		this._markers = new L.LayerGroup();

		map.addLayer(this._features);

        this.loadFeatures(true);

		DrawControlHandler.setControlInitialized();

		return container;
	},

	onRemove: function(map)
	{
		map.off("zoomend", this._onZoomEvent, this);
	},

    clearFeatures: function()
	{
		this._clearEvents();
		this._map.removeLayer(this._features);
		this._map.removeLayer(this._markers);
		this._features.clearLayers();
		this._markers.clearLayers();
		this._map.addLayer(this._features);

		this._moveRefreshPosition();
	},

	startDrawFeature: function(layerType)
	{
		this._clearEvents();
		this._markers.clearLayers();

		if (!this._map.hasLayer(this._features))
		{
			this._map.addLayer(this._features);
		}

		var style = JSON.parse(DrawControlHandler.getFeatureAddStyleAsString());

		console.log("L.Control.NativeDraw.startDrawFeature style : " + JSON.stringify(style));

		// starts drawing a marker
		if (layerType === "Point")
		{
			this._map.on("click", this._onMapClickEvent, this);

			this._currentFeature = this._createFeatureMarker();
		}

		// starts drawing a polyline
		if (layerType === "LineString")
		{
			this._map.on("click", this._onMapClickEvent, this);

			style.fill = false;

			this._currentFeature = new L.Polyline([], style);

			this._features.addLayer(this._currentFeature);
			this._map.addLayer(this._markers);
		}

		// starts drawing a polygon
		if (layerType === "Polygon")
		{
			this._map.on("click", this._onMapClickEvent, this);

			this._currentFeature = new L.Polygon([], style);

			this._features.addLayer(this._currentFeature);
			this._map.addLayer(this._markers);
		}

		// generates a new unique key for this feature
		if (this._currentFeature !== null)
		{
			this._currentFeature.options.key = layerType + "_" + (+new Date());

			DrawControlHandler.onAddingFeature(true);
		}
	},

	endDrawFeature: function()
	{
		console.log("L.Control.NativeDraw.endDrawFeature");

		this._clearEvents();
		this._map.removeLayer(this._markers);
		this._markers.clearLayers();

		if (this._currentFeature !== null)
		{
			if (this._currentFeature instanceof L.Polyline)
			{
				var style = JSON.parse(DrawControlHandler.getFeatureDefaultStyleAsString());

				if (!(this._currentFeature instanceof L.Polygon))
				{
					style.fill = false;
				}

				this._currentFeature.setStyle(style);
			}

			DrawControlHandler.onAddingFeature(false);
			this._currentFeature = null;
		}

		this._moveRefreshPosition();
	},

	startFindFeature: function(mode)
	{
		console.log("L.Control.NativeDraw.startFindFeature " + mode);

		this._clearEvents();
		this._markers.clearLayers();
		this._map.addLayer(this._markers);

		this._mode = mode;
		this._currentFeature = null;

		if ((this._mode !== null) && (this._mode === "delete"))
		{
			this._features.on("layerremove", this._onFeaturesLayerRemoveEvent, this);
		}

		this._features.eachLayer(function(feature)
		{
			// 'click' event only on features Marker
			if (feature instanceof L.Marker)
			{
				feature.on("click", this._onFeatureClickEvent, this);
			}
		}, this);

		this._map.on("click", this._onMapClickEvent, this);
	},

	startUpdateFeature: function(mode, featureId)
	{
		console.log("L.Control.NativeDraw.startUpdateFeature " + mode);

		this._clearEvents();
		this._markers.clearLayers();
		this._map.addLayer(this._markers);

		this._mode = mode;
		this._currentFeature = null;

		this._features.eachLayer(function(feature)
		{
			if (featureId === undefined)
			{
				if (feature instanceof L.Marker)
				{
					feature.dragging.enable();
				}

				feature.on("click", this._onFeatureClickEvent, this);
			}
			else
			{
				if (feature.options.key === featureId)
				{
					console.log("L.Control.NativeDraw.startUpdateFeature for feature '" + featureId + "'");
					this._onFeatureClickEvent({target: feature});
				}
			}

		}, this);
	},

	endUpdateFeature: function()
	{
		console.log("L.Control.NativeDraw.endUpdateFeature " + this._mode);

		this._clearEvents();
		this._map.removeLayer(this._markers);
		this._markers.clearLayers();

		if ((this._mode !== null) && (this._mode === "edit") && (this._currentFeature !== null))
		{
			if (this._currentFeature instanceof L.Polyline)
			{
				var style = JSON.parse(DrawControlHandler.getFeatureDefaultStyleAsString());

				if (!(this._currentFeature instanceof L.Polygon))
				{
					style.fill = false;
				}

				this._currentFeature.setStyle(style);
			}

			DrawControlHandler.onEditingFeature(false);
			this._currentFeature = null;
			this._mode = null;
		}

		this._moveRefreshPosition();
	},

    setFeatures: function()
    {
        this.loadFeatures(true);
    },

    loadFeatures: function(fitBounds)
	{
		this._clearEvents();
		this._map.removeLayer(this._features);
		this._features.clearLayers();
		this._map.addLayer(this._features);

		var featuresAsString = DrawControlHandler.loadFeatures();
		var features = JSON.parse(featuresAsString);

		var selectedFeatureAsString = DrawControlHandler.loadSelectedFeature();
		var selectedFeature = JSON.parse(selectedFeatureAsString);
		var isSelectedFeatureAdded = false;

		var style = JSON.parse(DrawControlHandler.getFeatureDefaultStyleAsString());

		console.log("L.Control.NativeDraw.loadFeatures: " + featuresAsString);
		console.log("L.Control.NativeDraw.loadFeatures: " + selectedFeatureAsString);

		for (var i = 0; i < features.features.length; i++)
		{
			var feature = features.features[i];

			if (selectedFeature.hasOwnProperty("id") && selectedFeature.hasOwnProperty("geometry") && (feature.id === selectedFeature.id))
			{
				feature = selectedFeature;
				isSelectedFeatureAdded = true;
			}

			this._addFeature(feature, style);
		}

		if (selectedFeature.hasOwnProperty("id") && selectedFeature.hasOwnProperty("geometry"))
		{
			if (!isSelectedFeatureAdded)
			{
				this._addFeature(selectedFeature, style);
			}
		}

        if (this._features.getLayers().length > 0 && fitBounds)
        {
            this._map.fitBounds(this._features.getBounds());
        }

		this._moveRefreshPosition();
	},

	deleteFeature: function(featureId) {
	    this._clearEvents();
        this._markers.clearLayers();
        this._map.addLayer(this._markers);

        this._mode = null;
        this._currentFeature = null;

        this._features.eachLayer(function(feature)
        {
            if (feature.options.key === featureId)
            {
                console.log("L.Control.NativeDraw.deleteFeature '" + featureId + "'");

                DrawControlHandler.onDeletingFeature(true);

                this._features.removeLayer(feature);
                this._moveRefreshPosition();
            }
        }, this);
	},

	addLatLngToFeature: function(lat, lng)
	{
		var latlng = new L.LatLng(lat, lng);

		// marker
		if (this._currentFeature instanceof L.Marker)
		{
			this._currentFeature.setLatLng(latlng);
			this._features.addLayer(this._currentFeature);

			this._saveFeature();
			this.endDrawFeature();
		}

		// polyline and polygon
		if (this._currentFeature instanceof L.Polyline)
		{
			this._currentFeature.addLatLng(latlng);

			// remove the last "ghost" marker
			if ((this._currentFeature.getLatLngs().length > 2) && (this._currentFeature instanceof L.Polygon))
			{
				var markers = this._markers.getLayers();
				var lastMarker = markers[markers.length - 1];

				if (typeof lastMarker.options.index === "undefined")
				{
					this._markers.removeLayer(lastMarker);
				}
			}

			this._addMarker(latlng, (this._currentFeature.getLatLngs().length - 1));

			// adding the last "ghost" marker between the last and the first latlng of this feature
			if ((this._currentFeature.getLatLngs().length > 2) && (this._currentFeature instanceof L.Polygon))
			{
				var markers = this._markers.getLayers();

				var lastMarker = this._markers.getLayer(this._markers.getLayerId(markers[markers.length - 1]));
				var firstMarker = this._markers.getLayer(this._markers.getLayerId(markers[0]));
				var ghostMarker = this._createGhostMarker(lastMarker, firstMarker);

				this._markers.addLayer(ghostMarker);
			}

			this._saveFeature();
		}
	},

	/**
	 * Adds given feature to FeatureGroup _features.
	 */
	_addFeature: function(feature, style)
	{
		if (feature.hasOwnProperty("id") && feature.hasOwnProperty("geometry"))
		{
			if (feature.geometry.type === "Point")
			{
				var featureMarker = this._createFeatureMarker();
				featureMarker.options.key = feature.id;
				featureMarker.setLatLng(new L.LatLng(feature.geometry.coordinates[1], feature.geometry.coordinates[0]));

				this._features.addLayer(featureMarker);
			}

			if (feature.geometry.type === "LineString")
			{
				var polyline = new L.Polyline([], style);
				polyline.options.clickable = false;
				polyline.options.fill = false;
				polyline.options.key = feature.id;

				for (var j = 0; j < feature.geometry.coordinates.length; j++)
				{
					polyline.addLatLng(new L.LatLng(feature.geometry.coordinates[j][1], feature.geometry.coordinates[j][0]));
				}

				this._features.addLayer(polyline);
			}

			if (feature.geometry.type === "Polygon")
			{
				var polygon = new L.Polygon([], style);
				polygon.options.clickable = false;
				polygon.options.key = feature.id;

				for (var j = 0; j < feature.geometry.coordinates[0].length; j++)
				{
				    // add the last LatLng only if different from the first LatLng added to this Polygon
				    if (j == (feature.geometry.coordinates[0].length - 1))
				    {
				        var lastLatLng = new L.LatLng(feature.geometry.coordinates[0][j][1], feature.geometry.coordinates[0][j][0]);

				        if (!polygon.getLatLngs()[0].equals(lastLatLng))
				        {
				            polygon.addLatLng(lastLatLng);
				        }
				    }
				    else
				    {
				        polygon.addLatLng(new L.LatLng(feature.geometry.coordinates[0][j][1], feature.geometry.coordinates[0][j][0]));
				    }
				}

				this._features.addLayer(polygon);
			}
		}
	},

	_saveFeature: function()
	{
	    var exported = this._exportSelectedFeature(this._currentFeature);
        DrawControlHandler.addOrUpdateFeature(JSON.stringify(exported));
        //this._debugMarkers();

        return;
	},

	_onZoomEvent: function(e)
	{
		DrawControlHandler.setZoom(this._map.getZoom());

		if ((DrawControlHandler.getMinimumZoomPointing() > this._map.getZoom()) && (this._currentFeature !== null))
		{
			if (this._mode === null)
			{
				this.endDrawFeature();
			}
			else
			{
				this.endUpdateFeature();
			}
		}
	},

	_onMapClickEvent: function(e)
	{
		console.log("L.Control.NativeDraw._onMapClickEvent " + JSON.stringify(e.latlng));

		if (this._currentFeature === null)
		{
			DrawControlHandler.findFeature(this._mode, e.latlng.lat, e.latlng.lng);
		}
		else
		{
			this.addLatLngToFeature(e.latlng.lat, e.latlng.lng);
		}
	},

	_onFeaturesLayerRemoveEvent: function(e)
	{
		console.log("L.Control.NativeDraw._onFeaturesLayerRemoveEvent : " + e.layer.options.key);

		DrawControlHandler.deleteFeature(e.layer.options.key);
		DrawControlHandler.onDeletingFeature(false);
	},

	/**
	 * Clears all used events.
	 */
	_clearEvents: function()
	{
		this._map.off("click", this._onMapClickEvent, this);
		this._features.off("layerremove", this._onFeaturesLayerRemoveEvent, this);

		this._features.eachLayer(function(feature)
		{
			feature.off("click", this._onFeatureClickEvent, this);

			if (feature instanceof L.Marker)
			{
				feature.setOpacity(1);
				feature.dragging.disable();
				feature.off("dragend", this._onFeatureMarkerDragEndEvent, this);
			}

			if (feature instanceof L.Polyline)
			{
				feature.setStyle(
				{
					opacity: 1
				});
			}
		}, this);

		this._markers.eachLayer(function(marker)
		{
			marker.off("click", this._onMarkerClickEvent, this);
			marker.off("drag", this._onMarkerDragEvent, this);
			marker.off("dragend", this._onMarkerDragEndEvent, this);
		}, this);
	},

	_createFeatureMarker: function()
	{
		var featureMarker = new L.Marker(null,
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
			})
		});

		return featureMarker;
	},

	/**
	 * Creates a marker.
	 * @return instance of L.Marker
	 */
	_createMarker: function(latlng, options)
	{
		var marker = new L.Marker(latlng,
		{
			icon: new L.DivIcon(
			{
				iconSize: new L.Point(20, 20),
				className: "leaflet-div-icon leaflet-editing-icon",
				zIndexOffset: 100
			}),
			draggable: true
		});

		L.setOptions(marker, options);

		marker.on("click", this._onMarkerClickEvent, this);
		marker.on("drag", this._onMarkerDragEvent, this);
		marker.on("dragend", this._onMarkerDragEndEvent, this);

		return marker;
	},

	/**
	 * Creates a "ghost" marker.
	 * @param marker1 instance of L.Marker as first marker
	 * @param marker2 instance of L.Marker as second marker
	 * @return instance of L.Marker
	 * @see _createMarker
	 */
	_createGhostMarker: function(marker1, marker2)
	{
		var ghostMarker = new L.Marker(this._getMiddleLatLng(marker1.getLatLng(), marker2.getLatLng()),
		{
			icon: new L.DivIcon(
			{
				iconSize: new L.Point(20, 20),
				className: "leaflet-div-icon leaflet-editing-icon",
				zIndexOffset: 100
			}),
			draggable: true,
			opacity: 0.5
		});

		ghostMarker.on("click", this._onMarkerClickEvent, this);
		ghostMarker.on("drag", this._onMarkerDragEvent, this);
		ghostMarker.on("dragend", this._onMarkerDragEndEvent, this);

		// sets the previous and next markers from the first marker and the second marker
		marker1.options.next = this._markers.getLayerId(ghostMarker);
		ghostMarker.options.prev = this._markers.getLayerId(marker1);
		ghostMarker.options.next = this._markers.getLayerId(marker2);
		marker2.options.prev = this._markers.getLayerId(ghostMarker);

		return ghostMarker;
	},

	/**
	 * Creates all associated markers from all L.LatLng of the selected feature (L.Polyline or L.Polygon).
	 * @see _addMarker
	 */
	_createMarkers: function()
	{
		console.log("L.Control.NativeDraw._createMarkers");

		if (this._currentFeature !== null)
		{
			this._markers.clearLayers();

			if (this._currentFeature instanceof L.Polyline)
			{
				var latlngs = this._currentFeature.getLatLngs();

				for (var i = 0; i < latlngs.length; i++)
				{
					this._addMarker(latlngs[i], i);
				}

				// adding the last "ghost" marker between the last and the first latlng of this feature
				if (this._currentFeature instanceof L.Polygon)
				{
					var markers = this._markers.getLayers();

					var lastMarker = this._markers.getLayer(this._markers.getLayerId(markers[markers.length - 1]));
					var firstMarker = this._markers.getLayer(this._markers.getLayerId(markers[0]));
					var ghostMarker = this._createGhostMarker(lastMarker, firstMarker);

					this._markers.addLayer(ghostMarker);
				}
			}
		}
	},

	/**
	 * Adds a "real" marker (i.e. has 'index' option defined) and optionally adds a "ghost" marker between the last marker and this marker
	 * @param latlng the marker position
	 * @param index the index to use for this marker
	 * @see _createMarker, _createGhostMarker
	 */
	_addMarker: function(latlng, index)
	{
		console.log("L.Control.NativeDraw._addMarker : adding marker " + index);

		var markers = this._markers.getLayers();

		if (markers.length > 0)
		{
			var lastMarker = this._markers.getLayer(this._markers.getLayerId(markers[markers.length - 1]));
			var newMarker = this._createMarker(latlng, {index: index});
			var ghostMarker = this._createGhostMarker(lastMarker, newMarker);

			this._markers.addLayer(lastMarker);
			this._markers.addLayer(ghostMarker);
			this._markers.addLayer(newMarker);
		}
		else
		{
			this._markers.addLayer(this._createMarker(latlng, {index: index}));
		}
	},

	/**
	 * listener used with 'click' event occurred to L.Marker instance.
	 * @param e event as MouseEvent instance
	 */
	_onMarkerClickEvent: function(e)
	{
		L.DomEvent.stopPropagation(e);

		if (this._currentFeature !== null)
		{
			var marker = e.target;

			// only for "real" marker
			if (typeof marker.options.index !== "undefined")
			{
				if (this._currentFeature instanceof L.Polyline)
				{
					this._currentFeature.spliceLatLngs(marker.options.index, 1);
					this._createMarkers();

					this._saveFeature();
				}
			}
		}
	},

	/**
	 * listener used with 'drag' event occurred to L.Marker instance.
	 * @param e event as Event instance
	 */
	_onMarkerDragEvent: function(e)
	{
		L.DomEvent.stopPropagation(e);

		if (this._currentFeature !== null)
		{
			var marker = e.target;
			marker.setOpacity(0.5);

			if (this._currentFeature instanceof L.Polyline)
			{
				if (typeof marker.options.index !== "undefined")
				{
					//console.log("L.Control.NativeDraw._onMarkerDragEvent : moving real marker " + this._markers.getLayerId(marker));

					// update the corresponding latlng in currentFeature
					this._currentFeature.spliceLatLngs(marker.options.index, 1, marker.getLatLng());
					this._currentFeature.redraw();

					// "real" marker : update "ghost" markers position
					if ((typeof marker.options.next === "undefined") || ((typeof marker.options.next !== "undefined") && (marker.options.index !== this._markers.getLayer(marker.options.next).options.index)))
					{
						if (typeof marker.options.prev !== "undefined")
						{
							var previousGhostMarker = this._markers.getLayer(marker.options.prev);
							var previousRealMaker = this._markers.getLayer(previousGhostMarker.options.prev);
							previousGhostMarker.setLatLng(this._getMiddleLatLng(previousRealMaker.getLatLng(), marker.getLatLng()));
						}

						if (typeof marker.options.next !== "undefined")
						{
							var nextGhostMarker = this._markers.getLayer(marker.options.next);
							var nextRealMaker = this._markers.getLayer(nextGhostMarker.options.next);
							nextGhostMarker.setLatLng(this._getMiddleLatLng(marker.getLatLng(), nextRealMaker.getLatLng()));
						}
					}
				}
				else
				{
					//console.log("L.Control.NativeDraw._onMarkerDragEvent : moving ghost marker " + this._markers.getLayerId(marker));

					// "ghost" marker become "real"
					marker.options.index = this._markers.getLayer(marker.options.next).options.index;
					// update this marker
					this._markers.addLayer(marker);

					// add a new latlng in currentFeature
					this._currentFeature.spliceLatLngs(marker.options.index, 0, marker.getLatLng());
					this._currentFeature.redraw();
				}

				//this._debugMarkers();
			}
		}
	},

	/**
	 * listener used with 'dragend' event occurred to L.Marker instance.
	 * @param e event as Event instance
	 */
	_onMarkerDragEndEvent: function(e)
	{
		console.log("L.Control.NativeDraw._onMarkerDragEndEvent");

		var marker = e.target;
		marker.setOpacity(1);

		// recreate all markers if this marker was a "ghost" marker
		if ((typeof marker.options.next !== "undefined") && (marker.options.index === this._markers.getLayer(marker.options.next).options.index))
		{
			this._createMarkers();
		}

		this._saveFeature();
	},

	_onFeatureClickEvent: function(e)
	{
		console.log("L.Control.NativeDraw._onFeatureClickEvent " + e.target.options.key);

		this._currentFeature = e.target;

		if ((this._mode !== null) && (this._mode === "edit"))
		{
			DrawControlHandler.onEditingFeature(true);

			var style = JSON.parse(DrawControlHandler.getFeatureEditStyleAsString());

			// marker
			if (this._currentFeature instanceof L.Marker)
			{
				this._currentFeature.setOpacity(0.5);
				this._currentFeature.dragging.enable();
				this._currentFeature.on("dragend", this._onFeatureMarkerDragEndEvent, this);
			}

			// polyline and polygon
			if (this._currentFeature instanceof L.Polyline)
			{
				this._map.on("click", this._onMapClickEvent, this);

				// adding a specific style for a polyline
				if (!(this._currentFeature instanceof L.Polygon))
				{
					style.fill = false;
				}

				this._currentFeature.setStyle(style);

				this._createMarkers();
				//this._debugMarkers();
			}
		}
		else if ((this._mode !== null) && (this._mode === "delete"))
		{
			DrawControlHandler.onDeletingFeature(true);

			this._features.removeLayer(this._currentFeature);
			this._currentFeature = null;
			this._moveRefreshPosition();
		}
	},

	_onFeatureMarkerDragEndEvent: function(e)
	{
		this._currentFeature = e.target;
		this._saveFeature();
		this._features.addLayer(this._currentFeature);
	},

	/**
	 * Gets the middle point between two given points.
	 * @param latlng1 first L.LatLng instance
	 * @param latlng2 second L.LatLng instance
	 */
	_getMiddleLatLng: function (latlng1, latlng2)
	{
		var p1 = this._map.latLngToLayerPoint(latlng1);
		var p2 = this._map.latLngToLayerPoint(latlng2);

		return this._map.layerPointToLatLng(p1.add(p2).divideBy(2));
	},

	_exportSelectedFeature: function(featureToExport)
	{
		var feature = {};
		feature.key = featureToExport.options.key;

		if (featureToExport instanceof L.Marker)
		{
			feature.type = "Point";
			feature.coordinates = featureToExport.getLatLng();
		}

		if (featureToExport instanceof L.Polyline)
		{
			feature.type = "LineString";
			feature.coordinates = featureToExport.getLatLngs();
		}

		if (featureToExport instanceof L.Polygon)
		{
			feature.type = "Polygon";
			feature.coordinates = featureToExport.getLatLngs();
		}

		return feature;
	},

	_isEmptyObject: function(obj)
	{
		for (var prop in obj)
		{
			if (obj.hasOwnProperty(prop)) return false;
		}

		return true;
	},

	_moveRefreshPosition: function()
	{
		var currentCenterPoint = this._map.latLngToContainerPoint(this._map.getCenter());
		var refreshCenter = this._map.containerPointToLatLng(currentCenterPoint);
		this._map.setView(refreshCenter, this._map.getZoom());
		this._map.setView(this._map.getCenter(), this._map.getZoom());
	},

	_debugMarkers: function()
	{
		this._markers.eachLayer(function(marker)
		{
			if (typeof marker.options.index !== "undefined")
			{
				if ((typeof marker.options.prev !== "undefined") && (typeof marker.options.next !== "undefined"))
				{
					console.log("L.Control.NativeDraw._debugMarkers : real marker [id: " + this._markers.getLayerId(marker) + ", index:" + marker.options.index + ", prev:" + marker.options.prev + ", next:" + marker.options.next + "]");
				}
				else if (typeof marker.options.prev !== "undefined")
				{
					console.log("L.Control.NativeDraw._debugMarkers : real marker [id: " + this._markers.getLayerId(marker) + ", index:" + marker.options.index + ", prev:" + marker.options.prev + ", next:null]");
				}
				else if (typeof marker.options.next !== "undefined")
				{
					console.log("L.Control.NativeDraw._debugMarkers : real marker [id: " + this._markers.getLayerId(marker) + ", index:" + marker.options.index + ", prev:null, next:" + marker.options.next + "]");
				}
				else
				{
					console.log("L.Control.NativeDraw._debugMarkers : real marker [id: " + this._markers.getLayerId(marker) + ", index:" + marker.options.index + ", prev:null, next:null]");
				}
			}
			else
			{
				console.log("L.Control.NativeDraw._debugMarkers : ghost marker [id: " + this._markers.getLayerId(marker) + ", prev:" + marker.options.prev + ", next:" + marker.options.next + "]");
			}
		}, this);
	}
});