
# RouteAtlas
RouteAtlas is a Java map image creation application designed for creating a printable PDF containing all map sections of a given route.



https://github.com/user-attachments/assets/62eeaa8b-45f6-4cbe-b7b3-599b4eba10a2



### Route Format
Currently, route information is sourced from an Ordnance Survey route. An OS route ID should be extracted from the route’s URL, for example: (ID=1)  https://explore.osmaps.com/route/1

### Atlas Creation
Segmentation of the route into its section pages is based on paper dimensions/map scale and computed by the PageFit algorithm.

### Map Tiles
A basic implementation of the WMTS protocol allows tile layers at specified zoom levels to be retrieved from different map servers and map projections. The providers.xml file allows additional servers to be configured in the provided format. 

**Note: Some map servers (eg. OS API) may need an API key to be added in the providers.xml file.**

<img width="2098" height="1470" alt="different_tiles_resized" src="https://github.com/user-attachments/assets/dfcdcb14-6430-40d9-9d5a-79fa44d5c6d8" />

### Overview Page
An overview of all the map sections is created on the first page of the PDF.
<img width="720" height="720" alt="overview_3_thumbnail_720x720" src="https://github.com/user-attachments/assets/62020a81-591f-4959-944a-ecaff73d350c" />



### Map Rendering
Various configurable render properties can be applied to a map.
#### Distance Markers
**Major interval (1 mile). Minor interval (1/4 mile)**
<img width="525" height="240" alt="mile_markers_resized" src="https://github.com/user-attachments/assets/a151ba0f-22fa-44fa-9f26-3ec8736612ef" />

#### Elevation Profile
<img width="429" height="215" alt="elevation_profile_resized" src="https://github.com/user-attachments/assets/90a66c0c-0f3f-4947-8d98-2cbf48761c10" />







(https://github.com/user-attachments/files/25131070/62511ab1-cc2a-41a5-81cb-a68bca8c5e59.pdf)
