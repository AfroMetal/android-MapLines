package com.afrometal.radoslaw.maplines;

import java.util.Collections;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Places {

    @SerializedName("places")
    @Expose
    private List<Place> places = null;

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }

    public Place getById(long id) {
        for (Place p : places) {
            if (p.getId() == id) {
                return p;
            }
        }
        return new Place();
    }

    public void sort() {
        Collections.sort(this.places);
    }
}