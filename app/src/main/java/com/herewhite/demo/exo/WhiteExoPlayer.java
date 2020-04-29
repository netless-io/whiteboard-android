package com.herewhite.demo.exo;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.herewhite.demo.R;
import com.herewhite.sdk.combinePlayer.NativePlayer;
import com.herewhite.sdk.combinePlayer.PlayerSyncManager;

import java.util.concurrent.TimeUnit;

public class WhiteExoPlayer implements NativePlayer, Player.EventListener {
    private static final String TAG = "WhiteExoPlayer";

    private Context mContext;
    private SimpleExoPlayer mExoPlayer;
    private MediaSource mMediaSource;
    private PlayerView mPlayerView;
    private DataSource.Factory mDataSourceFactory;

    private Handler mHandler;

    private PlayerSyncManager mPlayerSyncManager;
    private NativePlayerPhase mPlayerPhase = NativePlayerPhase.Idle;

    private int mCurrentState = Player.STATE_IDLE;

    public WhiteExoPlayer(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mExoPlayer = new SimpleExoPlayer.Builder(mContext.getApplicationContext()).build();
        mExoPlayer.addListener(this);
        mExoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true);
        mExoPlayer.setPlayWhenReady(false);
        mDataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)));
    }

    /**
     * 绑定 playerSyncManager，设置的同时，需要将当前实例的 NativePlayerPhase 也更新
     * @param player PlayerSyncManager 实例
     */
    public void setPlayerSyncManager(PlayerSyncManager player) {
        Log.d(TAG, "setPlayerSyncManager: " + mPlayerPhase);
        mPlayerSyncManager = player;
        mPlayerSyncManager.updateNativePhase(mPlayerPhase);
    }

    /**
     * 设置播放视图
     *
     * @param playerView 视图实例
     */
    public void setPlayerView(@NonNull PlayerView playerView) {
        mPlayerView = playerView;
        mPlayerView.requestFocus();
        mPlayerView.setPlayer(mExoPlayer);
    }

    /**
     * 设置播放链接
     *
     * @param path 播放链接
     */
    public void setVideoPath(String path) {
        if (path == null) {
            Log.e(TAG, "Play path is null !!!");
            return;
        }
        setVideoURI(Uri.parse(path));
    }

    /**
     * 设置播放 Uri
     *
     * @param uri 播放链接对应的 Uri
     */
    public void setVideoURI(Uri uri) {
        if (uri == null) {
            Log.e(TAG, "Play uri is null !!!");
            return;
        }
        mMediaSource = createMediaSource(uri);
        mPlayerPhase = NativePlayerPhase.Buffering;
        mExoPlayer.prepare(mMediaSource);
    }

    /**
     * 由 nativePlayer 进行主动 seek，然后在 seek 完成后，再调用 {@link PlayerSyncManager} 同步
     * @param time 跳转时间戳
     * @param unit 时间戳单位
     */
    public void seek(long time, TimeUnit unit) {
        long timestampMs = TimeUnit.MILLISECONDS.convert(time, unit);
        mExoPlayer.seekTo(timestampMs);
    }

    /**
     * 获取当前是否正在播放
     *
     * @return true or false
     */
    public boolean isPlaying() {
        if (mExoPlayer == null) {
            return false;
        }
        return mExoPlayer.isPlaying();
    }

    /**
     * 在当播放器对用户可见以及 {@code surface_type} 是 {@code spherical_gl_surface_view} 时，应调用此方法。
     * 此方法与 {@link #onPause()} 对应
     *
     * 通常应在 {@code Activity.onStart()}, 或者 API level <= 23 时的 {@code Activity.onResume()} 中调用此方法
     */
    public void onResume() {
        mPlayerView.onResume();
    }

    /**
     * 在当播放器对用户不可见以及 {@code surface_type} 为 {@code spherical_gl_surface_view} 时，应调用此方法
     * 此方法与 {@link #onResume()} 对应
     *
     * 通常应在 {@code Activity.onStop()}, 或者 API level <= 23 时的 {@code Activity.onPause()} 中调用此方法
     */
    public void onPause() {
        mPlayerView.onPause();
    }

    /**
     * 释放播放器资源
     */
    public void release() {
        if (mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer = null;
            mMediaSource = null;
        }
    }

    /**
     * 获取当前播放时间
     *
     * @return 当前播放时间，单位：ms
     */
    public long getCurrentPosition() {
        return mExoPlayer.getCurrentPosition();
    }

    /**
     * 获取视频总时长
     *
     * @return 视频总时长，单位：ms
     */
    public long getDuration() {
        return mExoPlayer.getDuration();
    }

    @Override
    public void play() {
        mHandler.post(() -> {
            if (isPlaying()) {
                return;
            }
            mExoPlayer.setPlayWhenReady(true);
        });
    }

    @Override
    public void pause() {
        mHandler.post(() -> {
            if (!isPlaying()) {
                return;
            }
            mExoPlayer.setPlayWhenReady(false);
        });
    }

    @Override
    public boolean hasEnoughBuffer() {
        if (mCurrentState == Player.STATE_IDLE) {
            return false;
        }
        return mPlayerPhase != NativePlayerPhase.Buffering;
    }

    @Override
    public NativePlayerPhase getPhase() {
        return mPlayerPhase;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        mCurrentState = playbackState;
        switch (playbackState){
            case Player.STATE_IDLE:
                // 空闲
                break;
            case Player.STATE_BUFFERING:
                // 缓冲中触发，缓冲完会触发 STATE_READY 状态
                mPlayerPhase = NativePlayerPhase.Buffering;
                if (mPlayerSyncManager != null) {
                    mPlayerSyncManager.updateNativePhase(mPlayerPhase);
                }
                break;
            case Player.STATE_READY:
                // 准备好并可以立即播放时触发
                mPlayerPhase = isPlaying() ? NativePlayerPhase.Playing : NativePlayerPhase.Pause;
                if (mPlayerSyncManager != null) {
                    mPlayerSyncManager.updateNativePhase(mPlayerPhase);
                }
                break;
            case Player.STATE_ENDED:
                // 结束播放时触发
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.e(TAG, "onError: " + error.getMessage());
        switch (error.type){
            case ExoPlaybackException.TYPE_SOURCE:
                // 加载资源时出错
                break;
            case ExoPlaybackException.TYPE_RENDERER:
                // 渲染时出错
                break;
            case ExoPlaybackException.TYPE_UNEXPECTED:
                // 意外的错误
                break;
        }
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        // 播放状态改变时会执行此回调
        mPlayerPhase = isPlaying ? NativePlayerPhase.Playing : NativePlayerPhase.Pause;
    }

    @Override
    public void onSeekProcessed() {
        if (mPlayerSyncManager != null) {
            long pos = mExoPlayer.getCurrentPosition();
            mPlayerSyncManager.seek(pos, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 创建播放源
     * 当前仅支持 Hls 以及 mp4 等常规媒体文件，其他格式请参考 {@link "https://exoplayer.dev/media-sources.html"}
     *
     * @param uri 播放地址
     * @return MediaSource 对象
     */
    private MediaSource createMediaSource(Uri uri) {
        @C.ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mDataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(mDataSourceFactory)
                        .createMediaSource(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }
}
