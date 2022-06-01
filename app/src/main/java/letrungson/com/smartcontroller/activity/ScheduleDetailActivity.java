package letrungson.com.smartcontroller.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import letrungson.com.smartcontroller.R;
import letrungson.com.smartcontroller.model.Device;
import letrungson.com.smartcontroller.model.Schedule;
import letrungson.com.smartcontroller.tools.Check;
import letrungson.com.smartcontroller.tools.Transform;

public class ScheduleDetailActivity extends AppCompatActivity {
    private final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    String scheduleId, roomId, action;
    TextView start_time, end_time, temp_data, humid_data, repeat_day_text, device_text, title;
    ImageButton up_temp_btn, down_temp_btn, up_humid_btn, down_humid_btn, close_btn, tick_btn;
    Button delete_btn;
    CheckBox temp_check, humid_check;
    List<Device> listDevice;
    private Schedule thisSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_schedule);
        title = (TextView) findViewById(R.id.title);
        start_time = (TextView) findViewById(R.id.start_time_text);
        end_time = (TextView) findViewById(R.id.end_time_text);
        up_temp_btn = (ImageButton) findViewById(R.id.up_temp_btn);
        down_temp_btn = (ImageButton) findViewById(R.id.down_temp_btn);
        up_humid_btn = (ImageButton) findViewById(R.id.up_humid_btn);
        down_humid_btn = (ImageButton) findViewById(R.id.down_humid_btn);
        temp_data = (TextView) findViewById(R.id.temp_data_view);
        humid_data = (TextView) findViewById(R.id.humid_data_view);
        close_btn = (ImageButton) findViewById(R.id.close_btn);
        tick_btn = (ImageButton) findViewById(R.id.tick_btn);
        delete_btn = (Button) findViewById(R.id.delete_btn);
        repeat_day_text = (TextView) findViewById(R.id.repeat_day_text);
        device_text = (TextView) findViewById(R.id.device_text);
        temp_check = (CheckBox) findViewById(R.id.checkbox_temp);
        humid_check = (CheckBox) findViewById(R.id.checkbox_humid);

        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");
        getAllDevicesInRoom(roomId);
        action = intent.getStringExtra("action");
        if (action.equals("add")) {
            title.setText("Add Schedule");
            delete_btn.setVisibility(View.GONE);
            setSchedule();
        } else {
            scheduleId = intent.getStringExtra("scheduleId");
            getSchedule(scheduleId);
        }


        start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flag = 0;
                setTime(flag);
            }
        });

        end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flag = 1;
                setTime(flag);
            }
        });

        up_temp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flag = 1;
                changeNum(v, flag);
            }
        });

        down_temp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flag = 2;
                changeNum(v, flag);
            }
        });

        up_humid_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flag = 3;
                changeNum(v, flag);
            }
        });

        down_humid_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flag = 4;
                changeNum(v, flag);
            }
        });


        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new androidx.appcompat.app.AlertDialog.Builder(ScheduleDetailActivity.this)
                        .setTitle("Delete")
                        .setMessage("Do you want to delete this schedule?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteSchedule();
                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tick_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!temp_check.isChecked()) {
                    thisSchedule.setTemp("");
                }
                if (!humid_check.isChecked()) {
                    thisSchedule.setHumid("");
                }
                if (action.equals("add")) {
                    addSchedule();
                } else {
                    updateSchedule();
                }
                finish();
            }
        });

        // lines below is prepare to set repeat day
        AlertDialog.Builder repeatBuilder = new AlertDialog.Builder(this);

        repeat_day_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                final ArrayList itemsSelected = new ArrayList();
                boolean yetChecked[] = new boolean[7];
                for (int i = 0; i < 7; i++) {
                    if (thisSchedule.getRepeatDay().toCharArray()[i] == '1') {
                        yetChecked[i] = true;
                        itemsSelected.add(i);
                    } else {
                        yetChecked[i] = false;
                    }
                }
                repeatBuilder.setTitle("Choose day:");
                repeatBuilder.setMultiChoiceItems(items, yetChecked,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedItemId,
                                                boolean isSelected) {
                                if (isSelected) {
                                    itemsSelected.add(selectedItemId);
                                } else if (itemsSelected.contains(selectedItemId)) {
                                    itemsSelected.remove(Integer.valueOf(selectedItemId));
                                }
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //Your logic when OK button is clicked
                                char A[] = new char[7];
                                for (int i = 0; i < 7; i++) {
                                    A[i] = '0';
                                }
                                for (int i = 0; i < itemsSelected.size(); i++) {
                                    A[Integer.parseInt(itemsSelected.get(i).toString())] = '1';
                                }
                                thisSchedule.setRepeatDay(String.valueOf(A));
                                repeat_day_text.setText(Transform.BinaryToDaily(String.valueOf(A)));
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

                Dialog dialog;
                dialog = repeatBuilder.create();
                //((AlertDialog)).getListView().setItemChecked(1, true);
                dialog.show();
            }
        });

        // lines below is prepare to set repeat day
        AlertDialog.Builder deviceBuilder = new AlertDialog.Builder(this);

        device_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> listDeviceName = listDevice.stream()
                        .map(Device::getDeviceName)
                        .collect(Collectors.toList());
                if (listDeviceName.size() == 0) {
                    deviceBuilder.setTitle("Choose device:");
                    deviceBuilder.setMessage("No device in this room!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    thisSchedule.setListDevice(null);
                                }
                            });

                } else {
                    final ArrayList itemsSelected = new ArrayList();
                    boolean yetChecked[] = new boolean[listDevice.size()];
                    for (int i = 0; i < listDevice.size(); i++) {
                        if (Check.checkExistDeviceId(thisSchedule.getListDevice(), listDevice.get(i).getDeviceId())) {
                            yetChecked[i] = true;
                            itemsSelected.add(i);
                        } else {
                            yetChecked[i] = false;
                        }
                    }
                    deviceBuilder.setTitle("Choose device:");
                    deviceBuilder.setMultiChoiceItems(listDeviceName.toArray(new String[listDevice.size()]), yetChecked,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedItemId,
                                                    boolean isSelected) {
                                    if (isSelected) {
                                        itemsSelected.add(selectedItemId);
                                    } else if (itemsSelected.contains(selectedItemId)) {
                                        itemsSelected.remove(Integer.valueOf(selectedItemId));
                                    }
                                }
                            })
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    //Your logic when OK button is clicked
                                    List<String> listId = new ArrayList<String>();
                                    for (int i = 0; i < itemsSelected.size(); i++) {
                                        listId.add(listDevice.get((Integer) itemsSelected.get(i)).getDeviceId());
                                    }
                                    thisSchedule.setListDevice(listId);
                                    device_text.setText(Transform.toListNameFromDeviceId(listDevice, thisSchedule.getListDevice()));
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                }
                Dialog dialog;
                dialog = deviceBuilder.create();
                //((AlertDialog)).getListView().setItemChecked(1, true);
                dialog.show();

            }
        });
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.checkbox_temp:
                if (checked) {
                    setVisibilityTemp(View.VISIBLE);
                } else {
                    setVisibilityTemp(View.GONE);
                }
                break;
            case R.id.checkbox_humid:
                if (checked) {
                    setVisibilityHumid(View.VISIBLE);
                } else {
                    setVisibilityHumid(View.GONE);
                }
                break;
        }
    }

    public void setVisibilityTemp(int state) {
        View temp_down = findViewById(R.id.down_temp_btn);
        View temp_data = findViewById(R.id.temp_data_view);
        View temp_up = findViewById(R.id.up_temp_btn);
        temp_down.setVisibility(state);
        temp_data.setVisibility(state);
        temp_up.setVisibility(state);
    }

    public void setVisibilityHumid(int state) {
        View humid_down = findViewById(R.id.down_humid_btn);
        View humid_data = findViewById(R.id.humid_data_view);
        View humid_up = findViewById(R.id.up_humid_btn);
        humid_down.setVisibility(state);
        humid_data.setVisibility(state);
        humid_up.setVisibility(state);
    }

    private void getSchedule(String scheduleId) {
        //thisSchedule = new Schedule();
        database.child("schedules").child(scheduleId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    thisSchedule = task.getResult().getValue(Schedule.class);
                    thisSchedule.setScheduleId(scheduleId);
                    if (thisSchedule.getTemp().equals("")) {
                        temp_check.setChecked(false);
                        setVisibilityTemp(View.GONE);
                        thisSchedule.setTemp("25");
                    } else {
                        temp_check.setChecked(true);
                        temp_data.setText(thisSchedule.getTemp());
                    }
                    if (thisSchedule.getHumid().equals("")) {
                        humid_check.setChecked(false);
                        setVisibilityHumid(View.GONE);
                        thisSchedule.setHumid("60");
                    } else {
                        humid_check.setChecked(true);
                        humid_data.setText(thisSchedule.getHumid());
                    }
                    start_time.setText(thisSchedule.getStartTime());
                    end_time.setText(thisSchedule.getEndTime());
                    repeat_day_text.setText(Transform.BinaryToDaily(thisSchedule.getRepeatDay()));
                    device_text.setText(Transform.toListNameFromDeviceId(listDevice, thisSchedule.getListDevice()));
                }
            }
        });
    }

    public void getAllDevicesInRoom(String roomId) {
        listDevice = new ArrayList<Device>();
        Query allDevice = database.child("devices").orderByChild("roomId").equalTo(roomId);
        allDevice.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listDevice.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Device device = data.getValue(Device.class);
                    if (!device.getType().equals("Sensor")) {
                        String deviceId = data.getKey();
                        device.setDeviceId(deviceId);
                        listDevice.add(device);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setSchedule() {
        thisSchedule = new Schedule();
        thisSchedule.setRoomId(roomId);
        temp_data.setText(String.valueOf(thisSchedule.getTemp()));
        humid_data.setText(String.valueOf(thisSchedule.getHumid()));
        start_time.setText(thisSchedule.getStartTime());
        end_time.setText(thisSchedule.getEndTime());
        repeat_day_text.setText("Choose day");
        device_text.setText("Choose device");
    }

    private void updateSchedule() {
        database.child("schedules").child(thisSchedule.getScheduleId()).child("temp").setValue(thisSchedule.getTemp());
        database.child("schedules").child(thisSchedule.getScheduleId()).child("humid").setValue(thisSchedule.getHumid());
        database.child("schedules").child(thisSchedule.getScheduleId()).child("startTime").setValue(thisSchedule.getStartTime());
        database.child("schedules").child(thisSchedule.getScheduleId()).child("endTime").setValue(thisSchedule.getEndTime());
        database.child("schedules").child(thisSchedule.getScheduleId()).child("repeatDay").setValue(thisSchedule.getRepeatDay());
        database.child("schedules").child(thisSchedule.getScheduleId()).child("listDevice").setValue(thisSchedule.getListDevice());
        database.child("schedules").child(thisSchedule.getScheduleId()).child("state").setValue("0");
    }

    private void addSchedule() {
        scheduleId = "Schedule" + database.child("schedules").push().getKey();
        database.child("schedules").child(scheduleId).setValue(thisSchedule);
        thisSchedule.setScheduleId(scheduleId);
    }

    private void deleteSchedule() {
        database.child("schedules").child(thisSchedule.getScheduleId()).removeValue();
    }

    private void setTime(int flag) {
        Calendar calendar = Calendar.getInstance();
        int previous_hour, previous_minute;

        if (action.equals("add")) {
            if (flag == 0) {
                previous_hour = calendar.get(Calendar.HOUR_OF_DAY);
            } else {
                previous_hour = calendar.get(Calendar.HOUR_OF_DAY) + 1;
            }
            previous_minute = calendar.get(Calendar.MINUTE);
        } else {
            if (flag == 0) {
                previous_hour = Integer.parseInt(thisSchedule.getStartTime().substring(0, 2));
                previous_minute = Integer.parseInt(thisSchedule.getStartTime().substring(3, 5));
            } else {
                previous_hour = Integer.parseInt(thisSchedule.getEndTime().substring(0, 2));
                previous_minute = Integer.parseInt(thisSchedule.getEndTime().substring(3, 5));
            }
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                if (flag == 0) {
                    calendar.set(0, 0, 0, hour, minute);
                    start_time.setText((simpleDateFormat.format(calendar.getTime())));
                    thisSchedule.setStartTime(simpleDateFormat.format(calendar.getTime()));
                } else {
                    calendar.set(0, 0, 0, hour, minute);
                    end_time.setText((simpleDateFormat.format(calendar.getTime())));
                    thisSchedule.setEndTime(simpleDateFormat.format(calendar.getTime()));
                }
            }
        }, previous_hour, previous_minute, true);
        timePickerDialog.show();
    }

    public void changeNum(View view, int flag) {
        int tempNum = Integer.parseInt(thisSchedule.getTemp());
        int humidNum = Integer.parseInt(thisSchedule.getHumid());
        switch (flag) {
            case 1:
                tempNum += 1;
                break;
            case 2:
                tempNum -= 1;
                break;
            case 3:
                humidNum += 1;
                break;
            case 4:
                humidNum -= 1;
                break;
        }
        if (flag < 3) {
            if (tempNum > 30) {
                tempNum = 30;
            } else if (tempNum < 16) {
                tempNum = 16;
            }
            temp_data.setText(String.valueOf(tempNum));
            thisSchedule.setTemp(String.valueOf(tempNum));
        } else {
            if (humidNum > 60) {
                humidNum = 60;
            } else if (humidNum < 40) {
                humidNum = 40;
            }
            humid_data.setText(String.valueOf(humidNum));
            thisSchedule.setHumid(String.valueOf(humidNum));
        }
    }
}