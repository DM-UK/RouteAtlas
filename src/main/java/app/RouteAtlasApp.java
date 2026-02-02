package app;

import ui.controller.CompileAtlasAction;
import ui.controller.CreateAtlasAction;
import ui.controller.MapTabBinder;
import ui.controller.MapUpdater;
import ordnancesurvey.OrdnanceSurveyRouteDownloader;
import render.AtlasImageCreator;
import routeatlas.RouteAtlas;
import routeatlas.RouteAtlasCompiler;
import ui.panes.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class RouteAtlasApp extends JFrame implements CompilationFeatureToggle, RouteAtlasStateListener{
    public final static Path BASE_DIR = Paths.get(System.getProperty("user.home")).resolve("RouteAtlas");
    private final static Path PDF_DIR = BASE_DIR.resolve("pdfs");
    private final static Path TILE_CACHE_DIR = BASE_DIR.resolve("tile_cache");

    private final OrdnanceSurveyRouteDownloader downloader = new OrdnanceSurveyRouteDownloader();
    private final AtlasSetupPane atlasSetupPane = new AtlasSetupPane();
    private final PageSetupPane overviewPageSetupPane = new PageSetupPane();
    private final PageSetupPane sectionPageSetupPane = new PageSetupPane();
    private final CompilationPane compilationPane = new CompilationPane();
    private final MapDisplayPane mapDisplayPane = new MapDisplayPane();
    private final FooterPane footerPane = new FooterPane();

    private final RouteAtlasState routeAtlasState = new RouteAtlasState();
    private final AtlasImageCreator atlasImageCreator = new AtlasImageCreator(TILE_CACHE_DIR, overviewPageSetupPane, sectionPageSetupPane, mapDisplayPane.getProgressListener());

    private final RouteAtlasCompiler routeAtlasCompiler = new RouteAtlasCompiler(PDF_DIR, atlasImageCreator, compilationPane.getProgressBar());
    private final MapUpdater mapUpdater = new MapUpdater(routeAtlasState, footerPane, atlasImageCreator);

    private final CompileAtlasAction compileAtlasAction = new CompileAtlasAction(routeAtlasCompiler, routeAtlasState, this);
    private final CreateAtlasAction createAtlasAction = new CreateAtlasAction(downloader, atlasSetupPane, routeAtlasState, this);

    public RouteAtlasApp(){
        super("Route Atlas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        JPanel setupPane = new JPanel();
        setupPane.setLayout(new BoxLayout(setupPane, BoxLayout.Y_AXIS));
        setupPane.add(atlasSetupPane);
        TabbedAccordionPanel pageSetupTab = new TabbedAccordionPanel();
        setupPane.add(pageSetupTab);
        setupPane.add(compilationPane);
        pageSetupTab.add("Overview", overviewPageSetupPane);
        pageSetupTab.add("Section", sectionPageSetupPane);


        JPanel setupPaneWrapper = new JPanel();
        setupPaneWrapper.add(setupPane);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mapDisplayPane, BorderLayout.CENTER);
        mainPanel.add(footerPane, BorderLayout.SOUTH);

        add(setupPaneWrapper, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);
        mapDisplayPane.setPreferredSize(new Dimension(800,800));
        pack();
        setLocationRelativeTo(null);
        setActions();

        enableCompilation(false);
        routeAtlasState.addListener(this);
        new MapTabBinder(routeAtlasState, pageSetupTab.getModel());

        setRendererDefaults();
    }

    private void setRendererDefaults() {
        ((ElevationSetupPane)overviewPageSetupPane.getElevationSettings()).setTickSpacing(10, 30);
        ((TileLayerSetupPane)overviewPageSetupPane.getTileLayer()).setProvider(1);
        ((TileLayerSetupPane)overviewPageSetupPane.getTileLayer()).setZoom(10);
    }

    private void setActions() {
        atlasSetupPane.getButton().setAction(createAtlasAction);
        compilationPane.getCompilationButton().setAction(compileAtlasAction);
        compilationPane.setCancelAction(mapUpdater::cancelMapRequest);
        mapDisplayPane.setCancelAction(mapUpdater::cancelMapRequest);

        mapDisplayPane.setSelectPreviousMapAction(routeAtlasState::selectPreviousMapIndex);
        mapDisplayPane.setSelectNextMapAction(routeAtlasState::selectNextMapIndex);
        overviewPageSetupPane.setPropertyChangeListener(evt -> mapUpdater.requestMap());
        sectionPageSetupPane.setPropertyChangeListener(evt -> mapUpdater.requestMap());
    }

    @Override
    public void enableAtlasCreation(boolean enable) {
        atlasSetupPane.getButton().setEnabled(enable);
    }

    @Override
    public void enableCompilation(boolean enable) {
        compilationPane.getCompilationButton().setEnabled(enable);
    }

    @Override
    public void onAtlasChanged(RouteAtlas newAtlas) {
    }

    @Override
    public void onMapSelectionIndexChanged(int newIndex) {
        mapUpdater.requestMap();
    }

    public static void main(String[] args) {
        String FILE_NAME = "providers.xml";

        //check if file "providers.xml" exists at BASE_DIR, otherwise copy it from resources folder to BASE_DIR
        try {
            Files.createDirectories(BASE_DIR);

            Path targetFile = BASE_DIR.resolve(FILE_NAME);

            if (Files.notExists(targetFile)) {
                copyFromResources(FILE_NAME, targetFile);
                System.out.println(FILE_NAME + " copied to " + BASE_DIR);
            } else {
                System.out.println(FILE_NAME + " already exists in " + BASE_DIR);
            }

            WebMapProviders.CONFIG_FILE_PATH = targetFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(RouteAtlasApp::new);
    }

    private static void copyFromResources(String resourceName, Path target) throws IOException {
        try (InputStream in = RouteAtlasApp.class.getClassLoader().getResourceAsStream(resourceName))
        {
            if (in == null)
                throw new IOException("Resource not found: " + resourceName);

            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static class TabbedAccordionPanel extends JTabbedPane {
        public TabbedAccordionPanel() {
            setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        }

        @Override
        public void addTab(String title, Component component) {
            JPanel container = new JPanel(new BorderLayout());
            container.add(BorderLayout.NORTH, component);
            super.addTab(title, container);
        }
    }
}
