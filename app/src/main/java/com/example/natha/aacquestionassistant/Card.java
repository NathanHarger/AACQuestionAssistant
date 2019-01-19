package com.example.natha.aacquestionassistant;

import android.os.Parcel;
import android.os.Parcelable;

public class Card implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    String label;
    String photoId;
    long key;
    int id;
    boolean isSelected;
    int resourceLocation;
    String pronunciation;

    Card(String label, String photoId) {
        this.key = this.hashCode();
        this.label = label;
        this.photoId = photoId;
        this.isSelected = false;
        pronunciation = "";

    }

    public Card(Parcel in) {
        this.key = in.readLong();
        this.label = in.readString();
        this.photoId = in.readString();
        this.isSelected = (Boolean) in.readValue(null);
        pronunciation = in.readString();
    }
    Card(String values[]) {
        this.key = this.hashCode();
        this.id = Integer.parseInt(values[0]);
        this.label = values[1];
        resourceLocation = Integer.parseInt(values[2]);
        if(values.length == 4){
            pronunciation = values[3];

        } else {
            pronunciation = "";
        }
    }
    public Card update(Card other) {
        if (other == null) {
            return null;
        }
        label = other.label;
        photoId = other.photoId;
        //key = other.key;
        isSelected = other.isSelected;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }



    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(key);
        out.writeString(label);
        out.writeString(photoId);
        out.writeValue(isSelected);
        out.writeString(pronunciation);
    }
    public Card(int id, String values[]){
        this.key = this.hashCode();

        this.id = id;
        label = values[0];
        resourceLocation =Integer.parseInt(values[1]);
        pronunciation = values[2];
    }

    public String toString() {
        return "Card{" +
                "key='" + key + '\'' +
                ", label='" + label + '\'' +
                ", photoId='" + photoId + '\'' +
                ", isSelected='" + isSelected + '\'' +
                ", pronunciation='" + pronunciation + '\''+
                '}';
    }
    public Card(int id, String filename, int imageLocation, String pronunciation) {
        this.key = this.hashCode();
        this.photoId = filename;
        this.id = id;
        this.label = filename;
        this.resourceLocation = imageLocation;
        this.pronunciation = pronunciation;
    }

}
