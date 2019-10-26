package com.example.gpio_led;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import android.os.Handler;

// import com.example.gpio_led.IDemoService;
import android.util.Log;
import android.os.IBinder;
import java.lang.reflect.*;
import java.lang.ref.WeakReference;
import android.os.Message;
import android.os.RemoteException;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    //static {
    //    System.loadLibrary("native-lib");
    //}

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();
//    public native String helloFromJNI();
//    public native String ledON();
//    public native String ledOFF();

    private IDemoService mService;
    private static final String TAG = "HelloActivity IDemoService";
    private InternalHandler handler;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Example of a call to a native method
        tv = findViewById(R.id.txt_Hello);
        tv.setText("Hello GPIO");
        //tv.setText(helloFromJNI());
        getDemoAPIService();
        handler = new InternalHandler(tv);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public  void buttonOnclick(View v) {
        Button btn = (Button) v;
        //((Button) v).setText("Clicked");
        //((Button) v).setText(ledON());
        //ledON();
        try {
            mService.LedOn();
        }catch(Exception e){
            Log.d(TAG,e.toString());
        }
    }

    public void btn_ledOff_Click(View v) {
        Button btn = (Button) v;
        //((Button) v).setText("Clicked");
        //((Button) v).setText(ledOFF());
        //ledOFF();
        try {
            mService.LedOff();
        }catch(Exception e){
            Log.d(TAG,e.toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static final String SERVICE_NAME="com.example.gpio_led";

    /*
     * Get binder service
     */
    private void getDemoAPIService()
    {
        IBinder binder=null;
        Log.d(TAG,"getDemoAPIService");
        try{
            //android.os.ServiceManager is hide class, we can not invoke them from SDK. So we have to use reflect to invoke these classes.
            Object object = new Object();
            Method getService = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            Object obj = getService.invoke(object, new Object[]{new String(SERVICE_NAME)});
            binder = (IBinder)obj;
        }catch(Exception e){
            Log.d(TAG, e.toString());
        }
        if(binder != null){
            mService = IDemoService.Stub.asInterface(binder);
            Log.d(TAG, "Find binder");
            tv.setText("Connected to LED Service");
            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                mService.regist(mCallback);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
                Log.d(TAG,"Failed to register callback.");
            }
        }
        else
            Log.d(TAG,"Service is null.");
    }

    // ----------------------------------------------------------------------
    // Code showing how to deal with callbacks.
    // ----------------------------------------------------------------------

    /**
     * This implementation is used to receive callbacks from the remote
     * service.
     */
    private ICallback mCallback = new ICallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */
        public void dataCallback(String value) {
            handler.sendMessage(handler.obtainMessage(BUMP_SW_MSG, 1, 0));
            Log.d(TAG,"From ICallback: " + value);
        }
    };

    private static final int BUMP_SW_MSG = 1;

    private static class InternalHandler extends Handler {
        private final WeakReference<TextView> weakTextView;
        private int cntCb = 0;

        InternalHandler(TextView textView) {
            weakTextView = new WeakReference<>(textView);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BUMP_SW_MSG:
                    TextView textView = weakTextView.get();
                    if (textView != null) {
                        cntCb += msg.arg1;
                        textView.setText("Received from service: " + cntCb);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
