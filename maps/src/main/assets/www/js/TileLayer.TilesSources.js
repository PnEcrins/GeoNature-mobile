L.TileLayer.TilesSources = L.TileLayer.extend(
{
	includes: L.Mixin.Events,
	
	initialize: function(options)
	{
		console.log("L.TileLayer.TilesSources.initialize");
		
		L.Util.setOptions(this, options);
	},
	
	getTileUrl: function(tilePoint)
	{
		//console.log("tile : [z=" + this._getZoomForUrl() + ", x=" + tilePoint.x + ", y=" + tilePoint.y + "]");
		
		// first loads tile data as Base64 format
		var tileData = MainControlHandler.getTile(this._getZoomForUrl(), tilePoint.x, tilePoint.y);
		
		// then gets the selected tiles layer for this tile
		var format = JSON.parse(MainControlHandler.getMetadata(MainControlHandler.getSelectedLayerName())).format;

		if (format)
		{
			return "data:image/" + format + ";base64," + tileData;
		}
		else
		{
			// assuming that tiles are in png as default format ...
			return "data:image/png;base64," + tileData;
		}
	}
});