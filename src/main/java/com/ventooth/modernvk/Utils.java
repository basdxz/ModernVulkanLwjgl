package com.ventooth.modernvk;

import lombok.val;
import org.joml.Matrix4dc;
import org.joml.Vector3dc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static com.ventooth.modernvk.Structs.mat4;
import static com.ventooth.modernvk.Structs.vec3;

public final class Utils {
    public static final double HALF_PI = Math.PI / 2D;

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
        return initArrayIndexed(size, _ -> constructor.get(), generator);
    }

    public static <T> T[] initArrayIndexed(int size, IntFunction<? extends T> constructor, IntFunction<T[]> generator) {
        val ret = generator.apply(size);
        for (var i = 0; i < size; i++) {
            ret[i] = constructor.apply(i);
        }
        return ret;
    }

    public static void setMat4(Matrix4dc src, mat4 dst) {
        // @formatter:off
        dst.m00((float) src.m00()).m01((float) src.m01()).m02((float) src.m02()).m03((float) src.m03())
           .m10((float) src.m10()).m11((float) src.m11()).m12((float) src.m12()).m13((float) src.m13())
           .m20((float) src.m20()).m21((float) src.m21()).m22((float) src.m22()).m23((float) src.m23())
           .m30((float) src.m30()).m31((float) src.m31()).m32((float) src.m32()).m33((float) src.m33());
        // @formatter:on
    }

    public static void setVec3(Vector3dc src, vec3 dst) {
        dst.x((float) src.x())
           .y((float) src.y())
           .z((float) src.z());
    }
}
