package letrungson.com.smartcontroller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import letrungson.com.smartcontroller.R;
import letrungson.com.smartcontroller.model.Room;

public class RoomDetailActivity extends Activity {
    private final DatabaseReference rooms = FirebaseDatabase.getInstance().getReference();
    TextView roomName, temperature, humidity, targetTemp;
    ConstraintLayout smart_schedule, device;
    ImageView smartScheduleImg, deviceImg, closeBtn, chartBtn;
    private Room thisRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        final String roomId = intent.getStringExtra("roomId");
        getRoom(roomId);

        setContentView(R.layout.roomdetail);
        roomName = findViewById(R.id.roomName);
        temperature = findViewById(R.id.roomdetail_temp_small);
        humidity = findViewById(R.id.roomdetail_humid);
        targetTemp = findViewById(R.id.roomdetail_temp_big);

        smart_schedule = findViewById(R.id.smart_schedule);
        smartScheduleImg = findViewById(R.id.calendar);
        smartScheduleImg.setImageResource(R.drawable.ic_calendar);
        deviceImg = findViewById(R.id.deviceImg);
        deviceImg.setImageResource(R.drawable.ic_settings);
        deviceImg.setColorFilter(getApplicationContext().getResources().getColor(R.color.white));
        smart_schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomDetailActivity.this, ScheduleActivity.class);
                intent.putExtra("roomId", thisRoom.getRoomId());
                intent.putExtra("roomName", thisRoom.getRoomName());
                startActivity(intent);
            }
        });

        device = findViewById(R.id.device);
        device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomDetailActivity.this, DevicesActivity.class);
                intent.putExtra("roomId", thisRoom.getRoomId());
                startActivity(intent);
            }
        });

        closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setImageResource(R.drawable.ic_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        chartBtn = findViewById(R.id.chartBtn);
        chartBtn.setImageResource(R.drawable.ic_baseline_bar_chart_24);
        chartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomDetailActivity.this, ViewChartActivity.class);
                intent.putExtra("roomId", thisRoom.getRoomId());
                startActivity(intent);
            }
        });
    }

    public void getRoom(String roomId) {
        Query roomDb = rooms.child("rooms").child(roomId);
        roomDb.keepSynced(true);
        roomDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    thisRoom = dataSnapshot.getValue(Room.class);
                    thisRoom.setRoomId(roomId);
                    roomName.setText(thisRoom.getRoomName());
                    if (thisRoom.getRoomCurrentTemp() != null && thisRoom.getRoomCurrentTemp().length() > 0) {
                        temperature.setText(thisRoom.getRoomCurrentTemp());
                    } else {
                        temperature.setText("--");
                    }
                    if (thisRoom.getRoomCurrentHumidity() != null && thisRoom.getRoomCurrentHumidity().length() > 0) {
                        humidity.setText(thisRoom.getRoomCurrentHumidity());
                    } else {
                        humidity.setText("--");
                    }
                    if (thisRoom.getRoomTargetTemp() != null && thisRoom.getRoomTargetTemp().length() > 0) {
                        targetTemp.setText(thisRoom.getRoomTargetTemp());
                    } else {
                        targetTemp.setText("--");
                    }
                } else {
                    Log.d("room", "Database is empty now!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
