package newTry;

import basics.Tools;
import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.photos.SearchParameters;
import ij.process.ImageProcessor;
import java.awt.color.CMMException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class FlickrImages
{
    private final String apiKey = "dc9a0bdf23fe6bcf678828b6206aa7c5";
    private final String host = "www.flickr.com";
    private String[] tags = new String[0];
    private Flickr flickr;
    private PhotosInterface photosInterface;
    private SearchParameters searchParameters = new SearchParameters();
    private final int numbersPerPage = 300;
    private int maxPageOffset = 20;
    private PhotoList photoList;
    private int nrOfPages = -1;

    public SearchParameters getSearchParameters() {
        return this.searchParameters;
    }

    public void setSearchParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    public FlickrImages() throws ParserConfigurationException {
        this.flickr = new Flickr("dc9a0bdf23fe6bcf678828b6206aa7c5", new REST("www.flickr.com"));
        Flickr.debugStream = false;
        this.photosInterface = this.flickr.getPhotosInterface();
    }

    public FlickrImages(String[] searchTags) throws ParserConfigurationException {
        this();
        this.searchParameters.setTags(searchTags);
    }

    public BufferedImage getImage(String id, int size) throws Exception {
        Photo p = this.photosInterface.getPhoto(id);
        return this.photosInterface.getImage(p, size);
    }

    public int getNrOfPages() throws IOException, FlickrException, SAXException {
        int count = this.maxPageOffset;
        while (this.photosInterface.search(this.searchParameters, 300, count).size() == 0) {
            count--;
        }

        return count;
    }

    public ImageProcessor getImageProcessor(Photo photo) {
        return Tools.loadImageProcessor(getImage(photo));
    }
    public BufferedImage getImage(Photo photo) {
        try {
            return this.photosInterface.getImage(photo, 4);
        } catch (Exception ex) {
            Logger.getLogger(FlickrImages.class.getName()).log(Level.SEVERE, null, ex);
        }return null;
    }

    public BufferedImage getRandomImage()
    {
        Photo photo = getRandomPhoto();
        return getImage(photo);
    }
    public Photo getRandomPhoto() {
        try {
            if (this.nrOfPages == -1) {
                this.nrOfPages = getNrOfPages();
            }

            if (this.nrOfPages > 0) {
                int pageOffset = (int)(Math.random() * this.nrOfPages);
                this.photoList = this.photosInterface.search(this.searchParameters, 300, pageOffset);
                Photo photo = (Photo)this.photoList.get((int)(Math.random() * this.photoList.size()));
                System.out.println("photo ID == " + photo.getId());
                return photo;
            }
        } catch (CMMException ex) {
            Logger.getLogger(FlickrImages.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(FlickrImages.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FlickrImages.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FlickrException ex) {
            Logger.getLogger(FlickrImages.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(FlickrImages.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static void main(String[] args) throws Exception {
        FlickrImages flickr = new FlickrImages();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setTags(new String[] { "200mm", "f2.8", "DOF" });
        flickr.setSearchParameters(searchParameters);
        for (int i = 0; i < 50; i++) {
            Photo photo = flickr.getRandomPhoto();
            ImageProcessor image = flickr.getImageProcessor(photo);
            Tools.save(image, "../../images/tmp/ID" + photo.getId() + "_org.jpg");
        }
    }
}