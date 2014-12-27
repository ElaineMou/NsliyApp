package com.elaine.nsliyapplication.input;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * Inspired by Eric Burke's SquareUp post.
 * Created by Elaine on 10/23/2014.
 */
public class DrawView extends View {

    /**
     * Name of text file for offsets data
     */
    public static final String OFFSET_FILE_NAME = "offsets.txt";
    /**
     * Name of display bitmap file to show complete character.
     */
    public static final String DISPLAY_IMAGE_NAME = "display.png";
    /**
     * Minimum width for ink strokes.
     */
    private static final float MIN_STROKE_WIDTH = 15f;
    /**
     * Maximum width of ink strokes.
     */
    private static final float MAX_STROKE_WIDTH = 45f;
    /**
     * Degree of previous events' velocity affecting current width.
     */
    private static final float VELOCITY_FILTER_WEIGHT = .7f;
    /**
     * Maximum velocity received by view.
     */
    private static final float MAX_VELOCITY = 6f;

    /**
     * Paint used to draw.
     */
    private Paint paint = new Paint();

    /**
     * Paint used for debugging.
     */
    private Paint redPaint = new Paint();

    /**
     * Width of drawn strokes.
     */
    private float strokeWidth = MIN_STROKE_WIDTH;
    /**
     * Velocity recorded at previous touch event.
     */
    private float lastVelocity = 0f;

    /**
     * Coordinates of last stroke.
     */
    RectF lastStroke = new RectF();
    /**
     * Bounds of all strokes so far.
     */
    ArrayList<RectF> rawStrokeBounds = new ArrayList<RectF>();
    /**
     * Bounds of character being written.
     */
    RectF rawCharacterBounds;

    /**
     * Bitmap for displaying to user.
     */
    Bitmap displayBitmap;
    /**
     * Canvas used for drawing to displayBitmap.
     */
    Canvas displayCanvas;

    /**
     * Canvas used to draw onto currentStroke.
     */
    Canvas currentStrokeCanvas;
    /**
     * Current stroke image being drawn onto
     */
    Bitmap currentStroke;
    /**
     * Bitmap images being written to.
     */
    ArrayList<Bitmap> strokes = new ArrayList<Bitmap>();
    /**
     * Bitmap coordinates to offset strokes by.
     */
    ArrayList<Point> offsetsFromCorner = new ArrayList<Point>();

    /**
     * List of touch points laid down in a single stroke.
     */
    private ArrayList<Point> touchPoints = new ArrayList<Point>();

    /**
     * The dirty rectangle to update the drawing on.
     */
    private final RectF rectToUpdate = new RectF();

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize paint values
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5f);

        setBackgroundColor(Color.WHITE);

        redPaint = new Paint(paint);
        redPaint.setColor(Color.RED);
    }

    // Clear screen
    public void clear(){
        touchPoints.clear();
        displayBitmap = null;
        strokes.clear();
        offsetsFromCorner.clear();
        rawCharacterBounds = null;
        rawStrokeBounds.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(displayBitmap !=null) {
            canvas.drawBitmap(displayBitmap,0,0,null);
        }
    }

    /**
     * Handles motion/touch events from user.
     * @param event - MotionEvent from user.
     * @return - Success or failure.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(displayBitmap == null) {
            // Create displayBitmap with transparency, new canvas to draw onto displayBitmap with.
            displayBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            displayCanvas = new Canvas(displayBitmap);
            displayCanvas.drawColor(Color.TRANSPARENT);
        }

        // boolean to return
        boolean result = true;

        // Save event's coordinates
        float eventX = event.getX();
        float eventY = event.getY();
        int eventAction = event.getAction();

        if(rawCharacterBounds == null){
            rawCharacterBounds = new RectF(eventX, eventY, eventX, eventY);
        }

        if(touchPoints.isEmpty() || eventAction != MotionEvent.ACTION_MOVE ||
                touchPoints.get(touchPoints.size() - 1).distanceTo(new Point(eventX,eventY)) > 3f) {

            // Depending on what type of event
            switch (eventAction) {
                // First touch down by user
                case MotionEvent.ACTION_DOWN:
                    // Create bitmap with transparency, new canvas to draw onto bitmap with.
                    currentStroke = Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);
                    currentStrokeCanvas = new Canvas(currentStroke);
                    currentStrokeCanvas.drawColor(Color.TRANSPARENT);

                    // Relocate/clear dirty rectangle and current stroke bounds
                    relocateRect(lastStroke,eventX,eventY);
                    relocateRectToUpdate(eventX, eventY);
                    // Initialize stroke width and velocity
                    lastVelocity = 0f;
                    strokeWidth = MIN_STROKE_WIDTH;

                    // Empty touch points and add new event's coordinates
                    touchPoints = new ArrayList<Point>();
                    touchPoints.add(new Point(eventX, eventY, event.getEventTime()));
                    result = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    // If we have recorded a touch down before this move/touch up
                    if(!touchPoints.isEmpty()) {
                        // Expand dirty rectangle to new event coordinates
                        expandRectToUpdate(eventX, eventY);
                        // Expand dirty rectangle to any previous touch points
                        for (Point point : touchPoints) {
                            expandRectToUpdate(point.getX(), point.getY());
                        }
                        int historySize = event.getHistorySize();

                        // Expand dirty rectangle to all points in history
                        for (int i = 0; i < historySize; i++) {
                            float historicalX = event.getHistoricalX(i);
                            float historicalY = event.getHistoricalY(i);
                            expandRectToUpdate(historicalX, historicalY);
                            // Add all points in history to touch points
                            touchPoints.add(new Point(historicalX, historicalY, event.getHistoricalEventTime(i)));

                            // When touchPoints has odd number of points
                            int size = touchPoints.size();
                            if (size % 2 == 1) {
                                // Add Bezier curve between last three points,
                                float velocity = touchPoints.get(size - 1).
                                        velocityFrom(touchPoints.get(size - 3));
                                velocity = VELOCITY_FILTER_WEIGHT * velocity +
                                        (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
                                float newWidth = strokeWidth(velocity);
                                Bezier bezier = new Bezier(touchPoints.get(size - 3),
                                        touchPoints.get(size - 2), touchPoints.get(size - 1),
                                        strokeWidth, newWidth);
                                bezier.paintCurve(currentStrokeCanvas, paint);
                                bezier.paintCurve(displayCanvas, paint);
                                lastVelocity = velocity;
                                strokeWidth = newWidth;
                            }
                        }

                        // Add event point as last touch point if moving (avoid end blot)
                        if(eventAction == MotionEvent.ACTION_MOVE) {
                            touchPoints.add(new Point(eventX, eventY, event.getEventTime()));
                            int size = touchPoints.size();
                            // Add curve if we have reached an odd number of touch points
                            if (size % 2 == 1) {
                                float velocity = touchPoints.get(size - 1).
                                        velocityFrom(touchPoints.get(size - 3));
                                velocity = VELOCITY_FILTER_WEIGHT * velocity +
                                        (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
                                float newWidth = strokeWidth(velocity);
                                Bezier bezier = new Bezier(touchPoints.get(size - 3),
                                        touchPoints.get(size - 2), touchPoints.get(size - 1),
                                        strokeWidth, newWidth);
                                bezier.paintCurve(currentStrokeCanvas, paint);
                                bezier.paintCurve(displayCanvas, paint);
                                lastVelocity = velocity;
                                strokeWidth = newWidth;
                                // Save last point if a curve to it has been completed
                                Point pointToSave = touchPoints.get(size - 1);
                                touchPoints = new ArrayList<Point>();
                                touchPoints.add(pointToSave);
                            // Save last two points if no curve has been made through them
                            } else if (size % 2 == 0) {
                                Point point1 = touchPoints.get(size - 2);
                                Point point2 = touchPoints.get(size - 1);
                                touchPoints = new ArrayList<Point>();
                                touchPoints.add(point1);
                                touchPoints.add(point2);
                            }
                            // Expand stroke bounds to include this event
                            expandRectWithRect(lastStroke, rectToUpdate);
                        // If finishing stroke
                        } else if (eventAction == MotionEvent.ACTION_UP) {
                            int size = touchPoints.size();
                            // Add linear line to final point if no curve has been made
                            if (size % 2 == 0) {
                                float velocity = touchPoints.get(size - 1).
                                        velocityFrom(touchPoints.get(size - 2));
                                velocity = VELOCITY_FILTER_WEIGHT * velocity +
                                        (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
                                float newWidth = strokeWidth(velocity);
                                Bezier bezier = new Bezier(touchPoints.get(size - 2), touchPoints.get(size - 1),
                                        strokeWidth, 0);
                                bezier.paintCurve(currentStrokeCanvas, paint);
                                bezier.paintCurve(displayCanvas, paint);
                                lastVelocity = velocity;
                                strokeWidth = newWidth;
                            }
                            // Expand stroke bounds to include this event, save copy to bounds list
                            expandRectWithRect(lastStroke,rectToUpdate);
                            rawStrokeBounds.add(new RectF(lastStroke));
                            // Find new stroke's boundaries within borders
                            int x = (int) Math.max(lastStroke.left - MAX_STROKE_WIDTH, 0);
                            int y = (int) Math.max(lastStroke.top - MAX_STROKE_WIDTH, 0);
                            int newStrokeWidth = (int) Math.min(
                                    lastStroke.width() + 2*MAX_STROKE_WIDTH, getWidth() - x);
                            int newStrokeHeight = (int) Math.min(
                                    lastStroke.height() + 2*MAX_STROKE_WIDTH, getHeight() - y);
                            // Excise stroke bounds from bitmap
                            Bitmap newStroke = Bitmap.createBitmap(currentStroke,
                                    x,y, newStrokeWidth, newStrokeHeight);
                            // Add new smaller stroke bitmap
                            strokes.add(newStroke);
                            offsetsFromCorner.add(new Point(x,y));
                            currentStroke = null;
                            // Expand character bounds to include this event
                            expandRectWithRect(rawCharacterBounds, lastStroke);
                            invalidate();
                        }

                        // Redraw the dirtied area
                        invalidate(
                                (int) (rectToUpdate.left - MAX_STROKE_WIDTH),
                                (int) (rectToUpdate.top - MAX_STROKE_WIDTH),
                                (int) (rectToUpdate.right + MAX_STROKE_WIDTH),
                                (int) (rectToUpdate.bottom + MAX_STROKE_WIDTH)
                        );
                        // Clear dirty rectangle
                        relocateRectToUpdate(eventX, eventY);
                    }
                    result = true;
                    break;
                default:
                    result = false;
            }
        }

        return result;
    }

    /**
     * Calculates stroke width based on velocity input.
     * @param velocity - Input velocity
     * @return - Width of stroke to be drawn
     */
    private float strokeWidth(float velocity) {
        float width = MIN_STROKE_WIDTH;
        if(velocity < MAX_VELOCITY){
            width = MIN_STROKE_WIDTH +
                    (MAX_STROKE_WIDTH - MIN_STROKE_WIDTH)*(1 - velocity/MAX_VELOCITY);
        } else {
            width = MIN_STROKE_WIDTH;
        }

        return width;
    }

    /**
     * Expands rectangle to include new point.
     * @param x - X coordinate of new point
     * @param y - Y coordinate of new point
     */
    private void expandRectToUpdate(float x, float y){
        if(x < rectToUpdate.left){
            rectToUpdate.left = x;
        } else if (x > rectToUpdate.right){
            rectToUpdate.right = x;
        }
        if(y < rectToUpdate.top){
            rectToUpdate.top = y;
        } else if (y > rectToUpdate.bottom){
            rectToUpdate.bottom = y;
        }
    }

    /**
     * Reset rectangle to a new location with 0 area.
     * @param x - X coordinate of rectangle
     * @param y - Y coordinate of rectangle.
     */
    private void relocateRectToUpdate(float x, float y){
        rectToUpdate.left = x;
        rectToUpdate.right = x;
        rectToUpdate.top = y;
        rectToUpdate.bottom = y;
    }

    /**
     * Expands first rectangle to include 2nd rectangle's bounds.
     * @param rectangle1 - Rectangle to be expanded.
     * @param rectangle2 - Rectangle used as input.
     */
    private void expandRectWithRect(RectF rectangle1, RectF rectangle2){
        if(rectangle2.left < rectangle1.left) {
            rectangle1.left = rectangle2.left;
        }
        if (rectangle2.right > rectangle1.right){
            rectangle1.right = rectangle2.right;
        }
        if(rectangle2.top < rectangle1.top) {
            rectangle1.top = rectangle2.top;
        }
        if (rectangle2.bottom > rectangle1.bottom){
            rectangle1.bottom = rectangle2.bottom;
        }
    }

    /**
     * Moves rectangle to coordinates with no area.
     * @param rectangle - Rectangle to be relocated.
     * @param x - X coordinate to move to
     * @param y - Y coordinate to move to
     */
    private void relocateRect(RectF rectangle, float x, float y){
        rectangle.left = x;
        rectangle.right = x;
        rectangle.top = y;
        rectangle.bottom = y;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void undo() {
        // If any set is empty, clear all data of previous strokes.
        if(strokes.isEmpty() || rawStrokeBounds.isEmpty() || offsetsFromCorner.isEmpty()){
            displayBitmap = null;
            rawCharacterBounds = null;

            strokes.clear();
            rawStrokeBounds.clear();
            offsetsFromCorner.clear();
        } else {
            //Remove last stroke from memory
            strokes.remove(strokes.size() - 1);
            rawStrokeBounds.remove(rawStrokeBounds.size() - 1);
            offsetsFromCorner.remove(offsetsFromCorner.size() - 1);
            touchPoints.clear();

            // If any set is newly empty, clear all data of previous strokes.
            if (strokes.isEmpty() || rawStrokeBounds.isEmpty() || offsetsFromCorner.isEmpty()) {
                displayBitmap = null;
                rawCharacterBounds = null;

                strokes.clear();
                rawStrokeBounds.clear();
                offsetsFromCorner.clear();
            } else { // If there are still strokes on the screen
                // Recreate displayBitmap with transparency, new canvas to draw onto displayBitmap with.
                displayBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                displayCanvas = new Canvas(displayBitmap);
                displayCanvas.drawColor(Color.TRANSPARENT);

                // Relocate raw character bounds, then redraw screen/expand bounds through all previous
                relocateRect(rawCharacterBounds, rawStrokeBounds.get(0).left, rawStrokeBounds.get(0).top);
                for (int i = 0; i < strokes.size(); i++) {
                    displayCanvas.drawBitmap(strokes.get(i),
                            offsetsFromCorner.get(i).getX(), offsetsFromCorner.get(i).getY(), null);
                    expandRectWithRect(rawCharacterBounds, rawStrokeBounds.get(i));
                }
            }
        }

        invalidate();
    }

    /**
     * Saves current character and strokes to file.
     * @param context - Activity passed in to get directory
     */
    public void saveCharacter(Context context){
        boolean success = false;
        File directory = null;
        if(isExternalStorageWritable()) { // If we can write to external storage
            // Check there are strokes and data matches up
            if (strokes.size() > 0 && strokes.size() == offsetsFromCorner.size()) {
                // Get the directory for the app's private pictures directory.
                int n;
                Random random = new Random();
                String directoryName;
                // Guarantee a unique folder for new character
                do{
                    n = random.nextInt(1000);
                    directoryName = "character-" + n;
                    directory = new File(context.getExternalFilesDir(
                            Environment.DIRECTORY_PICTURES), directoryName);
                }while(directory.exists());

                // Create directory, quit if not possible
                if (directory.mkdir()) {
                    success = true;
                    // For each stroke to send
                    int size = strokes.size();
                    for (int i = 0; i < size; i++) {
                        // Generate a numbered name
                        String fileName = "stroke-" + i + ".png";
                        File imageFile = new File(directory, fileName);
                        if (imageFile.exists()) {
                            imageFile.delete();
                        }
                        // Compress and send stroke image to file
                        try {
                            FileOutputStream out = new FileOutputStream(imageFile);
                            strokes.get(i).compress(Bitmap.CompressFormat.PNG, 90, out);
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            success = false;
                        }
                    }

                    RectF realCharacterBounds = new RectF();

                    // Find new character's boundaries within borders
                    realCharacterBounds.left = Math.max(
                            rawCharacterBounds.left - MAX_STROKE_WIDTH, 0);
                    realCharacterBounds.top = Math.max(
                            rawCharacterBounds.top - MAX_STROKE_WIDTH, 0);
                    realCharacterBounds.right = Math.min(
                            rawCharacterBounds.right + MAX_STROKE_WIDTH, getWidth() );
                    realCharacterBounds.bottom = Math.min(
                            rawCharacterBounds.bottom + MAX_STROKE_WIDTH, getHeight() );
                    // Excise character from screen bitmap
                    Bitmap characterImage = Bitmap.createBitmap(displayBitmap,
                            (int) realCharacterBounds.left, (int) realCharacterBounds.top,
                            (int) realCharacterBounds.width(), (int) realCharacterBounds.height());

                    // Make file location for character image
                    File imageFile = new File(directory, DISPLAY_IMAGE_NAME);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                    // Compress and send image to file
                    try {
                        FileOutputStream out = new FileOutputStream(imageFile);
                        characterImage.compress(Bitmap.CompressFormat.PNG, 90, out);
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        success = false;
                    }
                    // Build text line of coordinates from character's top left corner
                    StringBuilder strBuilder = new StringBuilder();
                    size = offsetsFromCorner.size();
                    for(int i=0;i<size;i++){
                        Point coordinates = offsetsFromCorner.get(i);
                        strBuilder.append((int) (coordinates.getX() - realCharacterBounds.left) )
                                .append(",")
                                .append((int) (coordinates.getY() - realCharacterBounds.top) )
                                .append(";");
                    }

                    // Create new text file
                    File textFile = new File(directory,OFFSET_FILE_NAME);
                    if(textFile.exists()){
                        textFile.delete();
                    }
                    FileOutputStream outputStream;
                    try {
                        outputStream = new FileOutputStream(textFile);
                        outputStream.write(strBuilder.toString().getBytes());
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        success = false;
                    }

                }
            }
        }
        // Notify user if we could save to external storage
        CharSequence text = "";
        if(success) {
            text = "Successfully added character.";
        } else {
            text = "Failed to save.";
            // Clear the folder if incomplete
            if(directory!=null && directory.exists()){
                directory.delete();
            }
        }

        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = widthSize;
        int height;

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(width, heightSize);
        } else {
            //Be whatever you want
            height = width;
        }

        setMeasuredDimension(width,height);
    }
}
