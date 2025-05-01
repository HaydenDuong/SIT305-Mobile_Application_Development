package com.example.itubeapp.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import com.example.itubeapp.R;

public class PlayVideoFragment extends Fragment {

    private WebView youtubeWebView;
    private String videoUrl;

    public PlayVideoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_video, container, false);

        youtubeWebView = view.findViewById(R.id.youtubeWebView);

        // Lấy videoUrl từ arguments
        if (getArguments() != null) {
            videoUrl = getArguments().getString("videoUrl");
        }

        // Cấu hình WebView
        WebSettings webSettings = youtubeWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Bật JavaScript
        webSettings.setDomStorageEnabled(true); // Cần cho IFrame API
        youtubeWebView.setWebViewClient(new android.webkit.WebViewClient());

        // Trích xuất videoId từ URL
        String videoId = extractVideoId(videoUrl);
        if (videoId == null) {
            Toast.makeText(getContext(), "Invalid video URL", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Tạo HTML với IFrame Player
        String html = "<!DOCTYPE html>" +
                "<html>" +
                "<body>" +
                "<div id=\"player\"></div>" +
                "<script>" +
                "var tag = document.createElement('script');" +
                "tag.src = \"https://www.youtube.com/iframe_api\";" +
                "var firstScriptTag = document.getElementsByTagName('script')[0];" +
                "firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);" +
                "var player;" +
                "function onYouTubeIframeAPIReady() {" +
                "  player = new YT.Player('player', {" +
                "    height: '100%'," +
                "    width: '100%'," +
                "    videoId: '" + videoId + "'," +
                "    playerVars: {" +
                "      'playsinline': 1," +
                "      'autoplay': 0," +
                "      'controls': 1" +
                "    }," +
                "    events: {" +
                "      'onReady': onPlayerReady," +
                "      'onStateChange': onPlayerStateChange," +
                "      'onError': onPlayerError" +
                "    }" +
                "  });" +
                "}" +
                "function onPlayerReady(event) {" +
                "  event.target.playVideo();" +
                "}" +
                "function onPlayerStateChange(event) {" +
                "}" +
                "function onPlayerError(event) {" +
                "  alert('Error playing video: ' + event.data);" +
                "}" +
                "</script>" +
                "</body>" +
                "</html>";

        // Tải HTML vào WebView
        youtubeWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

        return view;
    }

    private String extractVideoId(String videoUrl) {
        String videoId = null;
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(videoUrl);
        if (matcher.find()) {
            videoId = matcher.group();
        }
        return videoId;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        youtubeWebView.destroy();
    }
}