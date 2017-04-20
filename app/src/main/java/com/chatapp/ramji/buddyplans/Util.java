package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;

import static android.R.attr.path;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by user on 10-04-2017.
 */

public class Util {


//    long second = (millis / 1000) % 60;
//    long minute = (millis / (1000 * 60)) % 60;
//    long hour = (millis / (1000 * 60 * 60)) % 24;
//
//    String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);


    public static void getDate(Long timestamp)
    {




        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        String dateAsString = sdf.format (timestamp);
        Log.i("Inside Util func","date is "+ dateAsString );





    }


    public static void saveImage(Context context,String downloaduri,String photoContentName) {


        String appsegment = "/Buddyplans/pictures";

       // ContextWrapper cw = new ContextWrapper(getApplicationContext());
        //File directory = cw.getDir("imageDir", Context.MODE_ENABLE_WRITE_AHEAD_LOGGING);

        String dir = Environment.getExternalStorageDirectory().getPath()+appsegment;

        File fdir = new File(dir);

        if(!fdir.exists())

          fdir.mkdirs();

        String path = Environment.getExternalStorageDirectory().getPath()+appsegment+"/"+photoContentName;



        final File f=new File(path);

        if(f.exists())
            return;
        else
        {

            Bitmap bitmap = null;

            ImageView im = new ImageView(context);

            FileOutputStream fos = null;
            try {
                bitmap =  Glide.with(context).load(downloaduri).asBitmap().into(2048,2048).get();


            // bitmap = ((BitmapDrawable)im.getDrawable()).getBitmap();


                fos = new FileOutputStream(f);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
                catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            catch (Exception e) {
                e.printStackTrace();
            }
             finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }




        }



    }
}
