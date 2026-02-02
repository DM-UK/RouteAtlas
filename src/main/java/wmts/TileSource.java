package wmts;

public class TileSource {
    private final String name;
    private final String urlFormatString;
    private final String[] headers;
    private final String apiKey;

    public TileSource(String name, String urlFormatString, String[] headers, String apiKey) {
        this.name = name;
        this.urlFormatString = urlFormatString;
        this.headers = headers;
        this.apiKey = apiKey;
    }

    public String getName() {
        return name;
    }

    public String generateUrl(String layer, int z, int x, int y) {
        if (layer.equals("DEFAULT"))
            return String.format(urlFormatString, z, x, y, apiKey);
        return String.format(urlFormatString, layer, z, x, y, apiKey);
    }

    public String[] getHeaders() {
        return headers;
    }
}
