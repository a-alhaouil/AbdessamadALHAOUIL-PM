package com.example.abdessamadalhaouil_pm;


import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserActivities extends AppCompatActivity implements SensorEventListener {
     SensorManager sensorManager;
     Sensor accelerometer;
     float[] gravity = new float[3];
     float[] linear_acceleration = new float[3];
     TextView activityTextView;
     TextView walkingTextView;
     TextView jumpingTextView;
     TextView sittingTextView;
     TextView standingTextView;
     TextView runningTextView;


//    private TextView xText;
//    private TextView yText;
//    private TextView zText;
     LinearLayout linearLayoutassis;
     LinearLayout linearLayoutdebout;
     LinearLayout linearLayoutsauter;

     LinearLayout linearLayoutmarcher;
     LinearLayout linearLayoutrunning;


     int[] confidenceValues = new int[5];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_activities);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

//        activityTextView = findViewById(R.id.activity_text_view);
        sittingTextView  = findViewById(R.id.sitting_prob);
        standingTextView  = findViewById(R.id.standing_prob);
        jumpingTextView  = findViewById(R.id.jumping_prob);
        walkingTextView  = findViewById(R.id.walking_prob);
        runningTextView  = findViewById(R.id.running_prob);



//        xText = findViewById(R.id.text_view_x);
//        yText = findViewById(R.id.text_view_y);
//        zText = findViewById(R.id.text_view_z);

        // Initialize your LinearLayouts
        linearLayoutdebout = findViewById(R.id.standing_row);
        linearLayoutassis = findViewById(R.id.sitting_row);
        linearLayoutmarcher = findViewById(R.id.walking_row);
        linearLayoutsauter = findViewById(R.id.jumping_row);
        linearLayoutrunning = findViewById(R.id.running_row);




    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x =event.values[0];
        float y =event.values[1];
        float z =event.values[2];
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            final float alpha = 0.8f;
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            // Determine the activity based on the accelerometer data
            int activity = getActivity(linear_acceleration);
//            xText.setText(String.format("X : %.2f", x));
//            yText.setText(String.format("Y : %.2f", y));
//            zText.setText(String.format("Z : %.2f", z));



            // Update the confidence values in the table
            confidenceValues[activity] += 1;
            int totalConfidence = 0;
            for (int i = 0; i < confidenceValues.length; i++) {
                totalConfidence += confidenceValues[i];
            }

            if (totalConfidence > 0) {
                int sitting=(confidenceValues[0] * 100 / totalConfidence);
                int standing=(confidenceValues[1] * 100 / totalConfidence);
                int walking=(confidenceValues[2] * 100 / totalConfidence);
                int running=(confidenceValues[3] * 100 / totalConfidence);
                int jumping=(confidenceValues[4] * 100 / totalConfidence);


                sittingTextView.setText(sitting+ "%");
                standingTextView.setText(standing + "%");
                jumpingTextView.setText(jumping + "%");
                walkingTextView.setText(walking + "%");
                runningTextView.setText(running + "%");




                int max;
                max=Math.max(standing,Math.max(sitting,Math.max(walking,jumping)));
                if(max==standing) {
                    linearLayoutdebout.setBackgroundColor(Color.parseColor("#02CCFE"));
                    linearLayoutassis.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutmarcher.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutsauter.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutrunning.setBackgroundColor(Color.parseColor("#EEEEEE"));
                }
                else if(max==sitting) {
                    linearLayoutassis.setBackgroundColor(Color.parseColor("#02CCFE"));
                    linearLayoutmarcher.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutsauter.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutdebout.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutrunning.setBackgroundColor(Color.parseColor("#EEEEEE"));


                }
                else if(max==walking) {
                    linearLayoutmarcher.setBackgroundColor(Color.parseColor("#02CCFE"));
                    linearLayoutsauter.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutassis.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutdebout.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutrunning.setBackgroundColor(Color.parseColor("#EEEEEE"));



                }

                else if(max==running) {
                    linearLayoutsauter.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutmarcher.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutassis.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutdebout.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutrunning.setBackgroundColor(Color.parseColor("#02CCFE"));

                }
                else {
                    linearLayoutsauter.setBackgroundColor(Color.parseColor("#02CCFE"));
                    linearLayoutmarcher.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutassis.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutdebout.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    linearLayoutrunning.setBackgroundColor(Color.parseColor("#EEEEEE"));



                }

            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private int getActivity(float[] acceleration) {
        // Determine the activity based on the accelerometer data
        int activity;
        Float x = acceleration[0];
        Float y = acceleration[1];
        Float z = acceleration[2];

        Float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

        if (magnitude < 0.2f) {
            activity = 0; // Standing
        } else if (magnitude <0.5f ) {
            activity = 1; // Sitting
        } else if (magnitude < 1.0f) {
            activity = 2; // Walking
        } else if (magnitude < 1.5f){
            activity = 3; // Running
        }else  {
            activity = 4; // Jumpping
        }
        return activity;
    }

}












