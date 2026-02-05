package app;

import ui.controller.*;
import ordnancesurvey.OrdnanceSurveyRouteDownloader;
import render.AtlasImageCreator;
import routeatlas.RouteAtlasCompiler;
import ui.panes.*;
import ui.swing.TabbedAccordionPanel;
import wmts.WebMapProviders;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class RouteAtlasApp extends JFrame {
    public final static Path BASE_DIR = Paths.get(System.getProperty("user.home")).resolve("RouteAtlas");
    private final static Path PDF_DIR = BASE_DIR.resolve("pdfs");
    private final static Path TILE_CACHE_DIR = BASE_DIR.resolve("tile_cache");

    private final RouteAtlasState routeAtlasState = new RouteAtlasState();

    private final OrdnanceSurveyRouteDownloader downloader = new OrdnanceSurveyRouteDownloader();

    //swing panels
    private final AtlasSetupPane atlasSetupPane = new AtlasSetupPane();
    private final PageSetupPane overviewPageSetupPane = new PageSetupPane();
    private final PageSetupPane sectionPageSetupPane = new PageSetupPane();
    private final CompilationPane compilationPane = new CompilationPane();
    private final MapDisplayPane mapDisplayPane = new MapDisplayPane();
    private final FooterPane footerPane = new FooterPane();
    private final TabbedAccordionPanel pageSetupTab = new TabbedAccordionPanel();

    //atlas image/pdf creation
    private final AtlasImageCreator atlasImageCreator = new AtlasImageCreator(TILE_CACHE_DIR, overviewPageSetupPane, sectionPageSetupPane, mapDisplayPane.getProgressListener());
    private final RouteAtlasCompiler routeAtlasCompiler = new RouteAtlasCompiler(PDF_DIR, atlasImageCreator, compilationPane.getProgressBar());

    //controller/actions
    private final MapTabBinder mapTabBinder = new MapTabBinder(routeAtlasState, pageSetupTab.getModel());
    private final ButtonUpdater buttonUpdater = new ButtonUpdater(atlasSetupPane.getButton(), compilationPane.getCompilationButton());
    private final MapUpdater mapUpdater = new MapUpdater(routeAtlasState, footerPane, atlasImageCreator);
    private final CompileAtlasAction compileAtlasAction = new CompileAtlasAction(routeAtlasCompiler, routeAtlasState, buttonUpdater);
    private final CreateAtlasAction createAtlasAction = new CreateAtlasAction(downloader, atlasSetupPane, routeAtlasState, buttonUpdater);


    public RouteAtlasApp(){
        super("Route Atlas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        add(createSetupPane(), BorderLayout.WEST);
        add(createMainPanel(), BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setActions();
        buttonUpdater.setCompilationEnabled(false); //dont allow pdf compilation before atlas creation
        overviewPageSetupPane.setToOverviewDefaults(); //different layer/zoom configuration for overview setup
        routeAtlasState.addListener(mapUpdater); //register mapUpdater to listen for state changes
    }

    private JPanel createSetupPane() {
        JPanel setupPane = new JPanel();
        setupPane.setLayout(new BoxLayout(setupPane, BoxLayout.Y_AXIS));
        setupPane.add(atlasSetupPane);
        setupPane.add(pageSetupTab);
        setupPane.add(compilationPane);
        pageSetupTab.add("Overview", overviewPageSetupPane);
        pageSetupTab.add("Section", sectionPageSetupPane);
        JPanel setupPaneWrapper = new JPanel();
        setupPaneWrapper.add(setupPane);
        return setupPaneWrapper;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mapDisplayPane, BorderLayout.CENTER);
        mainPanel.add(footerPane, BorderLayout.SOUTH);
        mapDisplayPane.setPreferredSize(new Dimension(800,800));
        return mainPanel;
    }

    private void setActions() {
        atlasSetupPane.getButton().setAction(createAtlasAction);
        compilationPane.getCompilationButton().setAction(compileAtlasAction);
        compilationPane.setCancelAction(mapUpdater::cancelMapRequest);
        mapDisplayPane.setCancelAction(mapUpdater::cancelMapRequest);
        mapDisplayPane.setSelectPreviousMapAction(routeAtlasState::selectPreviousMap);
        mapDisplayPane.setSelectNextMapAction(routeAtlasState::selectNextMap);
        overviewPageSetupPane.setPropertyChangeListener(evt -> mapUpdater.requestMap());
        sectionPageSetupPane.setPropertyChangeListener(evt -> mapUpdater.requestMap());
    }

    public static void main(String[] args) {
        try {
            Path configFile = FileIO.ensureResourceFile(BASE_DIR, "providers.xml");
            WebMapProviders.CONFIG_FILE_PATH = configFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(RouteAtlasApp::new);
    }

    public static final class FileIO {

        public static Path ensureResourceFile(Path baseDir, String resourceName) throws IOException {
            Files.createDirectories(baseDir);
            Path targetFile = baseDir.resolve(resourceName);

            if (Files.notExists(targetFile)) {
                copyFromResources(resourceName, targetFile, FileIO.class);
                System.out.println(resourceName + " copied to " + baseDir);
            } else {
                System.out.println(resourceName + " already exists in " + baseDir);
            }

            return targetFile;
        }

        private static void copyFromResources(String resourceName, Path target, Class resourceOwner) throws IOException {
            try (InputStream in = resourceOwner
                    .getClassLoader()
                    .getResourceAsStream(resourceName)) {

                if (in == null)
                    throw new IOException("Resource not found: " + resourceName);

                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
