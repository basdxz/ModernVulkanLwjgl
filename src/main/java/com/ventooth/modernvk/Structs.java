package com.ventooth.modernvk;

import lombok.val;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.ffm.FFM;
import org.lwjgl.system.ffm.FFMDefinition;
import org.lwjgl.system.ffm.StructBinder;

import java.lang.invoke.MethodHandles;

public final class Structs {
    public static void initialize() {
        val cfg = FFM.ffmConfigBuilder(MethodHandles.lookup())
                     .withChecks(true)
                     .withNullableAnnotation(Nullable.class)
                     .build();
        FFM.ffmConfig(vec2.class, cfg);
        FFM.ffmConfig(vec3.class, cfg);
        FFM.ffmConfig(vec4.class, cfg);
        FFM.ffmConfig(mat4.class, cfg);
        FFM.ffmConfig(RenderItem.class, cfg);
        FFM.ffmConfig(Vertex.class, cfg);
        FFM.ffmConfig(Material.class, cfg);
        FFM.ffmConfig(Light.class, cfg);
        FFM.ffmConfig(FrameConstants.class, cfg);
    }

    @FFMDefinition("""
            struct vec2 {
                float x;
                float y;
            }""")
    public interface vec2 {
        // @formatter:off
        StructBinder<vec2> $ = FFM.ffmStruct(vec2.class)
                                  .m("x", FFM.float32)
                                  .m("y", FFM.float32)
                                  .build();

        vec2    x(float x);
        float   x();

        vec2    y(float y);
        float   y();
        // @formatter:on
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

        vec3    x(float x);
        float   x();

        vec3    y(float y);
        float   y();

        vec3    z(float z);
        float   z();
        // @formatter:on
    }

    @FFMDefinition("""
            struct vec4 {
                float x;
                float y;
                float z;
                float w;
            }""")
    public interface vec4 {
        // @formatter:off
        StructBinder<vec4> $ = FFM.ffmStruct(vec4.class)
            .m("x", FFM.float32)
            .m("y", FFM.float32)
            .m("z", FFM.float32)
            .m("w", FFM.float32)
            .build();

        vec4    x(float x);
        float   x();

        vec4    y(float y);
        float   y();

        vec4    z(float z);
        float   z();

        vec4    w(float w);
        float   w();
        // @formatter:on
    }

    @FFMDefinition("""
            struct mat4 {
                float m00, m01, m02, m03;
                float m10, m11, m12, m13;
                float m20, m21, m22, m23;
                float m30, m31, m32, m33;
            }""")
    public interface mat4 {
        // @formatter:off
        StructBinder<mat4> $ = FFM.ffmStruct(mat4.class)
            .m("m00", FFM.float32).m("m01", FFM.float32).m("m02", FFM.float32).m("m03", FFM.float32)
            .m("m10", FFM.float32).m("m11", FFM.float32).m("m12", FFM.float32).m("m13", FFM.float32)
            .m("m20", FFM.float32).m("m21", FFM.float32).m("m22", FFM.float32).m("m23", FFM.float32)
            .m("m30", FFM.float32).m("m31", FFM.float32).m("m32", FFM.float32).m("m33", FFM.float32)
            .build();

        mat4    m00(float m00);
        float   m00();
        mat4    m01(float m01);
        float   m01();
        mat4    m02(float m02);
        float   m02();
        mat4    m03(float m03);
        float   m03();

        mat4    m10(float m10);
        float   m10();
        mat4    m11(float m11);
        float   m11();
        mat4    m12(float m12);
        float   m12();
        mat4    m13(float m13);
        float   m13();

        mat4    m20(float m20);
        float   m20();
        mat4    m21(float m21);
        float   m21();
        mat4    m22(float m22);
        float   m22();
        mat4    m23(float m23);
        float   m23();

        mat4    m30(float m30);
        float   m30();
        mat4    m31(float m31);
        float   m31();
        mat4    m32(float m32);
        float   m32();
        mat4    m33(float m33);
        float   m33();

        // @formatter:on
    }

    @FFMDefinition("""
            struct RenderItem {
                mat4 wvp;
                mat4 worldMatrix;
                uint32_t materialIndex;
            }""")
    public interface RenderItem {
        // @formatter:off
        StructBinder<RenderItem> $ = FFM.ffmStruct(RenderItem.class)
            .m("wvp",           mat4.$)
            .m("worldMatrix",   mat4.$)
            .m("materialIndex", FFM.uint32_t)
            .build();

        RenderItem  wvp(mat4 wvp);
        mat4        wvp();

        RenderItem  worldMatrix(mat4 worldMatrix);
        mat4        worldMatrix();

        RenderItem  materialIndex(int materialIndex);
        int         materialIndex();
        // @formatter:on
    }

    @FFMDefinition("""
            struct Vertex {
                vec3 position;
                vec3 color;
                vec3 normal;
                vec2 uv;
            }""")
    public interface Vertex {
        // @formatter:off
        StructBinder<Vertex> $ = FFM.ffmStruct(Vertex.class)
            .m("position",  vec3.$)
            .m("color",     vec3.$)
            .m("normal",    vec3.$)
            .m("uv",        vec2.$)
            .build();

        Vertex  position(vec3 position);
        vec3   position();

        Vertex  color(vec3 color);
        vec3    color();

        Vertex  normal(vec3 normal);
        vec3    normal();

        Vertex  uv(vec2 uv);
        vec2    uv();
        // @formatter:on
    }

    @FFMDefinition("""
            struct Material {
            	vec4 baseColor;
            	uint32_t textureIndex;
            }""")
    public interface Material {
        // @formatter:off
        StructBinder<Material> $ = FFM.ffmStruct(Material.class)
            .m("baseColor", vec4.$)
            .m("textureIndex", FFM.uint32_t)
            .build();

        Material    baseColor(vec4 baseColor);
        vec4        baseColor();

        Material    textureIndex(int textureIndex);
        int         textureIndex();
        // @formatter:on
    }

    @FFMDefinition("""
            struct Light {
                uint type;
            	vec3 position;
            	vec3 color;
            	vec3 direction;
            	float intensity;
            	float range;
            	float innerConeAngle;
            	float outerConeAngle;
            }""")
    public interface Light {
        // @formatter:off
        StructBinder<Light> $ = FFM.ffmStruct(Light.class)
                                      .m("type", FFM.uint32_t)
                                      .m("position", vec3.$)
                                      .m("color", vec3.$)
                                      .m("direction", vec3.$)
                                      .m("intensity", FFM.float32)
                                      .m("range", FFM.float32)
                                      .m("innerConeAngle", FFM.float32)
                                      .m("outerConeAngle", FFM.float32)
                                      .build();

        Light   type(int type);
        int     type();

        Light   position(vec3 position);
        vec3    position();

        Light   color(vec3 color);
        vec3    color();

        Light   direction(vec3 direction);
        vec3    direction();

        Light   intensity(float intensity);
        float   intensity();

        Light   range(float range);
        float   range();

        Light   innerConeAngle(float innerConeAngle);
        float   innerConeAngle();

        Light   outerConeAngle(float outerConeAngle);
        float   outerConeAngle();
        // @formatter:on
    }

    @FFMDefinition("""
            struct FrameConstants {
            	uint64_t vertexBufferAddress;
            	uint64_t materialBufferAddress;
            	uint64_t renderItemsAddress;
            	uint64_t lightsBufferAddress;
            }""")
    public interface FrameConstants {
        // @formatter:off
        StructBinder<FrameConstants> $ = FFM.ffmStruct(FrameConstants.class)
            .m("vertexBufferAddress",   FFM.uint64_t)
            .m("materialBufferAddress", FFM.uint64_t)
            .m("renderItemsAddress",    FFM.uint64_t)
            .m("lightsBufferAddress",    FFM.uint64_t)
            .build();

        FrameConstants  vertexBufferAddress(long vertexBufferAddress);
        long            vertexBufferAddress();

        FrameConstants  materialBufferAddress(long materialBufferAddress);
        long            materialBufferAddress();

        FrameConstants  renderItemsAddress(long renderItemsAddress);
        long            renderItemsAddress();

        FrameConstants  lightsBufferAddress(long lightsBufferAddress);
        long            lightsBufferAddress();
        // @formatter:on
    }
}
