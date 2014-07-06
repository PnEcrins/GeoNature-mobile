L.TileLayer.TilesSource = L.TileLayer.extend(
{
	includes: L.Mixin.Events,
	
	_tilesSource : null,
	_base64Prefix : null,
	
	initialize: function(tilesSource, options)
	{
		console.log("L.TileLayer.TilesSource.initialize");
		
		L.Util.setOptions(this, options);
		
		this._tilesSource = tilesSource;
		
		var format = JSON.parse(MenuUnitiesControlHandler.getMetadata(tilesSource)).format;
		
		if (format)
		{
			this._base64Prefix = "data:image/" + format + ";base64,";
		}
		else
		{
			// assuming that tiles are in png as default format ...
			this._base64Prefix = "data:image/png;base64,";
		}
	},
	
	getTileUrl: function(tilePoint)
	{
		//console.log("tile : [z=" + this._getZoomForUrl() + ", x=" + tilePoint.x + ", y=" + tilePoint.y + "]");
		
		return this._base64Prefix + MenuUnitiesControlHandler.getTile(this._tilesSource, this._getZoomForUrl(), tilePoint.x, tilePoint.y);
	}
});