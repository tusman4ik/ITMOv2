package ru.tusman4ik.task.wire;

import java.util.Map;

public final class PrototypeInfos {

    private PrototypeInfos() {
    }

    public static <K, V> void putUnique(Map<K, V> map, K key, V value, String what) {
        if (map.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("duplicate " + what + ": " + key);
        }
    }
}
