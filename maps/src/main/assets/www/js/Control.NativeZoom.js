L.Control.NativeZoom = L.Control.extend(
{
	onAdd: function(map)
	{
		this._map = map;
		
		var container = L.DomUtil.create("div", "zoomControlHandler");
		
		map.on("zoomend", this._onZoomEvent, this);
		
		ZoomControlHandler.setControlInitialized();
		
		return container;
	},
	
	onRemove: function(map)
	{
		map.off("zoomend", this._onZoomEvent, this);
	},
	
	zoomIn: function()
	{
		this._map.zoomIn();
	},
	
	zoomOut: function()
	{
		this._map.zoomOut();
	},
	
	_onZoomEvent: function(e)
	{
		ZoomControlHandler.setZoom(this._map.getZoom());
	}
});