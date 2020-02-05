package com.example;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.vidyo.VidyoClient.Connector.Connector;
import com.vidyo.VidyoClient.Connector.ConnectorPkg;
import com.vidyo.VidyoClient.Endpoint.ChatMessage;
import com.vidyo.VidyoClient.Endpoint.Participant;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Connector.IConnect,
        Connector.IRegisterMessageEventListener {

    // TODO: every month, create a new vidyo.io free account using an email generator
    // email generator = https://temp-mail.org/en/
    // account info for when signing into developer.vidyo.io
    // private String accUsername = "lasef58547@tmailer.org";
    // private String accPassword = "McgillECSE457";
    // private String accExpiry = Jan 17th, 2020

    // TODO: find the libs directory with the generateToken.jar, and run the following in terminal
    // java -jar generateToken.jar --key=1eed540536314010abc543fc1508cd2d --appID=d4ed93.vidyo.io --userName=test --expiresInSecs=999999
    // Note: change values used if key, appID, etc. change when creating a new account
    // Dev info for generating a new token:
    // private String devKey = "1eed540536314010abc543fc1508cd2d";
    // private String appID = "d4ed93.vidyo.io";
    // private String userName = "userName";
    // private String expiresInSecs = "99999";
    // private long expiresInSecsLong = 99999;
    // private String vCard = "";
    public String authToken = "cHJvdmlzaW9uAHRlc3RAZDRlZDkzLnZpZHlvLmlvADYzNzQ0OTA4NDU5AAAzZWUzYTI5NzFhODMxZmNiYzQ0OGQwZWE2NDFiNjA3MzkyZmYxZjM0NWEwN2E1ZjRkNzY3NmI4MjQxMTNhMWI2NWNjMmFiYmFjMzM1ZmI2OWYxZGIyMWU1NDU5NGQ2MzM=";

    // TODO: remote debugging of the app when connected to the arduino
    // terminal: cd C:\Users\Mitchell\AppData\Local\Android\Sdk\platform-tools
    // terminal: adb connect 10.122.104.129 (tablet's ip address)
    // terminal: adb devices (to check if device connected properly)

    // permission requests
    public final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    public final int MY_PERMISSIONS_REQUEST = 102;
    public final String ACTION_USB_PERMISSION = "com.example.USB_PERMISSION";

    // Buttons
    private Button startAsUser, startAsRobot;
    private ImageButton leftUp, leftLeft, leftDown;
    private ImageButton rightUp, rightRight, rightDown;
    private TextView textView;

    // Start control boolean
    private boolean started = false;

    // Video elements
    private Connector vc;
    private FrameLayout videoFrame;

    // USB Serial Interface
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    PendingIntent usbPermissionIntent;

    // Event Listener
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            TextView textView = findViewById(R.id.textView);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                vc.sendChatMessage("x");
                runOnUiThread(() -> {
                    textView.setText("x");
                    if (serialPort != null) serialPort.write("x".getBytes());
                });
                view.performClick();
                // no need to listen for subsequent actions
                return false;
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                System.out.println("button pressed");
                switch (view.getId()) {
                    case R.id.LeftUpArrow:
                    case R.id.RightUpArrow:
                        vc.sendChatMessage("w");
                        runOnUiThread(() -> {
                            textView.setText("w");
                            if (serialPort != null) serialPort.write("w".getBytes());
                        });
                        break;
                    case R.id.LeftDownArrow:
                    case R.id.RightDownArrow:
                        vc.sendChatMessage("s");
                        runOnUiThread(() -> {
                            textView.setText("s");
                            if (serialPort != null) serialPort.write("s".getBytes());
                        });
                        break;
                    case R.id.LeftLeftArrow:
                        vc.sendChatMessage("a");
                        runOnUiThread(() -> {
                            textView.setText("a");
                            if (serialPort != null) serialPort.write("a".getBytes());
                        });
                        break;
                    case R.id.RightRightArrow:
                        vc.sendChatMessage("d");
                        runOnUiThread(() -> {
                            textView.setText("d");
                            if (serialPort != null) serialPort.write("d".getBytes());
                        });
                        break;
                }
                // listen for the subsequent up-action
                return true;
            }
            // no need to listen for subsequent actions
            return false;
        }
    };

    // Defining a Callback which triggers whenever data is read.
    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = new String(arg0, StandardCharsets.UTF_8);
            data = data.concat("/n");
            tvAppend(textView, data);
        }
    };

    // Broadcast Receiver to automatically start and stop the Serial connection.
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                    if (intent.getExtras() != null) {
                        boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                        if (granted) {
                            connection = usbManager.openDevice(device);
                            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                            if (serialPort != null) {
                                if (serialPort.open()) { //Set Serial Connection Parameters.
                                    serialPort.setBaudRate(9600);
                                    serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                                    serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                                    serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                                    serialPort.read(mCallback);
                                    tvAppend(textView, "Serial Connection Opened!\n");
                                } else {
                                    Log.d("SERIAL", "PORT NOT OPEN");
                                }
                            } else {
                                Log.d("SERIAL", "PORT IS NULL");
                            }
                        } else {
                            Log.d("SERIAL", "PERM NOT GRANTED");
                        }
                    }
                } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                    startUsbConnection();
                } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                    onClickStop(leftLeft);

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // keeps screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // button init
        startAsUser = findViewById(R.id.StartUser);
        startAsRobot = findViewById(R.id.StartRobot);
        leftUp = findViewById(R.id.LeftUpArrow);
        leftLeft = findViewById(R.id.LeftLeftArrow);
        leftDown = findViewById(R.id.LeftDownArrow);
        rightUp = findViewById(R.id.RightUpArrow);
        rightRight = findViewById(R.id.RightRightArrow);
        rightDown = findViewById(R.id.RightDownArrow);

        // enable the textView for debugging
        textView = findViewById(R.id.textView);
        enableDebugTextView(false);

        // usb init
        usbManager = (UsbManager) getSystemService(MainActivity.USB_SERVICE);
        usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
        startUsbConnection();

        // ask for permissions
        askForPermissions();

        // vidyo.io init
        ConnectorPkg.setApplicationUIContext(this);
        ConnectorPkg.initialize();
        videoFrame = findViewById(com.example.R.id.videoFrame);
    }

    public void startUsbConnection() {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2341)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }
    }

    public void askForPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST);
        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }

        // ask for usb permission
        if (device != null) {
            usbManager.requestPermission(device, usbPermissionIntent);
        }
    }

    public void StartAsUser(View view) {
        Start(view);
        startAsUser.setVisibility(View.GONE);
        startAsRobot.setVisibility(View.GONE);
    }

    public void StartAsRobot(View view) {
        Start(view);
        startAsUser.setVisibility(View.GONE);
        startAsRobot.setVisibility(View.GONE);
        leftUp.setVisibility(View.GONE);
        leftLeft.setVisibility(View.GONE);
        leftDown.setVisibility(View.GONE);
        rightUp.setVisibility(View.GONE);
        rightRight.setVisibility(View.GONE);
        rightDown.setVisibility(View.GONE);
    }

    public void Start(View view) {
        if (!started) {
            started = true;
            vc = new Connector(videoFrame, Connector.ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default, 15, "warning info@VidyoClient info@VidyoConnector", "", 0);
            vc.showViewAt(videoFrame, 0, 0, videoFrame.getWidth(), videoFrame.getHeight());
            Connect(view);
        }
    }

    public void enableDebugTextView(boolean visible) {
        if (visible) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    public void onClickStop(View view) {
        serialPort.close();
        tvAppend(textView,"\nSerial Connection Closed! \n");
    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(() -> ftv.append(ftext));
    }

    @SuppressLint("ClickableViewAccessibility")
    public void Connect(View view) {
        vc.connect("prod.vidyo.io", authToken, "User", "Room", this);
        vc.registerMessageEventListener(this);
        //vc.registerMessageEventListener(registerMessageEventListener);

        // button listeners
        leftUp.setOnTouchListener(touchListener);
        leftLeft.setOnTouchListener(touchListener);
        leftDown.setOnTouchListener(touchListener);
        rightUp.setOnTouchListener(touchListener);
        rightRight.setOnTouchListener(touchListener);
        rightDown.setOnTouchListener(touchListener);
    }

    public void onChatMessageReceived(Participant participant, ChatMessage chatMessage) {
        TextView textView = findViewById(R.id.textView);
        runOnUiThread(() -> {
            String tmp = "Received: " + chatMessage.body;
            textView.setText(tmp);
            if (serialPort != null) serialPort.write(chatMessage.body.getBytes());
        });

    }

    @Override
    public void finish() {
        vc.disconnect();
        super.finish();
    }

    public void onSuccess() {}

    public void onFailure(Connector.ConnectorFailReason reason) {}

    public void onDisconnected(Connector.ConnectorDisconnectReason reason) {}
}
