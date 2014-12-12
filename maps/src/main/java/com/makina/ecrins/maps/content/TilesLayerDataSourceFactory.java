package com.makina.ecrins.maps.content;

import com.makina.ecrins.maps.LayerSettings;

import java.io.File;
import java.io.IOException;

/**
 * Creates {@link ITilesLayerDataSource} tiles data sources from various sources
 * (MBTiles format or directory).
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class TilesLayerDataSourceFactory {

    private final File mTilesSourcePath;

    public TilesLayerDataSourceFactory(File pTilesSourcePath) {
        this.mTilesSourcePath = pTilesSourcePath;
    }

    /**
     * Gets the most appropriate {@link ITilesLayerDataSource} according to the given {@link LayerSettings}.
     *
     * @param pLayerSettings {@link LayerSettings} instance to use to load tiles
     * @return the best {@link ITilesLayerDataSource} implementation for the given {@link LayerSettings}
     * or thrown an {@link UnsupportedOperationException} if no implementation cannot be found
     * @throws IOException
     */
    public ITilesLayerDataSource getTilesLayerDataSource(LayerSettings pLayerSettings) throws UnsupportedOperationException, IOException {
        ITilesLayerDataSource tileLayerDataSource;

        if (pLayerSettings.getSource()
                .equals(LayerSettings.SOURCE_MBTILES)) {
            // try to load MBTiles data source implementation
            tileLayerDataSource = new MBTilesDataSource(mTilesSourcePath, pLayerSettings);
        }
        else if (pLayerSettings.getSource()
                .equals(LayerSettings.SOURCE_MBTILES_SPLIT)) {
            tileLayerDataSource = new MBTilesSplitDataSource(mTilesSourcePath, pLayerSettings);
        }
        else if (pLayerSettings.getSource()
                .equals(LayerSettings.SOURCE_DIR)) {
            tileLayerDataSource = new FileDataSource(mTilesSourcePath, pLayerSettings);
        }
        else if (pLayerSettings.getSource()
                .equals(LayerSettings.SOURCE_HTTP)) {
            throw new UnsupportedOperationException("no implementation found for '" + pLayerSettings.getName() + "' (source : " + pLayerSettings.getSource() + ")");
        }
        else {
            throw new UnsupportedOperationException("no implementation found for '" + pLayerSettings.getName() + "' (source : " + pLayerSettings.getSource() + ")");
        }

        return tileLayerDataSource;
    }
}
