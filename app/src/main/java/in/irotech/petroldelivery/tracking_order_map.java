package in.irotech.petroldelivery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class tracking_order_map extends FragmentActivity implements OnMapReadyCallback {

    private String token;
    private GoogleMap mMap;
    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference root=firebaseDatabase.getReference();
    private RelativeLayout pendingScreen;
    private Button cancleButton;
    private TextView trackLabel;
    private LatLng yourLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(tracking_order_map.this);

        trackLabel=findViewById(R.id.trackLabel);
        pendingScreen=findViewById(R.id.pendingScreen);
        if(getIntent().getExtras()!=null){
            token=getIntent().getStringExtra("token");
            root.child("Clients").child(token).child("status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue().toString().equals("inProcess")){
                        pendingScreen.setVisibility(View.VISIBLE);
                    }else if(dataSnapshot.getValue().toString().equals("tracking")){
                        pendingScreen.setVisibility(View.GONE);
                        root.child("Clients").child(token).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild("sCoordinates")){
                                    root.child("Clients").child(token).child("sCoordinates").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            seriveLocation((double)dataSnapshot.child("sLati").getValue(),(double)dataSnapshot.child("sLng").getValue());
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }else if(dataSnapshot.getValue().toString().equals("delivered")){
                        AlertDialog.Builder builder=new AlertDialog.Builder(tracking_order_map.this);
                        LayoutInflater inflater=getLayoutInflater();
                        View view1=inflater.inflate(R.layout.diloag_view_deilvered,null);
                        final Button order_sucess=view1.findViewById(R.id.deliver_success_btn);
                        builder.setView(view1);
                        order_sucess.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            removeOrder();
                            //dont put finish here, as it is showing the behaviour of interlinked with MainActivity, means that listener of that scree is working still, when u set the status to done with above methord it MainActicity listen to that and set it to mainBtn mode, and if u try finish() also after this, it will take u to order page as mainActicity was intent of order page
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        cancleButton=findViewById(R.id.cancelOrder);
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeOrder();
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapStyleOptions mapStyleOptions=MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style);
        mMap.setMapStyle(mapStyleOptions);
        if(getIntent().getExtras()!=null){
            double mLng=getIntent().getExtras().getDouble("longnitude");
            double mLati=getIntent().getExtras().getDouble("latitude");
            Log.d("loc",mLati+":"+mLng);
            yourLocation = new LatLng(mLng,mLati);

            mMap.addMarker(new MarkerOptions()
                    .position(yourLocation)
                    .title("Your Location")
                    .icon(bitmapDescriptorFromVector(tracking_order_map.this, R.drawable.marker_my_location)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yourLocation,15F));
        }
    }

    public void seriveLocation(double sLati,double sLng){
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(sLng,sLati))
                .title("Delivery")
                .icon(bitmapDescriptorFromVector(tracking_order_map.this, R.drawable.marker_pump_loactions)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(sLng,sLati),15F));
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void removeOrder(){
        root.child("Clients").child(token).child("Pump").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                root.child("Service").child(dataSnapshot.getValue().toString()).child(token).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        root.child("Clients").child(token).child("Pump").removeValue();
        root.child("Clients").child(token).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("sCoordinates")){
                    root.child("Clients").child(token).child("sCoordinates").removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //at LAST always
        root.child("Clients").child(token).child("status").setValue("done");
    }
}
