L.Control.SwitchLayers = L.Control.extend(
{
	onAdd: function(map)
	{
		this._map = map;
		
		var container = L.DomUtil.create("div", "switchLayerControlHandler");
		
		map.on("zoomend", this._onZoomEvent, this);
		
		SwitchLayersControlHandler.setControlInitialized();
		
		return container;
	},
	
	onRemove: function(map)
	{
		map.off("zoomend", this._onZoomEvent, this);
	},
	
	refreshMap: function()
	{
		this._map.setView(this._map.getCenter(), this._map.getZoom(), true);
	},
	
	_onZoomEvent: function(e)
	{
		SwitchLayersControlHandler.setZoom(this._map.getZoom());
	}
});