package routeatlas;

import org.locationtech.proj4j.CoordinateReferenceSystem;
import route.Route;
import route.RouteUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Represents a collection of map sections through a route, including an overview map which is the bounding box of all the sections bounds. */
public class RouteAtlas {
    private final Route route;
    private final List<MapPage> allPages;
    private final MapPage overview;
    private final List<MapPage> sections;

    public RouteAtlas(Route route, List<MapPage> maps) {
        this.route = route;
        this.allPages = maps;
        overview = maps.get(0);
        sections = maps.subList(1, maps.size());
    }

    public Route getRoute() {
        return route;
    }

    /** Returns a list of all map pages. The overview map (index == 0) followed by each section. */
    public List<MapPage> getAllPages() {
        return allPages;
    }

    public MapPage getOverviewPage() {
        return overview;
    }

    public List<MapPage> getSectionPages() {
        return sections;
    }

    /** Returns a new Route in the specified crs. */
    public RouteAtlas convertCRS(CoordinateReferenceSystem crs) {
        Route transformedRoute = RouteUtils.convertCRS(route, crs);
        List<MapPage> transformedPages = new ArrayList<>();

        for (MapPage map: getAllPages()){
            MapPage converted = map.transform(crs);
            transformedPages.add(converted);
        }

        return new RouteAtlas(transformedRoute, transformedPages);
    }
}
