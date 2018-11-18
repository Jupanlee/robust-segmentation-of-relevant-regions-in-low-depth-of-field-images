package deviationScoreRegions.modular;

import basics.ProgressListener;
import basics.SystemOutProgressListener;
import basics.Tools;
import basics.javaAddons.DEBUG;
import deviationScoreRegions.grow.ColorRegion;
import deviationScoreRegions.grow.ColorRegionTools;
import deviationScoreRegions.modular.scoreImage.ScoreImage;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Point;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ScoreRegions
{
    private final double maxScoreRegionSize = 0.5D;
    private final boolean checkMBO = true;
    private final double tetha_dist;
    private final double tetha_rel;
    private final ProgressListener progressListener;

    public ScoreRegions(double tetha_dist, double tetha_rel, ProgressListener progressListener)
    {
        this.progressListener = progressListener;
        this.tetha_dist = tetha_dist;
        this.tetha_rel = tetha_rel;
    }

    public ScoreRegions(double tetha_dist, double tetha_rel) {
        this(tetha_dist, tetha_rel, new SystemOutProgressListener());
    }

    public ScoreRegions() {
        this(25.0D, 0.75D);
    }

    public ImageProcessor removeLowScoreRegionsNew(ImageProcessor imageProcessor, ApproximationMask approximationMask) {
        double DELTAE = this.tetha_dist;
        double REL = this.tetha_rel;

        ImageProcessor mask = approximationMask.getImageProcessor().duplicate();
        ImageProcessor resized = imageProcessor.resize(approximationMask.getWidth(), approximationMask.getHeight());
        ImageProcessor scoreImageProcessor = approximationMask.getScoreClustering().getScoreImage().getImageProcessor();
        List colorRegions = ColorRegionTools.getRegionsFromImageProcessor(resized, DELTAE, mask);
        this.progressListener.updateImage(ColorRegionTools.draw(colorRegions, mask.getWidth(), mask.getHeight()));
        int maxSize = (int)Math.round(Tools.imageProcessorToPoints(mask, 1).size() * 0.5D);

        ImageProcessor refinedMask = new ByteProcessor(mask.getWidth(), mask.getHeight());
        for (ColorRegion colorRegion : colorRegions) {
            ColorRegion outline = new ColorRegion(ColorRegionTools.getOutline(colorRegion, 1, mask.getWidth(), mask.getHeight()));
            double maskOverlap = ColorRegionTools.overlap(colorRegion, mask);
            double maskBoundaryOverlap = ColorRegionTools.overlap(outline, mask);
            double scoreBoundaryOverlap = ColorRegionTools.overlap(outline, scoreImageProcessor);
            double maskRelevancy = maskBoundaryOverlap * scoreBoundaryOverlap;

            if ((maskBoundaryOverlap <= this.tetha_rel) && (maskRelevancy <= this.tetha_rel) && (colorRegion.size() <= maxSize)) {
                for (Point p : colorRegion.getPixels()) {
                    refinedMask.putPixel(p.x, p.y, 255);
                }
                if (Tools.chance(0.001D)) {
                    this.progressListener.updateImage(refinedMask);
                }
            }
        }

        return refinedMask;
    }

    public ImageProcessor removeLowScoreRegions(ImageProcessor imageProcessor, ApproximationMask approximationMask) {
        ImageProcessor mask = approximationMask.getImageProcessor().duplicate();
        ImageProcessor resized = imageProcessor.resize(approximationMask.getWidth(), approximationMask.getHeight());
        ImageProcessor scoreImageProcessor = approximationMask.getScoreClustering().getScoreImage().getImageProcessor();

        int maxSize = (int)Math.round(Tools.imageProcessorToPoints(mask, 1).size() * 0.5D);
        if (DEBUG.getVerbose()) {
            System.out.println("maxSize == " + maxSize);
        }

        List colorRegions = ColorRegionTools.getRegionsFromImageProcessor(resized, this.tetha_dist, mask);
        this.progressListener.updateImage(ColorRegionTools.draw(colorRegions, mask.getWidth(), mask.getHeight()));

        ColorRegion[][] colorRegionMap = new ColorRegion[mask.getWidth()][mask.getHeight()];
        for (Iterator i$ = colorRegions.iterator(); i$.hasNext(); ) { colorRegion = (ColorRegion)i$.next();
            for (Point p : colorRegion.getPixels())
                colorRegionMap[p.x][p.y] = colorRegion;
        }
        ColorRegion colorRegion;
        int iteration = 0;
        Set seedRegions = new HashSet(colorRegions);
        Set nextSeedRegions = new HashSet();
        Iterator iterator = seedRegions.iterator();
        while (iterator.hasNext())
        {
            ColorRegion colorRegion = (ColorRegion)iterator.next();
            iterator.remove();

            ColorRegion outline = new ColorRegion(ColorRegionTools.getOutline(colorRegion, 1, mask.getWidth(), mask.getHeight()));

            double maskBoundaryOverlap = ColorRegionTools.overlap(outline, mask);
            double scoreBoundaryOverlap = ColorRegionTools.overlap(outline, scoreImageProcessor);
            double maskRelevancy = maskBoundaryOverlap * scoreBoundaryOverlap;

            boolean clearRegion = (maskBoundaryOverlap <= this.tetha_rel) && (maskRelevancy <= this.tetha_rel) && (colorRegion.size() <= maxSize);

            if (clearRegion)
            {
                for (Point outlinePoint : outline.getPixels()) {
                    nextSeedRegions.add(colorRegionMap[outlinePoint.x][outlinePoint.y]);
                }

                colorRegions.remove(colorRegion);
            }
            if (colorRegion.size() > 10) {
                String s = "MBO=" + Tools.formatNumber(maskBoundaryOverlap) + " SBO=" + Tools.formatNumber(scoreBoundaryOverlap) + " REL=" + Tools.formatNumber(maskRelevancy);
                Color c = clearRegion ? Color.red : Color.green;
                this.progressListener.updateImage(Tools.write(s, ColorRegionTools.draw(colorRegion, ColorRegionTools.draw(colorRegions, mask.getWidth(), mask.getHeight()), true, c)));
            }

        }

        mask = ColorRegionTools.mask(colorRegions, mask.getWidth(), mask.getHeight());
        this.progressListener.updateImage(mask);
        return mask;
    }
}