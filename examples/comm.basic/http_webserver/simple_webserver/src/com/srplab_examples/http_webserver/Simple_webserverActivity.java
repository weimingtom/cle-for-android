package com.srplab_examples.http_webserver;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import java.util.Timer;  
import java.util.TimerTask; 
import android.os.Handler;  
import android.os.Message;

import com.srplab.www.starcore.*;

class WebServer_CommInterface extends StarCommInterfaceClass{
	private StarBinBufClass BinBuf;	
	private StarSrvGroupClass SrvGroup;
    public void _MsgProc(int uMes,Object[] Msg){
        if(uMes == _Getint("HTTP_ONREQUEST") ){
            if( _Toint(Msg[2]) == _Getint("HTTPREQUEST_GET") ){
            	String a = _FormatRspHeader("200 OK","","","",0);
                BinBuf._Clear();
                BinBuf._Set(0,0,"S",a);
                super._HttpSend(_Toint(Msg[0]),BinBuf,0,true);
                BinBuf._Clear();
                BinBuf._Set(0,0,"S","test response data");
                super._HttpSend(((Integer)Msg[0]).intValue(),BinBuf,0,false);
             }else if(_Toint(Msg[2]) == _Getint("HTTPREQUEST_POST") ){
                if(_Toint(Msg[3])!= 0){
                    Object[] Info=_HttpGetMultiPart((StarBinBufClass)Msg[11],0,_Toint(Msg[3]),(StarBinBufClass)Msg[9]); //PartLength,PartOffset,PartHeader
                    String FileName = _HttpGetNVValue( _HttpGetHeaderItem((String)Info[2],0,"Content-Disposition:"),"filename");
                    StarBinBufClass a = (StarBinBufClass)((StarBinBufClass)Msg[11])._Get(_Toint(Info[1]),_Toint(Info[0]),"r");
                    a._SaveToFile(FileName,false);
                    String b = _FormatRspHeader("200 OK","","Close","",0);
                    BinBuf._Clear();
                    BinBuf._Set(0,0,"S",b);
                    _HttpSend(_Toint(Msg[0]),BinBuf,0,false);
                }
            }
        }
    }
	public WebServer_CommInterface(StarSrvGroupClass In_SrvGroup,StarCommInterfaceClass srcobj){
		super(srcobj);
		SrvGroup = In_SrvGroup;
		BinBuf = SrvGroup._NewBinBuf();
	}    
}

public class Simple_webserverActivity extends Activity {
    /** Called when the activity is first created. */

	StarCoreFactory starcore;
	StarSrvGroupClass SrvGroup;
	
	Timer timer = new Timer();  
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		starcore= StarCoreFactory.GetFactory();
		StarServiceClass Service=starcore._InitSimple("test","123",0,0);
		SrvGroup = (StarSrvGroupClass)Service._Get("_ServiceGroup");
		WebServer_CommInterface CommInterface = new WebServer_CommInterface(SrvGroup,SrvGroup._NewCommInterface());

        int ConnectionID = CommInterface._HttpServer("",3040,100);
        SrvGroup._Print( "create webserver " + 3040 + "  success, id = "+ConnectionID);      
        
        final Handler handler = new Handler()  
        {  
            @Override  
            public void handleMessage(Message msg)  
            {  
                while( starcore._SRPDispatch(false) == true );
            }
        };
        timer.scheduleAtFixedRate(new TimerTask()  
        {  
            @Override  
            public void run()  
            {  
                Message mesasge = new Message();  
                mesasge.what = 0;  
                handler.sendMessage(mesasge);  
            }  
        }, 0, 10);         
    }
    
    @Override  
    protected void onDestroy()  
    {  
        // TODO Auto-generated method stub   
   	    SrvGroup._ClearService();
        starcore._ModuleExit();
        super.onDestroy();  
    }      
}