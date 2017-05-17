package com.afrometal.radoslaw.maplines;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Place implements Comparable {

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
    private double[] coordinates = null;

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

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return String.format("Place<%d: %s-%s [%f, %f]>", id, country, city, coordinates[0], coordinates[1]);
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
            if (oth.id == id && oth.country == country && oth.city == city && oth.coordinates[0] == coordinates[0] && oth.coordinates[1] == coordinates[1]) {
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
}