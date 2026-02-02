package ordnancesurvey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import route.Route;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Downloads routes from the Ordnance Survey API and converts to a RouteAtlas Route. */
public class OrdnanceSurveyRouteDownloader {
    /** WGS84 format the Ordnance Survey API should be in */
    private final static CoordinateReferenceSystem WGS84 = new CRSFactory().createFromName("EPSG:4326");
    /** Cache to avoid repeat api calls */
    private final Map<String, Route> cache = new ConcurrentHashMap<>();

    public Route downloadRoute(String routeId) throws IOException {
        // check cache first
        Route cached = cache.get(routeId);
        if (cached != null)
            return cached;

        // otherwise, download and cache the result
        OrdnanceSurveyRoute route = new OrdnanceSurveyRouteClient().downloadRoute(routeId);
        Route converted = convertRoute(route, routeId);

        cache.put(routeId, converted);
        return converted;
    }

    //convert from the OrdnanceSurveyRoute/JSON object to a RouteAtlas Route
    private Route convertRoute(OrdnanceSurveyRoute osRoute, String routeid) {
        Route route = new Route(routeid, WGS84);

        for (OrdnanceSurveyRoute.OrdnanceSurveyWayPoint osWaypoint: osRoute.getResult()) {
            Point2D.Double p = new Point2D.Double(osWaypoint.getLon(), osWaypoint.getLat());
            route.addWayPoint(p, osWaypoint.getHeight(), osWaypoint.getDistance());
        }

        return route;
    }

    public static class OrdnanceSurveyRouteClient {
        // reusable HTTP client for API calls
        private final HttpClient httpClient;

        // jackson mapper for JSON to OrdnanceSurveyRoute
        private final ObjectMapper objectMapper;

        public OrdnanceSurveyRouteClient() {
            this.httpClient = HttpClient.newHttpClient();
            this.objectMapper = new ObjectMapper();
        }

        public OrdnanceSurveyRoute downloadRoute(String routeId) throws IOException {
            String endpoint = String.format(
                    "https://consumerplatform.ordnancesurvey.co.uk/elevation-api/routes/%s/elevations?stream=true",
                    routeId
            );

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .headers("Apikey", "aF8lvsvbprKw3DKi7GREoljqrGYNIjh2")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200)
                    throw new IOException("Unexpected HTTP status: " + response.statusCode());

                return objectMapper.readValue(response.body(), OrdnanceSurveyRoute.class);
            } catch (JsonProcessingException e) {
                throw new IOException("Failed to parse route JSON");
            }
            catch (InterruptedException e) {
                throw new IOException("Thread interrupted while downloading route");
            }
        }
    }
}
