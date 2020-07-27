package com.example.letsplay.objects;

import java.io.Serializable;

public class LastSearchPreferencesSaved implements Serializable {
    private boolean isByDistance,isBySearch,isByGenre,isByInstrument;
    private int radius,genrePosition,instrumentPosition;

    public LastSearchPreferencesSaved(boolean isByDistance, boolean isBySearch, boolean isByGenre, boolean isByInstrument,
                                      int radius, int genrePosition, int instrumentPosition) {
        this.isByDistance = isByDistance;
        this.isBySearch = isBySearch;
        this.isByGenre = isByGenre;
        this.isByInstrument = isByInstrument;
        this.radius = radius;
        this.genrePosition = genrePosition;
        this.instrumentPosition = instrumentPosition;
    }

    public boolean isByDistance() {
        return isByDistance;
    }

    public void setByDistance(boolean byDistance) {
        isByDistance = byDistance;
    }

    public boolean isBySearch() {
        return isBySearch;
    }

    public void setBySearch(boolean bySearch) {
        isBySearch = bySearch;
    }

    public boolean isByGenre() {
        return isByGenre;
    }

    public void setByGenre(boolean byGenre) {
        isByGenre = byGenre;
    }

    public boolean isByInstrument() {
        return isByInstrument;
    }

    public void setByInstrument(boolean byInstrument) {
        isByInstrument = byInstrument;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getGenrePosition() {
        return genrePosition;
    }

    public void setGenrePosition(int genrePosition) {
        this.genrePosition = genrePosition;
    }

    public int getInstrumentPosition() {
        return instrumentPosition;
    }

    public void setInstrumentPosition(int instrumentPosition) {
        this.instrumentPosition = instrumentPosition;
    }
}

