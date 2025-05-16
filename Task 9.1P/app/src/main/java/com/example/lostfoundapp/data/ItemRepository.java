package com.example.lostfoundapp.data; // Or your repository package

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemRepository {

    private ItemDao itemDao;
    private LiveData<List<Item>> allItems;
    private LiveData<List<Item>> lostItems;
    private LiveData<List<Item>> foundItems;

    // ExecutorService for background operations
    private static final int NUMBER_OF_THREADS = 4;
    private final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Constructor
    public ItemRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        itemDao = db.itemDao();
        allItems = itemDao.getAllItems();
        // Example: Initialize LiveData for specific types if needed
        // lostItems = itemDao.getItemsByType("Lost");
        // foundItems = itemDao.getItemsByType("Found");
    }

    // Method to get all items (returns LiveData)
    public LiveData<List<Item>> getAllItems() {
        return allItems;
    }

    // Method to get items by type (returns LiveData)
    public LiveData<List<Item>> getItemsByType(String itemType) {
        return itemDao.getItemsByType(itemType);
    }

    // Method to get an item by on its id (return LiveData)
    public LiveData<Item> getItemById(int itemId) {
        return itemDao.getItemById(itemId);
    }

    // Method to insert an item (runs on a background thread)
    public void insert(Item item) {
        databaseWriteExecutor.execute(() -> {
            itemDao.insert(item);
        });
    }

    // Method to delete an item (runs on a background thread)
    public void delete(Item item) {
        databaseWriteExecutor.execute(() -> {
            itemDao.delete(item);
        });
    }
}
