package helper;

import android.location.Location;

public class MathHelper {

    public float getTheta(float bearing) {
        float theta = 0;
        if (bearing >= 0) {
            theta = 90 - bearing;
        } else if (bearing >= 90) {
            theta = bearing - 90;
        }
        if (bearing < 0) {
            theta = 90 + bearing;
        } else if (bearing >= -90) {
            theta = (bearing + 90) * -1;
        }
        return theta;
    }

    public float getAdjacent(float theta, float hypotenuse) {

        return Double.valueOf(Math.cos(theta) * hypotenuse).floatValue();

    }

    public float getOpposite(float theta, float hypotenuse) {

        return Double.valueOf(Math.sin(theta) * hypotenuse).floatValue();

    }

    public float calibratedToNorth(float currentHeading, float bearing){
        float calibratedBearing = 0;
        if(currentHeading >= bearing){

        }
        return 0;
    }

    public float bearingBtw2Points(double latitudeSource, double longitudeSource, double latitudeDes, double longitudeDes) {
        //angleNorth(3.216111, 101.733889, 3.2164133, 101.7335478);

        double lat1 = latitudeSource / 180 * Math.PI;
        double lng1 = longitudeSource / 180 * Math.PI;
        double lat2 = latitudeDes / 180 * Math.PI;
        double lng2 = longitudeDes / 180 * Math.PI;

        double y = Math.sin(lng2 - lng1) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1);

        double tan2 = Math.atan2(y, x);
        double bearing = tan2 * 180 / Math.PI;
/*        if (bearing < 0) {
            return (bearing + 360.0);
        } else {
            return bearing;
        }*/
        return Double.valueOf(bearing).floatValue();
    }

    public double bearingBtw2Points(Location locationSource, Location locationDes){
        double latA = locationSource.getLatitude();
        double lonA = locationSource.getLongitude();
        double latB = locationDes.getLatitude();
        double lonB = locationDes.getLongitude();

        double x = Math.cos(latB) * Math.sin((lonA-lonB));
        double y = Math.cos(latA) * Math.sin(latB) - Math.sin(latA) * Math.cos(latB) * Math.cos((lonA-lonB));
        return (Math.atan2(x,y) * 180 / Math.PI);
    }

    public double greatCircleDistance(Location locationSource, Location locationDes){
        double latDifference = locationSource.getLatitude() - locationDes.getLatitude();
        double lonDifference = locationSource.getLongitude() - locationDes.getLongitude();

        double a = Math.pow(Math.sin(latDifference),2) + Math.cos(locationSource.getLatitude()) * Math.cos(locationDes.getLatitude()) * Math.pow(Math.sin((lonDifference/2)),2);
        double c = 2 * Math.atan2((Math.sqrt(a)),Math.sqrt((1-a)));

        return 6371*c;
        //return (Math.acos(Math.sin(locationSource.getLatitude())*Math.sin(locationDes.getLatitude()) + Math.cos(locationSource.getLatitude()) * Math.cos(locationDes.getLatitude()) *
        //        Math.cos((locationDes.getLongitude() - locationSource.getLongitude()))) * 6370);

    }

}
