package com.antivirus.Antivirus.util;

import java.util.ArrayList;
import java.util.List;

public class CircularList<T> {
    private final List<T> list;
    private final int capacity;
    private int index = 0;

    public CircularList(int capacity) {
        this.capacity = capacity;
        this.list = new ArrayList<>(capacity);
    }

    public void add(T element) {
        if (list.size() < capacity) {
            list.add(element);
        } else {
            list.set(index, element); // Sobrescribe el elemento más antiguo
        }
        index = (index + 1) % capacity; // Mantiene el ciclo circular
    }

    public T getNext() {
        if (list.isEmpty()) return null;
        T element = list.get(index);
        index = (index + 1) % list.size();
        return element;
    }

    public List<T> getAll() {
        return new ArrayList<>(list);
    }
}
