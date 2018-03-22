package com.makesense.labs.spot.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
public class SpotData implements Parcelable {

    private double latitude;
    private double longitude;
    private String timeStamp;
    private String type;
    private String url;

    public SpotData(double latitude, double longitude, String timeStamp, String type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
        this.type = type;
    }

    private SpotData(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        timeStamp = in.readString();
        type = in.readString();
        url = in.readString();
    }

    public static final Creator<SpotData> CREATOR = new Creator<SpotData>() {
        @Override
        public SpotData createFromParcel(Parcel in) {
            return new SpotData(in);
        }

        @Override
        public SpotData[] newArray(int size) {
            return new SpotData[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(timeStamp);
        dest.writeString(type);
        dest.writeString(url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpotData spotData = (SpotData) o;

        if (Double.compare(spotData.latitude, latitude) != 0) return false;
        if (Double.compare(spotData.longitude, longitude) != 0) return false;
        if (!timeStamp.equals(spotData.timeStamp)) return false;
        return type.equals(spotData.type);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + timeStamp.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SpotData{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", timeStamp='" + timeStamp + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
