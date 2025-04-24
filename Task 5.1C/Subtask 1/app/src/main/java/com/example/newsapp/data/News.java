package com.example.newsapp.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class News implements Parcelable {
    private int id;
    private int imageResId;
    private String title;
    private String description;
    private List<News> relatedNews;

    // There will 2 Constructors for class News
    // 1: Without relatedNews
    public News(int id, int imageResId, String title, String description) {
        this.id = id;
        this.imageResId = imageResId;
        this.title = title;
        this.description = description;
        this.relatedNews = new ArrayList<>();   // Prevent NullPointException
    }

    // 2: With relateNews
    public News(int id, int imageResId, String title, String description, List<News> relatedNews) {
        this.id = id;
        this.imageResId = imageResId;
        this.title = title;
        this.description = description;
        this.relatedNews = relatedNews;
    }

    // Parcelable implementation
    protected News(Parcel in) {
        id = in.readInt();
        imageResId = in.readInt();
        title = in.readString();
        description = in.readString();
        relatedNews = new ArrayList<>();
        in.readList(relatedNews, News.class.getClassLoader());
    }

    public static final Creator<News> CREATOR = new Creator<News>() {
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };

    // Getters
    public int getId() {
        return id;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<News> getRelatedNews() {
        return relatedNews;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(imageResId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeList(relatedNews);
    }
}
