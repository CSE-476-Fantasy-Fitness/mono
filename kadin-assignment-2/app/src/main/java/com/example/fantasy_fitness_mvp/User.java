package com.example.fantasy_fitness_mvp;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Simple User class to manage and structure the storage and retrieval of
 * user information that may be needed throughout the fitness app.
 */
public class User implements Parcelable {
    private String username;
    private int age;
    private int height;
    private int weight;

    public User(String username) {
        this.username = username;
        this.age = 0;
        this.height = 0;
        this.weight = 0;
    }

    // Parcelable implementation
    protected User(Parcel in) {
        username = in.readString();
        age = in.readInt();
        height = in.readInt();
        weight = in.readInt();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(username);
        parcel.writeInt(age);
        parcel.writeInt(height);
        parcel.writeInt(weight);
    }

    // Getter and setter methods

    public String getUsername() {
        return username;
    }

    public int getAge() {
        return age;
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
