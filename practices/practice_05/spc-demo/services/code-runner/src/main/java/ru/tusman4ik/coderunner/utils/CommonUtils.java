package ru.tusman4ik.coderunner.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public final class CommonUtils {
    private CommonUtils() {}

    public static <T> T or(T cur, T def) {
        return cur == null ? def : cur;
    }

    public static <T> List<T> mergeNullableCollection(List<T> f, List<T> s) {
        List<T> res = null;

        if (f != null || s != null) {
            res = new ArrayList<>(Objects.requireNonNullElse(f, List.of()));
            res.addAll(Objects.requireNonNullElse(s, List.of()));
        }
        return res;
    }

    public static void consumeEveryLine(String s, Consumer<String> consumer) {
        for (String line : s.split("\n")) {
            consumer.accept(line);
        }
    }

}
