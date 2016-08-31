package com.fahim.lokman.bluetoothchatapp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fahim.lokman.bluetoothchatapp.Utils.FilePicker;
import com.fahim.lokman.bluetoothchatapp.adapter.MessageAdapter;
import com.fahim.lokman.bluetoothchatapp.btxfr.ClientThread;
import com.fahim.lokman.bluetoothchatapp.btxfr.MessageType;
import com.fahim.lokman.bluetoothchatapp.btxfr.ProgressData;
import com.fahim.lokman.bluetoothchatapp.btxfr.ServerThread;
import com.fahim.lokman.bluetoothchatapp.contents.Constant;
import com.fahim.lokman.bluetoothchatapp.contents.MessageContents;
import com.fahim.lokman.bluetoothchatapp.dbHelper.DBHandler;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import nl.changer.polypicker.Config;
import nl.changer.polypicker.ImagePickerActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothChatFragment";
    private static final int INTENT_REQUEST_GET_N_IMAGES = 14;
    private static final int REQUEST_PICK_FILE = 10;
    //    private static final String TAG = "BTPHOTO/MainActivity";
    private Spinner deviceSpinner;
    private ProgressDialog progressDialog;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    public static final String VOICE = "ITISAVOICEMESSAGE";
    public static final String PHOTO = "ITISAPHOTOMESSAGE";
    public static final String FILE = "ITISAFILEMESSAGE";
    public static String EXTENSION;
    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    public static ImageButton mSendButton;
    public static RelativeLayout statusBar;
    private ImageButton addImage, addFile;
    public static ImageButton recordingButton;
    public static TextView stateView, tryAgainView;
    public static SeekBar seekbar;
    public static ProgressBar pb;
    public static String CONNECTED_DEVICE;
    public static String CONNECTED_DEVICE_ADDRESS;
    public static String lastSentImage = "";
    public static String FLAG;

    /**
     * Name of the connected device
     */
    private static String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private static ArrayAdapter<String> mConversationArrayAdapter;
    private static MessageAdapter messageAdapter;
    public static ArrayList<MessageContents> messageContentses = new ArrayList<>();
    public static DBHandler dbHandler;
    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    /**
     * String buffer for outgoing messageContentses
     */
    private StringBuffer mOutStringBuffer;

    public static VoiceMessage voiceMessage;
    public static Animation show, hide;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String address = getIntent().getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        CONNECTED_DEVICE = device.getName();
        CONNECTED_DEVICE_ADDRESS = address;
        getSupportActionBar().setTitle(CONNECTED_DEVICE);


        dbHandler = new DBHandler(this);
        voiceMessage = new VoiceMessage();
        show = AnimationUtils.loadAnimation(this, R.anim.show);
        hide = AnimationUtils.loadAnimation(this, R.anim.hide);

        addImage = (ImageButton) findViewById(R.id.addImage);
        addFile = (ImageButton) findViewById(R.id.addFile);
        mConversationView = (ListView) findViewById(R.id.in);
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mSendButton = (ImageButton) findViewById(R.id.button_send);
        recordingButton = (ImageButton) findViewById(R.id.record);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        statusBar = (RelativeLayout) findViewById(R.id.statusBar);

        stateView = (TextView) findViewById(R.id.stateView);
        tryAgainView = (TextView) findViewById(R.id.tryAgain);
        pb = (ProgressBar) findViewById(R.id.progressBar);

        statusBar.setAnimation(show);
        show.start();

        setupChat();
        setInvisible(tryAgainView);
        setInvisible(mSendButton);
        setInvisible(seekbar);

        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startClientThread();

                if (voiceMessage.mediaRecorder == null) {
                    voiceMessage.initializeMediaRecord();
                }
                lastSentImage = voiceMessage.getVoiceStoragePath();
                voiceMessage.startAudioRecording();
                setInvisible(recordingButton);
                setVisible(mSendButton);
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLAG = PHOTO;
                sendMessage(PHOTO);
                startClientThread();
                getNImages();
            }
        });

        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLAG = FILE;
                sendMessage(FILE);

                startClientThread();
                Intent intent = new Intent(MainActivity.this, FilePicker.class);
                startActivityForResult(intent, REQUEST_PICK_FILE);
            }
        });

        tryAgainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDevice(getIntent(), true);
            }
        });

        /*
        Maintains the file sender's sending operations
         */

        MainApplication.clientHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MessageType.READY_FOR_DATA: {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File file = new File(Environment.getExternalStorageDirectory(), MainApplication.TEMP_IMAGE_FILE_NAME);
                        Uri outputFileUri = Uri.fromFile(file);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                        // startActivityForResult(takePictureIntent, MainApplication.PICTURE_RESULT_CODE);
                        break;
                    }

                    case MessageType.COULD_NOT_CONNECT: {
                        Toast.makeText(MainActivity.this, "Could not connect to the paired device", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.SENDING_DATA: {
                        stateView.setText("Sending");
                        setVisible(pb);
                        statusBar.setAnimation(show);
                        show.start();
                        break;
                    }

                    case MessageType.DATA_SENT_OK: {


                        String type = "";
                        String path = "";
                        if (FLAG.equals(PHOTO)) {
                            type = Constant.PHOTO;
                        } else if (FLAG.equals(VOICE)) {
                            type = Constant.VOICE;
                        } else {
                            type = Constant.FILE;
                        }

                        dbHandler.saveMessage(lastSentImage, DeviceListActivity.DEVICE_ADDRESS, CONNECTED_DEVICE_ADDRESS, type);

                        MessageContents imageContents = new MessageContents();
                        imageContents.message = lastSentImage;
                        imageContents.receiver = CONNECTED_DEVICE_ADDRESS;
                        imageContents.sender = DeviceListActivity.DEVICE_ADDRESS;
                        imageContents.type = type;

                        messageContentses.add(imageContents);
                        messageAdapter.notifyDataSetChanged();
                        setInvisible(mSendButton);
                        setVisible(recordingButton);
                        setInvisible(pb);
                        stateView.setText("Sent");
                        statusBar.setAnimation(hide);
                        hide.start();
                        break;
                    }

                    case MessageType.DIGEST_DID_NOT_MATCH: {
                        Toast.makeText(MainActivity.this, "Photo was sent, but didn't go through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }

                }
            }
        };

        /*
        Maintains the file receiver's receiving operations
         */
        MainApplication.serverHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {


                switch (message.what) {
                    case MessageType.DATA_RECEIVED: {

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        String path = "";
                        String type = "";
                        if (FLAG.equals(PHOTO)) {
                            Bitmap image = BitmapFactory.decodeByteArray(((byte[]) message.obj), 0, ((byte[]) message.obj).length, options);
                            path = saveToInternalStorage(image, 0);
                            type = Constant.PHOTO;
                        } else if (FLAG.equals(FILE)) {
                            path = saveFileToInternalStorage((byte[]) message.obj, 5);
                            type = Constant.FILE;
                        } else {
                            path = saveFileToInternalStorage((byte[]) message.obj, 1);
                            type = Constant.VOICE;
                        }
                        dbHandler.saveMessage(path, CONNECTED_DEVICE_ADDRESS, DeviceListActivity.DEVICE_ADDRESS, type);


                        MessageContents imageContents = new MessageContents();
                        imageContents.message = path;
                        imageContents.sender = CONNECTED_DEVICE_ADDRESS;
                        imageContents.receiver = DeviceListActivity.DEVICE_ADDRESS;
                        imageContents.type = type;

                        messageContentses.add(imageContents);
                        messageAdapter.notifyDataSetChanged();
                        stateView.setText("Received");
                        setInvisible(pb);
                        statusBar.setAnimation(hide);
                        hide.start();
                        break;
                    }

                    case MessageType.DIGEST_DID_NOT_MATCH: {
                        Toast.makeText(MainActivity.this, "Photo was received, but didn't come through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.DATA_PROGRESS_UPDATE: {
                        // some kind of update
                        MainApplication.progressData = (ProgressData) message.obj;
                        double pctRemaining = 100 - (((double) MainApplication.progressData.remainingSize / MainApplication.progressData.totalSize) * 100);
                        stateView.setText("Receiving...");
                        setVisible(seekbar);

                        statusBar.setAnimation(show);
                        show.start();
                        seekbar.setProgress((int) Math.floor(pctRemaining));
                        break;
                    }

                    case MessageType.INVALID_HEADER: {
                        Toast.makeText(MainActivity.this, "Photo was sent, but the header was formatted incorrectly", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void setupChat() {

        //Load data into arraylist
        messageContentses = dbHandler.getAllMessages(DeviceListActivity.DEVICE_ADDRESS, CONNECTED_DEVICE_ADDRESS);

        // Initialize the array adapter for the conversation thread
        messageAdapter = new MessageAdapter(this, messageContentses);
        mConversationView.setAdapter(messageAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = mOutEditText.getText().toString();
                if (message.length() == 0) {
                    FLAG = VOICE;
                    voiceMessage.stopAudioRecording();
                    sendMessage(VOICE);
                    sendVoice();
                } else {

                    sendMessage(message);
                }
            }
        });

        mOutEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 0) {
                    setInvisible(recordingButton);
                    setVisible(mSendButton);
                } else {
                    setInvisible(mSendButton);
                    setVisible(recordingButton);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        // Initialize the buffer for outgoing messageContentses
        mOutStringBuffer = new StringBuffer("");

        connectDevice(getIntent(), true);
    }

    private void sendVoice() {
        File file = new File(voiceMessage.voiceStoragePath);


        sendFile(convertToByte(file), 1);
    }

    private byte[] convertToByte(File file) {

        byte[] b = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);

        } catch (Exception e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        }

        return b;
    }

    /*
    Method which sends text messages
     */

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (MainApplication.mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            //connectDevice(getIntent(),true);
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            MainApplication.mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }

            return true;
        }
    };


    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    public static final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    if (stateView != null)
                        switch (msg.arg1) {
                            case BluetoothChatService.STATE_CONNECTED:
                                startServerThread();
                                stateView.setText("Connected to - " + MainActivity.CONNECTED_DEVICE);
                                setInvisible(pb);
                                setInvisible(tryAgainView);
                                statusBar.setAnimation(hide);
                                hide.start();
                                break;
                            case BluetoothChatService.STATE_CONNECTING:
                                stateView.setText(R.string.title_connecting);
                                setVisible(pb);
                                setInvisible(tryAgainView);
                                break;
                            case BluetoothChatService.STATE_LISTEN:

                            case BluetoothChatService.STATE_NONE:
                                stateView.setText(R.string.title_not_connected);
                                setInvisible(pb);
                                setVisible(tryAgainView);
                                statusBar.setAnimation(show);
                                show.start();
                                break;
                        }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);

                    if (!writeMessage.equals(VOICE) && !writeMessage.equals(PHOTO) && !writeMessage.startsWith(FILE)) {

                        dbHandler.saveMessage(writeMessage, DeviceListActivity.DEVICE_ADDRESS, CONNECTED_DEVICE_ADDRESS, Constant.TEXTS);

                        MessageContents message = new MessageContents();
                        message.message = writeMessage;
                        message.receiver = CONNECTED_DEVICE_ADDRESS;
                        message.sender = DeviceListActivity.DEVICE_ADDRESS;
                        message.type = Constant.TEXTS;

                        messageContentses.add(message);
                        messageAdapter.notifyDataSetChanged();
                    }

                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    if (!readMessage.equals(VOICE) && !readMessage.equals(PHOTO) && !readMessage.startsWith(FILE)) {
                        dbHandler.saveMessage(readMessage, CONNECTED_DEVICE_ADDRESS, DeviceListActivity.DEVICE_ADDRESS, Constant.TEXTS);
                        MessageContents messageread = new MessageContents();
                        messageread.message = readMessage;
                        messageread.sender = CONNECTED_DEVICE_ADDRESS;
                        messageread.receiver = DeviceListActivity.DEVICE_ADDRESS;
                        messageread.type = Constant.TEXTS;

                        messageContentses.add(messageread);
                        messageAdapter.notifyDataSetChanged();
                    } else {
                        String[] str;

                        FLAG = readMessage;
                        if (readMessage.startsWith(FILE)) {
                            str = readMessage.split("\\.");
                            EXTENSION = "." + str[str.length - 1];
                            FLAG = str[0];
                        }
                        startServerThread();
                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);

                    break;
                case Constants.MESSAGE_TOAST:

                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == INTENT_REQUEST_GET_N_IMAGES) {


            if (resultCode == RESULT_OK) {
                Log.v(TAG, "Photo acquired from camera intent");
                try {

                    Parcelable[] parcelableUris = data.getParcelableArrayExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);


                    if (parcelableUris == null) {
                        return;
                    }

                    //Toast.makeText(getApplicationContext(),parcelableUris.length+"",Toast.LENGTH_LONG).show();
                    // Java doesn't allow array casting, this is a little hack
                    Uri[] uris = new Uri[parcelableUris.length];
                    System.arraycopy(parcelableUris, 0, uris, 0, parcelableUris.length);
                    File file = new File(uris[0].toString());

                    lastSentImage = uris[0].toString();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath(), options);


                    ByteArrayOutputStream compressedImageStream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, MainApplication.IMAGE_QUALITY, compressedImageStream);
                    byte[] compressedImage = compressedImageStream.toByteArray();


                    sendFile(compressedImage, 20);


                } catch (Exception e) {
                    Log.d(TAG, e.toString());

                    Toast.makeText(getApplicationContext(), e + "", Toast.LENGTH_LONG).show();
                }
            }
            return;
        }

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_PICK_FILE:

                if (data.hasExtra(FilePicker.EXTRA_FILE_PATH)) {

                    File selectedFile = new File
                            (data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                    lastSentImage = selectedFile.getAbsolutePath();
                    String[] str = data.getStringExtra(FilePicker.EXTRA_FILE_PATH).split("\\.");
                    toast(data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                    sendMessage(FILE + "." + str[str.length - 1]);
                    sendFile(convertToByte(selectedFile), 5);

                }
                break;
        }
    }


    /*
    This method can send any kind of files converted into byte[]
     */

    public void sendFile(byte[] compressedFile, int arg) {


        // Invoke client thread to send
        Message message = new Message();
        message.obj = compressedFile;
        message.arg1 = arg;
        MainApplication.clientThread.incomingHandler.sendMessage(message);

    }

    /*
    starts server thread for receiver.
     */
    public static void startServerThread() {
        if (MainApplication.serverThread == null || MainApplication.serverThread.isInterrupted()) {
            Log.v(TAG, "Starting server thread.  Able to accept photos.");
            MainApplication.serverThread = new ServerThread(MainApplication.adapter, MainApplication.serverHandler);
            MainApplication.serverThread.start();

        }
    }

    /*
    starts client server for sender.
     */
    public void startClientThread() {
        if (MainApplication.clientThread != null) {
            MainApplication.clientThread.cancel();
        }
        MainApplication.clientThread = new ClientThread(mBluetoothAdapter.getRemoteDevice(CONNECTED_DEVICE_ADDRESS), MainApplication.clientHandler);
        MainApplication.clientThread.start();
    }

    /*
    connects device.
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        MainApplication.mChatService.connect(device, secure);
        CONNECTED_DEVICE = device.getName();
        CONNECTED_DEVICE_ADDRESS = address;
    }

    private void getNImages() {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        Config config = new Config.Builder()
                .setTabBackgroundColor(R.color.white)    // set tab background color. Default white.
                .setTabSelectionIndicatorColor(R.color.blue)
                .setCameraButtonColor(R.color.orange)
                .setSelectionLimit(1)    // set photo selection limit. Default unlimited selection.
                .build();
        ImagePickerActivity.setConfig(config);
        startActivityForResult(intent, INTENT_REQUEST_GET_N_IMAGES);
    }

    public void createDirectory() {
        File f1 = new File(Environment.getExternalStorageDirectory(), "BTChatApp/images/");
        File f2 = new File(Environment.getExternalStorageDirectory(), "BTChatApp/voices/");
        File f3 = new File(Environment.getExternalStorageDirectory(), "BTChatApp/files/");
        if (!f1.exists() || !f2.exists() || !f3.exists()) {
            f1.mkdirs();
            f2.mkdirs();
            f3.mkdirs();
        }
    }

    /*
    method for saving any file into storage.
     */
    private String saveFileToInternalStorage(byte[] byteArray, int arg) {

        createDirectory();
        Calendar calendar = Calendar.getInstance();

        File strFilePath = new File(Environment.getExternalStorageDirectory(), "BTChatApp/" + getFolder(arg) + calendar.getTimeInMillis() + getExtension(arg));
        try {
            FileOutputStream fos = new FileOutputStream(strFilePath);

            fos.write(byteArray);
            fos.close();
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException : " + ex);
        } catch (IOException ioe) {
            System.out.println("IOException : " + ioe);
        }
        return strFilePath.getAbsolutePath();
    }

    /*
    method for saving images to internal storage.
     */
    private String saveToInternalStorage(Bitmap bitmapImage, int arg) {

        // Create imageDir
        createDirectory();
        Calendar calendar = Calendar.getInstance();


        File mypath = new File(Environment.getExternalStorageDirectory(), "BTChatApp/" + getFolder(arg) + calendar.getTimeInMillis() + getExtension(arg));

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.getAbsolutePath();
    }

    public String getFolder(int arg) {
        if (arg == 5)
            return "files/";

        return (arg == 0) ? "images/" : "voices/";
    }

    public String getExtension(int arg) {
        if (arg == 5) {
            return EXTENSION;
        }
        return (arg == 0) ? ".png" : ".3gpp";
    }

    public void toast(String msg) {

        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MainApplication.serverThread = null;
    }

    public static void setVisible(View view) {
        view.setVisibility(View.VISIBLE);
    }

    public static void setInvisible(View view) {
        view.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.fahim.lokman.bluetoothchatapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.fahim.lokman.bluetoothchatapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
