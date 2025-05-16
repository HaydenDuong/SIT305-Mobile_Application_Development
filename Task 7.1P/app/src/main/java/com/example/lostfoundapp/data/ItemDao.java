package com.example.lostfoundapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ItemDao {
    @Insert
    void insert(Item item);
    @Delete
    void delete(Item item);
    @Query("SELECT * FROM lost_found_table ORDER BY date DESC")
    LiveData<List<Item>> getAllItems();
    @Query("SELECT * FROM lost_found_table WHERE type = :itemType ORDER BY date DESC")
    LiveData<List<Item>> getItemsByType(String itemType);
    @Query("SELECT * FROM lost_found_table WHERE id = :itemId")
    LiveData<Item> getItemById(int itemId);
}
