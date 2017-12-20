package com.example.administrator.myapplication;


import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2017/12/15 0015.
 */

public class TCPClient
{
    private Socket mSocket = null;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String content = "";
    private String mSendStr;
    private String mTPCServeraddr;
    private int mServerCom;
    private ReceiveTCPServer mReceiveTCPServer;
    private Date mLastKeepAliveOkTime;

    private String CONNECT_SUCCESS = "connect_success";
    private String CONNECT_FAIL = "connect_fail";
    private String SERVER_DISCONNECT="server_disconnect";


    private Lock mLock;


    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mReceiveTCPServer.RecevieDateFormServer(content);//接收到数据
        }
    };

    private Runnable mReceiveRunable = new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    if (!mSocket.isClosed()) {
                        if (mSocket.isConnected()) {
                            //接收
                            if (!mSocket.isInputShutdown()) {
                                if ((content  = in.readLine()) != null) {
                                    content  += "\n";
                                    mHandler.sendMessage(mHandler.obtainMessage());
                                    //记录最后一次接收到数据的时间
                                    mLastKeepAliveOkTime = Calendar.getInstance().getTime();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                checkIOclosed();
            }
        }
    };


    public TCPClient(String host, int port)
    {
        mTPCServeraddr = host;
        mServerCom = port;
        mLastKeepAliveOkTime = Calendar.getInstance().getTime();

        if(mLock==null)
            mLock = new ReentrantLock();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mSocket == null) {
                        checkIOclosed();
                        mSocket = new Socket(mTPCServeraddr, mServerCom);
                        in = new BufferedReader(new InputStreamReader(mSocket
                                .getInputStream()));
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                                mSocket.getOutputStream())), true);
                        new Thread(mReceiveRunable).start();
                        content=CONNECT_SUCCESS;
                        mHandler.sendMessage(mHandler.obtainMessage());
                        TCPClient.this.KeepAlive();
                    }
                }catch (IOException e){
                    content=CONNECT_FAIL;
                    mHandler.sendMessage(mHandler.obtainMessage());
                }
            }
        }).start();
    }

    private void checkIOclosed()
    {
        if(out != null || in != null){
            out.close();
            try {
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            mSocket=null;
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
                if (mSocket != null) {
                    if (mSocket.isConnected()) {
                        if (!mSocket.isOutputShutdown() && !mSendStr.isEmpty()) {
                            out.println(mSendStr);
                        }
                    }
                }
                mLock.unlock();
            }
        }).start();
    }

    private void KeepAlive()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(1000);
                        Log.d("zougui","mLastKeepAliveOkTime :"+mLastKeepAliveOkTime);
                        Date now = Calendar.getInstance().getTime();
                        long between = now.getTime()-mLastKeepAliveOkTime.getTime();
                        if(between>=2*1000 && between<=5*1000){
                            Send2Server("check_connect_status");
                        }else if(between >5*1000){
                            content=SERVER_DISCONNECT;
                            mHandler.sendMessage(mHandler.obtainMessage());
                            if(mSocket!=null)
                                mSocket.close();
                            mSocket=null;
                            checkIOclosed();
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
