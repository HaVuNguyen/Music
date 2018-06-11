package app.framgia.com.music.screen.home;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import app.framgia.com.music.R;
import app.framgia.com.music.data.SongAdapter;
import app.framgia.com.music.data.model.Song;
import app.framgia.com.music.data.remote.RemoteDataSource;
import app.framgia.com.music.data.remote.SoundcloudApiRequest;
import app.framgia.com.music.utils.Contants;
import app.framgia.com.music.utils.Utility;
import com.android.volley.RequestQueue;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "APP";
    private RecyclerView mRecyclerView;
    private SongAdapter mAdapter;
    private ArrayList<Song> songList;
    private int currentIndex;
    private TextView mTextNameSong, mTextDuration, mTextTime;
    private ImageView mImagePlay, mImageNext, mImagePrevious;
    private ProgressBar mProgressBarLoader, mProgressBarMainLoader;
    private MediaPlayer mediaPlayer;
    private long currentSongLength;
    private SeekBar mSeekBar;
    private boolean firstLaunch = true;
    private FloatingActionButton fab_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        //Initialisation des vues
        initializeViews();
        //Requête récupérant les chansons
        getSongList( "" );

        songList = new ArrayList<>();

        mRecyclerView.setLayoutManager( new LinearLayoutManager( getApplicationContext() ) );
        mAdapter = new SongAdapter( getApplicationContext(), songList,
                new SongAdapter.RecyclerItemClickListener() {
                    @Override
                    public void onClickListener(Song song, int position) {
                        firstLaunch = false;
                        changeSelectedSong( position );
                        prepareSong( song );
                    }
                } );
        mRecyclerView.setAdapter( mAdapter );

        //Initialisation du media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
        mediaPlayer.setOnPreparedListener( new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //Lancer la chanson
                togglePlay( mp );
            }
        } );
        mediaPlayer.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (currentIndex + 1 < songList.size()) {
                    Song next = songList.get( currentIndex + 1 );
                    changeSelectedSong( currentIndex + 1 );
                    prepareSong( next );
                } else {
                    Song next = songList.get( 0 );
                    changeSelectedSong( 0 );
                    prepareSong( next );
                }
            }
        } );

        //Gestion de la seekbar
        handleSeekbar();

        //Controle de la chanson
        pushPlay();
        pushPrevious();
        pushNext();

        //Gestion du click sur le bouton rechercher
        fab_search.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog();
            }
        } );
    }

    private void handleSeekbar() {
        mSeekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo( progress * 1000 );
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        } );
    }

    private void prepareSong(Song song) {

        currentSongLength = song.getDuration();
        mProgressBarLoader.setVisibility( View.VISIBLE );
        mTextNameSong.setVisibility( View.GONE );
        mImagePlay.setImageDrawable( ContextCompat.getDrawable( this, R.drawable.selector_play ) );
        mTextNameSong.setText( song.getTitle() );
        mTextTime.setText( Utility.convertDuration( song.getDuration() ) );
        String stream = song.getStreamUrl() + "?client_id=" + Contants.CLIENT_ID;
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource( stream );
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void togglePlay(MediaPlayer mp) {

        if (mp.isPlaying()) {
            mp.stop();
            mp.reset();
        } else {
            mProgressBarLoader.setVisibility( View.GONE );
            mTextNameSong.setVisibility( View.VISIBLE );
            mp.start();
            mImagePlay.setImageDrawable(
                    ContextCompat.getDrawable( this, R.drawable.selector_pause ) );
            final Handler mHandler = new Handler();
            this.runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    mSeekBar.setMax( (int) currentSongLength / 1000 );
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    mSeekBar.setProgress( mCurrentPosition );
                    mTextTime.setText(
                            Utility.convertDuration( (long) mediaPlayer.getCurrentPosition() ) );
                    mHandler.postDelayed( this, 1000 );
                }
            } );
        }
    }

    private void initializeViews() {

        mTextNameSong = (TextView) findViewById( R.id.text_name_song );
        mImagePlay = (ImageView) findViewById( R.id.image_button_play );
        mImageNext = (ImageView) findViewById( R.id.image_button_next );
        mImagePrevious = (ImageView) findViewById( R.id.image_button_prev );
        mProgressBarLoader = (ProgressBar) findViewById( R.id.progressbar_loader );
        mProgressBarMainLoader = (ProgressBar) findViewById( R.id.progressbar_main_loader );
        mRecyclerView = (RecyclerView) findViewById( R.id.recycler_main );
        mSeekBar = (SeekBar) findViewById( R.id.seek_bar );
        mTextTime = (TextView) findViewById( R.id.text_time );
        fab_search = (FloatingActionButton) findViewById( R.id.fab_search );
    }

    public void getSongList(String query) {
        RequestQueue queue = RemoteDataSource.getInstance( this ).getRequestQueue();
        SoundcloudApiRequest request = new SoundcloudApiRequest( queue );
        mProgressBarMainLoader.setVisibility( View.VISIBLE );
        request.getSongList( query, new SoundcloudApiRequest.SoundcloudInterface() {
            @Override
            public void onSuccess(ArrayList<Song> songs) {
                currentIndex = 0;
                mProgressBarMainLoader.setVisibility( View.GONE );
                songList.clear();
                songList.addAll( songs );
                mAdapter.notifyDataSetChanged();
                mAdapter.setSelectedPosition( 0 );
            }

            @Override
            public void onError(String message) {
                mProgressBarMainLoader.setVisibility( View.GONE );
                Toast.makeText( MainActivity.this, message, Toast.LENGTH_SHORT ).show();
            }
        } );
    }

    private void changeSelectedSong(int index) {
        mAdapter.notifyItemChanged( mAdapter.getSelectedPosition() );
        currentIndex = index;
        mAdapter.setSelectedPosition( currentIndex );
        mAdapter.notifyItemChanged( currentIndex );
    }

    private void pushPlay() {
        mImagePlay.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying() && mediaPlayer != null) {
                    mImagePlay.setImageDrawable( ContextCompat.getDrawable( MainActivity.this,
                            R.drawable.selector_play ) );
                    mediaPlayer.pause();
                } else {
                    if (firstLaunch) {
                        Song song = songList.get( 0 );
                        changeSelectedSong( 0 );
                        prepareSong( song );
                    } else {
                        mediaPlayer.start();
                        firstLaunch = false;
                    }
                    mImagePlay.setImageDrawable( ContextCompat.getDrawable( MainActivity.this,
                            R.drawable.selector_pause ) );
                }
            }
        } );
    }

    private void pushPrevious() {

        mImagePrevious.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLaunch = false;
                if (mediaPlayer != null) {

                    if (currentIndex - 1 >= 0) {
                        Song previous = songList.get( currentIndex - 1 );
                        changeSelectedSong( currentIndex - 1 );
                        prepareSong( previous );
                    } else {
                        changeSelectedSong( songList.size() - 1 );
                        prepareSong( songList.get( songList.size() - 1 ) );
                    }
                }
            }
        } );
    }

    private void pushNext() {

        mImageNext.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLaunch = false;
                if (mediaPlayer != null) {

                    if (currentIndex + 1 < songList.size()) {
                        Song next = songList.get( currentIndex + 1 );
                        changeSelectedSong( currentIndex + 1 );
                        prepareSong( next );
                    } else {
                        changeSelectedSong( 0 );
                        prepareSong( songList.get( 0 ) );
                    }
                }
            }
        } );
    }

    public void createDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this );
        final View view = getLayoutInflater().inflate( R.layout.item_search, null );
        builder.setTitle( R.string.rechercher );
        builder.setView( view );
        builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText et_search = (EditText) view.findViewById( R.id.edit_search );
                String search = et_search.getText().toString().trim();
                if (search.length() > 0) {
                    getSongList( search );
                } else {
                    Toast.makeText( MainActivity.this, "Vui lòng điền vào", Toast.LENGTH_SHORT )
                            .show();
                }
            }
        } );

        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.option_home, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.image_option_download:
                Toast.makeText( getApplicationContext(), "Download", Toast.LENGTH_LONG ).show();
                return true;
            case R.id.image_option_playlist:
                Toast.makeText( getApplicationContext(), "Add to Playlist", Toast.LENGTH_LONG )
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected( item );
        }
    }
}
