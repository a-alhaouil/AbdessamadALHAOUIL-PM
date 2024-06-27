package com.example.abdessamadalhaouil_pm;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class UserActivities extends AppCompatActivity implements SensorEventListener {
    private static final String CHANNEL_ID = "ActivityChangeChannel";
    private static final String TAG = "UserActivities";

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
    String currentActivity = "";



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
        sittingTextView = findViewById(R.id.sitting_prob);
        standingTextView = findViewById(R.id.standing_prob);
        jumpingTextView = findViewById(R.id.jumping_prob);
        walkingTextView = findViewById(R.id.walking_prob);
        runningTextView = findViewById(R.id.running_prob);


//        xText = findViewById(R.id.text_view_x);
//        yText = findViewById(R.id.text_view_y);
//        zText = findViewById(R.id.text_view_z);

        // Initialize your LinearLayouts
        linearLayoutdebout = findViewById(R.id.standing_row);
        linearLayoutassis = findViewById(R.id.sitting_row);
        linearLayoutmarcher = findViewById(R.id.walking_row);
        linearLayoutsauter = findViewById(R.id.jumping_row);
        linearLayoutrunning = findViewById(R.id.running_row);


        // Create notification channel (Only required for API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

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
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
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
                int sitting = (confidenceValues[0] * 100 / totalConfidence);
                int standing = (confidenceValues[1] * 100 / totalConfidence);
                int walking = (confidenceValues[2] * 100 / totalConfidence);
                int running = (confidenceValues[3] * 100 / totalConfidence);
                int jumping = (confidenceValues[4] * 100 / totalConfidence);


                sittingTextView.setText(sitting + "%");
                standingTextView.setText(standing + "%");
                jumpingTextView.setText(jumping + "%");
                walkingTextView.setText(walking + "%");
                runningTextView.setText(running + "%");


                int max = Math.max(standing, Math.max(sitting, Math.max(walking, jumping)));
                String newActivity = "";
                if (max == standing) {
                    newActivity = "Standing";
                    updateUIForActivity("Standing");
                } else if (max == sitting) {
                    newActivity = "Sitting";
                    updateUIForActivity("Sitting");
                } else if (max == walking) {
                    newActivity = "Walking";
                    updateUIForActivity("Walking");
                } else if (max == running) {
                    newActivity = "Running";
                    updateUIForActivity("Running");
                } else {
                    newActivity = "Jumping";
                    updateUIForActivity("Jumping");
                }
                // Notify if activity changes
                if (!newActivity.equals(currentActivity)) {
                    currentActivity = newActivity;
                    showNotification(newActivity);
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

        if ( 0.00f <magnitude && magnitude < 0.14f) {
            activity = 0; // Sitting
        } else if ( 0.15f <magnitude &&magnitude < 0.40f) {
            activity = 1; // Standing
        } else if ( 0.41f <magnitude &&magnitude < 0.8f) {
            activity = 2; // Walking
        } else if ( 0.81f <magnitude &&magnitude < 1.2f) {
            activity = 3; // Running

        } else {
            activity = 4; // Jumpping


        }
        return activity;
    }
    private void updateUIForActivity(String activity) {
        // Update UI based on the detected activity
        switch (activity) {
            case "Standing":
                linearLayoutdebout.setBackgroundColor(Color.parseColor("#02CCFE"));
                linearLayoutsauter.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutmarcher.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutassis.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutrunning.setBackgroundColor(Color.parseColor("#EEEEEE"));
                break;
            case "Sitting":
                linearLayoutassis.setBackgroundColor(Color.parseColor("#02CCFE"));
                linearLayoutsauter.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutmarcher.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutdebout.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutrunning.setBackgroundColor(Color.parseColor("#EEEEEE"));
                break;
            case "Walking":
                linearLayoutmarcher.setBackgroundColor(Color.parseColor("#02CCFE"));
                linearLayoutsauter.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutassis.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutdebout.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutrunning.setBackgroundColor(Color.parseColor("#EEEEEE"));

                break;
            case "Running":
                linearLayoutrunning.setBackgroundColor(Color.parseColor("#02CCFE"));
                linearLayoutsauter.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutmarcher.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutassis.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutdebout.setBackgroundColor(Color.parseColor("#EEEEEE"));

                break;
            case "Jumping":
                linearLayoutsauter.setBackgroundColor(Color.parseColor("#02CCFE"));
                linearLayoutmarcher.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutassis.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutdebout.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearLayoutrunning.setBackgroundColor(Color.parseColor("#EEEEEE"));
                break;
        }
    }

    private void showNotification(String activity) {
        // Create an explicit intent for an Activity in your app
        // For simplicity, here we create a basic notification without clicking functionality
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Activity Change")
                .setContentText("Detected activity: " + activity)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
    }
}












