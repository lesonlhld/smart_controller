package letrungson.com.smartcontroller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import letrungson.com.smartcontroller.R;
import letrungson.com.smartcontroller.tools.ChartHelper;
import ru.slybeaver.slycalendarview.SlyCalendarDialog;

public class ViewChartActivity extends AppCompatActivity implements SlyCalendarDialog.Callback {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    ChartHelper mTempChart, mHumidChart;
    LineChart tempChart, humidChart;
    String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_chart);

        tempChart = (LineChart) findViewById(R.id.tempChart);
        tempChart.setNoDataText("No temperature to display");
        mTempChart = new ChartHelper(tempChart);
        tempChart.getDescription().setText("Temperature");
        tempChart.setDrawBorders(true);

        humidChart = (LineChart) findViewById(R.id.humidChart);
        humidChart.setNoDataText("No humidity to display");
        mHumidChart = new ChartHelper(humidChart);
        humidChart.getDescription().setText("Humidity");
        humidChart.setDrawBorders(true);

        //Setup Toolbar
        Toolbar toolbar = findViewById(R.id.room_manage_toolbar);
        toolbar.setTitle("View chart");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");
        getAllRecord(roomId);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.date_range, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.btn_remove) {
            new SlyCalendarDialog()
                    .setSingle(false)
                    .setFirstMonday(false)
                    .setCallback(ViewChartActivity.this)
                    .show(getSupportFragmentManager(), "TAG_SLYCALENDAR");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllRecord(String roomId) {
        resetChart(tempChart);
        resetChart(humidChart);

        Query allRecord = firebaseDatabase.getReference("sensors").orderByChild("roomId").equalTo(roomId);
        allRecord.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String data = snapshot.child("last_value").getValue(String.class);
                String temp = data.substring(0, data.lastIndexOf('-')).trim();
                String humid = data.substring(data.lastIndexOf('-') + 1).trim();
                mTempChart.addEntry(Float.valueOf(temp));
                mHumidChart.addEntry(Float.valueOf(humid));
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void resetChart(LineChart chart) {
        if (chart.getData() != null) {
            chart.fitScreen();
            chart.getData().clearValues();
            chart.notifyDataSetChanged();
            chart.clear();
            chart.invalidate();
        }
    }

    private void getRecord(String roomId, Calendar firstDate, Calendar secondDate) {
        resetChart(tempChart);
        resetChart(humidChart);

        DateFormat currentFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Query record = firebaseDatabase.getReference("sensors").orderByChild("roomId").equalTo(roomId);
        record.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    java.util.Date date = currentFormat.parse(snapshot.child("updated_at").getValue(String.class));
                    if (date.after(firstDate.getTime())) {
                        if (secondDate == null || date.before(secondDate.getTime())) {
                            String data = snapshot.child("last_value").getValue(String.class);
                            String temp = data.substring(0, data.lastIndexOf('-')).trim();
                            String humid = data.substring(data.lastIndexOf('-') + 1).trim();
                            mTempChart.addEntry(Float.valueOf(temp));
                            mHumidChart.addEntry(Float.valueOf(humid));
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onDataSelected(Calendar firstDate, Calendar secondDate, int hours, int minutes) {
        if (firstDate != null) {
            firstDate.set(Calendar.HOUR_OF_DAY, hours);
            firstDate.set(Calendar.MINUTE, minutes);
            if (secondDate == null) {
                Toast.makeText(
                        this,
                        new SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault()).format(firstDate.getTime()),
                        Toast.LENGTH_LONG

                ).show();
            } else {
                secondDate.set(Calendar.HOUR_OF_DAY, hours);
                secondDate.set(Calendar.MINUTE, minutes);
                Toast.makeText(
                        this,
                        getString(
                                R.string.period,
                                new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault()).format(firstDate.getTime()),
                                new SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault()).format(secondDate.getTime())
                        ),
                        Toast.LENGTH_LONG

                ).show();
            }
            getRecord(roomId, firstDate, secondDate);
        } else {
            getAllRecord(roomId);
        }
    }
}
