package com.makina.ecrins.maps.content;

import android.support.annotation.NonNull;

import com.makina.ecrins.maps.settings.LayerSettings;

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

    public TilesLayerDataSourceFactory(@NonNull final File pTilesSourcePath) {
        this.mTilesSourcePath = pTilesSourcePath;
    }

    /**
     * Gets the most appropriate {@link ITilesLayerDataSource} according to the given {@link LayerSettings}.
     *
     * @param pLayerSettings {@link LayerSettings} instance to use to load tiles
     *
     * @return the best {@link ITilesLayerDataSource} implementation for the given {@link LayerSettings}
     * or thrown an {@link UnsupportedOperationException} if no implementation cannot be found
     *
     * @throws IOException
     */
    @NonNull
    public ITilesLayerDataSource getTilesLayerDataSource(@NonNull final LayerSettings pLayerSettings) throws
                                                                                                      UnsupportedOperationException,
                                                                                                      IOException {
        switch (pLayerSettings.getSource()) {
            case LayerSettings.SOURCE_MBTILES:
                // try to load MBTiles data source implementation
                return new MBTilesDataSource(mTilesSourcePath,
                                             pLayerSettings);
            case LayerSettings.SOURCE_MBTILES_SPLIT:
                return new MBTilesSplitDataSource(mTilesSourcePath,
                                                  pLayerSettings);
            case LayerSettings.SOURCE_DIR:
                return new FileDataSource(mTilesSourcePath,
                                          pLayerSettings);
            case LayerSettings.SOURCE_HTTP:
                throw new UnsupportedOperationException("no implementation found for '" + pLayerSettings.getName() + "' (source: " + pLayerSettings.getSource() + ")");
            default:
                throw new UnsupportedOperationException("no implementation found for '" + pLayerSettings.getName() + "' (source: " + pLayerSettings.getSource() + ")");
        }
    }
}
