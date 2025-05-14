package com.example.lostfoundapp.data;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class ItemViewModel extends AndroidViewModel {

    private ItemRepository repository;
    private LiveData<List<Item>> allItems;

    public ItemViewModel(@NonNull Application application) {
        super(application);
        repository = new ItemRepository(application);
        allItems = repository.getAllItems();
    }

    // Expose LiveData to get all items
    public LiveData<List<Item>> getAllItems() {
        return allItems;
    }

    // Expose LiveData to get items by type
    public LiveData<List<Item>> getItemsByType(String itemType) {
        return repository.getItemsByType(itemType);
    }

    public LiveData<Item> getItemById(int itemId) {
        return repository.getItemById(itemId);
    }

    // Wrapper for inserting an item
    public void insert(Item item) {
        repository.insert(item);
    }

    // Wrapper for deleting an item
    public void delete(Item item) {
        repository.delete(item);
    }
}