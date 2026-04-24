
package com.smartcampus.dao;
import com.smartcampus.model.BaseModel;
import com.smartcampus.exception.DataNotFoundException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Asus
 */
public class GenericDAO<T extends BaseModel>{
    private final List<T> items;

    public GenericDAO(List<T> items) {
        this.items = items;
    }

    public List<T> getAll() {
        synchronized (items) {
            return new ArrayList<>(items);
        }
    }

    public T getById(String id) {
        if (id == null) {
            throw new DataNotFoundException("ID cannot be null");
        }

        synchronized (items) { // Best practice to keep thread-safety
            for (T item : items) {
                if (item != null && id.equals(item.getId())) { 
                    return item;
                }
            }
        }
        throw new DataNotFoundException("Resource not found with id: " + id);
    }

    public void add(T item) {
        items.add(item);
    }

    public void update(T updatedItem) {

        synchronized (items) {
            for (int i = 0; i < items.size(); i++) {
                T item = items.get(i);
                // Add a null check for 'item'
                if (item != null && item.getId().equals(updatedItem.getId())) {
                    items.set(i, updatedItem);
                    return;
                }
            }
        }
    }

    public void delete(String id) {

        boolean removed = items.removeIf(item -> item.getId().equals(id));

        if (!removed) {
            throw new DataNotFoundException("Resource not found with id: " + id);
        }

    }
}
