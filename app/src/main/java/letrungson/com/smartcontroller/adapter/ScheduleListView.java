package letrungson.com.smartcontroller.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import letrungson.com.smartcontroller.R;
import letrungson.com.smartcontroller.activity.ScheduleDetailActivity;
import letrungson.com.smartcontroller.model.Schedule;
import letrungson.com.smartcontroller.tools.Transform;
import letrungson.com.smartcontroller.tools.TurnOnOffSchedule;

public class ScheduleListView extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    List<Schedule> schedules;


    public ScheduleListView(Context context, List<Schedule> schedules) {
        this.schedules = schedules;
        inflater = (LayoutInflater.from(context));
        this.context = context;
    }

    @Override
    public int getCount() {
        return schedules.size();
    }

    @Override
    public Object getItem(int position) {
        return schedules.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.schedule_items, null);
        TextView startDay = view.findViewById(R.id.startDay);
        TextView startTime = view.findViewById(R.id.startTime);
        TextView endTime = view.findViewById(R.id.endTime);
        TextView temp = view.findViewById(R.id.temp);
        TextView humid = view.findViewById(R.id.humid);
        Switch onOff = view.findViewById(R.id.schedule_sw);


        String repeatDay = Transform.BinaryToDaily(schedules.get(position).getRepeatDay());
        startDay.setText(repeatDay);
        startTime.setText(schedules.get(position).getStartTime());
        endTime.setText(schedules.get(position).getEndTime());

        if (schedules.get(position).getTemp().equals("")) {
            temp.setText("--C");
        } else {
            temp.setText(schedules.get(position).getTemp() + "C");
        }

        if (schedules.get(position).getHumid().equals("")) {
            humid.setText("--%");
        } else {
            humid.setText(schedules.get(position).getHumid() + "%");
        }
        onOff.setChecked(schedules.get(position).getState().equals("1"));

        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    List<Schedule> listSimilarSchedule = TurnOnOffSchedule.turnOffSimilarSchedule(schedules, position);
                    if (listSimilarSchedule.size() > 0) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setTitle("Warning");
                        alert.setMessage("If you turn on this schedule, similar schedules will be turn off!");
                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference().child("schedules").child(schedules.get(position).getScheduleId()).child("state").setValue("1");
                                for (Schedule sche : listSimilarSchedule) {
                                    FirebaseDatabase.getInstance().getReference().child("schedules").child(sche.getScheduleId()).child("state").setValue("0");
                                }
                            }
                        });
                        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onOff.setChecked(false);
                            }
                        });
                        alert.show();
                    } else {
                        FirebaseDatabase.getInstance().getReference().child("schedules").child(schedules.get(position).getScheduleId()).child("state").setValue("1");
                    }
                } else if (isChecked == false) {
                    FirebaseDatabase.getInstance().getReference().child("schedules").child(schedules.get(position).getScheduleId()).child("state").setValue("0");
                }
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "Clicked" + schedules.get(position).getScheduleId(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, ScheduleDetailActivity.class);
                intent.putExtra("scheduleId", schedules.get(position).getScheduleId());
                intent.putExtra("roomId", schedules.get(position).getRoomId());
                intent.putExtra("action", "edit");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                notifyDataSetChanged();
            }
        });
        return view;
    }
}
