package com.elaine.nsliyapplication.words;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import com.elaine.nsliyapplication.R;
import com.elaine.nsliyapplication.input.DrawView;
import com.elaine.nsliyapplication.input.Point;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * View that will step through characters stroke by stroke.
 * Created by Elaine on 1/15/2015.
 */
public class ReviewCharView extends View {

    /**
     * Size in dp for all ReviewCharViews
     */
    public static final int viewSize = 150;

    /**
     * File to source strokes from.
     */
    public File charFile=null;
    /**
     * Filters for only stroke images
     */
    public static FilenameFilter filenameFilter;
    /**
     * List of files for the strokes in this character.
     */
    private ArrayList<File> strokeFileList;

    /**
     * Stroke images for this view.
     */
    private ArrayList<Bitmap> strokes = new ArrayList<Bitmap>();
    /**
     * Offsets from the full character display image's top left corner for the strokes.
     */
    private ArrayList<Point> offsetsFromDisplay = new ArrayList<Point>();

    /**
     * The full display image for the complete character.
     */
    private Bitmap displayImage;
    /**
     * Offset from (0,0) of the display image.
     */
    private Point offSetOfDisplay;

    /**
     * Strokes already placed down on the view.
     */
    private int strokesWritten = 0;
    /**
     * The canvas onto which images are drawn.
     */
    private Canvas displayCanvas;
    /**
     * The bitmap updated through the display canvas.
     */
    private Bitmap displayBitmap;

    /**
     * Density value used for loading images.
     */
    private float scale;

    public ReviewCharView(Context context){
        super(context);
        scale = context.getResources().getDisplayMetrics().density;
    }

    public ReviewCharView(Context context, AttributeSet attrs) {
        super(context,attrs);
        scale = context.getResources().getDisplayMetrics().density;
    }

    public ReviewCharView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        scale = context.getResources().getDisplayMetrics().density;
    }

    /**
     * Loads stroke images from file.
     * @param file
     */
    public void init(File file){
        this.charFile = file;

        // Sets filter to only accept PNGs with the stroke prefix.
        if (filenameFilter == null) {
            filenameFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".png") && filename.startsWith(DrawView.STROKE_PREFIX);
                }
            };
        }

        // Fills stroke files list with valid image files
        if(charFile!=null) {
            strokeFileList = new ArrayList<File>();
            String[] strokeFileNames = charFile.list(filenameFilter);
            int size = strokeFileNames.length;
            for(int i=0;i<size;i++){
                String name = DrawView.STROKE_PREFIX + i + ".png";
                File strokeFile = new File(charFile,name);
                if(strokeFile.exists()){
                    strokeFileList.add(strokeFile);
                }
            }
        }
        setBackgroundColor(getResources().getColor(R.color.g50));
    }

    /**
     * Creates canvas if it is null or improperly sized, as long as width and height are greater than 0.
     */
    public void makeCanvas(){
        int width = getWidth();
        int height = getHeight();
        if(width > 0 && height > 0 && (displayCanvas == null || displayCanvas.getWidth() != width || displayCanvas.getHeight() != height)) {
            displayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            displayCanvas = new Canvas(displayBitmap);
            displayCanvas.drawColor(Color.TRANSPARENT);
        }
    }

    /**
     * Loads images from the loaded file list.
     * @throws JSONException
     */
    public void loadValuesFromFile() throws JSONException {
        strokes.clear();
        offsetsFromDisplay.clear();
        strokesWritten = 0;
        if(charFile!=null && charFile.isDirectory()) {
            File imageFile = new File(charFile, DrawView.DISPLAY_IMAGE_NAME);
            int reqHeight = (int)(viewSize*scale);
            int reqWidth = (int) (viewSize*scale);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            int inSampleSize = 1;

            // Find greatest inSampleSize where image is just larger than bounds
            if (imageHeight > reqHeight || imageWidth > reqWidth) {
                final int halfHeight = imageHeight / 2;
                final int halfWidth = imageWidth / 2;

                while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }

            }

            // Create sampled bitmap at smallest size (divided by power of 2) larger than given bounds
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;
            Bitmap sampleBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

            int width;
            int height;

            float scaleX = (float) reqWidth / options.outWidth;
            float scaleY = (float) reqHeight / options.outHeight;

            // If x coordinates need to be shrunk more than y coordinates
            if (scaleX < scaleY) {
                // Match width to bounds and scale height accordingly
                width = reqWidth;
                height = (int) (((float) imageHeight / imageWidth) * width);

                scaleY = ((float) height) / options.outHeight;
            } else { // If y coordinates need to be shrunk more than x coordinates
                // Match height to bounds and scale width accordingly
                height = reqHeight;
                width = (int) (((float) imageWidth / imageHeight) * height);

                scaleX = ((float) width) / options.outWidth;
            }
            // Return scaled bitmap of thumbnail size
            displayImage = Bitmap.createScaledBitmap(sampleBitmap, width, height, false);
            offSetOfDisplay = new Point((reqWidth - displayImage.getWidth())/2,
                    (reqHeight - displayImage.getHeight())/2);

            File offsets = new File(charFile, DrawView.OFFSET_FILE_NAME);
            if (offsets.exists()) {
                BufferedReader bufferedReader = null;
                StringBuilder stringBuilder = null;
                try {
                    bufferedReader = new BufferedReader(new FileReader(offsets));
                    stringBuilder = new StringBuilder();
                    String line = bufferedReader.readLine();

                    while (line != null) {
                        stringBuilder.append(line);
                        line = bufferedReader.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (stringBuilder != null) {
                    JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                    int length = jsonArray.length();
                    int size = strokeFileList.size();

                    if(length == size) {
                        for (int i = 0; i < size; i++) {
                            // For each stroke in file
                            File strokeFile = strokeFileList.get(i);
                            if (strokeFile.exists()) {
                                JSONArray coordinates = jsonArray.getJSONArray(i);
                                int x = coordinates.getInt(0);
                                int y = coordinates.getInt(1);
                                // Add to offsets
                                offsetsFromDisplay.add(new Point(x * (scaleX/inSampleSize), y * (scaleY/inSampleSize)));

                                // Use previous sample size to create stroke sampled images
                                Bitmap bitmap = BitmapFactory.decodeFile(strokeFile.getAbsolutePath(), options);
                                bitmap = Bitmap.createScaledBitmap(bitmap,
                                        (int) (options.outWidth * scaleX),
                                        (int) (options.outHeight * scaleY), false);
                                strokes.add(bitmap);
                            }
                        }
                    }
                }
            }
        } else {
            setBackgroundResource(R.drawable.question_mark);
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(displayBitmap !=null) {
            canvas.drawBitmap(displayBitmap, 0, 0, null);
        } else {
            displayBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            displayCanvas = new Canvas(displayBitmap);
            displayCanvas.drawColor(Color.TRANSPARENT);
        }
    }

    /**
     * Clears view, sets stroke count to 0.
     * @return if strokes were actually cleared; false if already empty.
     */
    public boolean clear(){
        if(strokesWritten > 0) {
            displayBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            displayCanvas = new Canvas(displayBitmap);
            displayCanvas.drawColor(Color.TRANSPARENT);
            invalidate();

            strokesWritten = 0;
            return true;
        }
        return false;
    }

    /**
     * Fills view with all strokes and sets stroke count to max.
     * @return - if character was drawn; false if already full of all strokes.
     */
    public boolean drawChar(){
        if(strokesWritten<strokes.size()) {
            displayCanvas.drawBitmap(displayImage, offSetOfDisplay.getX(), offSetOfDisplay.getY(), null);
            strokesWritten = strokes.size();
            invalidate();

            return true;
        }
        return false;
    }

    /**
     * Adds a stroke to the view and updates the strokesWritten value, unless already completely filled.
     * @return - true if stroke is added, false if all strokes already added in.
     */
    public boolean addStroke(){
        if(strokesWritten < strokes.size()) {
            displayCanvas.drawBitmap(strokes.get(strokesWritten),
                    offSetOfDisplay.getX() + offsetsFromDisplay.get(strokesWritten).getX(),
                    offSetOfDisplay.getY() + offsetsFromDisplay.get(strokesWritten).getY(), null);
            strokesWritten++;
            invalidate();
            return true;
        }
        return false;
    }

    /**
     * Removes a stroke and updates strokesWritten, unless already empty.
     * @return - true if stroke removed, false if already empty.
     */
    public boolean removeStroke(){
        if(strokesWritten > 0) {
            displayBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            displayCanvas = new Canvas(displayBitmap);
            displayCanvas.drawColor(Color.TRANSPARENT);

            strokesWritten--;

            for (int i = 0; i < strokesWritten; i++) {
                displayCanvas.drawBitmap(strokes.get(i),
                        offSetOfDisplay.getX() + offsetsFromDisplay.get(i).getX(),
                        offSetOfDisplay.getY() + offsetsFromDisplay.get(i).getY(), null);
            }
            invalidate();
            return true;
        }
        return false;
    }

}
