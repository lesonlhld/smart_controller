package letrungson.com.smartcontroller.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import letrungson.com.smartcontroller.R;
import letrungson.com.smartcontroller.model.Room;
import letrungson.com.smartcontroller.service.Database;

public class RoomActivity extends AppCompatActivity {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    Button add;
    FloatingActionButton addRoom;
    TextView roomName, cancel;
    ArrayAdapter<Room> arrayAdapter;
    ListView listView;
    private List<Room> listRoom;
    private DatabaseReference devices, rooms, schedules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item);

        devices = database.getReference("devices");
        rooms = database.getReference("rooms");
        schedules = database.getReference("schedules");

        //Setup Toolbar
        Toolbar toolbar = findViewById(R.id.list_toolbar);
        toolbar.setTitle("Manage Room");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getAllRoom();

        listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<Room>(this, android.R.layout.simple_list_item_1, listRoom);
        listView.setAdapter(arrayAdapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                String roomId = listRoom.get(pos).getRoomId();
                Log.d("room", roomId);
                new AlertDialog.Builder(RoomActivity.this)
                        .setTitle("Remove")
                        .setMessage("Do you want to remove this room?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeRoom(roomId);
                                arrayAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
        });

        addRoom = findViewById(R.id.list_add_btn);
        addRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRoom();
                arrayAdapter.notifyDataSetChanged();
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

    private void addRoom() {
        setContentView(R.layout.activity_addroom);
        roomName = findViewById(R.id.room_edt_text);

        add = findViewById(R.id.add_tbn);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = roomName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(RoomActivity.this, getResources().getString(R.string.error_room_name_required), Toast.LENGTH_LONG).show();
                } else if (isDeviceIdExist(name)) {
                    Toast.makeText(RoomActivity.this, getResources().getString(R.string.error_room_name_existed), Toast.LENGTH_LONG).show();
                } else if (name.length() > 50) {
                    Toast.makeText(RoomActivity.this, getResources().getString(R.string.error_room_name_too_long), Toast.LENGTH_LONG).show();
                } else {
                    Database.addRoom(name);
                    startActivity(new Intent(RoomActivity.this, RoomActivity.class));
                    finish();
                }
            }
        });

        cancel = findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RoomActivity.this, RoomActivity.class));
                finish();
            }
        });
    }

    private boolean isDeviceIdExist(String input) {
        for (Room room : listRoom) {
            if (room.getRoomName().toLowerCase().equals(input.toLowerCase()))
                return true;
        }
        return false;
    }

    public void getAllRoom() {
        listRoom = new ArrayList<>();
        Query allRoom = database.getReference("rooms");
        allRoom.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listRoom.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Room room = data.getValue(Room.class);
                    String roomId = data.getKey();
                    room.setRoomId(roomId);
                    listRoom.add(room);
                    Log.d("DB", room.getRoomName());
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void removeRoom(String roomId) {
        rooms.child(roomId).removeValue();
        devices.orderByChild("roomId").equalTo(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    data.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        schedules.orderByChild("roomId").equalTo(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    data.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
