package com.fahim.lokman.bluetoothchatapp;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

/**
 * Created by tawsifkhan on 8/18/16.
 */
public class VoiceMessage {


    public MediaRecorder mediaRecorder;
    public String voiceStoragePath;

    static final String AB = "abcdefghijklmnopqrstuvwxyz";
    static Random rnd = new Random();
    public static boolean isPlaying = false;

    public MediaPlayer mediaPlayer;

    public VoiceMessage(){
        hasSDCard();



        File file = new File(getVoiceStoragePath());
        byte[] array = new byte[(int)file.length()];
        try {
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.read(array);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ;



        initializeMediaRecord();
    }

    public String getVoiceStoragePath(){
        voiceStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File audioVoice = new File(voiceStoragePath + File.separator + "voices");
        if(!audioVoice.exists()){
            audioVoice.mkdir();
        }
        voiceStoragePath = voiceStoragePath + File.separator + "voices/" + generateVoiceFilename(6) + ".3gpp";

        return voiceStoragePath;
    }


    private String generateVoiceFilename( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    public void startAudioRecording(){
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopAudioRecording(){
        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }

    }

    public void playMusic(String path){

        isPlaying = true;

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();

    }

    public void stopAudioPlay(){
        if(mediaPlayer != null){
            isPlaying = false;
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void hasSDCard(){
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(isSDPresent)        {
            System.out.println("There is SDCard");
        }
        else{
            System.out.println("There is no SDCard");
        }
    }

    public void mediaPlayerPlaying(){
        if(!mediaPlayer.isPlaying()){
            stopAudioPlay();
        }
    }

    public void initializeMediaRecord(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(voiceStoragePath);
    }

}
