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

import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class WIFI_CTRL extends AppCompatActivity implements OnClickListener,ReceiveTCPServer {
    Button mButtonConnectTcpServer;
    Button mButtonConnectWifi;
    EditText mEditTextServerIp;
    EditText mEditTextServerCom;
    EditText mEditTextWifiSSID;
    EditText mEditTextWifiPWD;
    ToggleButton mbtn_machine_run_dir;
    ToggleButton mbtn_machine_power;

    String mServerIpAddr;
    String TAG = "zougui";

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

        mbtn_machine_run_dir = (ToggleButton)findViewById(R.id.btn_machine_run_direction);
        mbtn_machine_power = (ToggleButton)findViewById(R.id.btn_machine_power);
        mbtn_machine_power.setOnClickListener(this);
        mbtn_machine_run_dir.setOnClickListener(this);




        mServerIpAddr = getWifiIpAddr(this);
        mEditTextServerIp.setText(mServerIpAddr);
        mEditTextServerCom.setText("8080");

        mButtonConnectTcpServer.setOnClickListener(this);
        mButtonConnectWifi.setOnClickListener(this);

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
                if(mEditTextWifiSSID.getText().toString().isEmpty()==true){
                    Toast.makeText(this,getString(R.string.Waring_WIFI_String_empty),Toast.LENGTH_LONG).show();
                    break;
                }
                if(mEditTextWifiSSID.getText().toString().indexOf(" ") != -1 || mEditTextWifiPWD.getText().toString().indexOf(" ") != -1) {
                    Toast.makeText(this,getString(R.string.Waring_String_Illegal),Toast.LENGTH_LONG).show();
                    break;
                }

                SensorDateToServer("W:"+mEditTextWifiSSID.getText()+" P:"+mEditTextWifiPWD.getText()+" ;;\n");

                break;
            case R.id.btn_machine_power:
                if(mbtn_machine_power.isChecked())
                    SensorDateToServer("P:1\n");
                else
                    SensorDateToServer("P:0\n");
                break;
            case R.id.btn_machine_run_direction:
                if(mbtn_machine_run_dir.isChecked())
                    SensorDateToServer("O:1\n");
                else
                    SensorDateToServer("O:0\n");
                break;
        }
    }

    private void connectTPCServer(String addr, int com) {
        mTCPClient = new TCPClient(addr,com);
        mTCPClient.setTCPClientmpl(this);
    }


    private void SensorDateToServer(String str){
        if(mTCPClient!=null && str!=null){
            mTCPClient.Send2Server(str);
        }
    }

    @Override
    public void RecevieDateFormServer(String str) {
        //“switch,on,off,on”
        if(str == null){
            str=SERVER_DISCONNECT;
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


