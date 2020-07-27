package com.example.letsplay.objects;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    //Basic details
    private String uid, firstName, lastName, gender;
    //Musical instruments
    private Instrument mainInstrument, secondaryInstrument;
    //Self summary
    private String selfDescription;
    //URL for uploaded profile picture from firebase
    private String profileImageURL, backgroundImageURL;
    //Age,number of friends
    private String dateOfBirth;
    private int age, numOfFriends;
    private boolean isTokenRegistered, isFirstTime;

    //User status - online or offline
    public enum Status {
        ONLINE,
        OFFLINE
    }

    private Status status;
    //latitude and longitude for location management
    private double latitude, longitude;
    //User's location - <city>,<state>
    private String location;

    //Music genre list
    private ArrayList<Integer> musicGenres = new ArrayList<>();
    //Favorite artists' list
    private ArrayList<String> artists = new ArrayList<>();
    //Friend list
    private ArrayList<String> friends = new ArrayList<>();
    //Chat list
    private ArrayList<Chat> chats = new ArrayList<>();

    public User() {
    }

    public User(String uid, String firstName, String lastName, String gender, String dateOfBirth, int age,  String location, double latitude, double longitude, boolean isTokenRegistered, boolean isFirstTime) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.isTokenRegistered = isTokenRegistered;
        this.isFirstTime = isFirstTime;
    }

    public User(String uid, String firstName, String lastName, String gender, String dateOfBirth, int age, String location, boolean isTokenRegistered, boolean isFirstTime) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
        this.location = location;
        this.isTokenRegistered = isTokenRegistered;
        this.isFirstTime = isFirstTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSelfDescription() {
        return selfDescription;
    }

    public void setSelfDescription(String selfDescription) {
        this.selfDescription = selfDescription;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getNumOfFriends() {
        return numOfFriends;
    }

    public void setNumOfFriends(int numOfFriends) {
        this.numOfFriends = numOfFriends;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public ArrayList<Integer> getMusicGenres() {
        return musicGenres;
    }

    public void setMusicGenres(ArrayList<Integer> musicGenres) {
        this.musicGenres = musicGenres;
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }

    public ArrayList<Chat> getChats() {
        return chats;
    }

    public void setChats(ArrayList<Chat> chats) {
        this.chats = chats;
    }

    public Instrument getMainInstrument() {
        return mainInstrument;
    }

    public void setMainInstrument(Instrument mainInstrument) {
        this.mainInstrument = mainInstrument;
    }

    public Instrument getSecondaryInstrument() {
        return secondaryInstrument;
    }

    public void setSecondaryInstrument(Instrument secondaryInstrument) {
        this.secondaryInstrument = secondaryInstrument;
    }

    public String getBackgroundImageURL() {
        return backgroundImageURL;
    }

    public void setBackgroundImageURL(String backgroundImageURL) {
        this.backgroundImageURL = backgroundImageURL;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<String> getArtists() {
        return artists;
    }

    public void setArtists(ArrayList<String> artists) {
        this.artists = artists;
    }

    public boolean isTokenRegistered() {
        return isTokenRegistered;
    }

    public void setTokenRegistered(boolean tokenRegistered) {
        isTokenRegistered = tokenRegistered;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }

    public void setFirstTime(boolean firstTime) {
        isFirstTime = firstTime;
    }
}
