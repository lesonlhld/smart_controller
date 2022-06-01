package letrungson.com.smartcontroller.tools;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import letrungson.com.smartcontroller.model.Schedule;

public class TurnOnOffSchedule {

    static public int isLater(int hour1, int minute1, int hour2, int minute2) {
        if (hour1 > hour2) return 1;
        else if (hour1 < hour2) return -1;
        else {
            if (minute1 > minute2) return 1;
            else if (minute1 < minute2) return -1;
            else return 0;
        }
    }

    static public List<Schedule> turnOffSimilarSchedule(List<Schedule> listSchedule, int position) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        Schedule thisSchedule = listSchedule.get(position);
        int thisStartHour, thisStartMinute;
        int thisEndHour, thisEndMinute;
        thisStartHour = Integer.parseInt(thisSchedule.getStartTime().substring(0, 2));
        thisStartMinute = Integer.parseInt(thisSchedule.getStartTime().substring(3, 5));
        thisEndHour = Integer.parseInt(thisSchedule.getEndTime().substring(0, 2));
        thisEndMinute = Integer.parseInt(thisSchedule.getEndTime().substring(3, 5));

        // duyệt các schedule
        List<Schedule> listSimilarSchedule = new ArrayList<Schedule>();
        for (Schedule sche : listSchedule) {
            if (!thisSchedule.getScheduleId().equals(sche.getScheduleId()) && sche.getState().equals("1")) {
                int startHour, startMinute;
                int endHour, endMinute;
                startHour = Integer.parseInt(sche.getStartTime().substring(0, 2));
                startMinute = Integer.parseInt(sche.getStartTime().substring(3, 5));
                endHour = Integer.parseInt(sche.getEndTime().substring(0, 2));
                endMinute = Integer.parseInt(sche.getEndTime().substring(3, 5));
                // check xem có trùng giờ không
                //Log.d("test",thisStartHour + ":" + thisStartMinute + " " + thisEndHour + ":" + thisEndMinute);
                //Log.d("test", startHour + ":" + startMinute + " " + endHour + ":" + endMinute);
                boolean isTimeSimilar = true;
                if (isLater(thisEndHour, thisEndMinute, startHour, startMinute) == -1) {
                    isTimeSimilar = false;
                }
                if (isLater(thisStartHour, thisStartMinute, endHour, endMinute) == 1) {
                    isTimeSimilar = false;
                }
                if (isTimeSimilar) {
                    // đã trùng giờ, check xem có trùng ngày không
                    boolean isDaySimilar = false;
                    for (int i = 0; i < 7; i++) {
                        if (thisSchedule.getRepeatDay().charAt(i) == '1' && sche.getRepeatDay().charAt(i) == '1') {
                            isDaySimilar = true;
                            break;
                        }
                    }
                    if (isDaySimilar) {
                        listSimilarSchedule.add(sche);
                    }
                }
            }
        }
        return listSimilarSchedule;
    }
}
