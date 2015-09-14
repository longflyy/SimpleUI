package com.example.simpleui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MENU_ACTIVITY = 1;
    private static final int REQUEST_CODE_TAKE_PHOTO = 2;

    private EditText inputEditText;
    private Button sendButton;
    private CheckBox hideCheckBox;
    private ListView historyListView;
    private Spinner storeName;
    private ImageView photo;
    private TextView num;
    private ListView rubList;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private JSONObject menuInfo;
    private boolean hasPhoto = false;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // /data/data/com.example.simple/shared_prefs/settings.xml
        sp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sp.edit();

        initViewsInstance();
        initViewsValueAndListener();
    }

    private void initViewsInstance() {
        inputEditText = (EditText) findViewById(R.id.note_input_edit_text);
        sendButton = (Button) findViewById(R.id.send_order_button);
        historyListView = (ListView) findViewById(R.id.listView);
        hideCheckBox = (CheckBox) findViewById(R.id.hide_note_check_box);
        storeName = (Spinner) findViewById(R.id.store_name_spinner);
        photo = (ImageView) findViewById(R.id.imageView);
        num = (TextView) findViewById(R.id.textView5);
        rubList = (ListView) findViewById(R.id.listView2);
    }

    private void initViewsValueAndListener() {
        inputEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                String text = inputEditText.getText().toString();
                editor.putString("input", text);
                editor.commit();

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        send();
                    }
                    return true;
                }
                return false;
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });

        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.store_info);
                String address = textView.getText().toString().split(",")[1];
                goToOrderDetail(address);
            }
        });

        //TODO (homework2)
        hideCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("hide", isChecked);
                editor.commit();
                photo.setVisibility(View.INVISIBLE);
            }
        });



        inputEditText.setText(sp.getString("input", ""));
        hideCheckBox.setChecked(sp.getBoolean("hide", false));

        setStoreName(storeName);
        setHistoryData();
        sendSid();

    }



    private void setStoreName(final Spinner spinner) {

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("StoreInfo");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                List<String> data = new ArrayList<String>();
                for (ParseObject object : list) {
                    data.add(object.getString("name") + "," + object.getString("address"));
                }
                ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,
                        android.R.layout.simple_spinner_item, data);

                spinner.setAdapter(adapter);
            }
        });

    }

    //TODO (homework1)
    private String getDrinkCategory(JSONArray menu){
        try {
            for (int j = 0; j < menu.length(); j++) {
                JSONObject menuObj = menu.getJSONObject(j);
                String drinkName = menuObj.getString("drinkName");
                int s = menuObj.getInt("s");
                int m = menuObj.getInt("m");
                int l = menuObj.getInt("l");
                if(s>0 || m>0 || l>0){
                    return drinkName;
                }

            }

            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void sendSid() {

        final List<Map<String, String>> mapData = new ArrayList<>();

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Order");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                for (int i = 0; i < list.size(); i++) {
                    Map<String, String> item = new HashMap<>();

                    String note = num.getText().toString();


                    item.put("sid", note);
                    mapData.add(item);
                }

                String[] from = {"sid"};
                int[] to = {R.id.textView5};
                SimpleAdapter adapter = new SimpleAdapter(MainActivity.this,
                        mapData, R.layout.listview_item, from, to);

                rubList.setAdapter(adapter);
            }
        });
    }



    private int getDrinkSum(JSONArray menu) {

        try {
            int sum = 0;
            for (int j = 0; j < menu.length(); j++) {
                JSONObject menuObj = menu.getJSONObject(j);
                String drinkName = menuObj.getString("drinkName");
                int s = menuObj.getInt("s");
                int m = menuObj.getInt("m");
                int l = menuObj.getInt("l");
                sum += s + m + l;
            }
            return sum;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setHistoryData() {

        final List<Map<String, String>> mapData = new ArrayList<>();

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Order");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                for (int i = 0; i < list.size(); i++) {
                    Map<String, String> item = new HashMap<>();

                    ParseObject order = list.get(i);

                    String note = order.getString("note");
                    String storeInfo = order.getString("storeInfo");

                    JSONArray menuInfo = order.getJSONArray("menu");

                    item.put("note", note);
                    item.put("drink_category", getDrinkCategory(menuInfo));
                    item.put("drink_sum", "" + getDrinkSum(menuInfo));
                    item.put("store_info", storeInfo);
                    mapData.add(item);
                }

                String[] from = {"note", "drink_category", "drink_sum", "store_info"};
                int[] to = {R.id.note, R.id.drink_category, R.id.drink_sum, R.id.store_info};
                SimpleAdapter adapter = new SimpleAdapter(MainActivity.this,
                        mapData, R.layout.listview_item, from, to);

                historyListView.setAdapter(adapter);
            }
        });
    }

    /**
     * {
     * "note": "no ice, half sugar",
     * "menu": [
     * {"drinkName": "black tea", "s": 0, "m": 1, "l":1},
     * {"drinkName": "milk tea", "s": 0, "m": 1, "l":1},
     * {"drinkName": "green tea", "s": 0, "m": 1, "l":1}
     * ]
     * }
     */
    private void send() {

        JSONObject order = new JSONObject();

        String text = inputEditText.getText().toString();
        String storeInfo = (String) storeName.getSelectedItem();

        try {
            order.put("note", text);
            if (menuInfo != null) {
                order.put("menu", menuInfo.getJSONArray("result"));
            }


            ParseObject orderObject = new ParseObject("Order");
            orderObject.put("note", order.getString("note"));
            orderObject.put("menu", order.getJSONArray("menu"));
            orderObject.put("storeInfo", storeInfo);

            orderObject.pinInBackground();

            if (hasPhoto) {
                ParseFile file = new ParseFile("photo.png",
                        Utils.uriToBytes(this, Utils.getOutputUri()));
                orderObject.put("photo", file);
            }

            orderObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.d("debug", "done");
                    setHistoryData();
                }
            });

            Log.d("debug", "after saveInBackground");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Utils.writeFile(this, "history.txt", order.toString() + "\n");
        Toast.makeText(this, order.toString(), Toast.LENGTH_SHORT).show();
        inputEditText.setText("");
    }

    public void goToMenu(View view) {

        String storeInfo = (String) storeName.getSelectedItem();

        Intent intent = new Intent();
        intent.setClass(this, MenuActivity.class);
        intent.putExtra("storeInfo", storeInfo);
        startActivityForResult(intent, REQUEST_CODE_MENU_ACTIVITY);

    }

    private void goToOrderDetail(String address) {
        Intent intent = new Intent();
        intent.setClass(this, OrderDetailActivity.class);
        intent.putExtra("address", address);
        startActivity(intent);
    }

    private void goToCamera() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getOutputUri());
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_MENU_ACTIVITY && resultCode == RESULT_OK) {
            try {
                menuInfo = new JSONObject(data.getStringExtra("data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {
//            bitmap = data.getParcelableExtra("data");
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(Utils.getOutputUri());
            hasPhoto = true;
        }
    }

    public void send2(View view) {
        send();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_take_photo) {
            goToCamera();
        }

        return super.onOptionsItemSelected(item);
    }
}
