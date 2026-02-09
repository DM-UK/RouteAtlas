package app;

import org.xml.sax.SAXException;
import ui.controller.*;
import ordnancesurvey.OrdnanceSurveyRouteDownloader;
import render.AtlasImageCreator;
import render.RouteAtlasCompiler;
import ui.panes.*;
import ui.swing.TabbedAccordionPanel;
import utils.FileIOUtils;
import wmts.WebMapProviders;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    //atlas image creation
    private final AtlasImageCreator atlasImageCreator = new AtlasImageCreator(TILE_CACHE_DIR, overviewPageSetupPane, sectionPageSetupPane, mapDisplayPane.getProgressListener());

    //atlas pdf creation
    private final RouteAtlasCompiler routeAtlasCompiler = new RouteAtlasCompiler(PDF_DIR, atlasImageCreator, compilationPane.getProgressBar());

    //controllers
    private final MapTabBinder mapTabBinder = new MapTabBinder(routeAtlasState, pageSetupTab.getModel());
    private final ButtonUpdater buttonUpdater = new ButtonUpdater(atlasSetupPane.getButton(), compilationPane.getCompilationButton());
    private final MapUpdater mapUpdater = new MapUpdater(routeAtlasState, footerPane, atlasImageCreator);

    //button actions
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
        compilationPane.setCancelAction(compileAtlasAction::cancel);
        mapDisplayPane.setCancelAction(mapUpdater::cancelMapRequest);
        mapDisplayPane.setSelectPreviousMapAction(routeAtlasState::selectPreviousMap);
        mapDisplayPane.setSelectNextMapAction(routeAtlasState::selectNextMap);
        overviewPageSetupPane.setPropertyChangeListener(evt -> mapUpdater.requestMap());
        sectionPageSetupPane.setPropertyChangeListener(evt -> mapUpdater.requestMap());
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        try {
            Path configFile = FileIOUtils.ensureResourceFile(BASE_DIR, "providers.xml");
            WebMapProviders.load(configFile);
        } catch (Exception e) {

        }

        SwingUtilities.invokeLater(RouteAtlasApp::new);
    }

}
