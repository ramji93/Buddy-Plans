package com.chatapp.ramji.buddyplans;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.chatapp.ramji.buddyplans.db.MessageEntity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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


    public static String getDate(Long timestamp)
    {




        Calendar msgcalendar = Calendar.getInstance();

        msgcalendar.setTimeInMillis(timestamp);

        Date date = new Date();

        Calendar todaycalendar = Calendar.getInstance();

        todaycalendar.setTime(date);

        SimpleDateFormat sdf;

        if(todaycalendar.get(Calendar.DAY_OF_YEAR) > msgcalendar.get(Calendar.DAY_OF_YEAR))

        sdf = new SimpleDateFormat("HH:mm dd/MM");

        else

        sdf = new SimpleDateFormat("HH:mm");

        String dateAsString = sdf.format (timestamp);
        Log.i("Inside Util func","date is "+ dateAsString );


        return dateAsString;


    }


    public static String saveImage(Context context,String downloaduri,String photoContentName) {


        String appsegment = "/Buddyplans/pictures";

       // ContextWrapper cw = new ContextWrapper(getApplicationContext());
        //File directory = cw.getDir("imageDir", Context.MODE_ENABLE_WRITE_AHEAD_LOGGING);

        String dir = Environment.getExternalStorageDirectory().getPath()+appsegment;

        File fdir = new File(dir);

        boolean create_result = false;

        if(!fdir.exists())

         create_result = fdir.mkdirs();

        String path = Environment.getExternalStorageDirectory().getPath()+appsegment+"/"+photoContentName;



        final File f=new File(path);

        if(f.exists())
            return path;
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

                MediaStore.Images.Media.insertImage(context.getContentResolver(),path,photoContentName,photoContentName);
            }
                catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            catch (Exception e) {
                e.printStackTrace();
            }
             finally {
                try {
                    if(fos!=null)
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

;;
        }

      return path;

    }


    public static String saveProfileImage(Context context,String downloaduri,String photoContentName) {


        String appsegment = "/Buddyplans/pictures";

        // ContextWrapper cw = new ContextWrapper(getApplicationContext());
        //File directory = cw.getDir("imageDir", Context.MODE_ENABLE_WRITE_AHEAD_LOGGING);

        String dir = Environment.getExternalStorageDirectory().getPath()+appsegment;

        File fdir = new File(dir);

        boolean create_result = false;

        if(!fdir.exists())

            create_result = fdir.mkdirs();

        String path = Environment.getExternalStorageDirectory().getPath()+appsegment+"/"+photoContentName;



        final File f=new File(path);

        if(f.exists())
        {
            f.delete();
        }

            Bitmap bitmap = null;

            ImageView im = new ImageView(context);

            FileOutputStream fos = null;
            try {
                bitmap =  Glide.with(context).load(downloaduri).asBitmap().into(2048,2048).get();


                // bitmap = ((BitmapDrawable)im.getDrawable()).getBitmap();


                fos = new FileOutputStream(f);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                //MediaStore.Images.Media.insertImage(context.getContentResolver(),path,photoContentName,photoContentName);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if(fos!=null)
                        fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ;;


        return path;

    }


    public static MessageEntity getEntityfromMessage(Message message,String chatId,Context mcontext)
    {

        MessageEntity dbmessage;
        String dbphotoContentUrl=null;
        String dbphotoContentName=null;
        String dbText=null;
        String dbUserName=null;
        long dbTimestamp;
        String dbuserid=null;
        String dbUserPhotoUrl=null;
        Location dbLocation=null;
        String dbChatid=null;


        if(message.getPhotoContentUrl()!=null)
        {
            dbphotoContentUrl =  message.getPhotoContentUrl();
            dbphotoContentName = message.getPhotoContentName();
        }

        if(message.getText()!=null)
        {
            dbText = message.getText();
        }

        dbUserName = message.getUserName();

        dbTimestamp = message.getTimeStamp();

        dbuserid = message.getUid();

        if(message.getPhotoUrl()!= null)
        {
            dbUserPhotoUrl = message.getPhotoUrl();
        }

        if(message.getLocation()!=null)
        {
            dbLocation = message.getLocation();
        }

        if(chatId!=null) {
            dbChatid = chatId;
        }

        dbmessage = new MessageEntity(message.getMessageid(),dbText,dbphotoContentUrl,dbphotoContentName,dbUserName,dbTimestamp,dbUserPhotoUrl,dbuserid,dbLocation,dbChatid);

         return  dbmessage;
    }


    public static boolean checkConnection(Context mContext)
    {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;

    }



}
