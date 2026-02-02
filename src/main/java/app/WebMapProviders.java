package app;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import wmts.Layer;
import wmts.TileSource;
import wmts.TilingScheme;
import wmts.WebMapTileService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WebMapProviders {
    public static Path CONFIG_FILE_PATH;

    public static WebMapTileService[]  getAll(){
        try {

            return loadFromFile2(CONFIG_FILE_PATH);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static WebMapTileService[] loadFromFile2(Path filePath)
            throws IOException, ParserConfigurationException, SAXException {

        List<WebMapTileService> services = new ArrayList<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(filePath.toFile());
        doc.getDocumentElement().normalize();

        NodeList wmtsNodes = doc.getElementsByTagName("wmts");
        for (int i = 0; i < wmtsNodes.getLength(); i++) {
            Element wmtsElem = (Element) wmtsNodes.item(i);

            Element tileSourceElem = getFirst(wmtsElem, "tileSource");
            String tileSourceName = tileSourceElem.getAttribute("name");
            String urlTemplate = tileSourceElem.getAttribute("urlTemplate");
            String apiKey = tileSourceElem.hasAttribute("apiKey") ? tileSourceElem.getAttribute("apiKey") : null;

// headers as array of key, value, key, value ...
            NodeList headerNodes = tileSourceElem.getElementsByTagName("header");
            String[] headers = new String[headerNodes.getLength() * 2];
            for (int h = 0; h < headerNodes.getLength(); h++) {
                Element headerElem = (Element) headerNodes.item(h);
                headers[h * 2] = headerElem.getAttribute("key");
                headers[h * 2 + 1] = headerElem.getAttribute("value");
            }

            TileSource tileSource = new TileSource(tileSourceName, urlTemplate, headers, apiKey);

            // ---- Tiling Scheme ----
            Element tilingSchemeElem = getFirst(wmtsElem, "tilingScheme");
            double originX = Double.parseDouble(tilingSchemeElem.getAttribute("originX"));
            double originY = Double.parseDouble(tilingSchemeElem.getAttribute("originY"));
            double resolution = Double.parseDouble(tilingSchemeElem.getAttribute("resolution"));
            TilingScheme tilingScheme = new TilingScheme(new Point2D.Double(originX, originY), resolution);

            // ---- Layers ----
            List<Layer> layers = new ArrayList<>();
            Element layersElem = getFirst(wmtsElem, "layers");
            if (layersElem != null) {
                NodeList layerNodes = layersElem.getElementsByTagName("layer");
                for (int j = 0; j < layerNodes.getLength(); j++) {
                    Element layerElem = (Element) layerNodes.item(j);
                    String layerName = layerElem.getAttribute("name");
                    int minZoom = Integer.parseInt(layerElem.getAttribute("minZoom"));
                    int maxZoom = Integer.parseInt(layerElem.getAttribute("maxZoom"));
                    layers.add(new Layer(layerName, minZoom, maxZoom));
                }
            }

            // ---- CRS ----
            Element crsElem = getFirst(wmtsElem, "crs");
            String crsCode = (crsElem != null) ? crsElem.getTextContent().trim() : "3857"; // default 3857
            CoordinateReferenceSystem crs = new CRSFactory().createFromName("epsg:" + crsCode);
            Layer[] layerArr = layers.toArray(new Layer[0]);
            // ---- Construct Service ----
            services.add(new WebMapTileService(crs, tileSource, tilingScheme, layerArr, 20));
        }

        return services.toArray(new WebMapTileService[0]);
    }

    // Helper: safely get first child element by tag name
    private static Element getFirst(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        return (list.getLength() > 0) ? (Element) list.item(0) : null;
    }
}
