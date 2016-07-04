package com.rohan.app.nowplaying;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rohan.app.MusicPlayer;
import com.rohan.app.R;
import com.rohan.app.activities.BaseActivity;
import com.rohan.app.activities.MainActivity;
import com.rohan.app.adapters.BaseQueueAdapter;
import com.rohan.app.dataloaders.QueueLoader;
import com.rohan.app.listeners.MusicStateListener;
import com.rohan.app.timely.TimelyView;
import com.rohan.app.utils.Helpers;
import com.rohan.app.utils.TimberUtils;
import com.rohan.app.widgets.CircularSeekBar;
import com.rohan.app.widgets.DividerItemDecoration;
import com.rohan.app.widgets.PlayPauseButton;
import com.rohan.app.widgets.PlayPauseDrawable;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.security.InvalidParameterException;

public class BaseNowplayingFragment extends Fragment implements MusicStateListener {

    ImageView albumart;
    ImageView shuffle;
    ImageView repeat;
    MaterialIconView previous, next;
    PlayPauseButton mPlayPause;
    PlayPauseDrawable playPauseDrawable = new PlayPauseDrawable();
    FloatingActionButton playPauseFloating;
    View playPauseWrapper;

    String ateKey;
    int accentColor;

    TextView songtitle, songalbum, songartist, songduration, elapsedtime;
    SeekBar mProgress;
    //seekbar
    public Runnable mUpdateProgress = new Runnable() {

        @Override
        public void run() {

            if (mProgress != null) {
                long position = MusicPlayer.position();
                mProgress.setProgress((int) position);
                if (elapsedtime != null && getActivity() != null)
                    elapsedtime.setText(TimberUtils.makeShortTimeString(getActivity(), position / 1000));
            }

            if (MusicPlayer.isPlaying()) {
                mProgress.postDelayed(mUpdateProgress, 50);
            }

        }
    };
    CircularSeekBar mCircularProgress;
    //circular seekbar
    public Runnable mUpdateCircularProgress = new Runnable() {

        @Override
        public void run() {

            if (mCircularProgress != null) {
                long position = MusicPlayer.position();
                mCircularProgress.setProgress((int) position);
                if (elapsedtime != null && getActivity() != null)
                    elapsedtime.setText(TimberUtils.makeShortTimeString(getActivity(), position / 1000));

            }

            if (MusicPlayer.isPlaying()) {
                mCircularProgress.postDelayed(mUpdateCircularProgress, 50);
            }

        }
    };

    RecyclerView recyclerView;
    BaseQueueAdapter mAdapter;
    TimelyView timelyView11, timelyView12, timelyView13, timelyView14, timelyView15;
    TextView hourColon;
    int[] timeArr = new int[]{0, 0, 0, 0, 0};
    Handler mElapsedTimeHandler;
    public Runnable mUpdateElapsedTime = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null) {
                String time = TimberUtils.makeShortTimeString(getActivity(), MusicPlayer.position() / 1000);
                if (time.length() < 5) {
                    timelyView11.setVisibility(View.GONE);
                    timelyView12.setVisibility(View.GONE);
                    hourColon.setVisibility(View.GONE);
                    tv13(time.charAt(0) - '0');
                    tv14(time.charAt(2) - '0');
                    tv15(time.charAt(3) - '0');
                } else if (time.length() == 5) {
                    timelyView12.setVisibility(View.VISIBLE);
                    tv12(time.charAt(0) - '0');
                    tv13(time.charAt(1) - '0');
                    tv14(time.charAt(3) - '0');
                    tv15(time.charAt(4) - '0');
                } else {
                    timelyView11.setVisibility(View.VISIBLE);
                    hourColon.setVisibility(View.VISIBLE);
                    tv11(time.charAt(0) - '0');
                    tv12(time.charAt(2) - '0');
                    tv13(time.charAt(3) - '0');
                    tv14(time.charAt(5) - '0');
                    tv15(time.charAt(6) - '0');
                }
                mElapsedTimeHandler.postDelayed(this, 600);
            }

        }
    };
    private boolean duetoplaypause = false;
    private final View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            duetoplaypause = true;
            if (!mPlayPause.isPlayed()) {
                mPlayPause.setPlayed(true);
                mPlayPause.startAnimation();
            } else {
                mPlayPause.setPlayed(false);
                mPlayPause.startAnimation();
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.playOrPause();
                    if (recyclerView != null && recyclerView.getAdapter() != null)
                        recyclerView.getAdapter().notifyDataSetChanged();
                }
            }, 200);


        }
    };
    private final View.OnClickListener mFLoatingButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            duetoplaypause = true;
            playPauseDrawable.transformToPlay(true);
            playPauseDrawable.transformToPause(true);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.playOrPause();
                    if (recyclerView != null && recyclerView.getAdapter() != null)
                        recyclerView.getAdapter().notifyDataSetChanged();
                }
            }, 250);


        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ateKey = Helpers.getATEKey(getActivity());
        setStatusBarTranslucent(true);
        accentColor = ContextCompat.getColor(getActivity(), R.color.colorAccent);
    }

    public void setSongDetails(View view) {

        albumart = (ImageView) view.findViewById(R.id.album_art);
        shuffle = (ImageView) view.findViewById(R.id.shuffle);
        repeat = (ImageView) view.findViewById(R.id.repeat);
        next = (MaterialIconView) view.findViewById(R.id.next);
        previous = (MaterialIconView) view.findViewById(R.id.previous);
        mPlayPause = (PlayPauseButton) view.findViewById(R.id.playpause);
        playPauseFloating = (FloatingActionButton) view.findViewById(R.id.playpausefloating);
        playPauseWrapper = view.findViewById(R.id.playpausewrapper);

        songtitle = (TextView) view.findViewById(R.id.song_title);
        songtitle.setSelected(true);
        songalbum = (TextView) view.findViewById(R.id.song_album);
        songartist = (TextView) view.findViewById(R.id.song_artist);
        songduration = (TextView) view.findViewById(R.id.song_duration);
        elapsedtime = (TextView) view.findViewById(R.id.song_elapsed_time);

        timelyView11 = (TimelyView) view.findViewById(R.id.timelyView11);
        timelyView12 = (TimelyView) view.findViewById(R.id.timelyView12);
        timelyView13 = (TimelyView) view.findViewById(R.id.timelyView13);
        timelyView14 = (TimelyView) view.findViewById(R.id.timelyView14);
        timelyView15 = (TimelyView) view.findViewById(R.id.timelyView15);
        hourColon = (TextView) view.findViewById(R.id.hour_colon);

        mProgress = (SeekBar) view.findViewById(R.id.song_progress);
        mCircularProgress = (CircularSeekBar) view.findViewById(R.id.song_progress_circular);

        recyclerView = (RecyclerView) view.findViewById(R.id.queue_recyclerview);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("");
        }
        if (mPlayPause != null && getActivity() != null) {
            mPlayPause.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
        }

        if (playPauseFloating != null) {
            playPauseDrawable.setColorFilter(TimberUtils.getBlackWhiteColor(accentColor), PorterDuff.Mode.MULTIPLY);
            playPauseFloating.setImageDrawable(playPauseDrawable);
            if (MusicPlayer.isPlaying())
                playPauseDrawable.transformToPause(false);
            else playPauseDrawable.transformToPlay(false);
        }

        if (mCircularProgress != null) {
            mCircularProgress.setCircleProgressColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            mCircularProgress.setPointerColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            mCircularProgress.setPointerHaloColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        }

        if (timelyView11 != null) {
            String time = TimberUtils.makeShortTimeString(getActivity(), MusicPlayer.position() / 1000);
            if (time.length() < 5) {
                timelyView11.setVisibility(View.GONE);
                timelyView12.setVisibility(View.GONE);
                hourColon.setVisibility(View.GONE);

                changeDigit(timelyView13, time.charAt(0) - '0');
                changeDigit(timelyView14, time.charAt(2) - '0');
                changeDigit(timelyView15, time.charAt(3) - '0');

            } else if (time.length() == 5) {
                timelyView12.setVisibility(View.VISIBLE);
                changeDigit(timelyView12, time.charAt(0) - '0');
                changeDigit(timelyView13, time.charAt(1) - '0');
                changeDigit(timelyView14, time.charAt(3) - '0');
                changeDigit(timelyView15, time.charAt(4) - '0');
            } else {
                timelyView11.setVisibility(View.VISIBLE);
                hourColon.setVisibility(View.VISIBLE);
                changeDigit(timelyView11, time.charAt(0) - '0');
                changeDigit(timelyView12, time.charAt(2) - '0');
                changeDigit(timelyView13, time.charAt(3) - '0');
                changeDigit(timelyView14, time.charAt(5) - '0');
                changeDigit(timelyView15, time.charAt(6) - '0');
            }
        }

        setSongDetails();

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("dark_theme", false)) {
            ATE.apply(this, "dark_theme");
        } else {
            ATE.apply(this, "light_theme");
        }*/
    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void setSongDetails() {
        updateSongDetails();

        if (recyclerView != null)
            setQueueSongs();

        setSeekBarListener();

        if (next != null) {
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MusicPlayer.next();
                            notifyPlayingDrawableChange();
                        }
                    }, 200);

                }
            });
        }
        if (previous != null) {
            previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MusicPlayer.previous(getActivity(), false);
                            notifyPlayingDrawableChange();
                        }
                    }, 200);

                }
            });
        }

        if (playPauseWrapper != null)
            playPauseWrapper.setOnClickListener(mButtonListener);

        if (playPauseFloating != null)
            playPauseFloating.setOnClickListener(mFLoatingButtonListener);

        updateShuffleState();
        updateRepeatState();

    }

    public void updateShuffleState() {
        if (shuffle != null && getActivity() != null) {
            MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(getActivity())
                    .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE)
                    .setSizeDp(30);

            if (getActivity() != null) {
                if (MusicPlayer.getShuffleMode() == 0) {
                    builder.setColor(Color.WHITE);
                } else builder.setColor(accentColor);
            }

            shuffle.setImageDrawable(builder.build());
            shuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MusicPlayer.cycleShuffle();
                    updateShuffleState();
                    updateRepeatState();
                }
            });
        }
    }

    public void updateRepeatState() {
        if (repeat != null && getActivity() != null) {
            MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(getActivity())
                    .setIcon(MaterialDrawableBuilder.IconValue.REPEAT)
                    .setSizeDp(30);

            if (getActivity() != null) {
                if (MusicPlayer.getRepeatMode() == 0) {
                    builder.setColor(Color.WHITE);
                } else builder.setColor(accentColor);
            }

            repeat.setImageDrawable(builder.build());
            repeat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MusicPlayer.cycleRepeat();
                    updateRepeatState();
                    updateShuffleState();
                }
            });
        }
    }

    private void setSeekBarListener() {
        if (mProgress != null)
            mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b) {
                        MusicPlayer.seek((long) i);
                        //MainActivity.audioWidget.controller().duration(i);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        if (mCircularProgress != null) {
            mCircularProgress.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
                @Override
                public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        MusicPlayer.seek((long) progress);
                    }
                }

                @Override
                public void onStopTrackingTouch(CircularSeekBar seekBar) {

                }

                @Override
                public void onStartTrackingTouch(CircularSeekBar seekBar) {

                }
            });
        }
    }

    public void updateSongDetails() {
        //do not reload image if it was a play/pause change
        if (!duetoplaypause) {
            if (albumart != null) {
                ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(MusicPlayer.getCurrentAlbumId()).toString(), albumart,
                        new DisplayImageOptions.Builder().cacheInMemory(true)
                                .showImageOnFail(R.drawable.ic_dribble)
                                .build(), new SimpleImageLoadingListener() {

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                doAlbumArtStuff(loadedImage);
                                MainActivity.audioWidget.controller().albumCoverBitmap(getCroppedBitmap(loadedImage));
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                Bitmap failedBitmap = ImageLoader.getInstance().loadImageSync("drawable://" + R.drawable.ic_dribble);
                                doAlbumArtStuff(failedBitmap);
                                Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_dribble);
                                MainActivity.audioWidget.controller().albumCoverBitmap(getCroppedBitmap(largeIcon));
                            }

                        });
            }
        }
        duetoplaypause = false;

        if (mPlayPause != null)
            updatePlayPauseButton();

        if (playPauseFloating != null)
            updatePlayPauseFloatingButton();


        if (songtitle != null)
            songtitle.setText(MusicPlayer.getTrackName());

        if (songalbum != null)
            songalbum.setText(MusicPlayer.getAlbumName());

        if (songartist != null)
            songartist.setText(MusicPlayer.getArtistName());

        if (songduration != null && getActivity() != null)
            songduration.setText(TimberUtils.makeShortTimeString(getActivity(), MusicPlayer.duration() / 1000));

        if (mProgress != null) {
            mProgress.setMax((int) MusicPlayer.duration());
            if (mUpdateProgress != null) {
                mProgress.removeCallbacks(mUpdateProgress);
            }
            mProgress.postDelayed(mUpdateProgress, 10);
        }
        if (mCircularProgress != null) {
            mCircularProgress.setMax((int) MusicPlayer.duration());
            if (mUpdateCircularProgress != null) {
                mCircularProgress.removeCallbacks(mUpdateCircularProgress);
            }
            mCircularProgress.postDelayed(mUpdateCircularProgress, 10);
        }

        if (timelyView11 != null) {
            mElapsedTimeHandler = new Handler();
            mElapsedTimeHandler.postDelayed(mUpdateElapsedTime, 600);
        }
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    public void setQueueSongs() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //load queue songs in asynctask
        if (getActivity() != null)
            new loadQueueSongs().execute("");

    }

    public void updatePlayPauseButton() {
        if (MusicPlayer.isPlaying()) {
            if (!mPlayPause.isPlayed()) {
                mPlayPause.setPlayed(true);
                MainActivity.audioWidget.controller().start();
                mPlayPause.startAnimation();
            }
        } else {
            if (mPlayPause.isPlayed()) {
                mPlayPause.setPlayed(false);
                MainActivity.audioWidget.controller().stop();
                mPlayPause.startAnimation();
            }
        }
    }

    public void updatePlayPauseFloatingButton() {
        if (MusicPlayer.isPlaying()) {
            playPauseDrawable.transformToPause(false);
            MainActivity.audioWidget.controller().start();
        } else {
            playPauseDrawable.transformToPlay(false);
            MainActivity.audioWidget.controller().stop();
        }
    }

    public void notifyPlayingDrawableChange() {
        int position = MusicPlayer.getQueuePosition();
        BaseQueueAdapter.currentlyPlayingPosition = position;
    }

    public void restartLoader() {

    }

    public void onPlaylistChanged() {

    }

    public void onMetaChanged() {
        updateSongDetails();

        if (recyclerView != null && recyclerView.getAdapter() != null)
            recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void setMusicStateListener() {
        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);
    }

    public void doAlbumArtStuff(Bitmap loadedImage) {

    }

    public void changeDigit(TimelyView tv, int end) {
        ObjectAnimator obja = tv.animate(end);
        obja.setDuration(400);
        obja.start();
    }

    public void changeDigit(TimelyView tv, int start, int end) {
        try {
            ObjectAnimator obja = tv.animate(start, end);
            obja.setDuration(400);
            obja.start();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        }
    }

    public void tv11(int a) {
        if (a != timeArr[0]) {
            changeDigit(timelyView11, timeArr[0], a);
            timeArr[0] = a;
        }
    }

    public void tv12(int a) {
        if (a != timeArr[1]) {
            changeDigit(timelyView12, timeArr[1], a);
            timeArr[1] = a;
        }
    }

    public void tv13(int a) {
        if (a != timeArr[2]) {
            changeDigit(timelyView13, timeArr[2], a);
            timeArr[2] = a;
        }
    }

    public void tv14(int a) {
        if (a != timeArr[3]) {
            changeDigit(timelyView14, timeArr[3], a);
            timeArr[3] = a;
        }
    }

    public void tv15(int a) {
        if (a != timeArr[4]) {
            changeDigit(timelyView15, timeArr[4], a);
            timeArr[4] = a;
        }
    }

    private class loadQueueSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (getActivity() != null) {
                mAdapter = new BaseQueueAdapter((AppCompatActivity) getActivity(), QueueLoader.getQueueSongs(getActivity()));
                return "Executed";
            } else return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                recyclerView.setAdapter(mAdapter);
                if (getActivity() != null)
                    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
                recyclerView.scrollToPosition(MusicPlayer.getQueuePosition() - 1);
            }

        }

        @Override
        protected void onPreExecute() {
        }
    }
}
