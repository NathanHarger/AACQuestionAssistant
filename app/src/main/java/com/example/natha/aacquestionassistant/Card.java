package com.example.natha.aacquestionassistant;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Card implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        public Card[] newArray(int size) {
            return new Card[size];
        }
    };
    final long key;
    String label;
    String photoId;
    int id;
    boolean isSelected;
    int resourceLocation;
    String pronunciation;

    Card() {
        this.key = this.hashCode();
        this.label = "";
        this.photoId = "";
        this.isSelected = false;
        pronunciation = "";
    }

    private Card(Parcel in) {
        this.id = in.readInt();
        this.key = in.readLong();
        this.label = in.readString();
        this.photoId = in.readString();
        this.isSelected = (boolean) in.readValue(getClass().getClassLoader());
        pronunciation = in.readString();
    }

    Card(String values[]) {
        this.key = this.hashCode();
        this.id = Integer.parseInt(values[0]);
        this.label = values[1];
        this.photoId = values[1]+values[0];
        resourceLocation = Integer.parseInt(values[2]);
        if (values.length == 4) {
            pronunciation = values[3];
        } else {
            pronunciation = "";
        }
    }

    public Card(int id, String values[]) {
        this.key = this.hashCode();
        this.id = id;
        label = values[0];
        resourceLocation = Integer.parseInt(values[1]);
        if (values.length == 3) {
            pronunciation = values[2];
        } else {
            pronunciation = "";
        }
    }


    public Card(int id, String filename, String photoId, int imageLocation, String pronunciation) {
        this.key = this.hashCode();
        this.photoId = photoId;
        this.id = id;
        this.label = filename;
        this.resourceLocation = imageLocation;
        this.pronunciation = pronunciation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeLong(key);
        out.writeString(label);
        out.writeString(photoId);
        out.writeValue(isSelected);
        out.writeString(pronunciation);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Card)) {
            return false;
        }

        Card c = (Card) o;
        if(id == 0){
            return c.key == this.key;
        }
        return c.id == this.id;
    }

    @NonNull
    public String toString() {
        return "Card{" +
                "id='" + id + '\'' +
                "key='" + key + '\'' +
                ", label='" + label + '\'' +
                ", photoId='" + photoId + '\'' +
                ", isSelected='" + isSelected + '\'' +
                ", pronunciation='" + pronunciation + '\'' +
                '}';
    }
}
