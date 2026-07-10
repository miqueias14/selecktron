package com.selecktron.orcamento;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidApp");
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }

        super.onBackPressed();
    }

    public class AndroidBridge {
        @JavascriptInterface
        public void savePdf(String dataUri, String requestedFileName) {
            runOnUiThread(() -> {
                try {
                    byte[] pdfBytes = decodePdf(dataUri);
                    String fileName = sanitizeFileName(requestedFileName);
                    Uri savedUri = savePdfToDownloads(fileName, pdfBytes);
                    String message = savedUri != null
                            ? "PDF salvo em Downloads."
                            : "PDF salvo no armazenamento do aplicativo.";
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                } catch (Exception error) {
                    Toast.makeText(MainActivity.this, "Nao foi possivel salvar o PDF.", Toast.LENGTH_LONG).show();
                }
            });
        }

        private byte[] decodePdf(String dataUri) {
            String base64 = dataUri;
            int commaIndex = dataUri.indexOf(',');
            if (commaIndex >= 0) {
                base64 = dataUri.substring(commaIndex + 1);
            }
            return Base64.decode(base64, Base64.DEFAULT);
        }

        private Uri savePdfToDownloads(String fileName, byte[] bytes) throws Exception {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new IllegalStateException("Download destination unavailable");

                try (OutputStream output = getContentResolver().openOutputStream(uri)) {
                    if (output == null) throw new IllegalStateException("Output stream unavailable");
                    output.write(bytes);
                }
                return uri;
            }

            File downloads = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (downloads == null) throw new IllegalStateException("Download folder unavailable");
            if (!downloads.exists() && !downloads.mkdirs()) {
                throw new IllegalStateException("Could not create download folder");
            }

            File outputFile = new File(downloads, fileName);
            try (FileOutputStream output = new FileOutputStream(outputFile)) {
                output.write(bytes);
            }
            return null;
        }

        private String sanitizeFileName(String requestedFileName) {
            String name = requestedFileName == null ? "" : requestedFileName.trim();
            if (name.isEmpty()) {
                name = "orcamento-selecktron.pdf";
            }

            name = name.replaceAll("[\\\\/:*?\"<>|]+", "-");
            if (!name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                name += ".pdf";
            }
            return name;
        }
    }
}
