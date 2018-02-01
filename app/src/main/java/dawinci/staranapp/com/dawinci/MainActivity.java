package dawinci.staranapp.com.dawinci;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import dawinci.staranapp.com.dawinci.view.DaWinciView;

public class MainActivity extends AppCompatActivity {
    private DaWinciView daWinciView;
    private AlertDialog.Builder currentAlertDialog;
    private ImageView widthImageView;
    private AlertDialog widthLineDialog;
    private AlertDialog colorAlertDialog;

    private SeekBar alphaSeekBar;
    private SeekBar redSeekBar;
    private SeekBar greenSeekBar;
    private SeekBar blueSeekBar;

    private View colorView;
    private boolean eraseMode = false;
    private int curColor;
    private int curWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        daWinciView = findViewById(R.id.view);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clearId:
                daWinciView.clear();
                break;
            case R.id.saveId:
                daWinciView.saveToInternalStorage();
                break;
            case R.id.colorId:
                showColorDialog();
                break;
            case R.id.lineWidth:
                showLineWidthDialog();
                break;
            case R.id.eraseId:
                if(!eraseMode) {
                    eraseMode = true;
                    curColor = daWinciView.getDrawingColor();
                    curWidth = daWinciView.getLineWidth();
                    daWinciView.erase();
                }else{
                    eraseMode = false;
                    daWinciView.setDrawingColor(curColor);
                    daWinciView.setLineWidth(curWidth);
                }
                break;
            case R.id.figureId:
                daWinciView.setMode(1);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    void showFiguresDialog() {

    }

    void showColorDialog() {
        currentAlertDialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.color_dialog, null);
        alphaSeekBar = view.findViewById(R.id.alphaSeekBar);
        redSeekBar = view.findViewById(R.id.redSeekBar);
        greenSeekBar = view.findViewById(R.id.greenSeekBar);
        blueSeekBar = view.findViewById(R.id.blueSeekBar);
        colorView = view.findViewById(R.id.colorView);

        //register SeekBar event listeners
        alphaSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        redSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        greenSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        blueSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);

        int color = daWinciView.getDrawingColor();
        alphaSeekBar.setProgress(Color.alpha(color));
        redSeekBar.setProgress(Color.red(color));
        greenSeekBar.setProgress(Color.green(color));
        blueSeekBar.setProgress(Color.blue(color));

        Button setColorButton = view.findViewById(R.id.setColorButton);
        setColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                daWinciView.setDrawingColor(Color.argb(
                        alphaSeekBar.getProgress(),
                        redSeekBar.getProgress(),
                        greenSeekBar.getProgress(),
                        blueSeekBar.getProgress()
                ));
                colorAlertDialog.dismiss();
                currentAlertDialog = null;
            }
        });

        currentAlertDialog.setView(view);
        colorAlertDialog = currentAlertDialog.create();
        colorAlertDialog.setTitle("Choose color");
        colorAlertDialog.show();

    }

    void showLineWidthDialog() {
        currentAlertDialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.width_dialog, null);
        final SeekBar widthSeekBar = view.findViewById(R.id.widthSeekBar);
        Button setLineWidthButton = view.findViewById(R.id.widthDialogButton);
        widthImageView = view.findViewById(R.id.imageViewId);
        widthSeekBar.setOnSeekBarChangeListener(widthSeekBarChange);
        widthSeekBar.setProgress(daWinciView.getLineWidth());

        setLineWidthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                daWinciView.setLineWidth(widthSeekBar.getProgress());
                widthLineDialog.dismiss();
                currentAlertDialog = null;
            }
        });



        currentAlertDialog.setView(view);
        widthLineDialog = currentAlertDialog.create();
        widthLineDialog.setTitle("Set Line Width");
        widthLineDialog.show();
    }

    private SeekBar.OnSeekBarChangeListener colorSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            //display the cureent color
            colorView.setBackgroundColor(Color.argb(
                    alphaSeekBar.getProgress(),
                    redSeekBar.getProgress(),
                    greenSeekBar.getProgress(),
                    blueSeekBar.getProgress()
            ));

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private SeekBar.OnSeekBarChangeListener widthSeekBarChange = new SeekBar.OnSeekBarChangeListener() {
        Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            Paint p = new Paint();
            p.setColor(daWinciView.getDrawingColor());
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(progress);

            bitmap.eraseColor(Color.WHITE);
            canvas.drawLine(30, 50, 370,50, p);
            widthImageView.setImageBitmap(bitmap);


        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

}
