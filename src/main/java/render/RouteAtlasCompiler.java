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
import java.util.concurrent.atomic.AtomicBoolean;

public class RouteAtlasCompiler {
    private final Path pdfDir;
    private final AtlasImageCreator atlasImageCreator;
    private final ProgressListener progressListener;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    /** Saves a RouteAtlas to a multi-page PDF with progress and cancellation support.*/
    public RouteAtlasCompiler(Path pdfDir, AtlasImageCreator atlasImageCreator, ProgressListener progressListener){
        this.pdfDir = pdfDir;
        this.atlasImageCreator = atlasImageCreator;
        this.progressListener = progressListener;
    }

    /** Save a RouteAtlas. Returns path file if successful. CPU/IO intensive. */
    public Path compile(RouteAtlas atlas) throws IOException, WMTSException, CancellationException {
        cancelled.set(false);
        PDFExporter exporter = new PDFExporter();
        int total = atlas.getAllPages().size();
        progressListener.onStart(total);

        int index = 0;

        try {
            for (MapPage page : atlas.getAllPages()) {
                //check before image request
                if (cancelled.get())
                    throw new CancellationException("Route atlas compilation cancelled");

                BufferedImage img = atlasImageCreator.createAtlasImage(atlas, index);

                //check before saving to pdf
                if (cancelled.get())
                    throw new CancellationException("Route atlas compilation cancelled");

                exporter.addPage(img, page.getScaledPaper().getPaper(), page.getOrientation());

                progressListener.onProgress(index + 1, total);
                index++;
            }

            //return pdf path
            return exporter.saveToDisc();
        }
        finally {
            progressListener.onComplete(null);
            exporter.close();
        }
    }

    public void cancel() {
        cancelled.set(true);
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

        private Path saveToDisc() throws IOException {
            String uniqueID = UUID.randomUUID().toString();
            Files.createDirectories(pdfDir);
            Path output = pdfDir.resolve(uniqueID+".pdf");
            document.save(output.toFile());
            return output;
        }

        public void close() throws IOException {
            document.close();
        }
    }
}
