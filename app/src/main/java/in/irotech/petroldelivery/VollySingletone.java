package in.irotech.petroldelivery;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VollySingletone {
    public static VollySingletone mVollySingleton;
    public RequestQueue mRequestQueue;

    public VollySingletone(Context context){
        mRequestQueue= Volley.newRequestQueue(context.getApplicationContext());

    }

    public static synchronized VollySingletone getInstance(Context context){
        if(mVollySingleton==null){
            mVollySingleton=new VollySingletone(context);
        }
        return mVollySingleton;
    }

    public RequestQueue getRequestQueue(){
        return mRequestQueue;
    }
}
