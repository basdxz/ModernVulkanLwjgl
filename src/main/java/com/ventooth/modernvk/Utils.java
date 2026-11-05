package com.ventooth.modernvk;

import lombok.val;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public final class Utils {
    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static String readTextFile(String filePath) {
        // Currently this reads from the class path
        // In the future it should read from an actual path (hot-reloading shaders)
        try (val is = Utils.class.getClassLoader()
                                 .getResourceAsStream(filePath)) {
            if (is == null) {
                throw new IOException("File not found");
            }
            val bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException _) {
            return "";
        }
    }

    public static <T> T[] initArray(int size, Supplier<? extends T> constructor, IntFunction<T[]> generator) {
        return initArray(size, _ -> constructor.get(), generator);
    }

    public static <T> T[] initArray(int size, IntFunction<? extends T> constructor, IntFunction<T[]> generator) {
        val ret = generator.apply(size);
        for (var i = 0; i < size; i++) {
            ret[i] = constructor.apply(i);
        }
        return ret;
    }
}
