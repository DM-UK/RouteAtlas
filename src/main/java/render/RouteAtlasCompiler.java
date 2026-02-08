package render;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import routeatlas.MapPage;
import routeatlas.RouteAtlas;
import wmts.ProgressListener;
import wmts.WMTSException;

import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CancellationException;

public class RouteAtlasCompiler {
    private final Path pdfDir;
    private final AtlasImageCreator atlasImageCreator;
    private final ProgressListener progressListener;

    public RouteAtlasCompiler(Path pdfDir, AtlasImageCreator atlasImageCreator, ProgressListener progressListener){
        this.pdfDir = pdfDir;
        this.atlasImageCreator = atlasImageCreator;
        this.progressListener = progressListener;
    }

    public void cancel() {
        atlasImageCreator.getClient().cancelCurrentRequest();
    }

    public Path compile(RouteAtlas atlas) throws IOException, WMTSException, CancellationException {
        PDFExporter exporter = new PDFExporter();
        int total = atlas.getAllPages().size();
        progressListener.onStart(total);

        int index = 0;

        try {
            for (MapPage page: atlas.getAllPages()) {
               // if (index == 2)
                   // break;

                BufferedImage img = atlasImageCreator.createAtlasImage(atlas, index);
                exporter.addPage(img, page.getScaledPaper().getPaper(), page.getOrientation());
                progressListener.onProgress(index+1, total);
                index++;
            }
        }
        catch (CancellationException e){
            System.out.println(e);
            return null;
        }
        finally {
            atlasImageCreator.getClient().cancelCurrentRequest();
            progressListener.onComplete(null);
        }

        return exporter.save();
    }

    private class PDFExporter {
        PDDocument document = new PDDocument();

        public void addPage(BufferedImage img, Paper paper, int orientation) throws IOException {
            float width = (float) paper.getWidth();
            float height = (float) paper.getHeight();

            if (orientation == PageFormat.LANDSCAPE) {
                float tmp = width;
                width = height;
                height = tmp;
            }

            PDPage page = new PDPage(new PDRectangle(width, height));
            document.addPage(page);

            PDImageXObject pdImage = LosslessFactory.createFromImage(document, img);
            PDPageContentStream cs = new PDPageContentStream(document, page);
            cs.drawImage(pdImage, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            cs.close();
        }

        private Path save() throws IOException {
            String uniqueID = UUID.randomUUID().toString();
            Files.createDirectories(pdfDir);
            Path output = pdfDir.resolve(uniqueID+".pdf");
            document.save(output.toFile());
            document.close();
            return output;
        }
    }
}
