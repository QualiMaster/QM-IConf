package de.uni_hildesheim.sse.qmApp.images;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import de.uni_hildesheim.sse.qmApp.treeView.ElementStatusIndicator;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;

/**
 * Class for managing icons.
 * 
 * @author Nowatzki
 */
public class IconManager {

    public static final String PRE = "icons/";

    public static final String TYPE = PRE + "type.png";
    public static final String TYPES = PRE + "types.png";
    public static final String GP_MACHINE = PRE + "gpMachine.png";
    public static final String GP_MACHINES = PRE + "gpMachines.png";
    public static final String RECONFIG_MACHINE = PRE + "reconfigMachine.png";
    public static final String RECONFIG_MACHINES = PRE + "reconfigMachines.png";
    public static final String ALGORITHM = PRE + "algorithm.png";
    public static final String SUBALGORITHM = PRE + "subAlgorithm.png";
    public static final String HWALGORITHM = PRE + "hardwareAlgorithm.png";
    public static final String ALGORITHMS = PRE + "algorithms.png";
    public static final String DATA_SOURCE = PRE + "dataSource.png";
    public static final String DATA_SINK = PRE + "dataSink.png";
    public static final String DATA_ELEMENT = PRE + "dataElement.png";
    public static final String DATA_MANAGEMENT = PRE + "dataManagement.png";
    public static final String FAMILY = PRE + "family.png";
    public static final String FAMILIES = PRE + "families.png";
    public static final String SUBPIPELINE = PRE + "subPipeline.png";
    public static final String PIPELINE = PRE + "pipeline.png";
    public static final String PIPELINES = PRE + "pipelines.png";
    public static final String INFRASTRUCTURE = PRE + "infrastructure.png";
    public static final String QUALIMASTER_SMALL = PRE + "QualiMasterIcon.PNG";
    public static final String QUALIMASTER_BIG = PRE + "QMlogoMedium.png";
    public static final String QUALIMASTER_HELP = PRE + "QMlogoHelp.png";
    public static final String EASY_MEDIUM = PRE + "EasyIcon.PNG";
    public static final String EU_FLAG = PRE + "EuropeanUnion.jpg";
    public static final String RUNTIME = PRE + "runtime.png";
    public static final String RTVIL = PRE + "rtvil_file_icon.gif";
    public static final String ADAPTATION = PRE + "adaptation.png";
    public static final String ADAPTATION_WEIGHT = PRE + "weight.png";
    public static final String OBSERVABLE = PRE + "observable.png";
    public static final String OBSERVABLES = PRE + "observables.png";
    public static final String ERROR = PRE + "error.png";
    public static final String WARNING = PRE + "warning.png";
    public static final String CHECKEDBOX = PRE + "checked_checkbox.png";
    public static final String UNCHECKEDBOX = PRE + "unchecked_checkbox.png";
    public static final String TREE_FOLDER = PRE + "folder.png";
    public static final String TREE_FILE = PRE + "treefile.png";
    public static final String MAVEN_DIALOG_ICON = PRE + "mavenEditor.png";
    public static final String OVERLAY_ERROR_SMALL = PRE + "error_small.png";
    public static final String CLASS = PRE + "class_obj.png";
    
    public static final String RECTANGLE_RED1 = PRE + "red1.png";
    public static final String RECTANGLE_RED2 = PRE + "red2.png";
    public static final String RECTANGLE_ORANGE = PRE + "orange.png";
    public static final String RECTANGLE_GREEN1 = PRE + "green1.png";
    public static final String RECTANGLE_GREEN2 = PRE + "green2.png";
    
    public static final String SVG_DATAMANAGEMENT_VERY_HIGH = "datamanagement_very_high.svg";
    public static final String SVG_DATAMANAGEMENT_HIGH = "datamanagement_high.svg";
    public static final String SVG_DATAMANAGEMENT_MEDIUM = "datamanagement_medium.svg";
    public static final String SVG_DATAMANAGEMENT_LOW = "datamanagement_low.svg";
    public static final String SVG_DATAMANAGEMENT_VERY_LOW = "datamanagement_very_low.svg";
    
    public static final String SVG_FAMILYELEMENT_VERY_HIGH = "familyelement_very_high.svg";
    public static final String SVG_FAMILYELEMENT_HIGH = "familyelement_high.svg";
    public static final String SVG_FAMILYELEMENT_MEDIUM = "familyelement_medium.svg";
    public static final String SVG_FAMILYELEMENT_LOW = "familyelement_low.svg";
    public static final String SVG_FAMILYELEMENT_VERY_LOW = "familyelement_very_low.svg";
    
    public static final String SVG_SINK_VERY_HIGH = "sink_very_high.svg";
    public static final String SVG_SINK_HIGH = "sink_high.svg";
    public static final String SVG_SINK_MEDIUM = "sink_medium.svg";
    public static final String SVG_SINK_LOW = "sink_low.svg";
    public static final String SVG_SINK_VERY_LOW = "sink_very_low.svg";
    
    public static final String SVG_SOURCE_VERY_HIGH = "source_very_high.svg";
    public static final String SVG_SOURCE_HIGH = "source_high.svg";
    public static final String SVG_SOURCE_MEDIUM = "source_medium.svg";
    public static final String SVG_SOURCE_LOW = "source_low.svg";
    public static final String SVG_SOURCE_VERY_LOW = "source_very_low.svg";
    
    public static final org.eclipse.swt.graphics.Color DARK_GREEN =
            new org.eclipse.swt.graphics.Color(Display.getCurrent(), 102, 255, 102);
    public static final org.eclipse.swt.graphics.Color LIGHT_GREEN =
            new org.eclipse.swt.graphics.Color(Display.getCurrent(), 51, 255, 102);
    public static final org.eclipse.swt.graphics.Color YELLOW =
            new org.eclipse.swt.graphics.Color(Display.getCurrent(), 255, 255, 0);
    public static final org.eclipse.swt.graphics.Color LIGHT_RED =
            new org.eclipse.swt.graphics.Color(Display.getCurrent(), 255, 102, 102);
    public static final org.eclipse.swt.graphics.Color DARK_RED =
            new org.eclipse.swt.graphics.Color(Display.getCurrent(), 204, 0, 0);
    
    private static final Map<String, String> TYPE_MAP = new HashMap<String, String>();
    
    static {
        TYPE_MAP.put("Algorithm", ALGORITHM);
        TYPE_MAP.put("DataElement", DATA_ELEMENT);
        TYPE_MAP.put("PersistentDataElement", DATA_ELEMENT);
        TYPE_MAP.put("DataSource", DATA_SOURCE);
        TYPE_MAP.put("DataSink", DATA_SINK);
        TYPE_MAP.put("Family", FAMILY);
        TYPE_MAP.put("Pipeline", PIPELINE);
    }
    
    /**
     * Get icon by path.
     * 
     * @param path the path to the image/icon
     * @return the image/icon
     */
    public static Image retrieveImage(String path) {
        URL url = IconManager.class.getClassLoader().getResource(path);
        return ImageDescriptor.createFromURL(url).createImage();
    }
    
    /**
     * Get image-descrptor by path.
     * 
     * @param path the path to the image/icon
     * @return the image/icon
     */
    public static ImageDescriptor retrieveImageDescriptor(String path) {
        URL url = IconManager.class.getClassLoader().getResource(path);
        return ImageDescriptor.createFromURL(url);
    }
    
    /**
     * Retrieves the (registered) image for a data type.
     * 
     * @param type the type
     * @return the image
     */
    public static Image retrieveImage(IDatatype type) {
        String image = TYPE_MAP.get(type.getName());
        if (null == image) {
            image = ERROR;
        }
        return retrieveImage(image);
    }
    
    /**
     * Transforms coloured image into gray-scale image.
     * 
     * @param image Given coloured {@link Image}.
     * @return returnImage gray-scale {@link Image}.
     */
    public static Image filterImage(Image image) {
        Image result = image;
        if (null != image) {
            //Will be needed for creation of the newly transformed image
            Device device = image.getDevice();
        
            //Firstly tranform into a buffered image
            BufferedImage buffImage = toBufferedImage(image);
            int width = buffImage.getWidth();
            int height = buffImage.getHeight();
            
            //make grayscale
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Color c = new Color(buffImage.getRGB(j, i));
                    int red = (int) (c.getRed() * 0.299);
                    int green = (int) (c.getGreen() * 0.587);
                    int blue = (int) (c.getBlue() * 0.114);
                    Color newColor = new Color(red + green + blue,
                        red + green + blue, red + green + blue);
                    buffImage.setRGB(j, i, newColor.getRGB());
                }
            }
        
            //Now create a new image out of the transformed buffered image
            result = new Image(device, convertToSWT(buffImage));
        }
        return result;
    }
    
    /**
     * Convert given {@link Image} to {@link BufferedImage}.
     * 
     * @param image Given {@link Image}.
     * @return Transformed {@link BufferedImage}.
     */
    public static BufferedImage toBufferedImage(Image image) {
    
        BufferedImage buffImage = convertToAWT(image.getImageData());
        
        return buffImage;
        
    }
    
    //TODO: Move methods for image-editing in IconManager?
    /**
     * Convert given {@link ImageData} to a {@link BufferedImage}.
     * 
     * @param data Given {@link ImageData}.
     * @return toReturn {@link BufferedImage}.
     */
    private static BufferedImage convertToAWT(ImageData data) {
    
        //BufferedImage which will be returned
        BufferedImage toReturn;
    
        ColorModel colorModel = null;
        PaletteData palette = data.palette;
        if (palette.isDirect) {
            colorModel = new DirectColorModel(data.depth, palette.redMask, palette.greenMask, palette.blueMask);
            BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.
                    createCompatibleWritableRaster(data.width, data.height), false, null);
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int pixel = data.getPixel(x, y);
                    RGB rgb = palette.getRGB(pixel);
                    bufferedImage.setRGB(x, y,  rgb.red << 16 | rgb.green << 8 | rgb.blue);
                }
            }
            toReturn = bufferedImage;
        } else {
            RGB[] rgbs = palette.getRGBs();
            byte[] red = new byte[rgbs.length];
            byte[] green = new byte[rgbs.length];
            byte[] blue = new byte[rgbs.length];
            
            for (int i = 0; i < rgbs.length; i++) {
                RGB rgb = rgbs[i];
                red[i] = (byte) rgb.red;
                green[i] = (byte) rgb.green;
                blue[i] = (byte) rgb.blue;
            }
            if (data.transparentPixel != -1) {
                colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue, data.transparentPixel);
            } else {
                colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
            }
            BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.
                    createCompatibleWritableRaster(data.width, data.height), false, null);
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int pixel = data.getPixel(x, y);
                    pixelArray[0] = pixel;
                    raster.setPixel(x, y, pixelArray);
                }
            }
            toReturn =  bufferedImage;
        }
        return toReturn;
    }
    /**
     * Transform given {@link BufferedImage} into {@link ImageData}.
     * 
     * @param bufferedImage Given {@link BufferedImage}.
     * @return ImageData {@link ImageData} out of {@link BufferedImage}.
     */
    private static ImageData convertToSWT(BufferedImage bufferedImage) {
        //TODO: Will it always be ComponentModel?
        ImageData data = null;
    
        if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
            ComponentColorModel colorModel = (ComponentColorModel) bufferedImage.getColorModel();
    
            PaletteData palette = new PaletteData(0x0000FF, 0x00FF00, 0xFF0000);
            data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
                    colorModel.getPixelSize(), palette);

            //This is valid because we are using a 3-byte Data model with no transparent pixels
            data.transparentPixel = -1;

            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
                    data.setPixel(x, y, pixel);
                }
            }
        } else if (bufferedImage.getColorModel() instanceof DirectColorModel) {
    
            DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(),
                    colorModel.getBlueMask());
            data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF)); 
                    data.setPixel(x, y, pixel);
                    
                    if (colorModel.hasAlpha()) {
                        data.setAlpha(x, y, (rgb >> 24) & 0xFF);
                    }
                }
            }
        } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            //Not used...
            IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);           
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
            }            
            PaletteData palette = new PaletteData(rgbs);
            data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
        }
        return data;
    }
    
    /**
     * Adds a little error-image to the given 16x16-image.
     * @param image Given 16x16-image.
     * @return result - new Image with error.icon in the bottom-right corner.
     */
    public static Image addErrorToImage(Image image) {

        BufferedImage base = toBufferedImage(image);
        BufferedImage overlay = IconManager.toBufferedImage(retrieveImage(IconManager.OVERLAY_ERROR_SMALL));
        
        int w = Math.max(base.getWidth(), overlay.getWidth());
        int h = Math.max(base.getHeight(), overlay.getHeight());
        BufferedImage combined  = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        Graphics g = combined.getGraphics();
        g.drawImage(base, 0, 0, null);
        g.drawImage(overlay, 8, 8, null);
        
        Image result = new Image(Display.getCurrent(), convertToSWT(combined));
        return result;
    }

    /**
     * Adds a little error-image to the given 16x16-image.
     * @param image Given 16x16-image.
     * @param indicator indicated status of the element.
     * @return result - new Image with error.icon in the bottom-right corner.
     */
    public static Image addErrorToImage(Image image, ElementStatusIndicator indicator) {
        
        BufferedImage overlay = null;
        
        if (null == indicator) {
            overlay = IconManager.toBufferedImage(retrieveImage(IconManager.OVERLAY_ERROR_SMALL));
        }
        
        if (null != indicator) {
            switch (indicator) {
            case VERYHIGH:
                overlay = IconManager.toBufferedImage(retrieveImage(IconManager.RECTANGLE_RED2));
                break;
            case HIGH: 
                overlay = IconManager.toBufferedImage(retrieveImage(IconManager.RECTANGLE_RED1));
                break;
            case MEDIUM:  
                overlay = IconManager.toBufferedImage(retrieveImage(IconManager.RECTANGLE_ORANGE));
                break;
            case LOW:    
                overlay = IconManager.toBufferedImage(retrieveImage(IconManager.RECTANGLE_GREEN1));
                break;
            case VERYLOW:  
                overlay = IconManager.toBufferedImage(retrieveImage(IconManager.RECTANGLE_GREEN2));
                break;
            case NONE:
                overlay = null;
                break;
            default:
                overlay = null;
                break;
            }
        }
        
        if (null != overlay) {
            image = IconManager.addIndicatorToImage(image, overlay);
        }
        return image;
    }

    /**
     * Adds a little error-image to the given 16x16-image.
     * @param image Given 16x16-image.
     * @param overlay Overlay image.
     * @return result - new Image with error icon in the bottom-right corner.
     */
    private static Image addIndicatorToImage(Image image, BufferedImage overlay) {
        BufferedImage base = toBufferedImage(image);
        
        
        int w = Math.max(base.getWidth(), overlay.getWidth());
        int h = Math.max(base.getHeight(), overlay.getHeight());
        BufferedImage combined  = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        Graphics g = combined.getGraphics();
        g.drawImage(base, 0, 0, null);
        g.drawImage(overlay, 0, 8, null);
        
        Image result = new Image(Display.getCurrent(), convertToSWT(combined));
        return result;
    }
    
    /**
     * Adds a little error-image to the given 16x16-image.
     * @param image Given 16x16-image.
     * @param overlay Overlay image.
     * @return result - new Image with error icon in the bottom-right corner.
     */
    @SuppressWarnings("unused")
    private static Image addErrorToImage(Image image, BufferedImage overlay) {
        BufferedImage base = toBufferedImage(image);
        
        
        int w = Math.max(base.getWidth(), overlay.getWidth());
        int h = Math.max(base.getHeight(), overlay.getHeight());
        BufferedImage combined  = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        Graphics g = combined.getGraphics();
        g.drawImage(base, 0, 0, null);
        g.drawImage(overlay, 8, 8, null);
        
        Image result = new Image(Display.getCurrent(), convertToSWT(combined));
        return result;
    }
}
