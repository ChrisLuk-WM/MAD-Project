package com.example.mad_project.statistics;

import java.util.concurrent.atomic.AtomicReference;

public class StatisticsValue<T> {
    private final AtomicReference<T> value;
    private final T defaultValue;

    public StatisticsValue(T defaultValue) {
        this.defaultValue = defaultValue;
        this.value = new AtomicReference<>(defaultValue);
    }

    public T get() {
        T currentValue = value.get();
        return currentValue != null ? currentValue : defaultValue;
    }

    public void set(T newValue) {
        value.set(newValue);
    }

    public void reset() {
        value.set(defaultValue);
    }
}