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
    boolean isSelected;
    int resourceLocation;

    Card(String label, String photoId) {
        this.key = this.hashCode();
        this.label = label;
        this.photoId = photoId;
        this.isSelected = false;

    }

    public Card(Parcel in) {
        this.key = in.readLong();
        this.label = in.readString();
        this.photoId = in.readString();
        this.isSelected = (Boolean) in.readValue(null);
    }

    public Card update(Card other) {
        if (other == null) {
            return null;
        }
        label = other.label;
        photoId = other.photoId;
        key = other.key;
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
    }

    public String toString() {
        return "Card{" +
                "key='" + key + '\'' +
                ", label='" + label + '\'' +
                ", photoId='" + photoId + '\'' +
                ", isSelected='" + isSelected + '\'' +
                '}';
    }

}
