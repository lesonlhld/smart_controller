package letrungson.com.smartcontroller.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import letrungson.com.smartcontroller.R;
import letrungson.com.smartcontroller.activity.RoomDetailActivity;
import letrungson.com.smartcontroller.model.Device;
import letrungson.com.smartcontroller.model.Room;
import letrungson.com.smartcontroller.service.Database;

public class RoomViewAdapter extends RecyclerView.Adapter<RoomViewAdapter.MyViewHolder> {
    private List<Room> roomList;
    private Context context;

    public RoomViewAdapter(Context context, List<Room> roomList) {
        this.roomList = roomList;
        this.context = context;
    }

    public Context getContext() {
        return context;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.roomitem_cardview, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String roomId = this.roomList.get(position).getRoomId();

        ArrayList<Device> listRoomDevice = new ArrayList<Device>();
        holder.roomName.setText(this.roomList.get(position).getRoomName());
        if (this.roomList.get(position).getRoomTargetTemp() != null && this.roomList.get(position).getRoomTargetTemp().length() > 0) {
            holder.roomTargetTemp.setText("Heat to " + this.roomList.get(position).getRoomTargetTemp());
        } else {
            holder.roomTargetTemp.setText("No schedule");
        }
        holder.roomCurrentTemp.setText(this.roomList.get(position).getRoomCurrentTemp());
        Query refRoomDevices = FirebaseDatabase.getInstance().getReference("devices").orderByChild("roomId").equalTo(roomId);
        refRoomDevices.addChildEventListener(new ChildEventListener() {
            private int countDeviceOn = 0;

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Device device = snapshot.getValue(Device.class);
                device.setDeviceId(snapshot.getKey());
                if (!device.getType().equals("Sensor")) {
                    if (device.getState().equals("1")) countDeviceOn++;
                    listRoomDevice.add(device);
                    if (countDeviceOn == 0) {
                        holder.roomPowerButton.setColorFilter(Color.parseColor("#688396"));
                        holder.constraintLayout.setBackgroundColor(Color.parseColor("#8FA4B5"));
                        holder.isOn = false;
                    } else if (countDeviceOn == 1) {
                        holder.roomPowerButton.setColorFilter(Color.parseColor("#F20808"));
                        holder.constraintLayout.setBackgroundColor(Color.parseColor("#FFBE04"));
                        holder.isOn = true;
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, @Nullable String previousChildName) {
                Device device = snapshot.getValue(Device.class);
                String deviceID = snapshot.getKey();
                if (!device.getType().equals("Sensor")) {
                    for (Device device0 : listRoomDevice) {
                        if (device0.getDeviceId().equals(deviceID)) {
                            if (device.getState().equals("1") && device0.getState().equals("0"))
                                countDeviceOn++;
                            else if (device.getState().equals("0") && device0.getState().equals("1"))
                                countDeviceOn--;
                            device0.assign(device);
                            break;
                        }
                    }
                    if (countDeviceOn == 0) {
                        holder.roomPowerButton.setColorFilter(Color.parseColor("#688396"));
                        holder.constraintLayout.setBackgroundColor(Color.parseColor("#8FA4B5"));
                        holder.isOn = false;
                    } else if (countDeviceOn == 1) {
                        holder.roomPowerButton.setColorFilter(Color.parseColor("#F20808"));
                        holder.constraintLayout.setBackgroundColor(Color.parseColor("#FFBE04"));
                        holder.isOn = true;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Device device = snapshot.getValue(Device.class);
                String deviceID = snapshot.getKey();
                if (!device.getType().equals("Sensor")) {
                    for (Device device0 : listRoomDevice) {
                        if (device0.getDeviceId().equals(deviceID)) {
                            if (device0.getState().equals("1"))
                                countDeviceOn--;
                            listRoomDevice.remove(device0);
                            break;
                        }
                    }
                    if (countDeviceOn == 0) {
                        holder.roomPowerButton.setColorFilter(Color.parseColor("#688396"));
                        holder.constraintLayout.setBackgroundColor(Color.parseColor("#8FA4B5"));
                        holder.isOn = false;
                    } else if (countDeviceOn == 1) {
                        holder.roomPowerButton.setColorFilter(Color.parseColor("#F20808"));
                        holder.constraintLayout.setBackgroundColor(Color.parseColor("#FFBE04"));
                        holder.isOn = true;
                    }
                } else {
                    for (Device device0 : listRoomDevice) {
                        if (device0.getDeviceId().equals(deviceID)) {
                            listRoomDevice.remove(device0);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.roomPowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listRoomDevice.size() > 0) {
                    String newState = "0";
                    if (!holder.isOn) {
                        newState = "1";
                    }
                    for (Device device : listRoomDevice) {
                        if (!device.getType().equals("Sensor") && !device.getState().equals(newState)) {
                            Database.updateDevice(device.getDeviceId(), newState);
                            Database.addLog(device.getDeviceId(), newState);
                        }
                    }
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "Clicked: " + roomList.get(position).getRoomName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, RoomDetailActivity.class);
                intent.putExtra("roomId", roomList.get(position).getRoomId());
                context.startActivity(intent);
                notifyDataSetChanged();
            }
        });

    }


    @Override
    public int getItemCount() {
        return roomList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView roomName;
        TextView roomTargetTemp;
        TextView roomCurrentTemp;
        ImageButton roomPowerButton;
        ConstraintLayout constraintLayout;
        boolean isOn;

        public MyViewHolder(View itemView) {
            super(itemView);
            roomName = (TextView) itemView.findViewById(R.id.room_item_name);
            roomTargetTemp = (TextView) itemView.findViewById(R.id.room_item_description);
            roomCurrentTemp = (TextView) itemView.findViewById(R.id.room_item_temp);
            roomPowerButton = (ImageButton) itemView.findViewById(R.id.room_item_power_btn);
            constraintLayout = (ConstraintLayout) itemView.findViewById(R.id.room_item_layout);
            isOn = false;
        }
    }
}
