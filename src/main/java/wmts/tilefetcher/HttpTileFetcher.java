package wmts.tilefetcher;

import wmts.Layer;
import wmts.Tile;
import wmts.TileSource;
import wmts.WMTSException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public final class HttpTileFetcher implements TileFetcher {
    private final HttpClient client;
    private final TileSource tileSource;

    public HttpTileFetcher(TileSource tileSource) {
        this.tileSource = tileSource;
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public BufferedImage fetch(Tile tile, Layer layer) throws WMTSException {
        HttpRequest request = buildRequest(tile, layer);
        HttpResponse<byte[]> response = null;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException | InterruptedException e) {
            throw new WMTSException("HTTP Exception"+e);
        }

        if (response.statusCode() != 200)
            throw new WMTSException("HTTP Exception. Status code: "+response.statusCode());

        return decodeImage(response.body());
    }

    private HttpRequest buildRequest(Tile tile, Layer layer) {
        URI uri = URI.create(tileSource.generateUrl(layer.name(), tile.z(), tile.x(), tile.y()));
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri);

        String[] headers = tileSource.getHeaders();
        if (headers != null && headers.length > 0)
            builder.headers(headers);

        return builder.build();
    }

    private BufferedImage decodeImage(byte[] bytes) throws WMTSException {
        BufferedImage img = null;

        try {
            img = ImageIO.read(new ByteArrayInputStream(bytes));

            if (img == null) {
                System.err.println(new String(bytes, StandardCharsets.UTF_8));
                throw new WMTSException("Decoding image error");
            }
        } catch (IOException e) {
            System.err.println(new String(bytes, StandardCharsets.UTF_8));
            throw new WMTSException("Decoding image error");
        }

        return img;
    }
}