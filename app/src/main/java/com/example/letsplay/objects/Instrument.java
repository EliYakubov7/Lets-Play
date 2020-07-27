package com.example.letsplay.objects;

import java.io.Serializable;

public class Instrument implements Serializable {
    private int instrumentResId;
    private int skillResId;

    public Instrument() {}

    public Instrument(int instrumentResId, int skillResId) {
        this.instrumentResId = instrumentResId;
        this.skillResId = skillResId;
    }

    public int getInstrumentResId() {
        return instrumentResId;
    }

    public void setInstrumentResId(int instrumentResId) {
        this.instrumentResId = instrumentResId;
    }

    public int getSkillResId() {
        return skillResId;
    }

    public void setSkillResId(int skill) {
        this.skillResId = skill;
    }
}
