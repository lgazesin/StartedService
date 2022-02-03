package com.nexis.lab11;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    ImageView close, info;
    RelativeLayout mainContainer;
    Boolean reminderSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        close = findViewById(R.id.close);
        info = findViewById(R.id.info);
        mainContainer = findViewById(R.id.mainContainer);

        if (!reminderSet) {
            addReminderView();
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeApplication();
            }
        });


        info.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                BatteryManager bm = (BatteryManager)getSystemService(Context.BATTERY_SERVICE);

                if ((!bm.isCharging() || bm.isCharging()) && (ni != null && ni.isConnected())){
                    moveToAbout();
                } else {
                    Toast.makeText(getApplicationContext(), "Requirements not met. Make sure you are connected to internet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void addReminderView(){
        View addRemV = getLayoutInflater().inflate(R.layout.add_reminder, mainContainer, false);

        ImageView remV = addRemV.findViewById(R.id.addRem);


        remV.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                remSet();
                ((ViewGroup) addRemV.getParent()).removeView(addRemV);
            }
        });

        mainContainer.addView(addRemV);
        reminderSet = false;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void remSet(){
        View addRem = getLayoutInflater().inflate(R.layout.reminder_view, mainContainer, false);

        TextView remV = addRem.findViewById(R.id.cancel_button);
        TextView setR = addRem.findViewById(R.id.set_reminder);


        Spinner repAf = addRem.findViewById(R.id.repAf);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.repeat_after, android.R.layout.simple_spinner_item);
        repAf.setAdapter(adapter);


        remV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReminderView();
                ((ViewGroup) addRem.getParent()).removeView(addRem);
            }
        });


        setR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String spinVal = repAf.getSelectedItem().toString();
                int spinValInt;
                String spinVal_;
                boolean repeatStatus = false;
                if (spinVal.equals("Never")) {
                    repeatStatus = false;
                    spinVal_ = null;
                } else {
                    spinValInt = Integer.parseInt(spinVal);
                    repeatStatus = true;
                    spinVal_ = spinVal;
                }


                TimePicker timeP = addRem.findViewById(R.id.setTime);
                int hour = timeP.getHour();
                int minute = timeP.getMinute();

                EditText remMsg = addRem.findViewById(R.id.remMsg);
                String msg = remMsg.getText().toString();

                ((ViewGroup) addRem.getParent()).removeView(addRem);
                showInfo(minute, hour, msg, repeatStatus, spinVal_);
            }
        });

        mainContainer.addView(addRem);
        reminderSet = true;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void showInfo(int min, int hour, String msg, Boolean repeating, String spinVal){
        View showRem = getLayoutInflater().inflate(R.layout.alarm_display, mainContainer, false);

        TextView hour_ = showRem.findViewById(R.id.hour);
        if (hour > 12){
            hour_.setText(String.valueOf(hour - 12));
        } else {
            hour_.setText(String.valueOf(hour));
        }

        TextView min_ = showRem.findViewById(R.id.minute);
        min_.setText(String.valueOf(min));

        TextView time_ = showRem.findViewById(R.id.time);
        if (hour >= 12){
            time_.setText("PM");
        } else {
            time_.setText("AM");
        }

        TextView msg_ = showRem.findViewById(R.id.receivedMsg);
        msg_.setText(msg);

        if (repeating){
            TextView repStat = showRem.findViewById(R.id.repStat);
            repStat.setText("Repeat after " + spinVal + " min");
        }

        TextView cancelAlarm = showRem.findViewById(R.id.cancelAlarm);

        Calendar cal_alarm = Calendar.getInstance();
        cal_alarm.set(Calendar.HOUR_OF_DAY, hour);
        cal_alarm.set(Calendar.MINUTE, min);
        cal_alarm.set(Calendar.SECOND, 0);
        cal_alarm.set(Calendar.MILLISECOND, 0);

        if(cal_alarm.before(Calendar.getInstance())){
            Toast.makeText(getApplicationContext(), "Date passed moved to next date.", Toast.LENGTH_SHORT).show();
            cal_alarm.add(Calendar.DATE,1);
        }

        Intent intent = new Intent(getApplicationContext(), broadcastForReminder.class);
        intent.putExtra("rMsg", msg);
        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pi);
        }

        if (repeating){
            int tempTime = Integer.parseInt(spinVal);
            am.setRepeating(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), (1000 * 60 * tempTime), pi);
        }

        cancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Calendar.getInstance().compareTo(cal_alarm) < 0){
                    am.cancel(pi);
                } else {
                    am.cancel(pi);
                    broadcastForReminder.mp.stop();
                }
                ((ViewGroup) showRem.getParent()).removeView(showRem);
                addReminderView();
            }
        });
        mainContainer.addView(showRem);
    }




    public void moveToAbout() {
        Intent intent = new Intent(this, AppActivity.class);
        startActivity(intent);
    }


    public void closeApplication() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.exit_confirmation);
        dialog.setCancelable(true);

        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);


        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }
}