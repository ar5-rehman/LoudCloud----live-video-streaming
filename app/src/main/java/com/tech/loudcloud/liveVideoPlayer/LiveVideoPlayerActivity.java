/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tech.loudcloud.liveVideoPlayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.BuildConfig;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import com.tech.loudcloud.R;

/**
 * An activity that plays media using {@link SimpleExoPlayer}.
 */
public class LiveVideoPlayerActivity extends AppCompatActivity implements OnClickListener, ExoPlayer.EventListener,
        PlaybackControlView.VisibilityListener {

  public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

  private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
  private static final CookieManager DEFAULT_COOKIE_MANAGER;
  static {
    DEFAULT_COOKIE_MANAGER = new CookieManager();
    DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
  }

  String urll = "http://3.236.184.191:5080/LiveApp/streams/";

  private CastContext mCastContext;
  private CastSession mCastSession;
  private SessionManagerListener<CastSession> mSessionManagerListener;
  private MenuItem mediaRouteMenuItem;

  private Handler mainHandler;
  private EventLogger eventLogger;
  private SimpleExoPlayerView simpleExoPlayerView;
  private Button retryButton;

  private DataSource.Factory mediaDataSourceFactory;
  private SimpleExoPlayer player;
  private DefaultTrackSelector trackSelector;
  private boolean needRetrySource;

  private boolean shouldAutoPlay;
  private int resumeWindow;
  private long resumePosition;
  private RtmpDataSource.RtmpDataSourceFactory rtmpDataSourceFactory;
  protected String userAgent;
  String streamName, artistId, userName, userImage;
  ImageView chatBtn;

  // Activity lifecycle

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
    shouldAutoPlay = true;
    clearResumePosition();
    mediaDataSourceFactory = buildDataSourceFactory(true);
    rtmpDataSourceFactory = new RtmpDataSource.RtmpDataSourceFactory();
    mainHandler = new Handler();
    if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
      CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
    }

    setContentView(R.layout.activity_live_video_player);

    Intent in = getIntent();
    streamName = in.getStringExtra("streamName");
    artistId = in.getStringExtra("artistId");
    userName = in.getStringExtra("userName");
    userImage = in.getStringExtra("userImage");

    View rootView = findViewById(R.id.root);
    rootView.setOnClickListener(this);

    setupCastListener();
    mCastContext = CastContext.getSharedInstance(this);
    mCastSession = mCastContext.getSessionManager().getCurrentCastSession();

       simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
    simpleExoPlayerView.setControllerVisibilityListener(this);
    simpleExoPlayerView.requestFocus();

    URL = urll+streamName+"_adaptive.m3u8";

    initializePlayer(URL);

    chatBtn = findViewById(R.id.chat_btn);
    chatBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        ChatRoom bottomSheet = new ChatRoom(LiveVideoPlayerActivity.this, artistId, userName, userImage);
        bottomSheet.show(getSupportFragmentManager(),
                "ChatRoom");
      }
    });

  }

  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    releasePlayer();
    shouldAutoPlay = true;
    clearResumePosition();
    setIntent(intent);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      releasePlayer();
    }
    mCastContext.getSessionManager().removeSessionManagerListener(
            mSessionManagerListener, CastSession.class);
  }

  @Override
  protected void onResume() {
    mCastContext.getSessionManager().addSessionManagerListener(
            mSessionManagerListener, CastSession.class);
    super.onResume();
  }

  @Override
  public void onStop() {
    super.onStop();
    if (Util.SDK_INT > 23) {
      releasePlayer();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                         int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      play(null);
    } else {
      showToast(R.string.storage_permission_denied);
      finish();
    }
  }

  // Activity input

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    // Show the controls on any key event.
    simpleExoPlayerView.showController();
    // If the event was not handled then see if the player view can handle it as a media key event.
    return super.dispatchKeyEvent(event) || simpleExoPlayerView.dispatchMediaKeyEvent(event);
  }

  // OnClickListener methods

  @Override
  public void onClick(View view) {
    if (view == retryButton) {
      play(null);
    }
  }

  // PlaybackControlView.VisibilityListener implementation

  @Override
  public void onVisibilityChange(int visibility) {

  }

  // Internal methods

  private void initializePlayer(String rtmpUrl) {
    Intent intent = getIntent();
    boolean needNewPlayer = player == null;
    if (needNewPlayer) {

      boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
      @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
              useExtensionRenderers()
                      ? (preferExtensionDecoders ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
                      : SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
                      : SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;
      TrackSelection.Factory videoTrackSelectionFactory =
              new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
      trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
      player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
              null, extensionRendererMode);
   //   player = ExoPlayerFactory.newSimpleInstance(this, trackSelector,
   //           new DefaultLoadControl(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),  500, 1500, 500, 1500),
   //           null, extensionRendererMode);
      player.addListener(this);

      eventLogger = new EventLogger(trackSelector);
      player.addListener(eventLogger);
      player.setAudioDebugListener(eventLogger);
      player.setVideoDebugListener(eventLogger);
      player.setMetadataOutput(eventLogger);

      simpleExoPlayerView.setPlayer(player);
      player.setPlayWhenReady(shouldAutoPlay);
     // debugViewHelper = new DebugTextViewHelper(player, debugTextView);
      //debugViewHelper.start();
    }
    if (needNewPlayer || needRetrySource) {
      //  String action = intent.getAction();
      Uri[] uris;
      String[] extensions;

      uris = new Uri[1];
      uris[0] = Uri.parse(rtmpUrl);
      extensions = new String[1];
      extensions[0] = "";
      if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
        // The player will be reinitialized if the permission is granted.
        return;
      }
      MediaSource[] mediaSources = new MediaSource[uris.length];
      for (int i = 0; i < uris.length; i++) {
        mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
      }
      MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
              : new ConcatenatingMediaSource(mediaSources);
      boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(resumeWindow, resumePosition);
      }
      player.prepare(mediaSource, !haveResumePosition, false);
      needRetrySource = false;
    }
  }

  private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
    int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
            : Util.inferContentType("." + overrideExtension);
    switch (type) {
      case C.TYPE_SS:
        return new SsMediaSource(uri, buildDataSourceFactory(false),
                new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
      case C.TYPE_DASH:
        return new DashMediaSource(uri, buildDataSourceFactory(false),
                new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
      case C.TYPE_HLS:
        return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
      case C.TYPE_OTHER:
        if (uri.getScheme().equals("rtmp")) {
          return new ExtractorMediaSource(uri, rtmpDataSourceFactory, new DefaultExtractorsFactoryForFLV(),
                  mainHandler, eventLogger);
        }
        else {
          return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                  mainHandler, eventLogger);
        }
      default: {
        throw new IllegalStateException("Unsupported type: " + type);
      }
    }
  }


  private void releasePlayer() {
    if (player != null) {

      shouldAutoPlay = player.getPlayWhenReady();
      updateResumePosition();
      player.release();
      player = null;
      trackSelector = null;
      //trackSelectionHelper = null;
      eventLogger = null;
    }
  }

  private void updateResumePosition() {
    resumeWindow = player.getCurrentWindowIndex();
    resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
            : C.TIME_UNSET;
  }

  private void clearResumePosition() {
    resumeWindow = C.INDEX_UNSET;
    resumePosition = C.TIME_UNSET;
  }

  /**
   * Returns a new DataSource factory.
   *
   * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
   *     DataSource factory.
   * @return A new DataSource factory.
   */
  private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
    return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
  }

  /**
   * Returns a new HttpDataSource factory.
   *
   * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
   *     DataSource factory.
   * @return A new HttpDataSource factory.
   */
  private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
    return buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
  }

  // ExoPlayer.EventListener implementation

  @Override
  public void onLoadingChanged(boolean isLoading) {
    // Do nothing.
  }

  @Override
  public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
    if (playbackState == ExoPlayer.STATE_ENDED) {

    }
  }

  @Override
  public void onPositionDiscontinuity() {
    if (needRetrySource) {
      // This will only occur if the user has performed a seek whilst in the error state. Update the
      // resume position so that if the user then retries, playback will resume from the position to
      // which they seeked.
      updateResumePosition();
    }
  }

  @Override
  public void onTimelineChanged(Timeline timeline, Object manifest) {
    // Do nothing.
  }

  @Override
  public void onPlayerError(ExoPlaybackException e) {

    String errorString = null;
    if (e.type == ExoPlaybackException.TYPE_RENDERER) {
      Exception cause = e.getRendererException();
      if (cause instanceof DecoderInitializationException) {
        // Special case for decoder initialization failures.
        DecoderInitializationException decoderInitializationException =
                (DecoderInitializationException) cause;
        if (decoderInitializationException.decoderName == null) {
          if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
            errorString = getString(R.string.error_querying_decoders);
          } else if (decoderInitializationException.secureDecoderRequired) {
            errorString = getString(R.string.error_no_secure_decoder,
                    decoderInitializationException.mimeType);
          } else {
            errorString = getString(R.string.error_no_decoder,
                    decoderInitializationException.mimeType);
          }
        } else {
          errorString = getString(R.string.error_instantiating_decoder,
                  decoderInitializationException.decoderName);
        }
      }
    }
    if (errorString != null) {
      showToast(errorString);
    }
    needRetrySource = true;
    if (isBehindLiveWindow(e)) {
      clearResumePosition();
      play(null);
    } else {
      updateResumePosition();

    }
  }

  @Override
  public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
    if (mappedTrackInfo != null) {
      if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
              == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
        showToast(R.string.error_unsupported_video);
      }
      if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
              == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
        showToast(R.string.error_unsupported_audio);
      }
    }
  }

  private void showToast(int messageId) {
    showToast(getString(messageId));
  }

  private void showToast(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
  }

  private static boolean isBehindLiveWindow(ExoPlaybackException e) {
    if (e.type != ExoPlaybackException.TYPE_SOURCE) {
      return false;
    }
    Throwable cause = e.getSourceException();
    while (cause != null) {
      if (cause instanceof BehindLiveWindowException) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }


  public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
    return new DefaultDataSourceFactory(this, bandwidthMeter,
            buildHttpDataSourceFactory(bandwidthMeter));
  }

  public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
    return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
  }

  public boolean useExtensionRenderers() {
    return BuildConfig.FLAVOR.equals("withExtensions");
  }

  String URL = null;

  public void play(View view) {
    URL = urll+streamName+"_adaptive.m3u8";
    initializePlayer(URL);
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.browse, menu);
    mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
    return true;
  }

  private void setupCastListener() {
    mSessionManagerListener = new SessionManagerListener<CastSession>() {

      @Override
      public void onSessionEnded(CastSession session, int error) {
        onApplicationDisconnected();
      }

      @Override
      public void onSessionResumed(CastSession session, boolean wasSuspended) {
        onApplicationConnected(session);
      }

      @Override
      public void onSessionResumeFailed(CastSession session, int error) {
        onApplicationDisconnected();
      }

      @Override
      public void onSessionStarted(CastSession session, String sessionId) {
        onApplicationConnected(session);
      }

      @Override
      public void onSessionStartFailed(CastSession session, int error) {
        onApplicationDisconnected();
      }

      @Override
      public void onSessionStarting(CastSession session) {}

      @Override
      public void onSessionEnding(CastSession session) {}

      @Override
      public void onSessionResuming(CastSession session, String sessionId) {}

      @Override
      public void onSessionSuspended(CastSession session, int reason) {}

      private void onApplicationConnected(CastSession castSession) {
        mCastSession = castSession;
        try {
          loadRemoteMedia(true);
          player.stop();
        }catch(Exception e){
          Toast.makeText(LiveVideoPlayerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //supportInvalidateOptionsMenu();
      }

      private void onApplicationDisconnected() {
        play(null);
        supportInvalidateOptionsMenu();
      }
    };
  }

  private void loadRemoteMedia(boolean autoPlay) {
    if (mCastSession == null) {
      return;
    }
    final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
    if (remoteMediaClient == null) {
      return;
    }
    remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
      @Override
      public void onStatusUpdated() {
        remoteMediaClient.unregisterCallback(this);
      }
    });

    remoteMediaClient.load(new MediaLoadRequestData.Builder()
            .setMediaInfo(buildMediaInfo())
            .setAutoplay(autoPlay)
            .setCurrentTime(player.getCurrentPosition())
            .build());
  }

  private MediaInfo buildMediaInfo() {
    return new MediaInfo.Builder(URL)
            .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
            .setContentType("application/x-mpegURL")
            .build();
  }
}
