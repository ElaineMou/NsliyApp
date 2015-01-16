package com.elaine.nsliyapplication.words;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.elaine.nsliyapplication.R;
import com.elaine.nsliyapplication.input.DrawView;
import com.elaine.nsliyapplication.input.Point;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * View that will step through characters stroke by stroke.
 * Created by Elaine on 1/15/2015.
 */
public class ReviewCharView extends View {

    /**
     * File to source strokes from.
     */
    public File charFile=null;
    public static FilenameFilter filenameFilter;
    private List<File> strokeFileList;

    private ArrayList<Bitmap> strokes = new ArrayList<Bitmap>();
    private ArrayList<Point> offsetsFromDisplay = new ArrayList<Point>();

    private Bitmap displayImage;
    private Point offSetOfDisplay;

    private int strokesWritten = 0;
    private Canvas displayCanvas;
    private Bitmap displayBitmap;

    public ReviewCharView(Context context){
        super(context);
    }

    public ReviewCharView(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public ReviewCharView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    public void init(File file){
        this.charFile = file;

        if (filenameFilter == null) {
            filenameFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".png") && filename.startsWith(DrawView.STROKE_PREFIX);
                }
            };
        }

        if(charFile!=null) {
            strokeFileList = Arrays.asList(charFile.listFiles(filenameFilter));
        }
        setBackgroundColor(getResources().getColor(R.color.cream));
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w,h,oldw,oldh);

        if(displayBitmap==null) {
            displayBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            displayCanvas = new Canvas(displayBitmap);
            displayCanvas.drawColor(Color.TRANSPARENT);
        }
        loadValuesFromFile();
    }

    private void loadValuesFromFile(){
        if(charFile!=null && charFile.isDirectory()) {
            File imageFile = new File(charFile, DrawView.DISPLAY_IMAGE_NAME);
            int reqHeight = getWidth();
            int reqWidth = getHeight();

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
            offSetOfDisplay = new Point((getWidth() - displayImage.getWidth())/2,
                    (getHeight() - displayImage.getHeight())/2);


            File offsets = new File(charFile, DrawView.OFFSET_FILE_NAME);
            if (offsets.exists()) {

                Log.v("ReviewView","Offsets exists");
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
                    String[] pairs = stringBuilder.toString().split(DrawView.OFFSETS_SEPARATOR);

                    Log.v("ReviewView","StringBuilder not null");
                    int size = strokeFileList.size();
                    Log.v("ReviewView","Size: " + size + " Pairs length: " + pairs.length);
                    if(pairs.length == size) {
                        Log.v("ReviewView","Pairs length equals strokefile amount");
                        for (int i = 0; i < size; i++) {
                            // For each stroke in file
                            File strokeFile = strokeFileList.get(i);
                            if (strokeFile.exists()) {
                                String[] xy = pairs[i].split(DrawView.XY_SEPARATOR);
                                int x = Integer.parseInt(xy[0]);
                                int y = Integer.parseInt(xy[1]);
                                // Add to offsets
                                offsetsFromDisplay.add(new Point(x * scaleX, y * scaleY));

                                Log.v("ReviewView","Offset added.");

                                // Use previous sample size to create stroke sampled images
                                Bitmap bitmap = BitmapFactory.decodeFile(strokeFile.getAbsolutePath(), options);
                                bitmap = Bitmap.createScaledBitmap(bitmap,
                                        (int) (options.outWidth * scaleX),
                                        (int) (options.outHeight * scaleY), false);
                                strokes.add(bitmap);
                                Log.v("ReviewView", "Stroke bitmap added." + " Size: " + strokes.size());
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

    public boolean addStroke(){
        Log.v("ReviewView","Add, sw: " + strokesWritten + " Strokes size: " + strokes.size());
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

    public boolean removeStroke(){
        Log.v("ReviewView","Remove, sw: " + strokesWritten);
        if(strokesWritten > 0) {
            displayBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            displayCanvas = new Canvas(displayBitmap);
            displayCanvas.drawColor(Color.TRANSPARENT);

            strokesWritten--;

            for (int i = 0; i < strokesWritten; i++) {
                displayCanvas.drawBitmap(strokes.get(strokesWritten),
                        offSetOfDisplay.getX() + offsetsFromDisplay.get(strokesWritten).getX(),
                        offSetOfDisplay.getY() + offsetsFromDisplay.get(strokesWritten).getY(), null);
            }
            invalidate();
            return true;
        }
        return false;
    }

}
