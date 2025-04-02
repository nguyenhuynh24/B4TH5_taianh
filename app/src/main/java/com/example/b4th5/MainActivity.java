package com.example.b4th5;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private EditText urlEditText;
    private Button downloadButton;
    private ProgressBar progressBar;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlEditText = findViewById(R.id.urlEditText);
        downloadButton = findViewById(R.id.downloadButton);
        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.imageView);

        downloadButton.setOnClickListener(v -> {
            String url = urlEditText.getText().toString().trim();
            if (!url.isEmpty()) {
                new DownloadImageTask().execute(url);
            } else {
                Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // AsyncTask để tải ảnh từ URL
    private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
        @Override
        protected void onPreExecute() {
            // Hiển thị ProgressBar trước khi bắt đầu tải
            progressBar.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(null); // Xóa ảnh cũ nếu có
            downloadButton.setEnabled(false);
        }
        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap bitmap = null;
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                // Lấy kích thước file để tính tiến trình (nếu server hỗ trợ)
                int fileLength = connection.getContentLength();
                inputStream = connection.getInputStream();
                // Tải dữ liệu từng phần và cập nhật tiến trình
                byte[] data = new byte[4096];
                int total = 0;
                int count;
                java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                    // Cập nhật tiến trình nếu biết kích thước file
                    if (fileLength > 0) {
                        int progress = (int) ((total * 100) / fileLength);
                        publishProgress(progress);
                    }
                }
                byte[] imageData = output.toByteArray();
                bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                    if (connection != null) connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Cập nhật ProgressBar với phần trăm tải
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Ẩn ProgressBar và hiển thị ảnh
            progressBar.setVisibility(View.GONE);
            downloadButton.setEnabled(true);

            if (result != null) {
                imageView.setImageBitmap(result);
                Toast.makeText(MainActivity.this, "Image downloaded", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed to download image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}