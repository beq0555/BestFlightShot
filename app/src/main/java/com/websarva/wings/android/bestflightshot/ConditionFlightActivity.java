package com.websarva.wings.android.bestflightshot;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

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

public class ConditionFlightActivity extends AppCompatActivity {

    private String minTime;
    private String maxTime;
    private String aircraftType;
    private String airline;
    private String airport;
    private ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_condition_flight);
        setTitle("撮影する航空機を選択してください");
    }
    private class ConditionFlightReceiver extends AsyncTask<Void,String,String> {

        @Override
        public String doInBackground(Void... params) {
            Intent intent = getIntent();
            airport = intent.getStringExtra("airport");

            aircraftType = intent.getStringExtra("aircraftType");
            if(aircraftType != null) {
                aircraftType = "&odpt:aircraftType=" + aircraftType;
            }else {
                aircraftType = "";
            }

            airline = intent.getStringExtra("airline");
            if(airline != null) {
                airline = "&odpt:airline=" + airline;
            }else {
                airline = "";
            }
            String urlStr = "https://api-tokyochallenge.odpt.org/api/v4/odpt:FlightInformationDeparture?acl:consumerKey=2af0930edd9f426efa146aa64e7d90d9b41b4fb84b9bef1e1040dce7e6fed3cf&odpt:departureAirport=odpt.Airport:"+airport + aircraftType + airline;
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

            Intent intent = getIntent();
            minTime = intent.getStringExtra("minTime");
            maxTime = intent.getStringExtra("maxTime");
            airport = intent.getStringExtra("airport");

            //jsonデータを文字列配列にパースした後に、機種データと離陸時間データを格納する配列。
            List<String> typeList = new ArrayList<>();
            List<String> timeList = new ArrayList<>();
            List<String> airlineList = new ArrayList<>();

            //DBに格納後、SQLでクエリを行った機種と時間データを格納する配列。
            List<String> sortedTypeList = new ArrayList<>();
            List<String> sortedTimeList = new ArrayList<>();
            List<String> sortedAirlineList = new ArrayList<>();


            try {
                //Jsonデータを文字列配列にパース
                JSONArray jArray = new JSONArray(result);
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject rootJson = jArray.getJSONObject(i);
                    //データの取得例外処理（timeListとtypeList）
                    if (rootJson.has("odpt:scheduledDepartureTime")) {
                        timeList.add(i, rootJson.getString("odpt:scheduledDepartureTime"));
                    } else if (rootJson.has("JSONObject.NULL")) {
                        timeList.add(i, rootJson.getString("odpt:scheduledTime"));
                    } else {
                        timeList.add(i, "を取得できませんでした。");
                    }
                    if (rootJson.has("odpt:aircraftType")) {
                        typeList.add(i, rootJson.getString("odpt:aircraftType"));
                    } else {
                        typeList.add(i, "シークレット");
                    }
                    if(rootJson.has("odpt:airline")) {
                        airlineList.add(i,airline);
                    } else {
                        airlineList.add(i,"航空会社情報を取得できませんでした。");
                    }
                }
            } catch (JSONException ex) {
                ex.printStackTrace();

            }

            ConditionDatabaseHelper helper = new ConditionDatabaseHelper(ConditionFlightActivity.this);
            SQLiteDatabase db = helper.getWritableDatabase();

            try {
                //一旦データベースを削除して、新たにtypeListとtimeListの要素をDBにインサート。
                String sqlDelete = "DELETE FROM conditiondata";
                SQLiteStatement stmt = db.compileStatement(sqlDelete);
                stmt.executeUpdateDelete();

                String sqlInsert = "INSERT INTO conditiondata (id, type, time, airline) VALUES (?,?,?,?)";

                for (int i = 0; i < typeList.size(); i++) {
                    stmt = db.compileStatement(sqlInsert);
                    stmt.bindLong(1, i);
                    stmt.bindString(2, typeList.get(i));
                    stmt.bindString(3, timeList.get(i));
                    stmt.bindString(4,airlineList.get(i));

                    stmt.executeInsert();
                }
                //minTimeとmaxTimeの間の離陸情報を取得
                String sql = "SELECT * FROM conditindata WHERE time(time) >= time(minTime) AND time(time) <= time(maxTime) ORDER BY time(time) LIMIT 10";
                Cursor cursor = db.rawQuery(sql, null);
                while (cursor.moveToNext()) {

                    int idxType = cursor.getColumnIndex("type");
                    int idxTime = cursor.getColumnIndex("time");
                    int idxAirline = cursor.getColumnIndex("airline");

                    sortedTypeList.add(cursor.getString(idxType));
                    sortedTimeList.add(cursor.getString(idxTime));
                    sortedAirlineList.add(cursor.getString(idxAirline));
                }


            } finally {
                db.close();
            }
            //リストビューに機種名・離陸時間・機種画像を表示。
            List<ConditionListItem> list = new ArrayList<>();
            for (int i = 0; i < sortedTypeList.size(); i++) {
                ConditionListItem item = new ConditionListItem();

                item.setCraftType("<機種>" + sortedTypeList.get(i));
                item.setDepartureTime("<離陸時間>" + sortedTimeList.get(i));
                item.setAirline("<航空会社>" + sortedAirlineList.get(i));
                switch (sortedTypeList.get(i)) {
                    case "738":
                        item.setImageId(R.drawable.b_738);
                        break;
                    case "739":
                        item.setImageId(R.drawable.b_739);
                    case "E90":
                        item.setImageId(R.drawable.e90);
                        break;
                    case "E70":
                        item.setImageId(R.drawable.e70);
                        break;
                    case "Z20":
                        item.setImageId(R.drawable.z20);
                        break;
                    case "319":
                        item.setImageId(R.drawable.a_319);
                        break;
                    case "CR7":
                        item.setImageId(R.drawable.cr7);
                        break;
                    case "Q84":
                        item.setImageId(R.drawable.q84);
                        break;
                    case "735":
                        item.setImageId(R.drawable.b_735);
                        break;
                    case "777":
                        item.setImageId(R.drawable.b_777);
                        break;
                    case "320":
                        item.setImageId(R.drawable.a_321);
                        break;
                    case "321":
                        item.setImageId(R.drawable.a_321);
                    case "332":
                        item.setImageId(R.drawable.a_321);
                    case "787":
                        item.setImageId(R.drawable.b_787);
                        break;
                    case "767":
                        item.setImageId(R.drawable.b_767);
                    default:
                        item.setImageId(R.drawable.b_777);
                        item.setCraftType("<機種>" + sortedTypeList.get(i));
                        break;
                }
                list.add(item);
            }
            //ImageArrayAdapterに上のリストをセット。
            ConditionFlightImageArrayAdapter adapter = new ConditionFlightImageArrayAdapter(ConditionFlightActivity.this, R.layout.list_view_condition_flight_item, list);
            lv = (ListView) findViewById(R.id.listView);
            lv.setAdapter(adapter);


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
}
