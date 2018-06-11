package app.framgia.com.music.data.remote;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RemoteDataSource {

    private static RemoteDataSource mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private RemoteDataSource(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized RemoteDataSource getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RemoteDataSource( context );
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue( mCtx.getApplicationContext() );
        }
        return mRequestQueue;
    }
}
