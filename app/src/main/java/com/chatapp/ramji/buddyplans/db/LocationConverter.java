package com.chatapp.ramji.buddyplans.db;

import android.arch.persistence.room.TypeConverter;

import com.chatapp.ramji.buddyplans.Location;

/**
 * Created by ramji_v on 10/7/2017.
 */

public class LocationConverter {
    @TypeConverter
    public static Location stringtoLocation(String locationstring)
    {
        if(locationstring==null)
        {
            return null;
        }
        else {
            String result[] = locationstring.split(":");
            Location location = new Location(Double.parseDouble(result[0]), Double.parseDouble(result[1]));
            return location;
        }

    }

    @TypeConverter
    public static String locationtoString(Location location)
    {

        if(location==null)
        {
            return null;
        }
        else {
            StringBuffer stringBuffer = new StringBuffer("");
            stringBuffer.append(Double.toString(location.getLatitude()));
            stringBuffer.append(":");
            stringBuffer.append(Double.toString(location.getLongitude()));
            return stringBuffer.toString();
        }

    }

}
