package app.framgia.com.music.data.remote;

import android.util.Log;
import app.framgia.com.music.data.model.Song;
import app.framgia.com.music.utils.Contants;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SoundcloudApiRequest {

    public interface SoundcloudInterface {
        void onSuccess(ArrayList<Song> songs);

        void onError(String message);
    }

    private RequestQueue queue;
    private static final String URL = Contants.BASE_URL + Contants.CLIENT_ID;
    private static final String TAG = "APP";

    public SoundcloudApiRequest(RequestQueue queue) {
        this.queue = queue;
    }

    public void getSongList(String query, final SoundcloudInterface callback) {

        String url = URL;
        if (query.length() > 0) {
            try {
                query = URLEncoder.encode( query, "UTF-8" );
                url = URL + "&q=" + query;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        Log.d( TAG, "getSongList: " + url );

        JsonArrayRequest request =
                new JsonArrayRequest( Request.Method.GET, url, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d( TAG, "onResponse: " + response );

                        ArrayList<Song> songs = new ArrayList<>();
                        if (response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject songObject = response.getJSONObject( i );
                                    long id = songObject.getLong( "id" );
                                    String title = songObject.getString( "title" );
                                    String artworkUrl = songObject.getString( "artwork_url" );
                                    String streamUrl = songObject.getString( "stream_url" );
                                    long duration = songObject.getLong( "duration" );
                                    int playbackCount =
                                            songObject.has( "playback_count" ) ? songObject.getInt(
                                                    "playback_count" ) : 0;
                                    JSONObject user = songObject.getJSONObject( "user" );
                                    String artist = user.getString( "username" );

                                    Song song = new Song( id, title, artist, artworkUrl, duration,
                                            streamUrl, playbackCount );
                                    songs.add( song );
                                } catch (JSONException e) {
                                    Log.d( TAG, "onResponse: " + e.getMessage() );
                                    callback.onError( "Đã xãy ra lỗi" );
                                    e.printStackTrace();
                                }
                            }

                            callback.onSuccess( songs );
                        } else {
                            callback.onError( "Không tìm thấy bài hát nào!" );
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d( TAG, "onResponse: " + error.getMessage() );
                        callback.onError( "Đã xẩy ra lỗi!" );
                    }
                } );

        queue.add( request );
    }
}
