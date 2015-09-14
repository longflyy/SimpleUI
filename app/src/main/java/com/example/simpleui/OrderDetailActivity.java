package com.example.simpleui;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class OrderDetailActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressDialog progressDialog;
    private ImageView imageView;

    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        webView = (WebView) findViewById(R.id.static_map);
        imageView = (ImageView) findViewById(R.id.static_map_2);
        progressDialog = new ProgressDialog(this);

        String address = getIntent().getStringExtra("address");

        asyncTask.execute(address);

        googleMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map))
                .getMap();

    }

    //TODO (homework3)
    public void loadWebView(View view) {

    }

    //TODO (homework3)
    public void loadImageView(View view) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_order_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    AsyncTask<String, Integer, String> asyncTask = new AsyncTask<String, Integer, String>() {

        @Override
        protected String doInBackground(String... params) {
            String address = params[0];

            String out = null;
            try {
                out = Utils.fetch("https://maps.googleapis.com/maps/api/geocode/json?address=" +
                        URLEncoder.encode(address, "utf-8"));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.d("debug", "fetch: " + out);
            return out;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            try {
                JSONObject object = new JSONObject(jsonString);
                JSONObject location = object.getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location");

                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 14));
                googleMap.addMarker(new MarkerOptions()
                        .title("Here")
                        .snippet(getIntent().getStringExtra("address"))
                        .position(new LatLng(lat, lng)));


                String staticMapUrl = String.format(
                        "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=15&size=600x600&markers=%f,%f",
                        lat, lng, lat, lng);

                webView.loadUrl(staticMapUrl);

                ImageLoader imageLoader = new ImageLoader();
                imageLoader.execute(staticMapUrl);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    class ImageLoader extends AsyncTask<String, Integer, byte[]> {

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("ImageLoader");
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(0);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        @Override
        protected byte[] doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                URLConnection urlConnection = url.openConnection();
                InputStream is = urlConnection.getInputStream();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int len;

                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);

                }

                return baos.toByteArray();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bitmap);

            progressDialog.setProgress(100);
            progressDialog.dismiss();
        }
    }
}
//https://maps.googleapis.com/maps/api/staticmap?center=25.041171,121.565227&zoom=15&size=600x600