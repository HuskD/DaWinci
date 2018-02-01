package dawinci.staranapp.com.dawinci.view;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;

/**
 * Created by danie on 20.01.2018.
 */

public class DaWinciView extends View {

    public static final float TOUCH_TOLERANCE = 10;

    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paintScreen;
    private Paint paintLine;
    private HashMap<Integer, Path> pathMap;
    private HashMap<Integer, Point> previousPointMap;
    private int mode = 0;

    float pointX;
    float pointY;
    float startX;
    float startY;

    public DaWinciView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        paintScreen = new Paint();

        paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.BLACK);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(7f);
        paintLine.setStrokeCap(Paint.Cap.ROUND);

        pathMap = new HashMap<>();
        previousPointMap = new HashMap<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if(mode==0) {
            bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(bitmap);
            bitmap.eraseColor(Color.WHITE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);

        switch (mode) {
            case 0:
                for(Integer key: pathMap.keySet()) {
                    canvas.drawPath(pathMap.get(key), paintLine);
                }
                break;
            case 1:

                    canvas.drawRect(startX, startY, pointX, pointY, paintLine);
                break;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        if(mode == 0) {
            int action = event.getActionMasked(); //event type
            int actionIndex = event.getActionIndex(); // pointer ( finger, mouse)

            if (action == MotionEvent.ACTION_DOWN ||
                    action == MotionEvent.ACTION_POINTER_UP) {
                touchStarted(event.getX(actionIndex),
                        event.getY(actionIndex),
                        event.getPointerId(actionIndex));
            } else if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_POINTER_UP) {
                touchEnded(event.getPointerId(actionIndex));
            } else {
                touchMoved(event);
            }

            invalidate(); // redraw the screen



        }

        if(mode == 1) {
            pointX = event.getX();
            pointY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = pointX;
                    startY = pointY;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    break;
            }


            invalidate();
        }

        return true;
    }

    private void touchMoved(MotionEvent event) {

        if(mode == 0) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                int pointerId = event.getPointerId(i);
                int pointerIndex = event.findPointerIndex(pointerId);

                if (pathMap.containsKey(pointerId)) {
                    float newX = event.getX(pointerIndex);
                    float newY = event.getY(pointerIndex);

                    Path path = pathMap.get(pointerId);
                    Point point = previousPointMap.get(pointerId);

                    //Calculate how far the user moved from the last update
                    float deltaX = Math.abs(newX - point.x);
                    float deltaY = Math.abs(newY - point.y);

                    //If the distance is significant enough to be considered a movement,  then \/
                    if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                        //move path to the new direction
                        path.quadTo(point.x, point.y, (newX + point.x) / 2,
                                (newY + point.y) / 2);

                        //store the new coordinates
                        point.x = (int) newX;
                        point.y = (int) newY;
                    }
                }
            }

        }

    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setDrawingColor(int color) {
        paintLine.setColor(color);
    }

    public int getDrawingColor() {
        return paintLine.getColor();
    }

    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }

    public int getLineWidth() {
        return (int) paintLine.getStrokeWidth();
    }

    public void clear() {
        pathMap.clear();  //removes all of the paths
        previousPointMap.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate(); //refresh the screen
    }

    public void erase(){
        paintLine.setColor(Color.WHITE);
        paintLine.setStrokeWidth(35f);
    }

    private void touchEnded(int pointerId) {
        Path path = pathMap.get(pointerId);      // get the corresponding path
        bitmapCanvas.drawPath(path, paintLine);  //draw to bitmap Canvas obj.
        path.reset();
        
    }

    private void touchStarted(float x, float y, int pointerId) {
        Path path;   // store the path for given touch
        Point point; // store the lst point in path

        if(pathMap.containsKey(pointerId)) {
            path = pathMap.get(pointerId);
            point = previousPointMap.get(pointerId);
        } else {
            path = new Path();
            pathMap.put(pointerId, path);
            point = new Point();
            previousPointMap.put(pointerId, point);
        }

        //move to the coordinates of the touch
        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;
    }

    public void saveImage() {
        String fileName = "DaWinci" + System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");


        //get a URI for the location to save the file
        Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);

        try {
            OutputStream outputStream =
                    getContext().getContentResolver().openOutputStream(uri);

            //copy the bitmap to the output stream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 120, outputStream); // this is our image

            outputStream.flush();
            outputStream.close();

            Toast message = Toast.makeText(getContext(), "Image Saved", Toast.LENGTH_LONG);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
            message.show();
            
        } catch (FileNotFoundException e) {
            Toast message = Toast.makeText(getContext(), "Image Not Saved", Toast.LENGTH_LONG);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
            message.show();

            e.printStackTrace();
        } catch (IOException e) {
            Toast message = Toast.makeText(getContext(), "Image Not Saved", Toast.LENGTH_LONG);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
            message.show();
            e.printStackTrace();
        }

    }

    public void saveToInternalStorage(){
        ContextWrapper cw = new ContextWrapper(getContext());
        String fileName = "DaWinci" + System.currentTimeMillis();
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("daWinciDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,fileName + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            Toast message = Toast.makeText(getContext(), "Image Not Saved", Toast.LENGTH_LONG);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
            message.show();
            e.printStackTrace();
            e.printStackTrace();
        } finally {
            try {
                fos.flush();
                fos.close();
                Log.d("Image: ", directory.getAbsolutePath());
                Toast message = Toast.makeText(getContext(), "Image Saved", Toast.LENGTH_LONG);
                message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
                message.show();
            } catch (IOException e) {
                Toast message = Toast.makeText(getContext(), "Image Not Saved", Toast.LENGTH_LONG);
                message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
                message.show();
                e.printStackTrace();
                e.printStackTrace();
            }
        }
       // return directory.getAbsolutePath();
    }
}
