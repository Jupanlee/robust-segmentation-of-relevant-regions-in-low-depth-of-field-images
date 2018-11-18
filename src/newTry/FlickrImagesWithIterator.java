package newTry;

import basics.Tools;
import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.photos.SearchParameters;
import ij.process.ImageProcessor;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

public class FlickrImagesWithIterator
        implements Iterable<BufferedImage>
{
    private final String apiKey = "dc9a0bdf23fe6bcf678828b6206aa7c5";
    private final String host = "www.flickr.com";
    private String[] tags = new String[0];
    private int maxPhotoCount = 2147483647;
    private Flickr flickr;
    private PhotosInterface photosInterface;

    public FlickrImagesWithIterator()
            throws ParserConfigurationException
    {
        this.flickr = new Flickr("dc9a0bdf23fe6bcf678828b6206aa7c5", new REST("www.flickr.com"));
        Flickr.debugStream = false;
        this.photosInterface = this.flickr.getPhotosInterface();
    }

    public FlickrImagesWithIterator(String[] searchTags, int maxPhotoCount) throws ParserConfigurationException {
        this();
        this.tags = searchTags;
        this.maxPhotoCount = maxPhotoCount;
    }

    public BufferedImage getID(String id) throws Exception {
        Photo p = this.photosInterface.getPhoto(id);
        System.out.println(p.getLicense());
        return this.photosInterface.getImage(p, 3);
    }

    public Iterator<BufferedImage> iterator()
    {
        try
        {
            SearchParameters sp = new SearchParameters();
            sp.setTags(this.tags);
            return new FlickrIterator(sp, this.maxPhotoCount);
        } catch (Exception ex) {
            Logger.getLogger(FlickrImagesWithIterator.class.getName()).log(Level.SEVERE, null, ex);
        }throw new IllegalStateException(ex);
    }

    public static void main(String[] args) throws Exception
    {
        FlickrImages flickr = new FlickrImages();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setText("100mm 2.8 macro");

        flickr.setSearchParameters(searchParameters);

        for (int i = 0; i < 100; i++) {
            Photo photo = flickr.getRandomPhoto();
            if (photo != null) {
                ImageProcessor imageProcessor = Tools.loadImageProcessor(flickr.getImage(photo));
                if (imageProcessor != null)
                    Tools.save(imageProcessor, "../../images/flickrtmp/ID" + photo.getId() + "_org.jpg");
            }
        }
    }

    private class FlickrIterator
            implements Iterator<BufferedImage>
    {
        private SearchParameters sp = new SearchParameters();
        private final int numbersPerPage = 100;
        private final int size = 4;
        private final boolean shufflePhotos = true;
        private int pageOffset = 0;
        private PhotoList photoList;
        private int currentPhotoIndex = -1;
        private int maxPhotoCount;

        public FlickrIterator(SearchParameters sp, int maxPhotoCount)
                throws Exception
        {
            this.maxPhotoCount = maxPhotoCount;
            this.sp = sp;
            searchPage();
        }

        private void searchPage() throws Exception {
            this.photoList = FlickrImagesWithIterator.this.photosInterface.search(this.sp, 100, this.pageOffset);

            Collections.shuffle(this.photoList);
        }

        public boolean hasNext()
        {
            if (this.pageOffset * 100 + this.currentPhotoIndex + 1 >= this.maxPhotoCount) {
                return false;
            }

            if (this.currentPhotoIndex + 1 == this.photoList.size()) {
                this.pageOffset += 1;
                this.currentPhotoIndex = 0;
                try {
                    searchPage();
                } catch (Exception ex) {
                    Logger.getLogger(FlickrImagesWithIterator.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }

            return this.currentPhotoIndex + 1 < this.photoList.size();
        }

        public BufferedImage next()
        {
            try {
                this.currentPhotoIndex += 1;
                return FlickrImagesWithIterator.this.photosInterface.getImage((Photo)this.photoList.get(this.currentPhotoIndex), 4);
            } catch (Exception ex) {
                Logger.getLogger(FlickrImagesWithIterator.class.getName()).log(Level.SEVERE, null, ex);
            }return null;
        }

        public void remove()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}