package com.websarva.wings.android.bestflightshot;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SelectListActivity extends AppCompatActivity {
//インテントで受け取った空港名を格納する変数
private String airport = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_list);

        setTitle("Tap Way");

        MenuInfoReceiver receiver = new MenuInfoReceiver();
        receiver.execute();
    }

    private class MenuInfoReceiver extends AsyncTask<Void,String,String> {

        private ProgressBar progressBar;

        @Override
        public String doInBackground(Void... params) {

            progressBar = (ProgressBar)findViewById(R.id.selectProgressBar);
            this.progressBar.setVisibility(View.VISIBLE);

            Intent intent = getIntent();
            airport = intent.getStringExtra("airport");

            String urlStr = "https://api-tokyochallenge.odpt.org/api/v4/odpt:FlightInformationDeparture?acl:consumerKey=2af0930edd9f426efa146aa64e7d90d9b41b4fb84b9bef1e1040dce7e6fed3cf&odpt:departureAirport=odpt.Airport:" + airport;
            String result = "";

            HttpURLConnection con = null;
            InputStream is = null;
            try {
                URL url = new URL(urlStr);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();
                is = con.getInputStream();
                result = is2String(is);
            }catch (MalformedURLException ex) {
                ex.printStackTrace();
            }catch (IOException ex) {
                ex.printStackTrace();
            }finally {
                if(con != null) {
                    con.disconnect();
                }
                if(is != null) {
                    try {
                        is.close();
                    }catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return result;
        }
        public void onPostExecute(String result) {

            if(this.progressBar != null) {
                this.progressBar.setVisibility(View.GONE);
            }

            List<String> typeList = new ArrayList<>();
            List<String> timeList = new ArrayList<>();
            //odpt:flightInformationTextの内容を格納する配列
            List<String> infoList = new ArrayList<>();

            List<String> sortedTypeList = new ArrayList<>();
            List<String> sortedTimeList = new ArrayList<>();
            List<String> sortedInfoList = new ArrayList<>();

            try {
                //Jsonデータを文字列配列にパース
                JSONArray jArray = new JSONArray(result);
                for(int i = 0; i < jArray.length(); i++) {
                    JSONObject rootJson = jArray.getJSONObject(i);
                    //データの取得例外処理（timeListとtypeList）
                    if(rootJson.has("odpt:scheduledDepartureTime")) {
                        timeList.add(i, rootJson.getString("odpt:scheduledDepartureTime"));
                    } else if(rootJson.has("JSONObject.NULL")){
                        timeList.add(i,rootJson.getString("odpt:scheduledTime"));
                    } else {
                        timeList.add(i,"null");
                    }
                    if(rootJson.has("odpt:aircraftType")) {
                        typeList.add(i,rootJson.getString("odpt:aircraftType"));
                    } else {
                        typeList.add(i,"null");
                    }
                    if(rootJson.has("odpt:flightInformationText")) {
                        JSONObject infoJson = rootJson.getJSONObject("odpt:flightInformationText");
                        infoList.add(i,infoJson.getString("ja"));
                    }else {
                        infoList.add(i,"特記事項無し");
                    }
                }
            }catch (JSONException ex) {
                ex.printStackTrace();
            }
            InfoDatabaseHelper helper = new InfoDatabaseHelper(SelectListActivity.this);
            SQLiteDatabase db = helper.getWritableDatabase();

            try {
                //一旦データベースを削除して、新たにtypeListとtimeListの要素をDBにインサート。
                String sqlDelete = "DELETE FROM infodata";
                SQLiteStatement stmt = db.compileStatement(sqlDelete);
                stmt.executeUpdateDelete();

                String sqlInsert = "INSERT INTO infodata (id, type, time, info) VALUES (?,?,?,?)";

                for (int i = 0; i < typeList.size(); i++) {
                    stmt = db.compileStatement(sqlInsert);
                    stmt.bindLong(1, i);
                    stmt.bindString(2, typeList.get(i));
                    stmt.bindString(3, timeList.get(i));
                    stmt.bindString(4, infoList.get(i));

                    stmt.executeInsert();
                }
                //現在時刻から直近10件のフライト情報を取り出す。それをsortedTypeListとsortedTimeListとsortedInfoListに格納。
                String sql = "SELECT * FROM infodata WHERE time(time) >= time('now','localtime') AND info LIKE '%で運航%' OR info LIKE '%よる運航%' ORDER BY time(time) LIMIT 7";
                Cursor cursor = db.rawQuery(sql, null);
                while (cursor.moveToNext()) {

                    int idxType = cursor.getColumnIndex("type");
                    int idxTime = cursor.getColumnIndex("time");
                    int idxInfo = cursor.getColumnIndex("info");

                    sortedTypeList.add(cursor.getString(idxType));
                    sortedTimeList.add(cursor.getString(idxTime));
                    sortedInfoList.add(cursor.getString(idxInfo));
                }
                //sortedInfoListがnullか要素が0の時、スペシャルボタンを利用不可にする。
                if (sortedInfoList != null && sortedInfoList.size() != 0) {
                    Button btSpecial = findViewById(R.id.btSpecialFlight);
                    btSpecial.setEnabled(true);
                    btSpecial.setAlpha(1.0f);
                    btSpecial.setBackgroundResource(R.drawable.frame_style);
                }
                }finally {
                db.close();
            }
            }
    }
    //バイナリデータを文字列に変換するメソッド
    private String is2String(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuffer sb = new StringBuffer();
        char[] b = new char[1024];
        int line;
        while(0 <= (line = reader.read(b))) {
            sb.append(b,0,line);
        }
        return sb.toString();
    }
    //ここに3つのボタンを押した時、次の画面にインテントを渡すメソッドを３つ実装する。
   public void onRecentFlightButton(View view) {

       Intent intent=new Intent(SelectListActivity.this,RecentFlightActivity.class);
       intent.putExtra("airport",airport);
       startActivity(intent);
   }

    public void onConditionSelectButton(View view) {

       Intent intent = new Intent(SelectListActivity.this,ConditionSelectActivity.class);
       intent.putExtra("airport",airport);
       startActivity(intent);
    }

   public void onSpecialFlightButton(View view) {

        Intent intent = new Intent(SelectListActivity.this, SpecialFlightActivity.class);
        intent.putExtra("airport",airport);
        startActivity(intent);
    }
}

