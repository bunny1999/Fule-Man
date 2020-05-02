package in.irotech.petroldelivery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference root=firebaseDatabase.getReference();
    private String token="abc123";
    private double mLati;
    private double mLng;
    private LinearLayout linearLayout;
    private RelativeLayout splashScreen;
    private View mainBtnView;
    private ValueEventListener tokenStatusListner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout=findViewById(R.id.mainLayout);
        final LayoutInflater inflater=getLayoutInflater();
        splashScreen=findViewById(R.id.splashScreen);
        mainBtnView=inflater.inflate(R.layout.view_order_main_page,null);
        ImageButton mainBtn=mainBtnView.findViewById(R.id.mainButton);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getLocation = new Intent(MainActivity.this, progress_bar.class);
                getLocation.putExtra("token", token);
                startActivityForResult(getLocation, 121);
            }
        });

        tokenStatusListner =root.child("Clients").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                splashScreen.setVisibility(View.GONE);
                if(dataSnapshot.hasChild(token)){
                    root.child("Clients").child(token).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue().toString().equals("inProcess") || dataSnapshot.getValue().toString().equals("tracking")) {
                                Intent getLocation = new Intent(MainActivity.this, progress_bar.class);
                                getLocation.putExtra("token", token);
                                startActivityForResult(getLocation, 131);
                            } else {
                                linearLayout.addView(mainBtnView);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else{
                    linearLayout.addView(mainBtnView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        data.putExtra("token",token);
        Intent intent;
        if (requestCode==121) {
            if(data.getExtras()!=null){
                //for google map
                intent=new Intent(MainActivity.this,map_search.class);
                intent.putExtras(data);
                startActivity(intent);
            }
        }else if(requestCode==131){
            //tracking
            intent=new Intent(MainActivity.this,tracking_order_map.class);
            intent.putExtras(data);

            //may require result activity for feedback
            startActivity(intent);
        }
    }
}
