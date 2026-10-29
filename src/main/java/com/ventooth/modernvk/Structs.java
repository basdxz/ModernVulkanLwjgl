package com.ventooth.modernvk;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.ffm.FFM;
import org.lwjgl.system.ffm.FFMDefinition;
import org.lwjgl.system.ffm.StructBinder;

import java.lang.invoke.MethodHandles;

public final class Structs {
    public static void initialize() {
        FFM.ffmConfig(vec3.class,
                      FFM.ffmConfigBuilder(MethodHandles.lookup())
                         .withChecks(true)
                         .withNullableAnnotation(Nullable.class)
                         .build());
    }

    @FFMDefinition("""
            struct vec3 {
                float x;
                float y;
                float z;
            }""")
    public interface vec3 {
        // @formatter:off
        StructBinder<vec3> $ = FFM.ffmStruct(vec3.class)
                                  .m("x", FFM.float32)
                                  .m("y", FFM.float32)
                                  .m("z", FFM.float32)
                                  .build();
        // @formatter:on

        vec3 x(float x);

        float x();

        vec3 y(float y);

        float y();

        vec3 z(float z);

        float z();
    }
}
