L.LimitedZoomMap = L.Map.extend(
{
	options:
	{
		validZoomLevels: null,
	},

	// constructor
	initialize: function(id, options)
	{
		console.log("LimitedZoomMap.initialize");
		
		// Parameter sanitization
		if (options && options.validZoomLevels)
		{
			if (typeof options.validZoomLevels == "object")
			{
				options.validZoomLevels.sort(function(a, b)
				{
					return a - b;
				});
				// Also check consistency with minZoom/maxZoom ?
			}
			else
			{
				delete options["validZoomLevels"];
			}
		}
		
		return L.Map.prototype.initialize.call(this, id, options);
	},

	_limitZoom: function(zoom)
	{
		console.log("LimitedZoomMap._limitZoom [zoom : " + zoom + ", getZoom : " + this.getZoom() + "]");
		
		if (this.options.validZoomLevels)
		{
			var cursor;
			
			if (zoom > this.getZoom())
			{
				for (cursor = 0; cursor < this.options.validZoomLevels.length - 1; cursor++)
				{
					if (this.options.validZoomLevels[cursor] >= zoom)
					{
						break;
					}
				}
			}
			else
			{
				for (cursor = this.options.validZoomLevels.length - 1; cursor > 0; cursor--)
				{
					if (this.options.validZoomLevels[cursor] <= zoom)
					{
						break;
					}
				}
			}
			
			console.log("LimitedZoomMap._limitZoom [validZoomLevel : " + this.options.validZoomLevels[cursor] + "]");
			
			return L.Map.prototype._limitZoom.call(this, this.options.validZoomLevels[cursor]);
		}
		else
		{
			return L.Map.prototype._limitZoom.call(this, zoom);
		}
	}
});
