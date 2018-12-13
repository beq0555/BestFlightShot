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

public class SpecialFlightActivity extends AppCompatActivity {
    private String airport = "";
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_flight);
        setTitle("撮影する航空機を選択してください");

        SpecialInfoReceiver receiver = new SpecialInfoReceiver();
        receiver.execute();

    }

    private class SpecialInfoReceiver extends AsyncTask<Void, String, String> {

        @Override
        public String doInBackground(Void... params) {
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
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return result;
        }

        public void onPostExecute(String result) {

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
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject rootJson = jArray.getJSONObject(i);

                    //データの取得例外処理（timeListとtypeList）
                    if (rootJson.has("odpt:scheduledDepartureTime")) {
                        timeList.add(i, rootJson.getString("odpt:scheduledDepartureTime"));
                    } else if (rootJson.has("JSONObject.NULL")) {
                        timeList.add(i, rootJson.getString("odpt:scheduledTime"));
                    } else {
                        timeList.add(i, "null");
                    }
                    if (rootJson.has("odpt:aircraftType")) {
                        typeList.add(i, rootJson.getString("odpt:aircraftType"));
                    } else {
                        typeList.add(i, "null");
                    }
                    if (rootJson.has("odpt:flightInformationText")) {
                        JSONObject infoJson = rootJson.getJSONObject("odpt:flightInformationText");
                        infoList.add(i, infoJson.getString("ja"));
                   } else {
                      infoList.add(i, "特記事項無し");
                    }
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            InfoDatabaseHelper helper = new InfoDatabaseHelper(SpecialFlightActivity.this);
            SQLiteDatabase db = helper.getWritableDatabase();

            try {
                //一旦データベースを削除して、新たにtypeListとtimeListの要素をDBにインサート。
                String sqlDelete = "DELETE FROM infodata";
                SQLiteStatement stmt = db.compileStatement(sqlDelete);
                stmt.executeUpdateDelete();

                String sqlInsert = "INSERT INTO infodata (id, type, time,info) VALUES (?,?,?,?)";

                for (int i = 0; i < typeList.size(); i++) {
                    stmt = db.compileStatement(sqlInsert);
                    stmt.bindLong(1, i);
                    stmt.bindString(2, typeList.get(i));
                    stmt.bindString(3, timeList.get(i));
                    stmt.bindString(4, infoList.get(i));

                    stmt.executeInsert();
                }
                //現在時刻から直近10件のフライト情報を取り出す。それをsortedTypeListとsortedTimeListとsortedInfoListに格納。
                String sql = "SELECT * FROM infodata WHERE time(time) >= time('now','localtime') AND info like '%塗装%' ORDER BY time(time) LIMIT 5";
                Cursor cursor = db.rawQuery(sql, null);
                while (cursor.moveToNext()) {

                    int idxType = cursor.getColumnIndex("type");
                    int idxTime = cursor.getColumnIndex("time");
                    int idxInfo = cursor.getColumnIndex("info");

                    sortedTypeList.add(cursor.getString(idxType));
                    sortedTimeList.add(cursor.getString(idxTime));
                    sortedInfoList.add(cursor.getString(idxInfo));
                }
            }finally {
                db.close();
            }
                List<SpecialListItem> list = new ArrayList<>();
                for (int i = 0; i < sortedTypeList.size(); i++) {
                    SpecialListItem item = new SpecialListItem();
                    item.setCraftType("<機種>" + sortedTypeList.get(i));
                    item.setDepartureTime("離陸時間  " + sortedTimeList.get(i));
                    item.setSpecialInfo(sortedInfoList.get(i));
                    switch (sortedTypeList.get(i)) {
                        case "738":
                            item.setImageId(R.drawable.b_738);
                            break;
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
                        default:
                            item.setImageId(R.drawable.noimage);
                            item.setCraftType("<機種>" + sortedTypeList.get(i));
                            break;
                }
                list.add(item);
            }
            SpecialFlightImageArrayAdapter adapter = new SpecialFlightImageArrayAdapter(SpecialFlightActivity.this,R.layout.list_view_special_flight_item,list);
            lv = (ListView) findViewById(R.id.specialListView);
            lv.setAdapter(adapter);

        }
    }

    //バイナリデータを文字列に変換するメソッド
    private String is2String(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuffer sb = new StringBuffer();
        char[] b = new char[1024];
        int line;
        while (0 <= (line = reader.read(b))) {
            sb.append(b, 0, line);
        }
        return sb.toString();
    }
}
