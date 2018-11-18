//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package deviationScoreRegions.modular;

import basics.Tools;
import basics.filter.canny.CannyEdgeDetection;
import deviationScoreRegions.modular.edgeFilters.ImageJEdges;
import deviationScoreRegions.modular.scoreImage.DeviationScoreImage;
import deviationScoreRegions.modular.scoreImage.ScoreImage;
import deviationScoreRegions.modular.scoreImage.ScoreImageFeatures;
import evaluation.Batch.Batchable;
import ij.process.ImageProcessor;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class DofCheckerModular implements Batchable {
    public DofCheckerModular() {
    }

    public ImageProcessor run(ImageProcessor original) {
        ScoreImage scoreImage = new DeviationScoreImage(new ImageJEdges(), 10, 0.85D, 0.0D);
        scoreImage.generateScore(original);
        ScoreImageFeatures scoreImageFeatures = new ScoreImageFeatures(scoreImage);
        ImageProcessor edges = CannyEdgeDetection.run(original.convertToByte(true));
        Tools.save(Tools.write("canny", edges));
        double socreImageCover = scoreImageFeatures.closedCovering();
        double clearness = scoreImageFeatures.getCearness(1);
        int regions = scoreImageFeatures.getMorphedRegionNr();
        double instability = scoreImageFeatures.getInstability();
        Tools.save(Tools.write("instability = " + Tools.formatNumber(instability) + "\n regions = " + regions, scoreImage.getImageProcessor()));
        return original;
    }

    public void countRegions(ImageProcessor imageProcessor, int value) {
        int width = imageProcessor.getWidth();
        int height = imageProcessor.getHeight();
        boolean[][] visited = new boolean[width][height];

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                if (!visited[x][y]) {
                    visited[x][y] = true;
                    if (imageProcessor.getPixel(x, y) == value) {
                        ;
                    }
                }
            }
        }

    }

    public static void main(String[] args) throws IOException {
        int maxSize = 800;
        List<String> fileNames = Tools.getFilesFromDirectory("../../images/dofchecker", ".jpg");
        Collections.shuffle(fileNames, new Random(0L));

        ImageProcessor imageProcessor;
        for(Iterator i$ = fileNames.iterator(); i$.hasNext(); (new DofCheckerModular()).run(imageProcessor)) {
            String fileName = (String)i$.next();
            imageProcessor = Tools.loadImageProcessor(fileName);
            if (Tools.getLongestSide(imageProcessor) > maxSize) {
                imageProcessor = Tools.resize(imageProcessor, maxSize);
            }
        }

    }
}
