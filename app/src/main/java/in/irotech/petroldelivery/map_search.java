package in.irotech.petroldelivery;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//FragmentActivity
public class map_search extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RequestQueue requestQueue;
    public int RANGE_RADIOUS=0;
    public ProgressBar mapFetchingProgres;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapFetchingProgres=findViewById(R.id.mapFetchingProgress);
        if(getIntent().getExtras()!=null){
            token=getIntent().getStringExtra("token");
        }
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
            LatLng yourLocation = new LatLng(mLng,mLati);

            mMap.addMarker(new MarkerOptions()
                    .position(yourLocation)
                    .title("Your Location")
                    .icon(bitmapDescriptorFromVector(map_search.this, R.drawable.marker_my_location)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yourLocation,20F));

            //Searching for Petrol Pumps
            requestQueue=VollySingletone.getInstance(this).getRequestQueue();
            fetchNearByPetrolPump(mLng,mLati);
        }
    }

    public void fetchNearByPetrolPump(double longitude,double latitude){
        final double mLng=longitude;
        final double mLati=latitude;
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" +mLng+ "," +mLati);
        googlePlaceUrl.append("&radius=" + RANGE_RADIOUS);
        googlePlaceUrl.append("&types=" + "gas_station");
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + "AIzaSyDDfPTAeQB2gDcwU6nIUkjIaKrbBkHR4ug");

        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, googlePlaceUrl.toString(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray allPumps =response.getJSONArray("results");
                    if(allPumps.length()<4){
                        if(RANGE_RADIOUS<5000) {
                            RANGE_RADIOUS = RANGE_RADIOUS + 100;
                            fetchNearByPetrolPump(mLng, mLati);
                        }else{
                            AlertDialog.Builder builder=new AlertDialog.Builder(map_search.this);
                            builder.setMessage("No Petrol Pump Found Near You!");
                            builder.setCancelable(false);
                            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent=new Intent(map_search.this,MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }
                    }else{
                        mapFetchingProgres.setVisibility(View.GONE);

                        //for camera postioing
                        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(new LatLng(mLng,mLati));

                        //For Bottom sheet
                        LayoutInflater inflater=getLayoutInflater();

                        LinearLayout layout=findViewById(R.id.pumList);

                        LinearLayout layout1=findViewById(R.id.bottom_sheet);
                        BottomSheetBehavior bottomSheetBehavior=BottomSheetBehavior.from(layout1);

                        final View blurBackgroudViewMapScreen=findViewById(R.id.blurBg);
                        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                            @Override
                            public void onStateChanged(@NonNull View view, int i) {
                                if(i==BottomSheetBehavior.STATE_COLLAPSED)
                                {
                                    blurBackgroudViewMapScreen.setVisibility(View.GONE);
                                    moveCamera(0.10,builder.build());
                                }else{
                                    blurBackgroudViewMapScreen.setVisibility(View.VISIBLE);
                                    moveCamera(0.30,builder.build());
                                }
                            }
                            @Override
                            public void onSlide(@NonNull View view, float v) {

                            }
                        });
                        //dragger of bottom sheet
                        ImageButton dragger=findViewById(R.id.bottomSheetDrager);

                        //maker location for pumps
                        BitmapDescriptor pumpLocationMarker=bitmapDescriptorFromVector(map_search.this, R.drawable.marker_pump_loactions);

                        for(int i=0;i<allPumps.length();i++){
                            JSONObject jsonObject=allPumps.getJSONObject(i);
                            MarkerOptions markerOptions=new MarkerOptions();
                            double lat=jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                            double lng=jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                            final String pumpName=jsonObject.getString("name");
                            final String address=jsonObject.getString("vicinity");
                            LatLng latLng=new LatLng(lat,lng);
                            markerOptions.position(latLng);
                            markerOptions.title(pumpName+" : "+address);
                            markerOptions.icon(pumpLocationMarker);
                            mMap.addMarker(markerOptions);
                            //for move camera
                            builder.include(latLng);

                            View view=inflater.inflate(R.layout.list_tile_pumps,null);
                            TextView namePump=view.findViewById(R.id.pumpName);
                            TextView addressPump=view.findViewById(R.id.pumpAddress);
                            TextView distancePump=view.findViewById(R.id.pumpDistance);
                            namePump.setText(pumpName);
                            addressPump.setText(address);

                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Bundle bundle=new Bundle();
                                    bundle.putString("PumpName",pumpName);
                                    bundle.putString("PumpAddress",address);
                                    bundle.putString("mLati",mLati+"");
                                    bundle.putString("mLng",mLng+"");
                                    bundle.putString("token",token);
                                    Intent intent=new Intent(map_search.this,order_screen.class);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }
                            });

                            layout.addView(view);
                        }

                        //for camera
                        moveCamera(0.10,builder.build());

                        bottomSheetBehavior.setPeekHeight(dragger.getHeight());
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error","Faithing API Error");
            }
        });
        requestQueue.add(request);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void moveCamera(double focus,LatLngBounds bounds){
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * focus);
        CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(bounds,width,height,padding);
        mMap.moveCamera(cu);
        mMap.animateCamera(cu);
    }
}
