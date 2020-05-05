package anna.whalealarmclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Time;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //to make our alarm manager
    AlarmManager alarm_manager;
    Switch alarmEnabler;
    Button setAlarm;
    EditText angularVelocity;
    TimePickerDialog alarmTimePicker;
    Context context;
    PendingIntent pendingIntent;
    private Spinner musicSelector;
    int choose_whale_sound;
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Intent my_intent;
    int min, hour;
    private Timer t = new Timer();
    Calendar calendar;
    private Handler timerHandler = new Handler();
    Runnable updater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        // initialize our alarm manager
        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // create an instance of a calendar
        calendar = Calendar.getInstance();
        // create an intent to the Alarm Receiver class
        my_intent = new Intent(this.context, Alarm_Receiver.class);

        initialView();

        //sensor controller
        sensorController();

        setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hours = calendar.getTime().getHours();
                int minutes = calendar.getTime().getMinutes();

                alarmTimePicker = new TimePickerDialog(MainActivity.this, R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // convert the int values to strings
                        hour = hourOfDay;
                        min = minute;
                        String hour_string = String.valueOf(hourOfDay);
                        String minute_string = String.valueOf(minute);

                        if (minute < 10) {
                            //10:7 --> 10:07
                            minute_string = "0" + String.valueOf(minute);
                        }

                        // method that changes the update text Textbox
                        setAlarm.setText("Alarm set to: " + hour_string + ":" + minute_string);
                    }
                }, hours, minutes, true);
                alarmTimePicker.show();
            }
        });



        // create an onClick listener to start the alarm
        alarmEnabler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if (isChecked){
                    startAlarm();
                }
                else {
                    cancelAlarm();
                }
            }
        });
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        choose_whale_sound = (int) id;


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback

    }

    private void startAlarm() {
        updater = new Runnable() {
            int passedTime = -1;
            @Override
            public void run() {
                int currentMin = new Time(System.currentTimeMillis()).getMinutes();
                int currentHour = new Time(System.currentTimeMillis()).getHours();
                if (passedTime >= 0){
                    passedTime+=1;
                }
                if (passedTime == 600){
                    cancelAlarm();
                }
                if (currentHour == hour &&
                        currentMin == min && passedTime == -1){
                    passedTime = 0;
                    String hour_string = String.valueOf(hour);
                    String minute_string = String.valueOf(min);
                    if (min < 10) {
                        //10:7 --> 10:07
                        minute_string = "0" + minute_string;
                    }
                    setAlarm.setText("Alarm set to: " + hour_string + ":" + minute_string);

                    my_intent.putExtra("extra", "alarm on");

                    // put in an extra int into my_intent
                    // tells the clock that you want a certain value from the drop-down menu/spinner
                    my_intent.putExtra("whale_choice", choose_whale_sound);
                    Log.e("The whale id is" , String.valueOf(choose_whale_sound));

                    // create a pending intent that delays the intent
                    // until the specified calendar time
                    pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
                            my_intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // set the alarm manager
                    alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                            pendingIntent);
                }
                timerHandler.postDelayed(updater,1000);
            }
        };
        timerHandler.post(updater);

    }


    private void cancelAlarm(){
        timerHandler.removeCallbacks(updater);
        setAlarm.setText("Set Alarm");

        // cancel the alarm
        alarm_manager.cancel(pendingIntent);

        // put extra string into my_intent
        // tells the clock that you pressed the "alarm off" button
        my_intent.putExtra("extra", "alarm off");
        // also put an extra int into the alarm off section
        // to prevent crashes in a Null Pointer Exception
        my_intent.putExtra("whale_choice", choose_whale_sound);


        // stop the ringtone
        sendBroadcast(my_intent);
    }

    private void initialView(){
        //initial setAlarm Button
        setAlarm = (Button) findViewById(R.id.setAlarmButton);
        //initial alarmEnabler Switch
        alarmEnabler = (Switch) findViewById(R.id.onOff);
        //initial angularVelocity EditText
        angularVelocity = (EditText) findViewById(R.id.speed);
        // create the spinner in the main UI
        musicSelector = (Spinner) findViewById(R.id.music_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.whale_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        musicSelector.setAdapter(adapter);
        // Set an onclick listener to the onItemSelected method
        musicSelector.setOnItemSelectedListener(this);
    }

    private void sensorController(){
        //initialize sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Create a listener
        SensorEventListener gyroscopeSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String angVelocity = angularVelocity.getText().toString();
                Double speed = 0.5;
                try {
                    speed = Double.parseDouble(angVelocity);
                }catch (Exception e){

                }

                if(sensorEvent.values[2] > speed) { // anticlockwise
                    cancelAlarm();
                }
//                else if(sensorEvent.values[2] < -1*speed) { // clockwise
//                    getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
//                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        // Register the listener
        sensorManager.registerListener(gyroscopeSensorListener,
                gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
