L.Control.CenterPosition = L.Control.extend(
{
	onAdd: function(map)
	{
		this._map = map;
		
		var container = L.DomUtil.create("div", "centerPositionControlHandler");
		
		map.on("moveend", this._onCenterEvent, this);
		
		CenterPositionControlHandler.setControlInitialized();
		
		return container;
	},
	
	onRemove: function(map)
	{
		map.off("moveend", this._onCenterEvent, this);
	},
	
	setCenter: function(latitude, longitude)
	{
		this._map.setView([latitude, longitude], this._map.getZoom());
	},
	
	_onCenterEvent: function(e)
	{
		CenterPositionControlHandler.setCenter(this._map.getCenter().lat, this._map.getCenter().lng);
	}
});