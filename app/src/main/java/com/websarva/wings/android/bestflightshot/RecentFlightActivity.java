package com.websarva.wings.android.bestflightshot;

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

public class RecentFlightActivity extends AppCompatActivity {

    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //非同期処理を開始
        FlightInfoReceiver receiver = new FlightInfoReceiver();
        receiver.execute();
    }

    //非同期処理クラス。 doInBackgroundメソッドでhttp通信。onPostExecuteメソッドでjsonデータをパースにDBに格納とクエリを全て行う。
    private class FlightInfoReceiver extends AsyncTask<Void,String,String> {


        @Override
        public String doInBackground(Void... params) {
            String urlStr = "https://api-tokyochallenge.odpt.org/api/v4/odpt:FlightInformationDeparture?acl:consumerKey=2af0930edd9f426efa146aa64e7d90d9b41b4fb84b9bef1e1040dce7e6fed3cf&odpt:departureAirport=odpt.Airport:HND";
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

           //jsonデータを文字列配列にパースした後に、機種データと離陸時間データを格納する配列。
           List<String> typeList = new ArrayList<>();
           List<String> timeList = new ArrayList<>();

            //DBに格納後、SQLでクエリを行った機種と時間データを格納する配列。
           List<String> sortedTypeList = new ArrayList<>();
           List<String> sortedTimeList = new ArrayList<>();


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
                        timeList.add(i,"を取得できませんでした。");
                    }
                    if(rootJson.has("odpt:aircraftType")) {
                        typeList.add(i,rootJson.getString("odpt:aircraftType"));
                    } else {
                        typeList.add(i,"機種情報を取得できませんでした。");
                    }
                }
            }catch (JSONException ex) {
                ex.printStackTrace();
            }

            DatabaseHelper helper = new DatabaseHelper(RecentFlightActivity.this);
            SQLiteDatabase db = helper.getWritableDatabase();

            try {
                //一旦データベースを削除して、新たにtypeListとtimeListの要素をDBにインサート。
                String sqlDelete = "DELETE FROM flightdata";
                SQLiteStatement stmt = db.compileStatement(sqlDelete);
                stmt.executeUpdateDelete();

                String sqlInsert = "INSERT INTO flightdata (id, type, time) VALUES (?,?,?)";

                for(int i = 0; i < typeList.size(); i++) {
                    stmt = db.compileStatement(sqlInsert);
                    stmt.bindLong(1,i);
                    stmt.bindString(2,typeList.get(i));
                    stmt.bindString(3,timeList.get(i));

                    stmt.executeInsert();
                }
                //現在時刻から直近10件のフライト情報を取り出す。それをsortedTypeListとsortedTimeListに格納。
                String sql = "SELECT * FROM flightdata WHERE time(time) >= time('now','localtime') ORDER BY time(time) LIMIT 15";
                Cursor cursor = db.rawQuery(sql,null);
                while (cursor.moveToNext()) {

                    int idxType = cursor.getColumnIndex("type");
                    int idxTime = cursor.getColumnIndex("time");

                    sortedTypeList.add(cursor.getString(idxType));
                    sortedTimeList.add(cursor.getString(idxTime));
                }


            }finally {
                db.close();
            }
            //リストビューに機種名・離陸時間・機種画像を表示。
            List<ListItem> list = new ArrayList<>();
            for (int i = 0; i < sortedTypeList.size(); i++) {
                ListItem item = new ListItem();

                item.setCraftType("   機種         " + sortedTypeList.get(i));
                item.setDepartureTime("離陸時間  " + sortedTimeList.get(i));
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
                        item.setCraftType("機種情報を取得できませんでした");
                        break;
                }
                list.add(item);
            }
            //ImageArrayAdapterに上のリストをセット。
            ImageArrayAdapter adapter = new ImageArrayAdapter(RecentFlightActivity.this,R.layout.list_view_image_item,list);
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
