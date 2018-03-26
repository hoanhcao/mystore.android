package com.hevaisoi.android.databases;

import java.util.List;

/**
 * Created by ERP on 6/12/2017.
 */

public interface BaseDAO<T> {
    long save(T obj);
    void update(T obj);
    void delete(T obj);
    T get(long id);
    T getFirst();
    List<T> getAll();
}
