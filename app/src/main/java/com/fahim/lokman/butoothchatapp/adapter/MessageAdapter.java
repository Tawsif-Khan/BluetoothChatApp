package com.fahim.lokman.bluetoothchatapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.fahim.lokman.bluetoothchatapp.DeviceListActivity;
import com.fahim.lokman.bluetoothchatapp.MainActivity;
import com.fahim.lokman.bluetoothchatapp.R;
import com.fahim.lokman.bluetoothchatapp.contents.Constant;
import com.fahim.lokman.bluetoothchatapp.contents.MessageContents;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by tawsifkhan on 8/14/16.
 */
public class MessageAdapter extends BaseAdapter {


    Context context;
    ArrayList<MessageContents> messageContentses;
    LayoutInflater inflater;
    LinearLayout LL;
    public String messageValue;
    public ViewHolder holder;
    private int playingPosition;
    public ArrayList<ImageView> imageViews = new ArrayList<>();

    public MessageAdapter(Context context, ArrayList<MessageContents> messageContentses){
        this.context = context;
        this.messageContentses = messageContentses;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return messageContentses.size();
    }

    @Override
    public Object getItem(int position) {
        return messageContentses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

       final MessageContents messageContents = messageContentses.get(position);
        holder = new ViewHolder();
        if(convertView == null){
            convertView = inflater.inflate(R.layout.right_row,null);
            holder.messageView = (TextView) convertView.findViewById(R.id.textView);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.voiceView = (ImageView) convertView.findViewById(R.id.voice);
            holder.LL = (LinearLayout) convertView.findViewById(R.id.linearL);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }


        if(messageContents.type.equals(Constant.TEXTS)){
            holder.messageView.setVisibility(View.VISIBLE);
            holder.messageView.setText(messageContents.message);
            holder.imageView.setVisibility(View.GONE);
            holder.voiceView.setVisibility(View.GONE);
        }else if(messageContents.type.equals(Constant.PHOTO)){
            holder.messageView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.voiceView.setVisibility(View.GONE);
            holder.imageView.setImageBitmap(setImage(messageContents.message));
        }else if (messageContents.type.equals(Constant.VOICE)){

            holder.voiceView.setVisibility(View.VISIBLE);
            holder.messageView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);
            if(playingPosition == position)
                holder.voiceView.setImageResource(R.drawable.stop);
            else
                holder.voiceView.setImageResource(R.drawable.play);
        }else{

            holder.messageView.setVisibility(View.VISIBLE);
            holder.messageView.setText("Click here to open");
            holder.voiceView.setVisibility(View.VISIBLE);
            holder.voiceView.setImageResource(R.drawable.file);
            holder.imageView.setVisibility(View.GONE);
        }

        if(DeviceListActivity.DEVICE_ADDRESS.equals(messageContents.sender)) {
            holder.LL.setGravity(Gravity.RIGHT);
            holder.messageView.setBackgroundColor(Color.parseColor("#efefef"));
            holder.imageView.setBackgroundColor(Color.parseColor("#efefef"));
            holder.messageView.setTextColor(Color.BLUE);
        }
        else {
            holder.LL.setGravity(Gravity.LEFT);
            holder.messageView.setBackgroundColor(Color.BLUE);
            holder.imageView.setBackgroundColor(Color.BLUE);
            holder.messageView.setTextColor(Color.WHITE);

        }

        holder.messageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageContents messageContents1 = messageContentses.get(position);

            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageContents messageContents = messageContentses.get(position);
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.display_image);
                ImageView myImage = (ImageView) dialog.findViewById(R.id.imageView2);
                myImage.setImageBitmap(setImage(messageContents.message));
                dialog.show();
            }
        });

        holder.voiceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageContents messageContents = messageContentses.get(position);
                if(messageContents.type.equals(Constant.FILE)){
                    openFolder(messageContents.message);
                    return;
                }

                if(MainActivity.voiceMessage.isPlaying){
                   // setPlayButton(position);
                    MainActivity.voiceMessage.stopAudioPlay();
                }else {
                  //  setStopButton(position);
                    playingPosition = position;
                    MainActivity.voiceMessage.playMusic(messageContents.message);
                    MainActivity.voiceMessage.mediaPlayerPlaying();

                }

            }
        });

        return convertView;
    }

    private class ViewHolder{
        public TextView messageView;
        public ImageView imageView,voiceView;
        public LinearLayout LL;
    }

    private void setPlayButton(int pos){
        imageViews.get(pos).setImageResource(R.drawable.play);
    }

    private void setStopButton(int pos){
        imageViews.get(pos).setImageResource(R.drawable.stop);
    }

    private Bitmap setImage(String imgPath)
    {
        Bitmap b = null;
        try {
            File f=new File(imgPath);
             b = BitmapFactory.decodeStream(new FileInputStream(f));

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return b;
    }

    public void openFolder(String path)
    {
        Toast.makeText(context,path,Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(path);
        intent.setDataAndType(uri, "*/*");
        context.startActivity(Intent.createChooser(intent, "Open folder"));
    }
}
