package com.websarva.wings.android.bestflightshot;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SpotActivity extends FragmentActivity implements OnMapReadyCallback {

    //manifestfilに記述したようにGPSからの位置情報取得の許可、ネットワークからの位置情報取得の許可を変数に代入
    private static final int PERMISSIONS_REQUEST=317;
    private static final String[] PERMISSIONS={Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

    private GoogleMap mMap;

    private Intent intent;
    private String aircraftName;
    private String departureTime;
    private String airport;

    //保存された画像のURI
    private Uri _imageUri;

    //３６０度方位での空港の現在の風向を格納する。
    String strWindDeg;
    //16度方位での空港の現在の風向を格納する。
    String sixteenWindDeg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot);


        //風向き情報取得の非同期処理開始
        WindInfoReceiver receiver = new WindInfoReceiver();
        receiver.execute();

        Log.i("test","現在の風向き"+sixteenWindDeg);
        //各ListActivity画面からのIntentの受けとり
        intent=getIntent();
        aircraftName=intent.getStringExtra("aircraftName");
        departureTime=intent.getStringExtra("departureTime");
        airport=intent.getStringExtra("airport");

        //インテントで取得した機種名から<>部分を削除
        aircraftName = aircraftName.replace("<機種>","");
        departureTime = departureTime.replace("<離陸時間>","");
        //画面下部のテロップに撮影機体情報を表示
        TextView tvAircraftTelop = findViewById(R.id.tvAircraftTelop);
        tvAircraftTelop.setText("撮影する航空機: " + aircraftName);

        TextView tvDepartureTelop = findViewById(R.id.tvDepartureTelop);
        tvDepartureTelop.setText("予定離陸時間: " + departureTime);

    }
    //マップ処理をメソッド化して、非同期処理後のonPostExecute()内で呼ばれるようにして、風向きデータをマップ操作で利用できるようにする。
    public void createMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //Googleマップの準備ができた時のイベントを受け取るリスナを登録する
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){

        if (requestCode==PERMISSIONS_REQUEST){
            if (checkPermission()){
                mMap.setMyLocationEnabled(true);
            } else {
                Toast.makeText(this,"現在地を扱う権限がありません",Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *Googleマップの準備が出来た時に呼び出されるメソッド
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.i("test","現在の風向き"+sixteenWindDeg);

        //成田空港の位置情報
        LatLng NaritaAirport=new LatLng(35.771987,140.39285);
        //成田のA滑走路北向き
        LatLng NaritaAirport_A_North=new LatLng(35.765330,140.374804);
        //成田のB滑走路南向き
        LatLng NaritaAirport_A_South=new LatLng(35.749807,140.3861235);
        //成田のB滑走路北向き
        LatLng NaritaAirport_B_North=new LatLng(35.8024897,140.3803162);
        //成田のB滑走路南向き
        LatLng NaritaAirport_B_South=new LatLng(35.787778,140.390726);


        //羽田空港の位置情報
        LatLng HanedaAirport=new LatLng(35.550157,139.779891);
        //羽田のA滑走路の位置情報
        LatLng HanedaAirport_A=new LatLng(35.547002,139.778187);
        //羽田のB滑走路の位置情報
        LatLng HanedaAirport_B=new LatLng(35.556709,139.767825);
        //羽田のC滑走路の位置情報
        LatLng HanedaAirport_C=new LatLng(35.554913,139.794248);
        //羽田のD滑走路の位置情報
        LatLng HanedaAirport_D=new LatLng(35.529193,139.810077);

        //成田オススメスポット
        //成田市さくらの山公園（北西側）
        LatLng spot_Narita_sakurayama=new LatLng(35.776042,140.363373);
        //三里塚さくらの丘（南西側）
        LatLng spot_Narita_sakuraoka=new LatLng(35.742053,140.384459);
        //ひこうきの丘（南西側）
        LatLng spot_Narita_hikouki=new LatLng(35.738197,140.391813);
        //十余三東雲の丘（北東側）
        LatLng spot_Narita_shinonome=new LatLng(35.892995,140.375792);
        //芝山水辺の里（南西側）
        LatLng spot_Narita_shibayama=new LatLng(35.738354,140.396603);
        //東峰神社（北東側、B滑走路より）
        LatLng spot_Narita_touhou=new LatLng(35.784886,140.393309);
        //空の駅　風和里しばやま（南西側）
        LatLng spot_Narita_sora=new LatLng(35.731142,140.395036);
        //航空科学博物館（東南側）
        LatLng spot_Narita_museum=new LatLng(35.740369,140.397799);

        //羽田オススメスポット
        //浮島公園(南側)
        LatLng spot_Haneda_ukishima=new LatLng(35.520756,139.787836);
        //京浜島つばさ公園（北側）
        LatLng spot_Haneda_tsubasa=new LatLng(35.566419,139.767197);
        //城南島海浜公園(北側)
        LatLng spot_Haneda_jyounannshima=new LatLng(35.578304,139.783423);
        //都立東京港野鳥公園(北側)
        LatLng spot_Haneda_toritsu=new LatLng(35.583094,139.75833);
        //羽田空港第1ターミナル　展望デッキ（中央）
        LatLng spot_Haneda_terminal1=new LatLng(35.549086,139.783974);
        //羽田空港国際線旅客ターミナル展望台（A,B滑走路に近い）
        LatLng spot_Haneda_terminal=new LatLng(35.54523,139.768107);
        //羽田空港第2ターミナル　展望デッキ（C滑走路に近い）
        LatLng spot_Haneda_terminal2=new LatLng(35.551001,139.788613);


        mMap = googleMap;
        UiSettings uiSettings=mMap.getUiSettings();

        //貼り付ける飛行機画像の用意
        BitmapDescriptor plane_descriptor=BitmapDescriptorFactory.fromResource(R.drawable.plane);


        //成田空港選択の場合
        if (airport.equals("NRT")){
            // 成田空港付近をマップに表示させる
            CameraPosition.Builder builder=new CameraPosition.Builder().target(NaritaAirport).zoom(12.9f).bearing(0).tilt(25.0f);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));

            //成田空港にマーカーを表示させる
            MarkerOptions maker_narita=new MarkerOptions().position(NaritaAirport).title("成田国際空港");
            mMap.addMarker(maker_narita);



            //ここから風向きで画像の表示場所を切り替えてください
            //飛行機画像を成田A滑走路北向きにオーバーレイ表示
            if(sixteenWindDeg.equals("東北東")||sixteenWindDeg.equals("北東")||sixteenWindDeg.equals("北北東")||sixteenWindDeg.equals("北")||sixteenWindDeg.equals("北北西")||sixteenWindDeg.equals("北西")||sixteenWindDeg.equals("西北西")||sixteenWindDeg.equals("西")) {
                GroundOverlayOptions options1 = new GroundOverlayOptions()
                        .image(plane_descriptor)
                        //画像の位置と大きさを決める
                        .position(NaritaAirport_A_North, 500f, 600f)
                        .bearing(330);
                GroundOverlay narita_overlay_A_North = mMap.addGroundOverlay(options1);
            }else {

                //飛行機画像を成田A滑走路南向きにオーバーレイ表示
                GroundOverlayOptions options2 = new GroundOverlayOptions()
                        .image(plane_descriptor)
                        .position(NaritaAirport_A_South, 500f, 600f)
                        .bearing(155);
                GroundOverlay narita_overlay_A_South = mMap.addGroundOverlay(options2);

            }
            //飛行機画像を成田B滑走路北向きにオーバーレイ表示
            //GroundOverlayOptions options3=new GroundOverlayOptions()
            //        .image(plane_descriptor)
            //        .position(NaritaAirport_B_North,500f,600f)
            //        .bearing(330);
            //GroundOverlay narita_overlay_B_North=mMap.addGroundOverlay(options3);

            //飛行機画像を成田B滑走路北向きにオーバーレイ表示
            //GroundOverlayOptions options4=new GroundOverlayOptions()
            //        .image(plane_descriptor)
            //        .position(NaritaAirport_B_South,500f,600f)
            //        .bearing(155);
            //GroundOverlay narita_overlay_B_South=mMap.addGroundOverlay(options4);

            //成田空港オススメスポット
            if (sixteenWindDeg.equals ("西")||sixteenWindDeg.equals("西北西")||sixteenWindDeg.equals("北西")||sixteenWindDeg.equals("北北西")){
                //北西エリアに位置するオススメスポット
                MarkerOptions maker_narita_sakurayama=new MarkerOptions().position(spot_Narita_sakurayama).title("成田市さくらの山公園");
                mMap.addMarker(maker_narita_sakurayama);

            }else if (sixteenWindDeg.equals ("北")||sixteenWindDeg.equals("北北東")||sixteenWindDeg.equals("北東")||sixteenWindDeg.equals("東北東")){
                //北東エリアに位置するオススメスポット
                MarkerOptions maker_narita_shinonome=new MarkerOptions().position(spot_Narita_shinonome).title("十余三東雲の丘");
                mMap.addMarker(maker_narita_shinonome);

                MarkerOptions maker_narita_touhou=new MarkerOptions().position(spot_Narita_shinonome).title("東峰神社");
                mMap.addMarker(maker_narita_touhou);
            }else if (sixteenWindDeg.equals ("東")||sixteenWindDeg.equals("東南東")||sixteenWindDeg.equals("南東")||sixteenWindDeg.equals("南南東")){
                //東南エリアに位置するオススメスポット
                MarkerOptions maker_narita_museum=new MarkerOptions().position(spot_Narita_museum).title("航空科学博物館");
                mMap.addMarker(maker_narita_museum);

            }else if (sixteenWindDeg.equals ("南")||sixteenWindDeg.equals("南南西")||sixteenWindDeg.equals("南西")||sixteenWindDeg.equals("西南西")){
                //南西エリアに位置するオススメスポット
                MarkerOptions maker_narita_sakuraoka=new MarkerOptions().position(spot_Narita_sakuraoka).title("三里塚さくらの丘");
                mMap.addMarker(maker_narita_sakuraoka);

                MarkerOptions maker_narita_hikouki=new MarkerOptions().position(spot_Narita_sakuraoka).title("ひこうきの丘");
                mMap.addMarker(maker_narita_hikouki);

                MarkerOptions maker_narita_shibayama=new MarkerOptions().position(spot_Narita_shibayama).title("芝山水辺の里");
                mMap.addMarker(maker_narita_shibayama);

                MarkerOptions maker_narita_sora=new MarkerOptions().position(spot_Narita_sora).title("空の駅　風和里しばやま");
                mMap.addMarker(maker_narita_shibayama);

            }

        } else if (airport.equals("HND")){

            // 羽田空港付近をマップに表示させる
            CameraPosition.Builder builder=new CameraPosition.Builder().target(HanedaAirport).zoom(12.9f).bearing(0).tilt(25.0f);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));

            //羽田空港にマーカーを表示させる
            MarkerOptions maker_haneda=new MarkerOptions().position(NaritaAirport).title("羽田国際空港");
            mMap.addMarker(maker_haneda);


            //ここから風向きで画像の表示場所を切り替えてください
            //飛行機画像を羽田A滑走路にオーバーレイ表示

            GroundOverlayOptions options1=new GroundOverlayOptions()
                    .image(plane_descriptor)
                    //画像の位置と大きさを決める
                    .position(HanedaAirport_A,500f,600f)
                    .bearing(330);
            GroundOverlay haneda_overlay_A=mMap.addGroundOverlay(options1);

            //飛行機画像を羽田B滑走路にオーバーレイ表示
            GroundOverlayOptions options2=new GroundOverlayOptions()
                    .image(plane_descriptor)
                    .position(HanedaAirport_B,500f,600f)
                    .bearing(40);
            GroundOverlay haneda_overlay_B=mMap.addGroundOverlay(options2);

            //飛行機画像を羽田C滑走路にオーバーレイ表示
            GroundOverlayOptions options3=new GroundOverlayOptions()
                    .image(plane_descriptor)
                    .position(HanedaAirport_C,500f,600f)
                    .bearing(150);
            GroundOverlay haneda_overlay_C=mMap.addGroundOverlay(options3);

            //飛行機画像を羽田D滑走路にオーバーレイ表示
            GroundOverlayOptions options4=new GroundOverlayOptions()
                    .image(plane_descriptor)
                    .position(HanedaAirport_D,500f,600f)
                    .bearing(45);
            GroundOverlay haneda_overlay_D=mMap.addGroundOverlay(options4);


            //羽田空港おすすめスポット
            //南側
            MarkerOptions maker_haneda_ukishima=new MarkerOptions().position(spot_Haneda_ukishima).title("浮島公園");
            mMap.addMarker(maker_haneda_ukishima);

        }



        //現在地を表示させる
        if (checkPermission()){
            googleMap.setMyLocationEnabled(true);
        } else {
            requestPermission();
        }

        //ズームのコントロールが無効の場合、有効にする
        if (!uiSettings.isZoomControlsEnabled()){
            uiSettings.setZoomControlsEnabled(true);
        }

        //ズームのジェスチャー操作が無効の場合、有効にする
        if (!uiSettings.isZoomGesturesEnabled()){
            uiSettings.setZoomGesturesEnabled(true);
        }


    }

    //permissionを許可しているかの確認メソッド
    private boolean checkPermission(){
        return ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;
    }
    //権限の許可を求めるメソッド
    private void requestPermission(){
        ActivityCompat.requestPermissions(this,PERMISSIONS,PERMISSIONS_REQUEST);
    }

    //カメラ機能
    public void onCameraClick(View view){

        //WRITE_EXTERNAL_STORAGEの許可が下りていないなら
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            //WRITE_EXTERNAL_STORAGEの許可を求めるダイアログを表示する
            //その際のリクエストコードを2000とする
            String[] storage_permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this,storage_permissions,2000);
            return;

        }

        //日時データを「yyyyMMddHHmmss」の形式に整形するフォーマットを生成。
        //撮影写真に名前をつける
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
        Date now=new Date(System.currentTimeMillis());
        String nowStr=dateFormat.format(now);
        String fileName=aircraftName+nowStr+".jpg";
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
        ContentResolver resolver=getContentResolver();
        _imageUri=resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,_imageUri);
        startActivity(intent);

    }


    //風向き情報を取得する非同期クラス
    private class WindInfoReceiver extends AsyncTask<Void,String,String> {

        @Override
        public String doInBackground(Void... params) {
            //APIキー
            final String API_KEY = "appid=5ee5c307a24bd39c9942999bf17cdfd4";
            final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?";
            final String NARITA_ID = "&id=2111684";
            final String HANEDA_ID = "&id=6415253";
            //成田か羽田のIDを代入するための変数
            String airportKey;

            intent = getIntent();
            airport = intent.getStringExtra("airport");
            switch (airport) {
                case "NRT":
                    airportKey = NARITA_ID;
                    break;
                case "HND":
                    airportKey = HANEDA_ID;
                    break;
                default:
                    airportKey = null;
            }


            String urlStr = BASE_URL + API_KEY + airportKey;
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

            try {
                JSONObject rootJSON = new JSONObject(result);
                JSONObject windJSON = rootJSON.getJSONObject("wind");
                strWindDeg = windJSON.getString("deg");

                sixteenWindDeg = to16Orientation(strWindDeg);
                createMap();


            }catch (JSONException ex) {
                ex.printStackTrace();
            }



        }
    }
   // private class DestinationAirportReceiver extends AsyncTask<Void,String,String>{
   //     @Override
   //     public String doInBackground(Void... params) {

   //         String queryAirport;
   //         String queryAircraft;
  //          String queryDepartureTime;
  //          Intent intent = getIntent();
  //          airport = intent.getStringExtra("airport");
  //          aircraftName = intent.getStringExtra("aircraft");
  //          departureTime = intent.getStringExtra("airline");
   //     }

 //   }
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
    //３６０度方位の風向データを１６度方位の風向データに整形するメソッド。
    private String to16Orientation(String strWindDeg) {
        double doubleWindDeg = Double.parseDouble(strWindDeg);
        int intWindDeg=(int) doubleWindDeg;
        String[] dname = {"北","北北東","北東", "東北東", "東", "東南東", "南東", "南南東", "南", "南南西", "南西", "西南西", "西", "西北西", "北西", "北北西", "北"};
        int dindex =  (int)( (intWindDeg + 11.25) / 22.5 );
        return dname[dindex];
    }
}


