package com.herewhite.demo;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.herewhite.sdk.CombinePlayer.PlayerSyncManager;
import com.herewhite.sdk.CombinePlayer.NativePlayer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * NativePlayer 实现类
 * MediaPlayer 部分调用，参考自 https://github.com/android/media-samples/tree/master/MediaRouter
 */
public class NativePlayerImplement implements NativePlayer, SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener
{
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder mSurfaceHolder;
    private final Handler mHandler = new Handler();
    private PlayerSyncManager playerSyncManager;
    /**
     * NativePlayerPhase 状态，与 mediaPlayer State 并不一致，需要进行一些转换
     * https://developer.android.google.cn/reference/android/media/MediaPlayer.html#state-diagram
     */
    private NativePlayerPhase phase = NativePlayerPhase.Idle;

    private static final int STATE_IDLE = 0;
    private static final int STATE_PLAY_PENDING = 1;
    private static final int STATE_READY = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    /**
     * mediaPlayer 没有提供可以状态 API，需要开发者手动进行维护
     */
    private int mState = STATE_IDLE;
    /**
     * 跳转位置
     */
    private int mSeekToPos = 0;

    final String PLAYER_INFO = "nativePlayer info";


    public NativePlayerImplement(Context context, String uri) throws IOException {
        mMediaPlayer = new MediaPlayer();

        Uri mp4 = Uri.parse(uri);
        mMediaPlayer.setDataSource(context, mp4);

        //注册必须的监听类。其他回调，可以由开发者自行根据业务情况添加
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnInfoListener(this);

        phase = NativePlayerPhase.Buffering;
        mMediaPlayer.prepareAsync();
    }

    /**
     * 绑定 playerSyncManager
     * @param player PlayerSyncManager 实例
     */
    public void setPlayerSyncManager(PlayerSyncManager player) {
        playerSyncManager = player;
        playerSyncManager.updateNativePhase(phase);
    }

    /**
     * player 的预期播放状态，为了播放而缓冲也算
     * @return
     */
    public boolean isPlaying() {
        if (mState == STATE_IDLE) {
            return false;
        } else if (mState == STATE_PLAY_PENDING) {
            return true;
        }
        return mMediaPlayer.isPlaying();
    }

    /**
     * 不会主动调用
     */
    @Override
    public void play() {

        Log.e(PLAYER_INFO, "play: " + mState);

        if (mState == STATE_READY || mState == STATE_PAUSED) {
            phase = NativePlayerPhase.Playing;
            mMediaPlayer.start();
            mState = STATE_PLAYING;
        //如果处于准备状态，无法立即调用 mediaPlayer 进行播放，此时 NativePlayerNative 应该处于缓冲状态
        } else if (mState == STATE_IDLE) {
            mState = STATE_PLAY_PENDING;
            phase = NativePlayerPhase.Buffering;
        }
        //将结果更新给 playerSyncManager
        playerSyncManager.updateNativePhase(phase);
    }

    /**
     * 不会主动调用
     */
    @Override
    public void pause() {
        Log.e(PLAYER_INFO, "pause: " + mState);
        if (mState == STATE_PLAYING) {
            mMediaPlayer.pause();
            phase = NativePlayerPhase.Pause;
            mState = STATE_PAUSED;
        }
        playerSyncManager.updateNativePhase(phase);
    }

    /**
     * 由 nativePlayer 进行主动 seek，然后在 seek 完成后，再调用 {@link PlayerSyncManager} 同步
     * @param time
     * @param unit
     */
    public void seek(long time, TimeUnit unit) {

        Long milliseconds = TimeUnit.MILLISECONDS.convert(time, unit);

        if (mState == STATE_PLAYING || mState == STATE_PAUSED || mState == STATE_READY) {
            mMediaPlayer.seekTo(milliseconds.intValue());
            mSeekToPos = milliseconds.intValue();
        } else if (mState == STATE_IDLE || mState == STATE_PLAY_PENDING) {
            mSeekToPos = milliseconds.intValue();
        }
    }


    @Override
    public boolean hasEnoughBuffer() {
        if (mState == STATE_IDLE || mState == STATE_PLAY_PENDING) {
            return false;
        } else if (phase == NativePlayerPhase.Buffering) {
            return false;
        } else {
            return true;
        }
    }

    //MediaPlayer Listeners
    @Override
    public void onPrepared(final MediaPlayer mp) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(PLAYER_INFO, "prepared");
                if (mState == STATE_IDLE) {
                    mState = STATE_READY;
                    phase = NativePlayerPhase.Pause;
                } else if (mState == STATE_PLAY_PENDING) {
                    mState = STATE_PLAYING;
                    if (mSeekToPos > 0) {
                        mp.seekTo(mSeekToPos);
                    }
                    phase = NativePlayerPhase.Playing;
                    mp.start();
                } else {
                    phase = NativePlayerPhase.Pause;
                }

                if (playerSyncManager != null) {
                    playerSyncManager.updateNativePhase(NativePlayerPhase.Pause);
                }
            }
        });
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.d(PLAYER_INFO, "onInfo: " + what);

        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                phase = NativePlayerPhase.Buffering;
                if (playerSyncManager != null) {
                    playerSyncManager.updateNativePhase(phase);
                }
                break;
            }
            case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                phase = mp.isPlaying() ? NativePlayerPhase.Playing : NativePlayerPhase.Pause;
                if (playerSyncManager != null) {
                    playerSyncManager.updateNativePhase(phase);
                }

                break;
            }
            default:
                break;
        }
        return false;
    }

    @Override
    public void onSeekComplete(final MediaPlayer mp) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(PLAYER_INFO, "seek complete");
                mSeekToPos = 0;
                long pos = mp.getCurrentPosition();
                playerSyncManager.seek(pos, TimeUnit.MILLISECONDS);
            }
        });
    }


    private void updateSurface() {
        Log.e(PLAYER_INFO, "updateSurface" + "mSurfaceHolder: " + mSurfaceHolder);
        if (mSurfaceHolder != null) {
            mMediaPlayer.setDisplay(mSurfaceHolder);
        } else {
            mMediaPlayer.setDisplay(null);
        }
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        surfaceView.getHolder().addCallback(this);
        setSurface(surfaceView.getHolder());
    }

    private void setSurface(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        updateSurface();
    }

    private void removeSurface(SurfaceHolder holder) {
        if (mSurfaceHolder == holder) {
            mSurfaceHolder = null;
            updateSurface();
        }
    }

    private void updateSize() {

    }

    // SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height) {
        setSurface(holder);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setSurface(holder);
        updateSize();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        removeSurface(holder);
    }

}
