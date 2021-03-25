package com.herewhite.demo.ijk;

import android.net.Uri;
import android.util.Log;

import com.herewhite.demo.ijk.widget.media.IMediaController;
import com.herewhite.demo.ijk.widget.media.IjkVideoView;
import com.herewhite.sdk.combinePlayer.NativePlayer;
import com.herewhite.sdk.combinePlayer.PlayerSyncManager;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import tv.danmaku.ijk.media.player.IMediaPlayer;

import static com.herewhite.demo.ijk.widget.media.IjkVideoView.STATE_ERROR;
import static com.herewhite.demo.ijk.widget.media.IjkVideoView.STATE_IDLE;
import static com.herewhite.demo.ijk.widget.media.IjkVideoView.STATE_PREPARING;

public class WhiteIjkPlayer implements NativePlayer,
        IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnSeekCompleteListener {
    private static final String TAG = "WhiteMediaPlayer";

    private IjkVideoView mVideoView;
    private PlayerSyncManager mPlayerSyncManager;
    private NativePlayerPhase mPlayerPhase = NativePlayerPhase.Idle;

    public WhiteIjkPlayer(@NonNull IjkVideoView videoView) {
        mVideoView = videoView;
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnSeekCompleteListener(this);
    }

    /**
     * 绑定 playerSyncManager，设置的同时，需要将当前实例的 NativePlayerPhase 也更新
     *
     * @param player PlayerSyncManager 实例
     */
    public void setPlayerSyncManager(PlayerSyncManager player) {
        Log.d(TAG, "setPlayerSyncManager: " + mPlayerPhase);
        mPlayerSyncManager = player;
        mPlayerSyncManager.updateNativePhase(mPlayerPhase);
    }

    /**
     * 设置播放链接
     *
     * @param path 播放链接
     */
    public void setVideoPath(String path) {
        mVideoView.setVideoPath(path);
    }

    /**
     * 设置播放 Uri
     *
     * @param uri 播放 Uri
     */
    public void setVideoURI(Uri uri) {
        mVideoView.setVideoURI(uri);
    }

    /**
     * 设置媒体控制器，可参考 {@link com.herewhite.demo.ijk.widget.media.AndroidMediaController}
     *
     * @param mediaController 媒体控制器实例
     */
    public void setMediaController(IMediaController mediaController) {
        mVideoView.setMediaController(mediaController);
    }

    /**
     * 获取当前播放位置
     *
     * @return 当前播放位置
     */
    public int getCurrentPosition() {
        return mVideoView.getCurrentPosition();
    }

    /**
     * 获取视频总时长
     *
     * @return 视频总时长
     */
    public int getDuration() {
        return mVideoView.getDuration();
    }

    /**
     * 跳到指定时间开始播放
     *
     * @param time 指定的时间
     * @param unit 指定时间的单位
     */
    public void seek(long time, TimeUnit unit) {
        // 在时间戳为 0 的场景下，会 seek 失败
        int milliseconds = (int) TimeUnit.MILLISECONDS.convert(time, unit) == 0 ? 1 : (int) TimeUnit.MILLISECONDS.convert(time, unit);
        mVideoView.seekTo(milliseconds);
    }

    /**
     * 切换画面填充方式
     */
    public void toggleAspectRatio() {
        mVideoView.toggleAspectRatio();
    }

    /**
     * 是否在正常的播放状态: PREPARED、PLAYING、PAUSED
     *
     * @return true or false
     */
    public boolean isNormalState() {
        int state = mVideoView.getCurrentState();
        return state != STATE_IDLE && state != STATE_PREPARING && state != STATE_ERROR;
    }

    /**
     * player 的预期播放状态，为了播放而缓冲也算
     *
     * @return 播放状态
     */
    public boolean isPlaying() {
        return (mVideoView.getCurrentState() == STATE_PREPARING) || mVideoView.isPlaying();
    }

    /**
     * 重新配置并启用播放器
     */
    public void resume() {
        mVideoView.resume();
    }

    /**
     * 释放播放器资源
     * <p>
     * release 后需要调用 resume 重新进行播放器资源的初始化
     */
    public void release() {
        mVideoView.stopPlayback();
    }

    @Override
    public void play() {
        if (mVideoView.getCurrentState() == STATE_IDLE) {
            mPlayerPhase = NativePlayerPhase.Buffering;
            //如果处于缓冲状态，需要更新 playerSyncManager
            mPlayerSyncManager.updateNativePhase(mPlayerPhase);
        }
        mVideoView.start();
    }

    @Override
    public void pause() {
        mVideoView.pause();
        mPlayerPhase = NativePlayerPhase.Pause;
    }

    @Override
    public boolean hasEnoughBuffer() {
        if (mVideoView.getCurrentState() == STATE_IDLE || mVideoView.getCurrentState() == STATE_PREPARING) {
            return false;
        } else {
            return mPlayerPhase != NativePlayerPhase.Buffering;
        }
    }

    @Override
    public NativePlayerPhase getPhase() {
        return mPlayerPhase;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
        if (framework_err == -10000) {
            resume();
            return true;
        }
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        switch (i) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                mPlayerPhase = NativePlayerPhase.Buffering;
                if (mPlayerSyncManager != null) {
                    mPlayerSyncManager.updateNativePhase(mPlayerPhase);
                }
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                mPlayerPhase = iMediaPlayer.isPlaying() ? NativePlayerPhase.Playing : NativePlayerPhase.Pause;
                if (mPlayerSyncManager != null) {
                    mPlayerSyncManager.updateNativePhase(mPlayerPhase);
                }
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        if (mPlayerSyncManager != null) {
            long pos = iMediaPlayer.getCurrentPosition();
            mPlayerSyncManager.seek(pos, TimeUnit.MILLISECONDS);
        }
    }
}
