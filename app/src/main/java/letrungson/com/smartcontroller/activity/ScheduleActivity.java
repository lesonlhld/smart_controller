package letrungson.com.smartcontroller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import letrungson.com.smartcontroller.R;
import letrungson.com.smartcontroller.adapter.ScheduleListView;
import letrungson.com.smartcontroller.model.Schedule;

public class ScheduleActivity extends AppCompatActivity {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FloatingActionButton floating_action_btn;
    String roomId;
    ScheduleListView scheduleListView;
    private List<Schedule> lstSchedule;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item);
        floating_action_btn = findViewById(R.id.list_add_btn);
        ListView listView = findViewById(R.id.listView);
        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");

        //Setup Toolbar
        Toolbar toolbar = findViewById(R.id.list_toolbar);
        toolbar.setTitle("Smart Schedule");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getAllSchedule(roomId);
        //scheduleListView = new ScheduleListView(getApplicationContext(), lstSchedule);
        scheduleListView = new ScheduleListView(ScheduleActivity.this, lstSchedule);
        listView.setAdapter(scheduleListView);

        floating_action_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleActivity.this, ScheduleDetailActivity.class);
                intent.putExtra("roomId", roomId);
                intent.putExtra("action", "add");
                startActivity(intent);
                scheduleListView.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return false;
        }
    }

    private void getAllSchedule(String roomId) {
        lstSchedule = new ArrayList<>();
        Query allSchedule = firebaseDatabase.getReference("schedules").orderByChild("roomId").equalTo(roomId);
        allSchedule.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lstSchedule.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Schedule schedule = data.getValue(Schedule.class);
                    String scheduleId = data.getKey();
                    schedule.setScheduleId(scheduleId);
                    lstSchedule.add(schedule);
                }
                scheduleListView.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
