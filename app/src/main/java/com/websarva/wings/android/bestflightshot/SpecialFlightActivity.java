package com.websarva.wings.android.bestflightshot;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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

public class SpecialFlightActivity extends AppCompatActivity {

    private String airport = "";
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_flight);
        Intent intent = getIntent();
        airport = intent.getStringExtra("airport");
        if(airport.equals("NRT")) {
            airport = "成田";
        } else {
            airport = "羽田";
        }
        setTitle(airport+">撮影する機体を選択してください");

        SpecialInfoReceiver receiver = new SpecialInfoReceiver();
        receiver.execute();

    }

    private class SpecialInfoReceiver extends AsyncTask<Void, String, String> {

        private ProgressBar progressBar;

        @Override
        public String doInBackground(Void... params) {

            progressBar = (ProgressBar)findViewById(R.id.specialProgressBar);
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

            if(this.progressBar != null) {
                this.progressBar.setVisibility(View.GONE);
            }

            List<String> typeList = new ArrayList<>();
            List<String> airlineList = new ArrayList<>();
            List<String> timeList = new ArrayList<>();
            //odpt:flightInformationTextの内容を格納する配列
            List<String> infoList = new ArrayList<>();

            List<String> sortedTypeList = new ArrayList<>();
            List<String> sortedAirlineList = new ArrayList<>();
            List<String> sortedTimeList = new ArrayList<>();
            List<String> sortedInfoList = new ArrayList<>();

            //airlineはodpt.Operator:ANAという風に取得されるため、ANAという文字列に整形するために用いる変数
            String fullAirline;
            String airline;

            try {
                //Jsonデータを文字列配列にパース
                JSONArray jArray = new JSONArray(result);
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject rootJson = jArray.getJSONObject(i);

                    //データの取得例外処理
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
                    if(rootJson.has("odpt:airline")) {
                        fullAirline = rootJson.getString("odpt:airline");
                        airline = fullAirline.replaceAll("odpt.Operator:","");
                        airlineList.add(i,airline);
                    } else {
                        airlineList.add(i,"を取得できませんでした");
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
            SpecialDatabaseHelper helper = new SpecialDatabaseHelper(SpecialFlightActivity.this);
            SQLiteDatabase db = helper.getWritableDatabase();

            try {
                //一旦データベースを削除して、新たにtypeListとtimeListの要素をDBにインサート。
                String sqlDelete = "DELETE FROM specialdata";
                SQLiteStatement stmt = db.compileStatement(sqlDelete);
                stmt.executeUpdateDelete();

                String sqlInsert = "INSERT INTO specialdata (id,type,airline,time,info) VALUES (?,?,?,?,?)";

                for (int i = 0; i < typeList.size(); i++) {
                    stmt = db.compileStatement(sqlInsert);
                    stmt.bindLong(1, i);
                    stmt.bindString(2, typeList.get(i));
                    stmt.bindString(3,airlineList.get(i));
                    stmt.bindString(4, timeList.get(i));
                    stmt.bindString(5, infoList.get(i));

                    stmt.executeInsert();
                }
                //現在時刻から直近10件のフライト情報を取り出す。それをsortedTypeListとsortedTimeListとsortedInfoListに格納。
                String sql = "SELECT * FROM specialdata WHERE time(time) >= time('now','localtime') AND info LIKE '%で運航%' OR info LIKE '%よる運航%' ORDER BY time(time) LIMIT 7";
                Cursor cursor = db.rawQuery(sql, null);
                while (cursor.moveToNext()) {

                    int idxType = cursor.getColumnIndex("type");
                    int idxAirlin = cursor.getColumnIndex("airline");
                    int idxTime = cursor.getColumnIndex("time");
                    int idxInfo = cursor.getColumnIndex("info");

                    sortedTypeList.add(cursor.getString(idxType));
                    sortedAirlineList.add(cursor.getString(idxAirlin));
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
                    item.setAirline("<航空会社>"+ sortedAirlineList.get(i));
                    item.setDepartureTime("<離陸時間>  " + sortedTimeList.get(i));
                    item.setSpecialInfo(sortedInfoList.get(i));
                    switch (sortedTypeList.get(i)) {
                        //ボーイング系
                        case "735":
                            item.setImageId(R.drawable.b_737);
                            break;
                        case "737":
                            item.setImageId(R.drawable.b_737);
                            break;
                        case "738":
                            item.setImageId(R.drawable.b_738);
                            break;
                        case "739":
                            item.setImageId(R.drawable.b_738);
                            break;
                        case "744":
                            item.setImageId(R.drawable.b_747);
                        case "747":
                            item.setImageId(R.drawable.b_747);
                            break;
                        case "748":
                            item.setImageId(R.drawable.b_748);
                        case "763":
                            item.setImageId(R.drawable.b_763);
                            break;
                        case "767":
                            item.setImageId(R.drawable.b_767);
                            break;
                        case "772":
                            item.setImageId(R.drawable.b_772);
                            break;
                        case "773":
                            item.setImageId(R.drawable.b_773);
                            break;
                        case "777":
                            item.setImageId(R.drawable.b_777);
                            break;
                        case "787":
                            item.setImageId(R.drawable.b_787);
                            break;
                        case "788":
                            item.setImageId(R.drawable.b_788);
                            break;
                        case "789":
                            item.setImageId(R.drawable.b_789);
                            break;
                        case "73D":
                            item.setImageId(R.drawable.b_737);
                            break;
                        case "73H":
                            item.setImageId(R.drawable.b_737);
                            break;
                        case "73P":
                            item.setImageId(R.drawable.b_737);
                            break;
                        case "73L":
                            item.setImageId(R.drawable.b_737);
                            break;
                        case "76E":
                            item.setImageId(R.drawable.b_767);
                            break;
                        case "76P":
                            item.setImageId(R.drawable.b_767);
                            break;
                        case "77I":
                            item.setImageId(R.drawable.b_777);
                            break;
                        case "77W":
                            item.setImageId(R.drawable.b_777);
                            break;
                        case "78I":
                            item.setImageId(R.drawable.b_787);
                            break;
                        case "78P":
                            item.setImageId(R.drawable.b_787);
                            break;
                        //エアバス系
                        case "319":
                            item.setImageId(R.drawable.a_319);
                            break;
                        case "320":
                            item.setImageId(R.drawable.a_320);
                            break;
                        case "321":
                            item.setImageId(R.drawable.a_321);
                            break;
                        case "330":
                            item.setImageId(R.drawable.a_330);
                            break;
                        case "332":
                            item.setImageId(R.drawable.a_332);
                            break;
                        case "333":
                            item.setImageId(R.drawable.a_333);
                            break;
                        case "359":
                            item.setImageId(R.drawable.a_359);
                            break;
                        case "380":
                            item.setImageId(R.drawable.a_380);
                            break;
                        //その他
                        case "CR7":
                            item.setImageId(R.drawable.cr7);
                            break;
                        case "E767":
                            item.setImageId(R.drawable.e_767);
                            break;
                        case "E70":
                            item.setImageId(R.drawable.e70);
                            break;
                        case "E90":
                            item.setImageId(R.drawable.e90);
                            break;
                        case "Q84":
                            item.setImageId(R.drawable.q84);
                            break;
                        case "Z20":
                            item.setImageId(R.drawable.z20);
                            break;
                        case "US1R":
                            item.setImageId(R.drawable.us1r);
                            break;
                        default:
                            item.setImageId(R.drawable.noimage);
                            break;
                }
                list.add(item);
            }
            SpecialFlightImageArrayAdapter adapter = new SpecialFlightImageArrayAdapter(SpecialFlightActivity.this,R.layout.list_view_special_flight_item,list);
            lv = (ListView) findViewById(R.id.specialListView);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new ListItemClickListener());

        }
    }

    private class ListItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent,View view,int position,long id){

            Intent intent=new Intent(SpecialFlightActivity.this,SpotActivity.class);
            startActivity(intent);
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

