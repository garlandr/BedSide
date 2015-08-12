package com.garland.bedside;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.AlarmClock;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AnalogClock;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;





public class MainActivity extends Activity {


    private TextView cityText;
    private TextView condDescr;
    private TextView temp;
    private TextView hitemp;
    private TextView lowtemp;
    private TextView press;
    private TextView windSpeed;
    private TextView windDeg;

    private TextView hum;
    private ImageView imgView;

    private AnalogClock clockbutton;

    private static TextView calLab;
    private static TextView cal1;
    private static TextView cal2;
    private static TextView cal3;
    private static TextView cal4;
    private static TextView cal5;
    private static TextView cal6;

    private Timer timer;
    private TimerTask timerTask;
    private String city = "Pittsburgh,USA";

    private static boolean LOG = true;



    //handler to update weather from TimerTask
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);


        cityText = (TextView) findViewById(R.id.cityText);
        condDescr = (TextView) findViewById(R.id.condDescr);
        temp = (TextView) findViewById(R.id.temp);
        hitemp = (TextView) findViewById(R.id.hitemp);
        lowtemp = (TextView) findViewById(R.id.lowtemp);
        hum = (TextView) findViewById(R.id.hum);
        press = (TextView) findViewById(R.id.press);
        windSpeed = (TextView) findViewById(R.id.windSpeed);
        windDeg = (TextView) findViewById(R.id.windDeg);
        imgView = (ImageView) findViewById(R.id.condIcon);

        calLab = (TextView) findViewById(R.id.calLab);
        cal1 = (TextView) findViewById(R.id.cal1);
        cal2 = (TextView) findViewById(R.id.cal2);
        cal3 = (TextView) findViewById(R.id.cal3);
        cal4 = (TextView) findViewById(R.id.cal4);
        cal5 = (TextView) findViewById(R.id.cal5);
        cal6 = (TextView) findViewById(R.id.cal6);

        JSONWeatherTask task = new JSONWeatherTask();
        task.execute(new String[]{city});

        clockbutton = (AnalogClock) findViewById(R.id.analogClock);
        clockbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent openClockIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                openClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(openClockIntent);

            }
        });

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd");
        calLab.setText(df.format(c.getTime()));

        readCalendar(MainActivity.this, 1, 0);

        startTimer();

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

    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            try {
                String data = ((new WeatherHttpClient()).getWeatherData(params[0]));

                try {
                    weather = JSONWeatherParser.getWeather(data);

                    // Let's retrieve the icon
                    weather.iconData = ((new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception je) {
                je.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            if (weather != null) {

                if (weather.iconData != null && weather.iconData.length > 0) {
                    Bitmap img = BitmapFactory.decodeByteArray(weather.iconData, 0, weather.iconData.length);
                    imgView.setImageBitmap(img);
                }

                cityText.setText(weather.location.getCity());
                Log.i("WEATHER", weather.location.getCity());

                condDescr.setText(weather.currentCondition.getCondition() + " (" + weather.currentCondition.getDescr() + ")");
                Log.i("WEATHER", weather.currentCondition.getCondition() + " (" + weather.currentCondition.getDescr() + ")");

                //temp.setText("" + Math.round((weather.temperature.getTemp() - 273.15)) + "�C");
                //temp.setText("" + Math.round(((weather.temperature.getTemp() * 9)/5 - 459.67)) + " \u00B0 F");
                temp.setText("" + Math.round(((weather.temperature.getTemp()))) + " \u00B0 F");
                Log.i("WEATHER", "" + Math.round(((weather.temperature.getTemp()))) + " \u00B0 F");

                hitemp.setText("" + weather.temperature.getMaxTemp() + " \u00B0 F");
                Log.i("WEATHER", "" + weather.temperature.getMinTemp() + " \u00B0 F");

                lowtemp.setText("" + weather.temperature.getMinTemp() + " \u00B0 F");
                Log.i("WEATHER", "" + weather.temperature.getMinTemp() + " \u00B0 F");

                hum.setText("" + weather.currentCondition.getHumidity() + "%");
                Log.i("WEATHER", "" + weather.currentCondition.getHumidity() + "%");

                press.setText(" " + weather.currentCondition.getPressure() + " hPa");
                Log.i("WEATHER", " " + weather.currentCondition.getPressure() + " hPa");

                windSpeed.setText(" " + weather.wind.getSpeed() + " mph");
                Log.i("WEATHER", " " + weather.wind.getSpeed() + " mph");

                //windDeg.setText(" " + weather.wind.getDeg() + "\u00B0");
                //Log.i("WEATHER"," " + weather.wind.getDeg() + "\u00B0");

            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //start our timer so it can start when the app comes from the background
        startTimer();
    }


    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 10000, 900000); //
    }

    public void stoptimertask(View v) {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to show the current timestamp
                handler.post(new Runnable() {
                    public void run() {

                        Log.i("TIMER","timer started");

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd");
                        calLab.setText(df.format(c.getTime()));


                        // get westher info
                        JSONWeatherTask task = new JSONWeatherTask();
                        task.execute(new String[]{city});

                        //Show Calendar
                        readCalendar(MainActivity.this, 0, 1);


                        /*
                        //show the toast
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(getApplicationContext(), strDate, duration);
                        toast.show();
                        */
                    }
                });
            }
        };
    }


    // Use to specify specific the time span
    public static void readCalendar(Context context, int days, int hours) {

        ContentResolver contentResolver = context.getContentResolver();

        // Create a cursor and read from the calendar (for Android API below 4.0)
        //final Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"),
        //        (new String[] { "_id", "displayName", "selected" }), null, null, null);

		/*
		* Use the cursor below for Android API 4.0+ (Thanks to SLEEPLisNight)
		*/
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/events"),
                new String[]{ "calendar_id", "title", "description", "dtstart", "dtend", "eventLocation" },
                null, null, null);
		/*
		*/

        // Create a set containing all of the calendar IDs available on the phone
        HashSet<String> calendarIds = getCalenderIds(cursor);

        // Create a hash map of calendar ids and the events of each id
        HashMap<String, List<CalendarEvent>> eventMap = new HashMap<String, List<CalendarEvent>>();

        // Loop over all of the calendars
        for (String id : calendarIds) {

            // Create a builder to define the time span
            Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
            long now = new Date().getTime();
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat startFormatter = new SimpleDateFormat("MM/dd/yy");
            String dateString = startFormatter.format(calendar.getTime());

            SimpleDateFormat formatterr = new SimpleDateFormat("hh:mm:ss MM/dd/yy");
            Calendar endOfDay = Calendar.getInstance();
            try {
                Date dateCCC = formatterr.parse("23:59:59 " + dateString);
                endOfDay.setTime(dateCCC);
            } catch (Exception e) {
                e.printStackTrace();
            }



            // create the time span based on the inputs
            //ContentUris.appendId(builder, now - (DateUtils.DAY_IN_MILLIS * days) - (DateUtils.HOUR_IN_MILLIS * hours));
            ContentUris.appendId(builder, now);
            ContentUris.appendId(builder, endOfDay.getTimeInMillis());

            // Create an event cursor to find all events in the calendar
            Cursor eventCursor = contentResolver.query(builder.build(),
                    new String[]  { "title", "begin", "end", "allDay"}, "calendar_id=" + id,
                    null, "startDay ASC, startMinute ASC");

            if(LOG) Log.i("CAL","eventCursor count="+eventCursor.getCount());

            // If there are actual events in the current calendar, the count will exceed zero
            if(eventCursor.getCount()>0)
            {

                // Create a list of calendar events for the specific calendar
                List<CalendarEvent> eventList = new ArrayList<CalendarEvent>();

                // Move to the first object
                eventCursor.moveToFirst();

                // Create an object of CalendarEvent which contains the title, when the event begins and ends,
                // and if it is a full day event or not
                CalendarEvent ce = loadEvent(eventCursor);

                // Adds the first object to the list of events
                eventList.add(ce);

                //System.out.println(ce.toString())
                if (LOG) Log.i("CAL 1",ce.toString());
                SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
                String estart = df.format(ce.getBegin().getTime());
                String eend = df.format(ce.getEnd().getTime());
                cal1.setText(ce.getTitle() + " " + estart + " to " + eend);

                int i = 1;
                cal6.setVisibility(View.INVISIBLE);
                // While there are more events in the current calendar, move to the next instance
                while (eventCursor.moveToNext())
                {

                    // Adds the object to the list of events
                    ce = loadEvent(eventCursor);
                    eventList.add(ce);
                    estart = df.format(ce.getBegin().getTime());
                    eend = df.format(ce.getEnd().getTime());
                    i++;
                    switch (i) {
                        case 2:
                            cal2.setText(ce.getTitle() + " " + estart + " to " + eend);
                            break;
                        case 3:
                            cal3.setText(ce.getTitle() + " " + estart + " to " + eend);
                            break;
                        case 4:
                            cal4.setText(ce.getTitle() + " " + estart + " to " + eend);
                            break;
                        case 5:
                            cal5.setText(ce.getTitle() + " " + estart + " to " + eend);
                            break;
                        case 6:
                            cal6.setVisibility(View.VISIBLE);
                            break;
                        default:
                            break;
                    }


                    //System.out.println(ce.toString());
                    if(LOG) Log.i("CAL " + i,ce.toString());

                }

                Collections.sort(eventList);
                eventMap.put(id, eventList);


                //System.out.println(eventMap.keySet().size() + " " + eventMap.values());
                if(LOG) Log.i("CAL",eventMap.keySet().size() + " " + eventMap.values());

            } else {
                cal1.setText("");
                cal2.setText("");
                cal3.setText("");
                cal4.setText("");
                cal5.setText("");
                cal6.setText("");
            }
            eventCursor.close();
        }
        cursor.close();
    }

    // Returns a new instance of the calendar object
    private static CalendarEvent loadEvent(Cursor csr) {
        return new CalendarEvent(csr.getString(0),
                new Date(csr.getLong(1)),
                new Date(csr.getLong(2)),
                !csr.getString(3).equals("0"));
    }

    // Creates the list of calendar ids and returns it in a set
    private static HashSet<String> getCalenderIds(Cursor cursor) {

        HashSet<String> calendarIds = new HashSet<String>();

        try
        {

            // If there are more than 0 calendars, continue
            if(cursor.getCount() > 0)
            {

                // Loop to set the id for all of the calendars
                while (cursor.moveToNext()) {

                    String _id = cursor.getString(0);
                    String displayName = cursor.getString(1);
                    Boolean selected = !cursor.getString(2).equals("0");

                    //System.out.println("Id: " + _id + " Display Name: " + displayName + " Selected: " + selected);
                    //if(LOG) Log.i("CAL","Id: " + _id + " Display Name: " + displayName + " Selected: " + selected);
                    calendarIds.add(_id);

                }
                cursor.close();
            }
        }

        catch(AssertionError ex)
        {
            ex.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return calendarIds;

    }


}
