package com.ventooth.modernvk;

import lombok.val;
import org.jspecify.annotations.Nullable;
import org.lwjgl.vulkan.VK10;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FILTER_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_LOD_CLAMP_NONE;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST;

// https://github.com/syoyo/tinygltf/blob/0d9ce1a9561688c6c6e7471430d15a50a0b15f1a/tiny_gltf_v3.h#L171C1-L212
public final class GltfModel {
    // @formatter:off

    /* Primitive modes */
    public static final int TG3_MODE_POINTS         = 0;
    public static final int TG3_MODE_LINE           = 1;
    public static final int TG3_MODE_LINE_LOOP      = 2;
    public static final int TG3_MODE_LINE_STRIP     = 3;
    public static final int TG3_MODE_TRIANGLES      = 4;
    public static final int TG3_MODE_TRIANGLE_STRIP = 5;
    public static final int TG3_MODE_TRIANGLE_FAN   = 6;

    /* Component types */
    public static final int TG3_COMPONENT_TYPE_BYTE             = 5120;
    public static final int TG3_COMPONENT_TYPE_UNSIGNED_BYTE    = 5121;
    public static final int TG3_COMPONENT_TYPE_SHORT            = 5122;
    public static final int TG3_COMPONENT_TYPE_UNSIGNED_SHORT   = 5123;
    public static final int TG3_COMPONENT_TYPE_INT              = 5124;
    public static final int TG3_COMPONENT_TYPE_UNSIGNED_INT     = 5125;
    public static final int TG3_COMPONENT_TYPE_FLOAT            = 5126;
    public static final int TG3_COMPONENT_TYPE_DOUBLE           = 5130;

    /* Texture filter */
    public static final int TG3_TEXTURE_FILTER_NEAREST                  = 9728;
    public static final int TG3_TEXTURE_FILTER_LINEAR                   = 9729;
    public static final int TG3_TEXTURE_FILTER_NEAREST_MIPMAP_NEAREST   = 9984;
    public static final int TG3_TEXTURE_FILTER_LINEAR_MIPMAP_NEAREST    = 9985;
    public static final int TG3_TEXTURE_FILTER_NEAREST_MIPMAP_LINEAR    = 9986;
    public static final int TG3_TEXTURE_FILTER_LINEAR_MIPMAP_LINEAR     = 9987;

    /* Texture wrap */
    public static final int TG3_TEXTURE_WRAP_REPEAT             = 10497;
    public static final int TG3_TEXTURE_WRAP_CLAMP_TO_EDGE      = 33071;
    public static final int TG3_TEXTURE_WRAP_MIRRORED_REPEAT    = 33648;

    // @formatter:on

    public static final Map<Integer, Triple<Integer, Integer, Float>> filterMap;
    public static final Map<Integer, Integer> wrapMap;

    static {
        val filterMapTemp = new HashMap<Integer, Triple<Integer, Integer, Float>>();
        val wrapMapTemp = new HashMap<Integer, Integer>();

        // @formatter:off
        filterMapTemp.put(TG3_TEXTURE_FILTER_NEAREST,                   new Triple<>(VK_FILTER_NEAREST, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0.25F));
        filterMapTemp.put(TG3_TEXTURE_FILTER_LINEAR,                    new Triple<>(VK_FILTER_LINEAR,  VK_SAMPLER_MIPMAP_MODE_NEAREST, 0.25F));
        filterMapTemp.put(TG3_TEXTURE_FILTER_LINEAR_MIPMAP_LINEAR,      new Triple<>(VK_FILTER_LINEAR,  VK_SAMPLER_MIPMAP_MODE_LINEAR,  VK_LOD_CLAMP_NONE));
        filterMapTemp.put(TG3_TEXTURE_FILTER_NEAREST_MIPMAP_NEAREST,    new Triple<>(VK_FILTER_NEAREST, VK_SAMPLER_MIPMAP_MODE_NEAREST, VK_LOD_CLAMP_NONE));
        filterMapTemp.put(TG3_TEXTURE_FILTER_NEAREST_MIPMAP_LINEAR,     new Triple<>(VK_FILTER_NEAREST, VK_SAMPLER_MIPMAP_MODE_LINEAR,  VK_LOD_CLAMP_NONE));
        filterMapTemp.put(TG3_TEXTURE_FILTER_LINEAR_MIPMAP_NEAREST,     new Triple<>(VK_FILTER_LINEAR,  VK_SAMPLER_MIPMAP_MODE_NEAREST, VK_LOD_CLAMP_NONE));

        wrapMapTemp.put(TG3_TEXTURE_WRAP_REPEAT,            VK_SAMPLER_ADDRESS_MODE_REPEAT);
        wrapMapTemp.put(TG3_TEXTURE_WRAP_CLAMP_TO_EDGE,     VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
        wrapMapTemp.put(TG3_TEXTURE_WRAP_MIRRORED_REPEAT,   VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);
        // @formatter:on

        filterMap = Collections.unmodifiableMap(filterMapTemp);
        wrapMap = Collections.unmodifiableMap(wrapMapTemp);
    }

    public static final Triple<Integer, Integer, Float> defaultFilter = filterMap.get(TG3_TEXTURE_FILTER_LINEAR);
    public static final int defaultWrap = wrapMap.get(TG3_TEXTURE_WRAP_REPEAT);
}
