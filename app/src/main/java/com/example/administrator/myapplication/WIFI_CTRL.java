package com.example.administrator.myapplication;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


public class WIFI_CTRL extends AppCompatActivity implements OnClickListener,ReceiveTCPServer {
    Button mButtonConnectTcpServer;
    Button mButtonConnectWifi;
    EditText mEditTextServerIp;
    EditText mEditTextServerCom;
    EditText mEditTextWifiSSID;
    EditText mEditTextWifiPWD;
    TextView mTextviewSwitch1;
    TextView mTextviewSwitch2;
    TextView mTextviewSwitch3;
    String mServerIpAddr;
    String TAG = "zougui";
    Switch mSwitch1;
    Switch mSwitch2;
    Switch mSwitch3;
    TCPClient mTCPClient;

    private String CONNECT_SUCCESS = "connect_success";
    private String CONNECT_FAIL = "connect_fail";
    private String SERVER_DISCONNECT="server_disconnect";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi__ctrl);
        mButtonConnectTcpServer = (Button)findViewById(R.id.btn_connect_tcp_server);
        mButtonConnectWifi = (Button)findViewById(R.id.btn_connect_wifi);
        mEditTextServerIp = (EditText)findViewById(R.id.edit_tcp_server_ip);
        mEditTextServerCom = (EditText)findViewById(R.id.edit_tcp_server_com);
        mEditTextWifiSSID = (EditText)findViewById(R.id.edit_neeed_connect_wifi_ssid);
        mEditTextWifiPWD = (EditText)findViewById(R.id.edit_neeed_connect_wifi_pwd);
        mSwitch1 = (Switch)findViewById(R.id.switch_1);
        mSwitch2 = (Switch)findViewById(R.id.switch_2);
        mSwitch3 = (Switch)findViewById(R.id.switch_3);

        mTextviewSwitch1 = (TextView)findViewById(R.id.textview_switch1_status);
        mTextviewSwitch2 = (TextView)findViewById(R.id.textview_switch2_status);
        mTextviewSwitch3 = (TextView)findViewById(R.id.textview_switch3_status);


        mServerIpAddr = getWifiIpAddr(this);
        mEditTextServerIp.setText(mServerIpAddr);
        mEditTextServerCom.setText("8080");

        mButtonConnectTcpServer.setOnClickListener(this);
        mButtonConnectWifi.setOnClickListener(this);
        mSwitch1.setOnClickListener(this);
        mSwitch2.setOnClickListener(this);
        mSwitch3.setOnClickListener(this);
        mButtonConnectWifi.setEnabled(false);
    }

    public static String getWifiIpAddr(Context context)
    {
        NetworkInfo info = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if(info== null || info.getType() != ConnectivityManager.TYPE_WIFI){
            Toast.makeText(context,R.string.wifi_connect_check_error,Toast.LENGTH_LONG).show();
            return null;
        }else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
          //  String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
            return getTcpServerAddrbyClientAddr(wifiInfo.getIpAddress());
        }
        return null;
    }


    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    //获得TCP server 地址
    public static String getTcpServerAddrbyClientAddr(int ip)
    {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                "1";
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_connect_tcp_server:
                connectTPCServer(mEditTextServerIp.getText().toString(),Integer.parseInt(mEditTextServerCom.getText().toString()));
                break;
            case R.id.btn_connect_wifi:
                mTCPClient.Send2Server("SSID:"+mEditTextWifiSSID.getText());
                mTCPClient.Send2Server("PWD:"+mEditTextWifiPWD.getText());
                break;
            case R.id.switch_1:
            case R.id.switch_2:// 按键开关控制
            case R.id.switch_3:
                mTCPClient.Send2Server("switch:"+mSwitch1.isChecked()+"-"+mSwitch2.isChecked()+"-"+mSwitch3.isChecked());
                break;
        }
    }

    private void connectTPCServer(String addr, int com) {
        mTCPClient = new TCPClient(addr,com);
        mTCPClient.setTCPClientmpl(this);
    }




    @Override
    public void RecevieDateFormServer(String str) {
        //“switch,on,off,on”
        if(str == null){
            str=SERVER_DISCONNECT;
        }
        String[] strs=str.split(",");
        Log.d("zougui",strs[0]);
        if(strs[0].equals("switch")){
            mTextviewSwitch1.setText(strs[1]);
            mTextviewSwitch2.setText(strs[2]);
            mTextviewSwitch3.setText(strs[3]);
        }





        //connect success
        if(str.equals(CONNECT_SUCCESS)){
            mButtonConnectTcpServer.setEnabled(false);
            Toast.makeText(this,getString(R.string.connect_success),Toast.LENGTH_SHORT).show();
            mButtonConnectWifi.setEnabled(true);
        }

        //connect fail
        if(str.equals(CONNECT_FAIL)){
            mButtonConnectTcpServer.setEnabled(true);
            Toast.makeText(this,getString(R.string.connect_fail),Toast.LENGTH_SHORT).show();
            mButtonConnectWifi.setEnabled(false);
        }

       //disconnect server
        if(str.equals(SERVER_DISCONNECT)){
            Log.d(TAG,"server disconnect...");
            Toast.makeText(this,getString(R.string.disconnect),Toast.LENGTH_SHORT).show();
            mButtonConnectWifi.setEnabled(false);
            mButtonConnectTcpServer.setEnabled(true);
        }
    }

}


