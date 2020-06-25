package com.example.zaccelerometer;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private static final int X_AXIS_INDEX = 0;
    private static final int Y_AXIS_INDEX = 1;
    private static final int Z_AXIS_INDEX = 2;

    // declare our Line chart
    private LineChart mChart;
    private LineData mData;
    private Thread mThread;
    private boolean plotData = true;

    TextView xvalue, yvalue, zvalue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        xvalue = (TextView) findViewById(R.id.xvalue);
        yvalue = (TextView) findViewById(R.id.yvalue);
        zvalue = (TextView) findViewById(R.id.zvalue);

        // Instantiating the SensorManager Class
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Instantiating the object  of sensor class by calling the getDefaultsensor() method
        // of the SensorManager class
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Check if Accelerometer is available or not
        if (mAccelerometer != null) {
            Log.d(TAG,"accelerometer is present");
            // Now register the listener and override two methods onAccuracyCHanged
            // and onSensorChanged
        } else {
            Log.d(TAG,"accelerometer is absent");
        }

        // Instantiating the Charts
        mChart = (LineChart) findViewById(R.id.chart1);
        // Enable Description text
        mChart.getDescription().setEnabled(true);
        mChart.setTouchEnabled(false);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(true);
        mChart.setBackgroundColor(Color.BLUE);
        mChart.setGridBackgroundColor(Color.BLUE);

        mData = new LineData();
        mData.setValueTextColor(Color.BLUE);
        // add empty data
        mChart.setData(mData);

        startPlot(mChart);

    }

    private void startPlot(LineChart mChart) {
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(10f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.setDrawBorders(false);

        feedMultiple();
    }

    private void feedMultiple() {
        if(mThread != null) {
            mThread.interrupt();
        }
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    plotData = true;
                    try {
                        mThread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d(TAG, "onSensorChanged: X: "+ sensorEvent.values[X_AXIS_INDEX] + "Y: " +
                sensorEvent.values[Y_AXIS_INDEX] + "Z: " + sensorEvent.values[Z_AXIS_INDEX]);
        xvalue.setText("     X-axis     " + sensorEvent.values[X_AXIS_INDEX]);
        yvalue.setText("     Y-axis     " + sensorEvent.values[Y_AXIS_INDEX]);
        zvalue.setText("     Z-axis     " + sensorEvent.values[Z_AXIS_INDEX]);

        if (plotData) {
            addEntry(sensorEvent);
            plotData = false;
        }

    }

    private void addEntry(SensorEvent sensorEvent) {
        LineData data = mChart.getData();
 
        if (data != null) {
            ILineDataSet xData = data.getDataSetByIndex(X_AXIS_INDEX);
            ILineDataSet yData = data.getDataSetByIndex(Y_AXIS_INDEX);
            ILineDataSet zData = data.getDataSetByIndex(Z_AXIS_INDEX);

            if (xData == null) {
                xData = createSetX();
                mChart.getData().addDataSet(xData);
            }
            if (yData == null) {
                yData = createSetY();
                mChart.getData().addDataSet(yData);
            }
            if (zData == null) {
                zData = createSetZ();
                mChart.getData().addDataSet(zData);
            }

            if (xData != null ) {
                mChart.getData().addEntry(new Entry(xData.getEntryCount(), sensorEvent.values[X_AXIS_INDEX] + 5), X_AXIS_INDEX);
                mChart.getData().notifyDataChanged();
                mChart.notifyDataSetChanged();
                mChart.setVisibleXRangeMaximum(150);
                mChart.moveViewToX(mChart.getData().getEntryCount());
                mChart.moveViewToX(mChart.getData().getXMax());
            }
            if (yData != null) {
                mChart.getData().addEntry(new Entry(yData.getEntryCount(), sensorEvent.values[Y_AXIS_INDEX]),  Y_AXIS_INDEX);
                mChart.getData().notifyDataChanged();
                mChart.notifyDataSetChanged();
                mChart.setVisibleXRangeMaximum(150);
                mChart.moveViewToX(mChart.getData().getEntryCount());
                mChart.moveViewToX(mChart.getData().getXMax());

            }
            if (zData != null) {
                mChart.getData().addEntry(new Entry(zData.getEntryCount(), sensorEvent.values[Z_AXIS_INDEX]), Z_AXIS_INDEX);
                mChart.getData().notifyDataChanged();
                mChart.notifyDataSetChanged();
                mChart.setVisibleXRangeMaximum(150);
                mChart.moveViewToX(mChart.getData().getEntryCount());
                mChart.moveViewToX(mChart.getData().getXMax());
            }

        }
    }



   private ILineDataSet createSetX() {
        LineDataSet set = new LineDataSet(null, "Real Time X-Axis Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setFormLineWidth(3f);
        set.setColor(Color.MAGENTA);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private ILineDataSet createSetY() {
        LineDataSet set = new LineDataSet(null, "Real Time Y-Axis Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setFormLineWidth(3f);
        set.setColor(Color.CYAN);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private ILineDataSet createSetZ() {
        LineDataSet set = new LineDataSet(null, "Real Time Z-Axis Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setFormLineWidth(3f);
        set.setColor(Color.YELLOW);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }



    @Override
    protected void onPause() {
        // unregister listener to make sure battery is not drained because of this
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        mSensorManager.unregisterListener(MainActivity.this);
        mThread.interrupt();
        super.onDestroy();
    }

}