package com.example.manas.musicplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

import static com.example.manas.musicplayer.AlbumDetailsAdapter.albumFiles;
import static com.example.manas.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.example.manas.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.example.manas.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.manas.musicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.manas.musicplayer.MainActivity.musicFiles;
import static com.example.manas.musicplayer.MainActivity.repeatBoolean;
import static com.example.manas.musicplayer.MainActivity.shuffleBoolean;
import static com.example.manas.musicplayer.MusicAdapter.mFiles;

public class playerActivity extends AppCompatActivity implements  ActionPlaying, ServiceConnection {

    TextView songName, artistName, durationPlayed, durationTotal;
    ImageView coverArt, nextBtn, backBtn, prevBtn, repeatBtn, shuffleBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekbar;
    int position = -1;
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
   // static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, nextThread, prevThread;
    MusicService musicService;
    MediaSessionCompat mediaSessionCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(),"My Audio");
        inItViews();
        getIntentMethod();


        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {
                    musicService.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        playerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekbar.setProgress(mCurrentPosition);
                    durationPlayed.setText(formattedText(mCurrentPosition));
                }


                handler.postDelayed(this, 0);
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shuffleBoolean){
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_off);
                }
                else{
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_on);
                }
            }
        });
        
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatBoolean){
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_off);
                }
                else{
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_on);
                }
            }
        });
    }

    @Override
    protected void onPostResume() {
        Intent intent = new Intent(this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        playThreadBtn();
        prevThreadBtn();
        nextThreadBtn();
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();

    }

    public void nextBtnClicked() {

        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size()-1);
            }
            else if (!shuffleBoolean && !repeatBoolean)
            {
            position = ((position + 1) % listSongs.size());}
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekbar.setMax(musicService.getDuration() / 1000);
            playerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekbar.setProgress(mCurrentPosition);
                    }

                    handler.postDelayed(this, 1000);
                }
            });
            musicService.onCompleted();
            showNotification(R.drawable.ic_baseline_pause_24);
            playPauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            musicService.start();
        } else {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size()-1);
            }
            else if (!shuffleBoolean && !repeatBoolean)
            {
                position = ((position + 1) % listSongs.size());}
            uri = Uri.parse(listSongs.get(position).getPath());
           musicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekbar.setMax(musicService.getDuration() / 1000);
            playerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekbar.setProgress(mCurrentPosition);
                    }

                    handler.postDelayed(this, 1000);
                }
            });
            showNotification(R.drawable.ic_baseline_play_arrow_24);
            musicService.onCompleted();
            playPauseBtn.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }

    private void prevThreadBtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked() {

        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size()-1);
            }
            else if (!shuffleBoolean && !repeatBoolean)
            {
            position = (position - 1) < 0 ? listSongs.size() - 1 : (position - 1);}
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekbar.setMax(musicService.getDuration() / 1000);
            playerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekbar.setProgress(mCurrentPosition);
                    }

                    handler.postDelayed(this, 1000);
                }
            });
            musicService.onCompleted();
            showNotification(R.drawable.ic_baseline_play_arrow_24);
            playPauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            musicService.start();
        } else {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size()-1);
            }
            else if (!shuffleBoolean && !repeatBoolean)
            {
                position = (position - 1) < 0 ? listSongs.size() - 1 : (position - 1);}
            uri = Uri.parse(listSongs.get(position).getPath());
           musicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekbar.setMax(musicService.getDuration() / 1000);
            playerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekbar.setProgress(mCurrentPosition);
                    }

                    handler.postDelayed(this, 1000);
                }
            });
            showNotification(R.drawable.ic_baseline_play_arrow_24);
            musicService.onCompleted();
            playPauseBtn.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }

    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked() {
        if (musicService.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            showNotification(R.drawable.ic_baseline_play_arrow_24);
            musicService.pause();
            seekbar.setMax(musicService.getDuration() / 1000);
            playerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekbar.setProgress(mCurrentPosition);
                    }

                    handler.postDelayed(this, 1000);
                }
            });
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);
            musicService.start();
            showNotification(R.drawable.ic_baseline_pause_24);
            seekbar.setMax(musicService.getDuration() / 1000);
            playerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekbar.setProgress(mCurrentPosition);
                    }

                    handler.postDelayed(this, 1000);
                }
            });
        }

    }

    private String formattedText(int mCurrentPosition) {
        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;

        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalOut;
        }

    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");
        if (sender != null && sender.equals("albumDetails"))
        {

            listSongs = albumFiles;
        }
        else{
            listSongs = mFiles;
        }

        if (listSongs != null) {
            playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        showNotification(R.drawable.ic_baseline_pause_24);

      Intent intent = new Intent(this,MusicService.class);
        intent.putExtra("servicePosition",position);
        startService(intent);

    }

    private void inItViews() {
        songName = findViewById(R.id.song_name);
        artistName = findViewById(R.id.song_artist);
        durationPlayed = findViewById(R.id.duration_played);
        durationTotal = findViewById(R.id.duration_total);
        coverArt = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.next);
        backBtn = findViewById(R.id.next);

        prevBtn = findViewById(R.id.prev);
        repeatBtn = findViewById(R.id.repeat);
        shuffleBtn = findViewById(R.id.shuffle);
        playPauseBtn = findViewById(R.id.play_pause_btn);
        seekbar = findViewById(R.id.seek_bar);

    }

    private void metaData(Uri uri) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotals = Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        durationTotal.setText(formattedText(durationTotals));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this,coverArt,bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null) {
                        ImageView gradient = findViewById(R.id.image_view_gradient);
                        RelativeLayout mContainer = findViewById(R.id.m_container);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});
                        gradient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBg);
                        songName.setTextColor(swatch.getTitleTextColor());
                        artistName.setTextColor(swatch.getBodyTextColor());
                    } else {
                        if (swatch != null) {
                            ImageView gradient = findViewById(R.id.image_view_gradient);
                            RelativeLayout mContainer = findViewById(R.id.m_container);
                            gradient.setBackgroundResource(R.drawable.gradient_bg);
                            mContainer.setBackgroundResource(R.drawable.main_bg);
                            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{0xff000000, 0xff000000});
                            gradient.setBackground(gradientDrawable);

                            GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{0xff000000, 0xff000000});
                            mContainer.setBackground(gradientDrawableBg);
                            songName.setTextColor(Color.WHITE);
                            artistName.setTextColor(Color.DKGRAY);
                        }
                    }
                }
            });
        } else {

            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.music)
                    .into(coverArt);
        }
    }

    public void ImageAnimation(final Context context , final ImageView imageView , final Bitmap bitmap){

       Animation animOut = AnimationUtils.loadAnimation(context,android.R.anim.fade_out);
       final Animation animIn = AnimationUtils.loadAnimation(context,android.R.anim.fade_in);
       animOut.setAnimationListener(new Animation.AnimationListener() {
           @Override
           public void onAnimationStart(Animation animation) {

           }

           @Override
           public void onAnimationEnd(Animation animation) {

               Glide.with(context).load(bitmap).into(imageView);
               animIn.setAnimationListener(new Animation.AnimationListener() {
                   @Override
                   public void onAnimationStart(Animation animation) {

                   }

                   @Override
                   public void onAnimationEnd(Animation animation) {

                   }

                   @Override
                   public void onAnimationRepeat(Animation animation) {

                   }
               });
               imageView.startAnimation(animIn);
           }

           @Override
           public void onAnimationRepeat(Animation animation) {

           }
       });
        imageView.startAnimation(animOut);
    }



    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder)service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        Toast.makeText(this, "Connected"+ musicService, Toast.LENGTH_SHORT).show();
        seekbar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        songName.setText(listSongs.get(position).getTitle());
        artistName.setText(listSongs.get(position).getArtist());
        musicService.onCompleted();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

        musicService = null;
    }

    void showNotification(int playPauseBtn){

        Intent intent = new Intent(this,playerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent,0);

        Intent prevIntent = new Intent(this,NotificationReciever.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this,NotificationReciever .class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this,0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this,NotificationReciever.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        picture = getAlbum(listSongs.get(position).getPath());
        Bitmap thumb = null;
        if(picture != null){
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);
        }
        else{
            thumb = BitmapFactory.decodeResource(getResources(),R.drawable.music_image_1);
        }
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(listSongs.get(position).getTitle())
                .setContentText(listSongs.get(position).getArtist())
                .addAction(R.drawable.ic_baseline_skip_previous,"Previous",prevPending)
                .addAction(playPauseBtn,"Pause",pausePending)
                .addAction(R.drawable.ic_baseline_skip_next_24,"Next",nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);
    }
    private byte[] getAlbum (String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return  art;
    }
}