package com.example.administrator.myapplication;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2017/12/15 0015.
 */

public class TCPClient implements Runnable
{
    private Socket mSocket = null;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String content = "";
    private String mSendStr;
    private String mTPCServeraddr;
    private int mServerCom;
    public ReceiveTCPServer mReceiveTCPServer;

    private Lock mLock;


    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("zougui",content);
            mReceiveTCPServer.RecevieDateFormServer(content);//接收到数据
        }
    };


    public TCPClient(String host, int port)
    {
        mTPCServeraddr = host;
        mServerCom = port;
        mLock = new ReentrantLock();
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {

            mSocket = new Socket(mTPCServeraddr, mServerCom);
            in = new BufferedReader(new InputStreamReader(mSocket
                    .getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    mSocket.getOutputStream())), true);

            while (true) {
                if (!mSocket.isClosed()) {
                    if (mSocket.isConnected()) {
                       //接收
                        if (!mSocket.isInputShutdown()) {
                            if ((content  = in.readLine()) != null) {
                                content  += "\n";
                                mHandler.sendMessage(mHandler.obtainMessage());
                            } else {

                            }
                        }
                        /*
                        //发送
                        if(!mSocket.isClosed()&& mSocket.isConnected()){
                            if(!mSocket.isOutputShutdown()&& !mSendStr.isEmpty() ){
                                out.println(mSendStr);
                            }
                        }
                        */
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTCPClientmpl(ReceiveTCPServer l)
    {
        mReceiveTCPServer = l;
    }

    public void Send2Server(final String str)
    {

        new Thread(new Runnable() {
            @Override
            public void run() {
                mLock.lock();
                mSendStr = str;
                if(mSocket==null){
                }else
                if(mSocket.isConnected()){
                    if(!mSocket.isOutputShutdown()&& !mSendStr.isEmpty() ){
                        out.println(mSendStr);
                        mLock.unlock();
                    }
                }
            }
        }).start();
    }
}
