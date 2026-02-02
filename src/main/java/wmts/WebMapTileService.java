package wmts;

import org.locationtech.proj4j.CoordinateReferenceSystem;

public record WebMapTileService(CoordinateReferenceSystem crs, TileSource tileSource, TilingScheme tilingScheme, Layer[] layers, int maxConcurrentConnections){
    public String getName(){
        return tileSource.getName();
    }

    @Override
    public String toString() {
        return getName();
    }
}

