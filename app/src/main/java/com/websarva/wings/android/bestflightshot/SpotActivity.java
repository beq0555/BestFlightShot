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
import android.widget.ProgressBar;
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

import org.json.JSONArray;
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
import java.util.ArrayList;
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
    private String destinationAirport;

    //保存された画像のURI
    private Uri _imageUri;

    //３６０度方位での空港の現在の風向を格納する。
    String strWindDeg;
    //16度方位での空港の現在の風向を格納する。
    String sixteenWindDeg;

    //成田のオーバーレイ変数
    GroundOverlay narita_overlay_A_North;
    GroundOverlay narita_overlay_A_South;

    //羽田のオーバーレイ変数
    GroundOverlay haneda_overlay_D_North;
    GroundOverlay haneda_overlay_C_North;
    GroundOverlay haneda_overlay_C_South;
    GroundOverlay haneda_overlay_A_South;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot);


        //風向き情報取得の非同期処理開始
        WindInfoReceiver receiver = new WindInfoReceiver();
        receiver.execute();

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
        //羽田のA滑走路の南風時
        LatLng HanedaAirport_A_South=new LatLng(35.5424174,139.7816119);
        //羽田のC滑走路の南風時
        LatLng HanedaAirport_C_South=new LatLng(35.5436385,139.8025040);
        //羽田のC滑走路の北風時
        LatLng HanedaAirport_C_North=new LatLng(35.5638344,139.7879828);
        //羽田のD滑走路の北風時
        LatLng HanedaAirport_D_North=new LatLng(35.529193,139.810077);

        //滑走路予測マーク用変数
        //羽田
        GroundOverlayOptions options1=new GroundOverlayOptions()
                .image(plane_descriptor)
                .position(HanedaAirport_C_North,500f,600f)
                .bearing(330);

        GroundOverlayOptions options2=new GroundOverlayOptions()
                .image(plane_descriptor)
                .position(HanedaAirport_D_North,500f,600f)
                .bearing(45);

        GroundOverlayOptions options3=new GroundOverlayOptions()
                .image(plane_descriptor)
                .position(HanedaAirport_C_South,500f,600f)
                .bearing(145);

        GroundOverlayOptions options4=new GroundOverlayOptions()
                .image(plane_descriptor)
                //画像の位置と大きさを決める
                .position(HanedaAirport_A_South,500f,600f)
                .bearing(145);

        //成田
        GroundOverlayOptions options5 =new GroundOverlayOptions()
                .image(plane_descriptor)
                .position(NaritaAirport_A_North, 500f, 600f)
                .bearing(330);

        GroundOverlayOptions options6 = new GroundOverlayOptions()
                .image(plane_descriptor)
                .position(NaritaAirport_A_South, 500f, 600f)
                .bearing(155);



        //成田空港選択の場合
        if (airport.equals("NRT")){
            // 成田空港付近をマップに表示させる
            CameraPosition.Builder builder=new CameraPosition.Builder().target(NaritaAirport).zoom(12.9f).bearing(0).tilt(25.0f);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));


            //ここから風向きで画像の表示場所を切り替えてください
            //飛行機画像を成田A滑走路北向きにオーバーレイ表示
            if(sixteenWindDeg.equals("東北東")||sixteenWindDeg.equals("北東")||sixteenWindDeg.equals("北北東")||sixteenWindDeg.equals("北")||sixteenWindDeg.equals("北北西")||sixteenWindDeg.equals("北西")||sixteenWindDeg.equals("西北西")||sixteenWindDeg.equals("西")) {
               narita_overlay_A_North = mMap.addGroundOverlay(options5);
            }else {
                //飛行機画像を成田A滑走路南向きにオーバーレイ表示
                narita_overlay_A_South = mMap.addGroundOverlay(options6);
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

            //成田空港のポリゴン表示
            PolygonOptions options=new PolygonOptions()
                    .addAll(create_narita_Poligon())
                    .strokeColor(Color.RED)
                    .strokeWidth(4)
                    .fillColor(Color.argb(50,51,204,255));
            mMap.addPolygon(options);


        } else if (airport.equals("HND")){

            // 羽田空港付近をマップに表示させる
            CameraPosition.Builder builder=new CameraPosition.Builder().target(HanedaAirport).zoom(12.9f).bearing(0).tilt(25.0f);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));



            //ここから風向きで画像の表示場所を切り替えてください
            //飛行機画像を羽田A滑走路にオーバーレイ表示
            //北風がふいているとき
            if(sixteenWindDeg.equals("東")||sixteenWindDeg.equals("東北東")||sixteenWindDeg.equals("北東")||sixteenWindDeg.equals("北北東")||sixteenWindDeg.equals("北")||sixteenWindDeg.equals("北北西")||sixteenWindDeg.equals("北西")||sixteenWindDeg.equals("西北西")){
                switch (destinationAirport) {
                    //シンガポール空港
                    case "odpt.Airport:SIN":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //クアラルンプール空港
                    case "odpt.Airport:KUL":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //旭川空港
                    case "odpt.Airport:AKJ":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //秋田空港
                    case "odpt.Airport:AXT":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //スワンナプーム国際空港(バンコク)
                    case "odpt.Airport:BKK":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //広州白雲国際空港
                    case "odpt.Airport:CAN":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //シャルル・ド・ゴール空港
                    case "odpt.Airport:CDG":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //新千歳空港
                    case "odpt.Airport:CTS":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //フランクフルト空港
                    case "odpt.Airport:FRA":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //福岡空港
                    case "odpt.Airport:FUK":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //ドバイ国際空港
                    case "odpt.Airport:DXB":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //タンソンニャット空港(ホーチミン)
                    case "odpt.Airport:SGN":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //仁川空港(ソウル)
                    case "odpt.Airport:ICO":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //ロンドン・ヒースロー空港
                    case "odpt.Airport:LHR":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //北九州空港
                    case "odpt.Airport:KKJ":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //鹿児島空港
                    case "odpt.Airport:KOJ":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //那覇空港
                    case "odpt.Airport:OKA":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //伊丹空港
                    case "odpt.Airport:ITM":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //石垣空港
                    case "odpt.Airport:ISG":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //宮古空港
                    case "odpt.Airport:MMY":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //熊本空港
                    case "odpt.Airport:KMJ":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //女満別空港
                    case "odpt.Airport:MMB":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //徳島飛行場
                    case "odpt.Airport:TKS":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //関西国際空港
                    case "odpt.Airport:KIX":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //広島空港
                    case "odpt.Airport:HIJ":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //山形空港
                    case "odpt.Airport:GAJ":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //南紀白浜空港
                    case "odpt.Airport:SHM":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //松山空港
                    case "odpt.Airport:MYJ":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //高知空港
                    case "odpt.Airport:KCZ":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //長崎空港
                    case "odpt.Airport:NGS":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //台北松山空港
                    case "odpt.Airport:TSA":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //高松空港
                    case "odpt.Airport:TAK":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //函館空港
                    case "odpt.Airport:HKD":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //山口宇部空港
                    case "odpt.Airport:UBJ":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //青森空港
                    case "odpt.Airport:AOJ":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //小松空港
                    case "odpt.Airport:KMQ":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //帯広空港
                    case "odpt.Airport:OBO":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //釧路空港
                    case "odpt.Airport:KUH":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //大分空港
                    case "odpt.Airport:OIT":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //宮崎空港
                    case "odpt.Airport:KMI":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //岡山空港
                    case "odpt.Airport:OKJ":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //中部国際空港
                    case "odpt.Airport:NGO":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //ノイバイ空港
                    case "odpt.Airport:HAN":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //スカルノ・ハッタ空港
                    case "odpt.Airport:CGK":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //金浦空港
                    case "odpt.Airport:GMP":
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        break;
                    //八丈島空港
                    case "odpt.Airport:HAC":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //香港空港
                    case "odpt.Airport:HKG":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //ダニエル・K・イノウエ国際空港
                    case "odpt.Airport:HNL":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                    //佐賀空港
                    case "odpt.Airport:HSG":
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        break;
                    //石見空港
                    case "odpt.Airport:IYJ":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //
                    case "odpt.Airport:":
                    //
                    //case "odpt.Airport:":
                    //
                    //case "odpt.Airport:":
                    default:
                        haneda_overlay_C_North=mMap.addGroundOverlay(options1);
                        haneda_overlay_D_North=mMap.addGroundOverlay(options2);
                        }
                //南風がふいているとき
            } else {
                switch (destinationAirport) {
                    //シンガポール
                    case "odpt.Airport:SIN":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //クアラルンプール
                    case "odpt.Airport:KUL":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //旭川空港
                    case "odpt.Airport:AKJ":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //秋田空港
                    case "odpt.Airport:AXT":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //スワンナプーム国際空港(バンコク)
                    case "odpt.Airport:BKK":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //広州白雲国際空港
                    case "odpt.Airport:CAN":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //シャルル・ド・ゴール空港
                    case "odpt.Airport:CDG":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //新千歳空港
                    case "odpt.Airport:CTS":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //フランクフルト空港
                    case "odpt.Airport:FRA":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //福岡空港
                    case "odpt.Airport:FUK":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //ドバイ国際空港
                    case "odpt.Airport:DXB":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //タンソンニャット空港(ホーチミン)
                    case "odpt.Airport:SGN":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //仁川空港(ソウル)
                    case "odpt.Airport:ICO":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //ロンドン・ヒースロー空港
                    case "odpt.Airport:LHR":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //北九州空港
                    case "odpt.Airport:KKJ":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //鹿児島空港
                    case "odpt.Airport:KOJ":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //那覇空港
                    case "odpt.Airport:OKA":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //伊丹空港
                    case "odpt.Airport:ITM":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //石垣空港
                    case "odpt.Airport:ISG":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //宮古空港
                    case "odpt.Airport:MMY":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //熊本空港
                    case "odpt.Airport:KMJ":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //女満別空港
                    case "odpt.Airport:MMB":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //徳島飛行場
                    case "odpt.Airport:TKS":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //関西国際空港
                    case "odpt.Airport:KIX":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //広島空港
                    case "odpt.Airport:HIJ":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //山形空港
                    case "odpt.Airport:GAJ":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //南紀白浜空港
                    case "odpt.Airport:SHM":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //松山空港
                    case "odpt.Airport:MYJ":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //高知空港
                    case "odpt.Airport: KCZ":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //長崎空港
                    case "odpt.Airport:NGS":

                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //台北松山空港
                    case "odpt.Airport:TSA":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //高松空港
                    case "odpt.Airport:TAK":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //函館空港
                    case "odpt.Airport:HKD":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //山口宇部空港
                    case "odpt.Airport:UBJ":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //青森空港
                    case "odpt.Airport:AOJ":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //小松空港
                    case "odpt.Airport:KMQ":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //帯広空港
                    case "odpt.Airport:OBO":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //釧路空港
                    case "odpt.Airport:KUH":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //大分空港
                    case "odpt.Airport:OIT":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options3);
                        break;
                    //宮崎空港
                    case "odpt.Airport:KMI":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //岡山空港
                    case "OKJ":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //中部国際空港
                    case "odpt.Airport:NGO":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //ノイバイ空港
                    case "odpt.Airport:HAN":
                    haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //スカルノ・ハッタ空港
                    case "odpt.Airport:CGK":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //金浦空港
                    case "odpt.Airport:GMP":
                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                        break;
                    //八丈島空港
                    case "odpt.Airport:HAC":
                    haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //香港空港
                    case "odpt.Airport:HKG":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //ダニエル・K・イノウエ国際空港
                    case "odpt.Airport:HNL":
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //佐賀空港
                    case "odpy:Airport:HSG":
                    haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //石見空港
                    case "odpy:Airport:IWJ":
                    haneda_overlay_A_South=mMap.addGroundOverlay(options4);
                        break;
                    //
                    //case "odpt.Airport:":
                    //
                    //case "odpt.Airport:":

                    default:
                        haneda_overlay_A_South=mMap.addGroundOverlay(options4);


                        haneda_overlay_C_South=mMap.addGroundOverlay(options3);
                }
            }
            //羽田空港おすすめスポット
            //滑走路別にオススメスポットを表示させる

            MarkerOptions maker_haneda_ukishima=new MarkerOptions().position(spot_Haneda_ukishima).title("浮島公園");
            mMap.addMarker(maker_haneda_ukishima);

            //羽田空港のポリゴン表示
            PolygonOptions options=new PolygonOptions()
                    .addAll(create_haneda_Poligon())
                    .strokeColor(Color.RED)
                    .strokeWidth(4)
                    .fillColor(Color.argb(55,51,204,255));
            mMap.addPolygon(options);

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
        private ProgressBar progressBar1;

        @Override
        public String doInBackground(Void... params) {

            progressBar1 = (ProgressBar)findViewById(R.id.spotProgressBar1);
            this.progressBar1.setVisibility(View.VISIBLE);
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

            if(this.progressBar1 != null) {
                this.progressBar1.setVisibility(View.GONE);
            }

            try {
                JSONObject rootJSON = new JSONObject(result);
                JSONObject windJSON = rootJSON.getJSONObject("wind");
                strWindDeg = windJSON.getString("deg");

                sixteenWindDeg = to16Orientation(strWindDeg);



            }catch (JSONException ex) {
                ex.printStackTrace();
            }
            DestinationAirportReceiver receiver = new DestinationAirportReceiver();
            receiver.execute();



        }
    }
    private class DestinationAirportReceiver extends AsyncTask<Void,String,String> {

        private ProgressBar progressBar2;
        @Override
        public String doInBackground(Void... params) {



            String queryAirport;
            String queryAircraft;
            String queryDepartureTime;
            Intent intent = getIntent();
            airport = intent.getStringExtra("airport");
            queryAirport = "&odpt:departureAirport=odpt.Airport:" + airport;
            aircraftName = intent.getStringExtra("aircraftName");
            aircraftName = aircraftName.replace("<機種>","");
            queryAircraft = "&odpt:aircraftType=" + aircraftName;
            departureTime = intent.getStringExtra("departureTime");
            departureTime = departureTime.replace("<離陸時間>","");
            queryDepartureTime = "&odpt:scheduledDepartureTime="+departureTime;

            String urlStr = "https://api-tokyochallenge.odpt.org/api/v4/odpt:FlightInformationDeparture?acl:consumerKey=2af0930edd9f426efa146aa64e7d90d9b41b4fb84b9bef1e1040dce7e6fed3cf" + queryAirport + queryAircraft + queryDepartureTime;
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


            List<String> destinationAirportList = new ArrayList<>();
            try {
                JSONArray jArray = new JSONArray(result);
                    JSONObject rootJson = jArray.getJSONObject(0);
                    if (rootJson.has("odpt:destinationAirport")) {
                        destinationAirportList.add(0, rootJson.getString("odpt:destinationAirport"));
                    } else {
                        destinationAirportList.add(0, "目的地不明");
                    }

            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            //目的地空港を非同期処理で取得して代入している
            destinationAirport = destinationAirportList.get(0);
            createMap();
        }
    }
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

        //３６０度方位の風向データを１６度方位の風向データに整形するメソッド。
        private String to16Orientation(String strWindDeg) {
            double doubleWindDeg = Double.parseDouble(strWindDeg);
            int intWindDeg = (int) doubleWindDeg;
            String[] dname = {"北", "北北東", "北東", "東北東", "東", "東南東", "南東", "南南東", "南", "南南西", "南西", "西南西", "西", "西北西", "北西", "北北西", "北"};
            int dindex = (int) ((intWindDeg + 11.25) / 22.5);
            return dname[dindex];
        }


    //羽田のポリゴン
    private List<LatLng> create_haneda_Poligon(){
        return Arrays.asList(
                new LatLng(35.5475549643121,139.801632107064),
                new LatLng(35.5456939643119,139.803986107064),
                new LatLng(35.5386009643119,139.808950107064),
                new LatLng(35.5348949643121,139.811463107064),
                new LatLng(35.54243196431198726,139.82012010706412752),
                new LatLng(35.54356796431201815,139.82302610706418022),
                new LatLng(35.54129496431207258,139.82605210706427101),
                new LatLng(35.52024096431196654,139.80238210706423274),
                new LatLng(35.52263796431205378,139.7992941070641848),
                new LatLng(35.52449096431209341,139.80132210706429419),
                new LatLng(35.52576396431206263,139.79964210706421568),
                new LatLng(35.53196196431204612,139.79464710706409392),
                new LatLng(35.53420296431201564,139.78761210706412044),
                new LatLng(35.53577996431195629,139.78639810706422963),
                new LatLng(35.53521896431206528,139.78518510706422262),
                new LatLng(35.53533896431204653,139.78454810706412559),
                new LatLng(35.53886296431196001,139.78194810706409612),
                new LatLng(35.53685996431195804,139.77843210706410559),
                new LatLng(35.5411369643119599,139.76899210706417875),
                new LatLng(35.54131696431196019,139.76840410706418538),
                new LatLng(35.54130096431205743,139.76771710706421459),
                new LatLng(35.54136796431188827,139.76760710706415125),
                new LatLng(35.54149596431201985,139.76755810706416128),
                new LatLng(35.54179896431205066,139.76755510706405516),
                new LatLng(35.54231896431187465,139.76661410706415722),
                new LatLng(35.54271896431194477,139.76592710706418643),
                new LatLng(35.54451996431194516,139.76196110706419518),
                new LatLng(35.54542396431193652,139.75836310706418431),
                new LatLng(35.5457819643118853,139.75689710706424762),
                new LatLng(35.54598196431194879,139.75548710706405586),
                new LatLng(35.54618196431195543,139.75296810706416295),
                new LatLng(35.54666014275224484,139.75223032119245659),
                new LatLng(35.54728473922799026,139.75274070171715834),
                new LatLng(35.54770352436500502,139.7531023797901355),
                new LatLng(35.54827459500646114,139.75306430841399674),
                new LatLng(35.54951191472943606,139.75317852254229933),
                new LatLng(35.55006394968282279,139.75363537905539602),
                new LatLng(35.55103476977328114,139.75334984373469638),
                new LatLng(35.55554622784026719,139.75340695079890452),
                new LatLng(35.55998154315514626,139.75483462740228902),
                new LatLng(35.56140096431190045,139.75536610706413398),
                new LatLng(35.56196296431207315,139.75550610706423527),
                new LatLng(35.5627239643119708,139.75580010706406142),
                new LatLng(35.56430796431195063,139.75864410706415697),
                new LatLng(35.56307596431193474,139.76107810706426449),
                new LatLng(35.56054496431195844,139.76320710706423256),
                new LatLng(35.56039496431199609,139.76391810706425645),
                new LatLng(35.56885296431195798,139.77113510706422517),
                new LatLng(35.57043896431204644,139.78548010706415994),
                new LatLng(35.54755496431207007,139.80163210706399468)
        );
    }

    //成田のポリゴン
    private List<LatLng> create_narita_Poligon(){
        return Arrays.asList(
                new LatLng(35.737765,140.393058),
                new LatLng(35.740523,140.391),
                new LatLng(35.739424,140.388598),
                new LatLng(35.739453 ,140.387615),
                new LatLng(35.740195,140.386778),
                new LatLng(35.740936,140.386231),
                new LatLng(35.741589,140.385794),
                new LatLng(35.742568,140.385248),
                new LatLng(35.744792,140.384118),
                new LatLng(35.745208,140.383644),
                new LatLng(35.746305,140.382406),
                new LatLng(35.748707,140.380657),
                new LatLng(35.753171,140.377873),
                new LatLng(35.757146,140.375067),
                new LatLng(35.757635,140.374934),
                new LatLng(35.757962,140.375262),
                new LatLng(35.758466,140.374497),
                new LatLng(35.759207,140.373986),
                new LatLng(35.759534,140.374004),
                new LatLng(35.760528,140.373785),
                new LatLng(35.760691,140.373985),
                new LatLng(35.764047,140.371668),
                new LatLng(35.766208,140.370019),
                new LatLng(35.766136,140.369754),
                new LatLng(35.766736,140.369342),
                new LatLng(35.767159,140.369483),
                new LatLng(35.768583,140.368319),
                new LatLng(35.769948,140.367128),
                new LatLng(35.770616,140.366946),
                new LatLng(35.771476,140.366018),
                new LatLng(35.774933,140.363964),
                new LatLng(35.775585,140.363823),
                new LatLng(35.776282,140.365112),
                new LatLng(35.77701,140.364783),
                new LatLng(35.777707,140.36443),
                new LatLng(35.780011,140.362929),
                new LatLng(35.780395,140.362507),
                new LatLng(35.781704,140.36192),
                new LatLng(35.782215,140.361994),
                new LatLng(35.782904,140.363415),
                new LatLng(35.782589,140.364216),
                new LatLng(35.781276,140.365161),
                new LatLng(35.781024,140.36548),
                new LatLng(35.778209,140.367466),
                new LatLng(35.777921,140.367466),
                new LatLng(35.77773,140.366906),
                new LatLng(35.777108,140.36697),
                new LatLng(35.776485,140.367388),
                new LatLng(35.777166,140.3691),
                new LatLng(35.777107,140.369537),
                new LatLng(35.77681,140.369719),
                new LatLng(35.776676,140.37001),
                new LatLng(35.77632,140.370465),
                new LatLng(35.77586,140.370884),
                new LatLng(35.775252,140.371393),
                new LatLng(35.775433,140.371659),
                new LatLng(35.775463,140.371704),
                new LatLng(35.775409,140.371752),
                new LatLng(35.774131,140.372876),
                new LatLng(35.774131,140.373431),
                new LatLng(35.773716,140.37415),
                new LatLng(35.77419,140.374533),
                new LatLng(35.773789,140.374942),
                new LatLng(35.773404,140.375051),
                new LatLng(35.772707,140.374868),
                new LatLng(35.772336,140.37536),
                new LatLng(35.776018,140.379667),
                new LatLng(35.778604,140.382865),
                new LatLng(35.781488,140.386105),
                new LatLng(35.781701,140.385865),
                new LatLng(35.781838,140.385442),
                new LatLng(35.782045,140.385358),
                new LatLng(35.782252,140.385329),
                new LatLng(35.782436,140.385146),
                new LatLng(35.782711,140.385258),
                new LatLng(35.783177,140.386062),
                new LatLng(35.783569,140.387235),
                new LatLng(35.783327,140.387344),
                new LatLng(35.783097,140.387175),
                new LatLng(35.782614,140.387599),
                new LatLng(35.782534,140.387993),
                new LatLng(35.782987,140.388572),
                new LatLng(35.782928,140.388827),
                new LatLng(35.781222,140.388864),
                new LatLng(35.781207,140.389838),
                new LatLng(35.783878,140.389482),
                new LatLng(35.78413,140.389937),
                new LatLng(35.784353,140.389882),
                new LatLng(35.784783,140.389445),
                new LatLng(35.784708,140.389117),
                new LatLng(35.789522,140.387334),
                new LatLng(35.789893,140.387124),
                new LatLng(35.790071,140.386851),
                new LatLng(35.7901,140.386614),
                new LatLng(35.790055,140.385594),
                new LatLng(35.796388,140.3808),
                new LatLng(35.796625,140.380571),
                new LatLng(35.7972,140.38036),
                new LatLng(35.79818,140.379691),
                new LatLng(35.798215,140.379396),
                new LatLng(35.798163,140.378742),
                new LatLng(35.798285,140.378363),
                new LatLng(35.799208,140.377667),
                new LatLng(35.80109,140.376381),
                new LatLng(35.80123,140.375959),
                new LatLng(35.801896,140.376336),
                new LatLng(35.802411,140.377053),
                new LatLng(35.803756,140.375664),
                new LatLng(35.805211,140.374157),
                new LatLng(35.805543,140.373904),
                new LatLng(35.806449,140.375211),
                new LatLng(35.80685,140.375295),
                new LatLng(35.807128,140.375422),
                new LatLng(35.807494,140.375906),
                new LatLng(35.808209,140.377804),
                new LatLng(35.807843,140.37831),
                new LatLng(35.807512,140.378521),
                new LatLng(35.807808,140.379259),
                new LatLng(35.807355,140.379575),
                new LatLng(35.807494,140.380397),
                new LatLng(35.807895,140.381177),
                new LatLng(35.806693,140.381999),
                new LatLng(35.805943,140.382358),
                new LatLng(35.804532,140.38278),
                new LatLng(35.804863,140.38474),
                new LatLng(35.803713,140.384614),
                new LatLng(35.80204,140.385415),
                new LatLng(35.803185,140.386979),
                new LatLng(35.801257,140.388173),
                new LatLng(35.80053,140.388975),
                new LatLng(35.800056,140.389339),
                new LatLng(35.797979,140.390179),
                new LatLng(35.797141,140.39022),
                new LatLng(35.797178,140.389947),
                new LatLng(35.797326,140.389865),
                new LatLng(35.797208,140.389778),
                new LatLng(35.796762,140.389523),
                new LatLng(35.796559,140.390203),
                new LatLng(35.795368,140.390235),
                new LatLng(35.794879,140.390735),
                new LatLng(35.791723,140.395786),
                new LatLng(35.787537,140.395903),
                new LatLng(35.786647,140.395867),
                new LatLng(35.786284,140.395835),
                new LatLng(35.786076,140.395781),
                new LatLng(35.785957,140.395308),
                new LatLng(35.784978,140.395308),
                new LatLng(35.785022,140.394789),
                new LatLng(35.7852,140.394862),
                new LatLng(35.785556,140.394497),
                new LatLng(35.785215,140.394753),
                new LatLng(35.785052,140.394698),
                new LatLng(35.78514,140.39376),
                new LatLng(35.785066,140.393341),
                new LatLng(35.784948,140.392724),
                new LatLng(35.784579,140.39199),
                new LatLng(35.782647,140.392575),
                new LatLng(35.782064,140.392105),
                new LatLng(35.781174,140.392162),
                new LatLng(35.780752,140.391748),
                new LatLng(35.780308,140.391805),
                new LatLng(35.779748,140.391598),
                new LatLng(35.779089,140.393726),
                new LatLng(35.779089,140.394535),
                new LatLng(35.779657,140.395928),
                new LatLng(35.77968,140.395864),
                new LatLng(35.782584,140.394743),
                new LatLng(35.782753,140.395421),
                new LatLng(35.781576,140.395893),
                new LatLng(35.781456,140.395421),
                new LatLng(35.780952,140.39551),
                new LatLng(35.780761,140.396748),
                new LatLng(35.783209,140.395538),
                new LatLng(35.782969,140.394536),
                new LatLng(35.783497,140.394212),
                new LatLng(35.783641,140.394654),
                new LatLng(35.783161,140.39486),
                new LatLng(35.783425,140.395833),
                new LatLng(35.780376,140.397161),
                new LatLng(35.780257,140.39778),
                new LatLng(35.779753,140.398104),
                new LatLng(35.77944,140.397309),
                new LatLng(35.775547,140.400044),
                new LatLng(35.774732,140.40252),
                new LatLng(35.770457,140.400282),
                new LatLng(35.769833,140.4004),
                new LatLng(35.766022,140.397285),
                new LatLng(35.765704,140.39727),
                new LatLng(35.765485,140.397259),
                new LatLng(35.764743,140.39778),
                new LatLng(35.762566,140.396459),
                new LatLng(35.761692,140.393997),
                new LatLng(35.761737,140.393461),
                new LatLng(35.76091,140.392644),
                new LatLng(35.757317,140.394905),
                new LatLng(35.754802,140.396396),
                new LatLng(35.753221,140.400608),
                new LatLng(35.753005,140.400873),
                new LatLng(35.752369,140.4008),
                new LatLng(35.751072,140.400447),
                new LatLng(35.75011,140.400054),
                new LatLng(35.748948,140.400669),
                new LatLng(35.746176,140.402731),
                new LatLng(35.744974,140.403532),
                new LatLng(35.743197,140.403179),
                new LatLng(35.742845,140.403822),
                new LatLng(35.742798,140.403875),
                new LatLng(35.742657,140.403857),
                new LatLng(35.742605,140.40378),
                new LatLng(35.74259,140.403125),
                new LatLng(35.741884,140.402762),
                new LatLng(35.74156,140.4026),
                new LatLng(35.740834,140.401879),
                new LatLng(35.741626,140.400922),
                new LatLng(35.74195,140.400539),
                new LatLng(35.742262,140.399981),
                new LatLng(35.742337,140.399744),
                new LatLng(35.742366,140.399344),
                new LatLng(35.742307,140.399052),
                new LatLng(35.742188,140.39877),
                new LatLng(35.741802,140.398143),
                new LatLng(35.742283,140.397642),
                new LatLng(35.742024,140.397078),
                new LatLng(35.742083,140.39675),
                new LatLng(35.742929,140.396231),
                new LatLng(35.741325,140.392847),
                new LatLng(35.738537,140.394823),
                new LatLng(35.737765,140.393058)
        );
    }

}









