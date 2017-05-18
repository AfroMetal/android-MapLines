package com.afrometal.radoslaw.maplines;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Place implements Comparable, Parcelable {

    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("coordinates")
    @Expose
    private List<Double> coordinates = new ArrayList<>();

    public Place() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return String.format("Place<%d: %s-%s [%f, %f]>", id, country, city, coordinates.get(0), coordinates.get(1));
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Place) == false) {
            return false;
        } else {
            Place oth = (Place) other;
            if (oth.id == id && oth.country.equals(country) && oth.city.equals(city) && oth.coordinates.equals(coordinates)) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return city.compareTo(((Place) o).city);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(country);
        dest.writeString(city);
        dest.writeList(coordinates);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    protected Place(Parcel in) {
        id = in.readLong();
        country = in.readString();
        city = in.readString();
        coordinates = in.readArrayList(Double.class.getClassLoader());
    }
}