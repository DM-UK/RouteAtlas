package wmts;

public record Layer(String name, int minZoom, int maxZoom) {
    @Override
    public String toString() {
        return name;
    }
}