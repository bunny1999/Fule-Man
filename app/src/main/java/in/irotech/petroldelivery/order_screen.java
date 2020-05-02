package in.irotech.petroldelivery;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.zip.Inflater;

public class order_screen extends AppCompatActivity {
    private String token;

    private TextView titleName;
    private TextView addressStr;
    private EditText leters;
    private Button orderButton;
    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference root=firebaseDatabase.getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_screen);

        titleName=findViewById(R.id.pumpTitle);
        addressStr=findViewById(R.id.pumpAddress);
        leters=findViewById(R.id.leterCountEdit);
        orderButton=findViewById(R.id.orderBtn);

        if (getIntent().getExtras()!=null) {
            token=getIntent().getStringExtra("token");
            if (!token.equals("")) {
                final String title=getIntent().getStringExtra("PumpName");
                String address=getIntent().getStringExtra("PumpAddress");
                final String mLati=getIntent().getStringExtra("mLati");
                final String mLng=getIntent().getStringExtra("mLng");
                titleName.setText(title);
                addressStr.setText(address);

                DatabaseReference Client=root.child("Clients");
                final DatabaseReference Service=root.child("Service");
                final DatabaseReference thisToken=Client.child(token);
                final DatabaseReference thisPetrolPump=Service.child(title);

                orderButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        String count=leters.getText().toString();
                        if(!count.isEmpty()){
                            //client update
                            final HashMap forClient=new HashMap();
                            forClient.put("status","inProcess");
                            forClient.put("Pump",title);

                            //service update
                            HashMap forService=new HashMap();
//                            forService.put("token",token);
                            forService.put("Status","Pending");
                            forService.put("Count",count);

                            HashMap<String,String> coordinates=new HashMap<>();
                            coordinates.put("cLati",mLati);
                            coordinates.put("cLng",mLng);

                            forService.put("cCoordinates",coordinates);

                            thisPetrolPump.child(token).setValue(forService).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    thisToken.setValue(forClient).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            AlertDialog.Builder builder=new AlertDialog.Builder(order_screen.this);
                                            LayoutInflater inflater=getLayoutInflater();
                                            View view1=inflater.inflate(R.layout.dialog_inflater_view_on_success,null);
                                            final Button order_sucess=view1.findViewById(R.id.order_success_btn);
                                            builder.setView(view1);
                                            order_sucess.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Intent intent=new Intent(order_screen.this,MainActivity.class);
                                                    startActivity(intent);
                                                }
                                            });
                                            builder.setCancelable(false);
                                            builder.show();
                                        }
                                    });
                                }
                            });
                        }else{
                            leters.setError("Empty!");
                        }
                    }
                });
            }
        }
    }
}
