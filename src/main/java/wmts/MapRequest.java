package wmts;

import wmts.bounds.Bounds;

public record MapRequest(WebMapTileService wmts, Bounds bounds, Layer layer, int zoom, ProgressListener progress) {
}
