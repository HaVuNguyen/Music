package app.framgia.com.music.data;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import app.framgia.com.music.R;
import app.framgia.com.music.data.model.Song;
import app.framgia.com.music.utils.Utility;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private Context mContext;
    private ArrayList<Song> songList;
    private RecyclerItemClickListener listener;
    private int selectedPosition;

    public SongAdapter(Context context, ArrayList<Song> songList,
            RecyclerItemClickListener listener) {

        this.mContext = context;
        this.songList = songList;
        this.listener = listener;
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from( parent.getContext() )
                .inflate( R.layout.item_song, parent, false );

        return new SongViewHolder( view );
    }

    public void onDownLoadError(String mgs) {
        Toast.makeText( mContext, mgs, Toast.LENGTH_SHORT ).show();
    }

    public void onDownloading() {
        Toast.makeText( mContext, "Downloading...", Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {

        Song song = songList.get( position );
        if (song != null) {

            if (selectedPosition == position) {
                holder.itemView.setBackgroundColor(
                        ContextCompat.getColor( mContext, R.color.colorPrimary ) );
                holder.mImagePlay.setVisibility( View.VISIBLE );
            } else {
                holder.itemView.setBackgroundColor(
                        ContextCompat.getColor( mContext, android.R.color.transparent ) );
                holder.mImagePlay.setVisibility( View.INVISIBLE );
            }

            holder.mTextNameSong.setText( song.getTitle() );
            holder.mTextNameSing.setText( song.getArtist() );
            String duration = Utility.convertDuration( song.getDuration() );
            holder.mTextDuration.setText( duration );
            Picasso.with( mContext )
                    .load( song.getArtworkUrl() )
                    .placeholder( R.drawable.ic_drummer )
                    .into( holder.mImageSing );

            holder.bind( song, listener );
        }
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextNameSong, mTextNameSing, mTextDuration, mTextOption;
        private ImageView mImageSing, mImagePlay;

        public SongViewHolder(View itemView) {
            super( itemView );
            mTextNameSong = (TextView) itemView.findViewById( R.id.text_name_song );
            mTextNameSing = (TextView) itemView.findViewById( R.id.text_name_sing );
            mTextDuration = (TextView) itemView.findViewById( R.id.text_duration );
            mImageSing = (ImageView) itemView.findViewById( R.id.image_sing );
            mImagePlay = (ImageView) itemView.findViewById( R.id.image_button_play );
            mTextOption = (TextView) itemView.findViewById( R.id.text_option );
        }

        public void bind(final Song song, final RecyclerItemClickListener listener) {
            itemView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickListener( song, getLayoutPosition() );
                }
            } );
        }
    }

    public interface RecyclerItemClickListener {
        void onClickListener(Song song, int position);
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}
