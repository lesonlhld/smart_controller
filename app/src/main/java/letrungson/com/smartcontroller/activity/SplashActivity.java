package letrungson.com.smartcontroller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import letrungson.com.smartcontroller.util.Constant;

public class SplashActivity extends AppCompatActivity {
    public static List<String> typeDevices = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Constant.initServer();
//                typeDevices.add("All Household Equipments");
//                typeDevices.add("Air Conditioner");
//                typeDevices.add("Fan");
//                typeDevices.add("Heater");
//                typeDevices.add("Sensor");
//                typeDevices.add("Others");
//                FirebaseDatabase.getInstance().getReference("typeDevices").setValue(typeDevices);

                Query allType = FirebaseDatabase.getInstance().getReference("typeDevices");
                allType.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        typeDevices.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            String type = data.getValue(String.class);
                            typeDevices.add(type);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(getApplicationContext(), AccountActivity.class));
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }, 3000);
    }
}