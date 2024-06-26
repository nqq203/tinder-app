package com.main;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public final class MemoryData {
    public static void saveData(String data, Context context) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput("data.txt", Context.MODE_PRIVATE);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveLastMsg(String data, String chatId, Context context) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(chatId + ".text", Context.MODE_PRIVATE);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getData(Context context) {
        String data = "";
        try {
            FileInputStream fis = context.openFileInput("data.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
        }
         catch (IOException e) {
            e.printStackTrace();
         }
        return data;
    }

    public static String getLastMsg(Context context, String chatId) {
        String data = "0";
        try {
            FileInputStream fis = context.openFileInput(chatId + ".txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
