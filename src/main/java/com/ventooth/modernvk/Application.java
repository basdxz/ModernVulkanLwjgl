package com.ventooth.modernvk;

import com.ventooth.modernvk.Structs.FrameConstants;
import com.ventooth.modernvk.Structs.Material;
import com.ventooth.modernvk.Structs.RenderItem;
import com.ventooth.modernvk.Structs.vec3;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.model.io.v2.GltfReaderV2;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.SegmentStack;
import org.lwjgl.system.ffm.FFM;
import org.lwjgl.system.ffm.StructArray;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferDeviceAddressInfo;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferSubmitInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBindingFlagsCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkDrawIndexedIndirectCommand;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier2;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan14Features;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties2;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderingAttachmentInfo;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;
import org.lwjgl.vulkan.VkSemaphoreTypeCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreWaitInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubmitInfo2;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.ventooth.modernvk.Camera.CameraType;
import static com.ventooth.modernvk.GltfModel.TG3_COMPONENT_TYPE_FLOAT;
import static com.ventooth.modernvk.GltfModel.TG3_COMPONENT_TYPE_UNSIGNED_INT;
import static com.ventooth.modernvk.GltfModel.TG3_COMPONENT_TYPE_UNSIGNED_SHORT;
import static com.ventooth.modernvk.Structs.Vertex;
import static com.ventooth.modernvk.Structs.vec2;
import static com.ventooth.modernvk.Structs.vec4;
import static com.ventooth.modernvk.Utils.HALF_PI;
import static com.ventooth.modernvk.Utils.readTextFile;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_KEY_UP;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_MOUSE_MOTION;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_QUIT;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_RESIZED;
import static org.lwjgl.sdl.SDLEvents.SDL_PollEvent;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_VIDEO;
import static org.lwjgl.sdl.SDLInit.SDL_InitSubSystem;
import static org.lwjgl.sdl.SDLInit.SDL_Quit;
import static org.lwjgl.sdl.SDLKeyboard.SDL_GetKeyboardState;
import static org.lwjgl.sdl.SDLMessageBox.SDL_MESSAGEBOX_ERROR;
import static org.lwjgl.sdl.SDLMessageBox.SDL_ShowSimpleMessageBox;
import static org.lwjgl.sdl.SDLMouse.SDL_GetWindowRelativeMouseMode;
import static org.lwjgl.sdl.SDLMouse.SDL_SetWindowRelativeMouseMode;
import static org.lwjgl.sdl.SDLScancode.SDL_SCANCODE_A;
import static org.lwjgl.sdl.SDLScancode.SDL_SCANCODE_D;
import static org.lwjgl.sdl.SDLScancode.SDL_SCANCODE_DOWN;
import static org.lwjgl.sdl.SDLScancode.SDL_SCANCODE_ESCAPE;
import static org.lwjgl.sdl.SDLScancode.SDL_SCANCODE_GRAVE;
import static org.lwjgl.sdl.SDLScancode.SDL_SCANCODE_LEFT;
import static org.lwjgl.sdl.SDLScancode.SDL_SCANCODE_RIGHT;
import static org.lwjgl.sdl.SDLScancode.SDL_SCANCODE_S;
import static org.lwjgl.sdl.SDLScancode.SDL_SCANCODE_UP;
import static org.lwjgl.sdl.SDLScancode.SDL_SCANCODE_W;
import static org.lwjgl.sdl.SDLTimer.SDL_GetTicks;
import static org.lwjgl.sdl.SDLVideo.SDL_CreateWindow;
import static org.lwjgl.sdl.SDLVideo.SDL_DestroyWindow;
import static org.lwjgl.sdl.SDLVideo.SDL_WINDOW_RESIZABLE;
import static org.lwjgl.sdl.SDLVideo.SDL_WINDOW_VULKAN;
import static org.lwjgl.sdl.SDLVulkan.SDL_Vulkan_CreateSurface;
import static org.lwjgl.sdl.SDLVulkan.SDL_Vulkan_GetInstanceExtensions;
import static org.lwjgl.sdl.SDLVulkan.SDL_Vulkan_GetVkGetInstanceProcAddr;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compilation_status_success;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compile_into_spv;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compile_options_initialize;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compile_options_release;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compile_options_set_optimization_level;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compile_options_set_target_env;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compile_options_set_target_spirv;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compiler_initialize;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compiler_release;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_env_version_vulkan_1_4;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_fragment_shader;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_optimization_level_performance;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_result_get_bytes;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_result_get_compilation_status;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_result_get_error_message;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_result_release;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_spirv_version_1_6;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_target_env_vulkan;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_vertex_shader;
import static org.lwjgl.util.vma.Vma.VMA_ALLOCATION_CREATE_DEDICATED_MEMORY_BIT;
import static org.lwjgl.util.vma.Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT;
import static org.lwjgl.util.vma.Vma.VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_AUTO;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_AUTO_PREFER_HOST;
import static org.lwjgl.util.vma.Vma.vmaCreateAllocator;
import static org.lwjgl.util.vma.Vma.vmaCreateBuffer;
import static org.lwjgl.util.vma.Vma.vmaCreateImage;
import static org.lwjgl.util.vma.Vma.vmaDestroyAllocator;
import static org.lwjgl.util.vma.Vma.vmaDestroyBuffer;
import static org.lwjgl.util.vma.Vma.vmaDestroyImage;
import static org.lwjgl.util.vma.Vma.vmaFreeMemory;
import static org.lwjgl.util.vma.Vma.vmaMapMemory;
import static org.lwjgl.util.vma.Vma.vmaUnmapMemory;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT;
import static org.lwjgl.vulkan.KHRSurface.VK_COLORSPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_CLEAR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_STORE;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_POOL_CREATE_TRANSIENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_SCISSOR;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_VIEWPORT;
import static org.lwjgl.vulkan.VK10.VK_FALSE;
import static org.lwjgl.vulkan.VK10.VK_FILTER_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBufferToImage;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexedIndirect;
import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkCreateSampler;
import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyCommandPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkDestroyImage;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroySampler;
import static org.lwjgl.vulkan.VK10.vkDestroySemaphore;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK10.vkFreeCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceProperties;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkQueueWaitIdle;
import static org.lwjgl.vulkan.VK10.vkResetCommandPool;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceFeatures2;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceQueueFamilyProperties2;
import static org.lwjgl.vulkan.VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT;
import static org.lwjgl.vulkan.VK12.VK_DESCRIPTOR_BINDING_PARTIALLY_BOUND_BIT;
import static org.lwjgl.vulkan.VK12.VK_DESCRIPTOR_BINDING_UPDATE_AFTER_BIND_BIT;
import static org.lwjgl.vulkan.VK12.VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT;
import static org.lwjgl.vulkan.VK12.VK_DESCRIPTOR_SET_LAYOUT_CREATE_UPDATE_AFTER_BIND_POOL_BIT;
import static org.lwjgl.vulkan.VK12.VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK12.VK_SEMAPHORE_TYPE_TIMELINE;
import static org.lwjgl.vulkan.VK12.vkGetBufferDeviceAddress;
import static org.lwjgl.vulkan.VK12.vkWaitSemaphores;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_NONE;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_TRANSFER_WRITE_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_COPY_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_NONE;
import static org.lwjgl.vulkan.VK13.vkCmdBeginRendering;
import static org.lwjgl.vulkan.VK13.vkCmdEndRendering;
import static org.lwjgl.vulkan.VK13.vkCmdPipelineBarrier2;
import static org.lwjgl.vulkan.VK13.vkQueueSubmit2;
import static org.lwjgl.vulkan.VK14.VK_API_VERSION_1_4;

@NoArgsConstructor
public final class Application {
    private static final int VulkanVersion = VK_API_VERSION_1_4;
    private static final int swapchainFormat = VK_FORMAT_B8G8R8A8_SRGB;
    private static final int depthFormat = VK_FORMAT_D32_SFLOAT;
    private static final int MaxTextures = 1024;
    private static final int MaxMaterials = 1024;
    private static final int MaxFramesInFlight = 2;
    private static final String shaderEntryPoint = "main";

    private static final long VertexBufferBytes = 128 * 1024 * 1024; // 128MB vertex budget
    private static final long IndexBufferBytes = 64 * 1024 * 1024; // 64MB index budget
    private static final int MaxNumVertices;
    private static final int MaxNumIndices;

    static {
        val vertexSize = Vertex.$.layout()
                                 .byteSize();
        val indexSize = Vertex.$.layout()
                                .byteSize();

        MaxNumVertices = (int) (VertexBufferBytes / vertexSize);
        MaxNumIndices = (int) (IndexBufferBytes / indexSize);
    }

    private /*(SDL_Window*)*/ long m_window = NULL;
    private int m_width = 1280;
    private int m_height = 720;
    private long m_frameIndex = 0;
    private long m_nextSignalValue = MaxFramesInFlight + 1;

    // vulkan core
    private @Nullable VkDebugUtilsMessengerCallbackEXT m_debugCallback;
    private @Nullable VkInstance m_vulkanInstance = null;
    private /*(VkSurfaceKHR)*/ long m_surface = NULL;
    private @Nullable VkPhysicalDevice m_physicalDevice = null;
    private @Nullable VkDevice m_device = null;
    private /*(VmaAllocator)*/ long m_vmaAllocator = NULL;

    // queue related
    private int m_gfxQueueFamIdx = -1;
    private @Nullable VkQueue m_gfxQueue = null;
    private /*(VkCommandPool)*/ long m_commandPool = NULL;

    // swapchain related
    private /*(VkSwapchainKHR)*/ long m_swapchain = NULL;
    private /*(VkImage[])*/ long @Nullable [] m_swapchainImages = null;
    private /*(VkImageView[])*/ long @Nullable [] m_swapchainImageViews = null;
    private /*(VkSemaphore[])*/ long @Nullable [] m_renderCompleteSemaphores = null;
    private boolean m_requireSwapchainRecreate = false;
    private int m_swapchainWidth = -1;
    private int m_swapchainHeight = -1;

    private /*(VkImage)*/ long m_depthImage = NULL;
    private /*(VkImageView)*/ long m_depthImageView = NULL;
    private /*(VmaAllocation)*/ long m_depthImageAllocation = NULL;

    // graphics pipeline related
    private /*(VkPipelineLayout)*/ long m_pipelineLayout = NULL;
    private /*(VkPipeline)*/ long m_pipeline = NULL;

    // shader resources
    private /*(VkShaderModule)*/ long m_vertShader = NULL;
    private /*(VkShaderModule)*/ long m_fragShader = NULL;

    // frame and synchronization resources
    private /*(VkSemaphore)*/ long m_timelineSemaphore = NULL;
    private FrameResources @Nullable [] m_frameResources = null;

    // cpu resources
    private @Nullable Arena m_arena;
    private final List<Mesh> m_meshes = new ArrayList<>();
    private @Nullable StructArray<Vertex> m_vertices;
    private @Nullable MemorySegment m_indices;
    private int m_vertOffset = 0;
    private int m_idxOffset = 0;

    // Gpu resources
    private int m_whitePixelImageId = 0;
    private int m_vertexBufferId = 0;
    private int m_indexBufferId = 0;
    private int m_matBufferId = 0;
    private final List<GPUImage> m_images = new ArrayList<>();
    private final List</*(VkSampler)*/Long> m_samplers = new ArrayList<>();
    private final List<Texture> m_textures = new ArrayList<>();
    private final List<GPUBuffer> m_buffers = new ArrayList<>();
    private @Nullable StructArray<Material> m_materials;
    private int m_matOffset = 0;

    // descriptors
    private /*(VkDescriptorSetLayout)*/ long m_globalDSLayout = NULL;
    private /*(VkDescriptorSet)*/ long m_globalDescSet = NULL;
    private /*(VkDescriptorPool)*/ long m_descPool = NULL;

    // game nodes and scene data
    private @Nullable NodeWorld m_nodeWorld;
    private int m_rootNodeId = 0;
    private int m_lastRootNodeId = 0;
    private final Deque<Pair<Node, Matrix4dc>> m_nodeRenderStack = new ArrayDeque<>(128);

    // camera related
    private int m_cameraNodeId = 0;
    private final Camera m_camera = new Camera();
    private final Vector3d m_moveDirection = new Vector3d();

    private final Matrix4d m_matView = new Matrix4d();
    private final Matrix4d m_matProj = new Matrix4d();
    private final Matrix4d m_viewProjMatrix = new Matrix4d();

    // mouse related
    private double m_mouseXRel = 0D;
    private double m_mouseYRel = 0D;
    private double m_mouseSensitivity = 0.3D;

    private void showError(String errorMessage) {
        SDL_ShowSimpleMessageBox(SDL_MESSAGEBOX_ERROR, "Error", errorMessage, m_window);
    }

    public boolean initialize() {
        SDL_InitSubSystem(SDL_INIT_VIDEO);
        m_window = SDL_CreateWindow("Vulkan Learning", m_width, m_height, SDL_WINDOW_VULKAN | SDL_WINDOW_RESIZABLE);

        // setup renderer specifics here
        m_nodeWorld = new NodeWorld(1024); // maximum node-world size
        //        m_nodeRenderStack.reserve(128); // render-stack per-frame size

        if (m_window == NULL) {
            showError("Error creating window");
            return false;
        }
        SDL_SetWindowRelativeMouseMode(m_window, true);

        return initializeVulkan();
    }

    public boolean loadData() {
        assert m_device != null;
        assert m_vmaAllocator != NULL;
        assert m_nodeWorld != null;

        @Cleanup val stack = MemoryStack.stackPush();

        // Preallocate mem for vertex and index data
        m_arena = Arena.ofShared();
        m_vertices = Vertex.$.allocate(m_arena, MaxNumVertices);
        m_indices = FFM.uint32_t.allocateSegment(m_arena, MaxNumIndices);

        m_materials = Material.$.allocate(m_arena, MaxMaterials);

        // Fallback 1x1 white texture base color texture (0-index in tex array)
        val whitePixelData = FFM.uint32_t.allocateSegment(m_arena, 1);
        whitePixelData.asByteBuffer()
                      .putInt(0xFFFFFFFF); // RGBA

        val whitePixel = new Image(1, 1, 4, whitePixelData);

        val whiteImgCmdBuff = startTransientCommandBuffer();
        final int whiteImageId;
        final GPUBuffer whiteStagingBuffer;
        {
            val pair = createImage(whiteImgCmdBuff, whitePixel);
            whiteImageId = pair.fst();
            whiteStagingBuffer = pair.snd();
        }
        m_whitePixelImageId = whiteImageId;
        submitTransientCommandBuffer(whiteImgCmdBuff); // submit and wait
        vmaDestroyBuffer(m_vmaAllocator, whiteStagingBuffer.vkBuffer, whiteStagingBuffer.allocation);

        // Fallback texture sampler
        final long sampler;
        {
            val samplerInfo = VkSamplerCreateInfo.calloc(stack)
                                                 .sType$Default()
                                                 .magFilter(VK_FILTER_NEAREST)
                                                 .minFilter(VK_FILTER_NEAREST)
                                                 .addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                                                 .addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                                                 .addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                                                 .compareEnable(false);

            val ptr = stack.callocLong(1);
            if (vkCreateSampler(m_device, samplerInfo, null, ptr) != VK_SUCCESS) {
                showError("Unable to create texture sampler");
                return false;
            }
            sampler = ptr.get(0);
        }
        // store sampler and get ID
        m_samplers.add(sampler);
        val whiteSamplerId = m_samplers.size();

        m_textures.add(new Texture(m_whitePixelImageId, whiteSamplerId));

//        loadGltf("./glTF-Sample-Assets/Models/Sponza/glTF/Sponza.gltf");
        //        loadGltf("./glTF-Sample-Assets/Models/Cube/glTF/Cube.gltf");
        //        loadGltf("./glTF-Sample-Assets/Models/AntiqueCamera/glTF/AntiqueCamera.gltf");
        loadGltf("./modular-demo/modular-demo.gltf");


        // fallback camera setup
        if (m_cameraNodeId == 0) {
            val pair = m_nodeWorld.createNode();
            val camNode = pair.fst();
            m_cameraNodeId = pair.snd();

            camNode.setTranslation(new Vector3d(1D, 1.5D, 5D));
        }

        // Staging buffers for geo data
        val vertexBufferStage = createBuffer(VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                             VertexBufferBytes,
                                             true,
                                             VMA_MEMORY_USAGE_AUTO);
        if (vertexBufferStage.vkBuffer == NULL) {
            showError("Error creating vertex staging buffer");
            return false;
        }
        val indexBufferStage = createBuffer(VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                            IndexBufferBytes,
                                            true,
                                            VMA_MEMORY_USAGE_AUTO);
        if (indexBufferStage.vkBuffer == NULL) {
            showError("Error creating index staging buffer");
            return false;
        }
        // Device-local buffers
        val vertexBuffer = createBuffer(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
                                        VertexBufferBytes,
                                        false,
                                        VMA_MEMORY_USAGE_AUTO);
        if (vertexBuffer.vkBuffer == NULL) {
            showError("Error creating vertex buffer");
            return false;
        }
        m_vertexBufferId = addBuffer(vertexBuffer);
        mapCopyBufferData(vertexBufferStage, 0, m_vertices.segment());

        val indexBuffer = createBuffer(VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                       IndexBufferBytes,
                                       false,
                                       VMA_MEMORY_USAGE_AUTO);
        if (indexBuffer.vkBuffer == NULL) {
            showError("Error creating index buffer");
            return false;
        }
        m_indexBufferId = addBuffer(indexBuffer);
        mapCopyBufferData(indexBufferStage, 0, m_indices);

        // Copy staged geo data to VRAM
        val geoCmdBuffer = startTransientCommandBuffer();
        {
            val buffCopyVerts = VkBufferCopy.calloc(1, stack)
                                            .srcOffset(0L)
                                            .dstOffset(0L)
                                            .size(VertexBufferBytes);
            vkCmdCopyBuffer(geoCmdBuffer, vertexBufferStage.vkBuffer, vertexBuffer.vkBuffer, buffCopyVerts);
            val buffCopyIndices = VkBufferCopy.calloc(1, stack)
                                              .srcOffset(0L)
                                              .dstOffset(0L)
                                              .size(IndexBufferBytes);
            vkCmdCopyBuffer(geoCmdBuffer, indexBufferStage.vkBuffer, indexBuffer.vkBuffer, buffCopyIndices);
        }
        submitTransientCommandBuffer(geoCmdBuffer); // submit and wait

        // Delete staging geo buffers
        vmaDestroyBuffer(m_vmaAllocator, vertexBufferStage.vkBuffer, vertexBufferStage.allocation);
        vmaDestroyBuffer(m_vmaAllocator, indexBufferStage.vkBuffer, indexBufferStage.allocation);

        updateTextureDescriptors();

        // Material buffer
        val matDataBytes = m_materials.sizeof();
        val matBuffer = createBuffer(VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
                                     matDataBytes,
                                     true,
                                     VMA_MEMORY_USAGE_AUTO);
        if (matBuffer.vkBuffer == NULL) {
            showError("Error creating material buffer");
            return false;
        }
        m_matBufferId = addBuffer(matBuffer);
        mapCopyBufferData(matBuffer, 0, m_materials.segment());
        return true;
    }

    public void run() {
        assert m_nodeWorld != null;
        assert m_cameraNodeId != 0 : "Camera node must be initialized";
        updateProjectionMatrix();

        var running = true;
        @Cleanup val stack = MemoryStack.stackPush();

        var prevTime = SDL_GetTicks();
        val event = SDL_Event.calloc(stack);
        while (running) {
            val nowTime = SDL_GetTicks();
            val deltaTime = (nowTime - prevTime) / 1000D;
            prevTime = nowTime;

            while (SDL_PollEvent(event)) {
                switch (event.type()) {
                    case SDL_EVENT_QUIT: {
                        running = false;
                    }
                    case SDL_EVENT_WINDOW_RESIZED: {
                        val windowData = event.window();
                        m_width = windowData.data1();
                        m_height = windowData.data2();
                        updateProjectionMatrix();

                        if (running) { // Fixes crash from trying to re-create swapchain when the window is closing
                            m_requireSwapchainRecreate = true;
                        }
                    }
                    case SDL_EVENT_MOUSE_MOTION: {
                        val motionData = event.motion();

                        if (SDL_GetWindowRelativeMouseMode(m_window)) {
                            m_mouseXRel += motionData.xrel();
                            m_mouseYRel += motionData.yrel();
                        }
                    }
                    case SDL_EVENT_KEY_UP: {
                        val keyData = event.key();
                        val scancode = keyData.scancode();

                        if (scancode == SDL_SCANCODE_GRAVE || scancode == SDL_SCANCODE_ESCAPE) {
                            if (m_camera.type == CameraType.firstPerson) {
                                m_camera.type = CameraType.orbit;
                                SDL_SetWindowRelativeMouseMode(m_window, false);
                            } else {
                                m_camera.type = CameraType.firstPerson;
                                SDL_SetWindowRelativeMouseMode(m_window, true);
                            }
                        }
                    }
                }
            }

            // Handle basic cam movement
            val speed = 3D;
            val epsilon = 0.01D;
            val pitchLimit = HALF_PI - epsilon;

            final MemorySegment keys;
            {
                val buf = SDL_GetKeyboardState();
                if (buf == null) {
                    throw new IllegalStateException();
                }
                keys = MemorySegment.ofBuffer(buf);
            }

            if (m_camera.type == CameraType.orbit) {
                if (keys.get(FFM.bool.layout(), SDL_SCANCODE_LEFT)) {
                    m_camera.yaw += speed * deltaTime;
                }
                if (keys.get(FFM.bool.layout(), SDL_SCANCODE_RIGHT)) {
                    m_camera.yaw -= speed * deltaTime;
                }
                if (keys.get(FFM.bool.layout(), SDL_SCANCODE_W)) {
                    m_camera.distance -= speed * deltaTime;
                    m_camera.distance = Math.max(m_camera.distance, epsilon);
                }
                if (keys.get(FFM.bool.layout(), SDL_SCANCODE_S)) {
                    m_camera.distance += speed * deltaTime;
                }
                if (keys.get(FFM.bool.layout(), SDL_SCANCODE_UP)) {
                    m_camera.pitch += speed * deltaTime;
                    m_camera.pitch = Math.clamp(m_camera.pitch, -pitchLimit, pitchLimit);
                }
                if (keys.get(FFM.bool.layout(), SDL_SCANCODE_DOWN)) {
                    m_camera.pitch -= speed * deltaTime;
                    m_camera.pitch = Math.clamp(m_camera.pitch, -pitchLimit, pitchLimit);
                }
            } else if (m_camera.type == CameraType.firstPerson) {
                val camNode = m_nodeWorld.getNode(m_cameraNodeId);
                val translation = new Vector3d(camNode.getTranslation());
                if (keys.get(FFM.bool.layout(), SDL_SCANCODE_A)) {
                    translation.sub(new Vector3d(m_camera.right).mul(speed * deltaTime));
                }
                if (keys.get(FFM.bool.layout(), SDL_SCANCODE_D)) {
                    translation.add(new Vector3d(m_camera.right).mul(speed * deltaTime));
                }
                if (keys.get(FFM.bool.layout(), SDL_SCANCODE_W)) {
                    translation.add(new Vector3d(m_moveDirection).mul(speed * deltaTime));
                }
                if (keys.get(FFM.bool.layout(), SDL_SCANCODE_S)) {
                    translation.sub(new Vector3d(m_moveDirection).mul(speed * deltaTime));
                }
                camNode.setTranslation(translation);
            }

            updateViewMatrix();
            render();
        }
    }

    public void shutdown() {
        // wait in case resources are in use
        if (m_device != null) {
            vkDeviceWaitIdle(m_device);
        }

        // clean up descriptor layouts and pool
        if (m_globalDSLayout != NULL) {
            assert m_device != null;
            vkDestroyDescriptorSetLayout(m_device, m_globalDSLayout, null);
            m_globalDSLayout = NULL;
        }
        if (m_descPool != NULL) {
            assert m_device != null;
            vkDestroyDescriptorPool(m_device, m_descPool, null); // destroys descriptor sets implicitly
            m_descPool = NULL;
            m_globalDescSet = NULL;
        }

        // Delete textures and samplers
        for (val img : m_images) {
            if (img.imageView != NULL) {
                assert m_device != null;
                vkDestroyImageView(m_device, img.imageView, null);
                img.imageView = NULL;
            }
            if (img.image != NULL) {
                assert m_device != null;
                vkDestroyImage(m_device, img.image, null);
                img.image = NULL;
            }
            if (img.allocation != NULL) {
                assert m_vmaAllocator != NULL;
                vmaFreeMemory(m_vmaAllocator, img.allocation);
                img.allocation = NULL;
            }
        }
        m_whitePixelImageId = 0;
        m_images.clear();
        for (val sampler : m_samplers) {
            if (sampler != NULL) {
                assert m_device != null;
                vkDestroySampler(m_device, sampler, null);
            }
        }
        m_samplers.clear();
        m_textures.clear();

        // Delete buffers
        for (val buff : m_buffers) {
            if (buff.vkBuffer != NULL) {
                assert m_device != null;
                vkDestroyBuffer(m_device, buff.vkBuffer, null);
                buff.vkBuffer = NULL;
            }
            if (buff.allocation != NULL) {
                assert m_vmaAllocator != NULL;
                vmaFreeMemory(m_vmaAllocator, buff.allocation);
                buff.allocation = NULL;
            }
        }
        m_buffers.clear();

        // frame / sync object cleanup
        if (m_timelineSemaphore != NULL) {
            assert m_device != null;
            vkDestroySemaphore(m_device, m_timelineSemaphore, null);
        }
        if (m_frameResources != null) {
            assert m_device != null;
            for (val res : m_frameResources) {
                if (res.imageAcquiredSemaphore != NULL) {
                    vkDestroySemaphore(m_device, res.imageAcquiredSemaphore, null);
                    res.imageAcquiredSemaphore = NULL;
                }
                if (res.commandPool != NULL) {
                    vkDestroyCommandPool(m_device, res.commandPool, null); // destroys buffers implicitly
                    res.commandPool = NULL;
                    res.commandBuffer = null;
                }

                // Indirect draw
                if (res.indirectDrawBuffer != null) {
                    if (res.indirectDrawBuffer.allocation != NULL) {
                        vmaUnmapMemory(m_vmaAllocator, res.indirectDrawBuffer.allocation);
                        if (res.indirectDrawBuffer.vkBuffer != NULL) {
                            vkDestroyBuffer(m_device, res.indirectDrawBuffer.vkBuffer, null);
                            res.indirectDrawBuffer.vkBuffer = NULL;
                        }
                        vmaFreeMemory(m_vmaAllocator, res.indirectDrawBuffer.allocation);
                        res.indirectDrawBuffer.allocation = NULL;
                        res.indirectDrawBuffer.deviceAddress = NULL;
                        res.indirectDrawPtr = null;
                    } else {
                        assert res.indirectDrawBuffer.vkBuffer == NULL;
                        assert res.indirectDrawBuffer.deviceAddress == NULL;
                        assert res.indirectDrawPtr == null;
                    }
                }

                // Render items
                if (res.renderItemBuffer != null) {
                    if (res.renderItemBuffer.allocation != NULL) {
                        vmaUnmapMemory(m_vmaAllocator, res.renderItemBuffer.allocation);
                        if (res.renderItemBuffer.vkBuffer != NULL) {
                            vkDestroyBuffer(m_device, res.renderItemBuffer.vkBuffer, null);
                            res.renderItemBuffer.vkBuffer = NULL;
                        }
                        vmaFreeMemory(m_vmaAllocator, res.renderItemBuffer.allocation);
                        res.renderItemBuffer.allocation = NULL;
                        res.renderItemPtr = null;
                    } else {
                        assert res.renderItemBuffer.vkBuffer == NULL;
                        assert res.renderItemBuffer.deviceAddress == NULL;
                        assert res.renderItemPtr == null;
                    }
                }
            }
            m_frameResources = null;
        }
        // one-time use command buffer pool
        if (m_commandPool != NULL) {
            assert m_device != null;
            vkDestroyCommandPool(m_device, m_commandPool, null); // destroys buffers implicitly
            m_commandPool = NULL;
        }

        // Pipeline cleanup
        if (m_pipelineLayout != NULL) {
            assert m_device != null;
            vkDestroyPipelineLayout(m_device, m_pipelineLayout, null);
            m_pipelineLayout = NULL;
        }
        if (m_pipeline != NULL) {
            assert m_device != null;
            vkDestroyPipeline(m_device, m_pipeline, null);
            m_pipeline = NULL;
        }

        // Cleanup shaders
        if (m_vertShader != NULL) {
            assert m_device != null;
            vkDestroyShaderModule(m_device, m_vertShader, null);
            m_vertShader = NULL;
        }
        if (m_fragShader != NULL) {
            assert m_device != null;
            vkDestroyShaderModule(m_device, m_fragShader, null);
            m_fragShader = NULL;
        }

        // Cleanup swapchain
        destroySwapchain();

        // VMA
        if (m_vmaAllocator != NULL) {
            vmaDestroyAllocator(m_vmaAllocator);
            m_vmaAllocator = NULL;
        }

        // Cleanup Vulkan
        m_gfxQueue = null;
        if (m_device != null) {
            vkDestroyDevice(m_device, null);
            m_device = null;
        }
        m_gfxQueueFamIdx = -1;
        m_physicalDevice = null;
        if (m_surface != NULL) {
            assert m_vulkanInstance != null;
            vkDestroySurfaceKHR(m_vulkanInstance, m_surface, null);
            m_surface = NULL;
        }
        if (m_vulkanInstance != null) {
            vkDestroyInstance(m_vulkanInstance, null);
            m_vulkanInstance = null;
        }
        if (m_debugCallback != null) {
            m_debugCallback.free();
            m_debugCallback = null;
        }
        VK.destroy();

        m_meshes.clear();
        m_matOffset = 0;
        m_idxOffset = 0;
        m_vertOffset = 0;
        m_materials = null;
        m_vertices = null;
        m_indices = null;
        if (m_arena != null) {
            m_arena.close();
            m_arena = null;
        }

        // Cleanup SDL
        if (m_window != NULL) {
            SDL_DestroyWindow(m_window);
            m_window = NULL;
        }
        SDL_Quit();
    }

    private boolean initializeVulkan() {
        if (!createVulkanInstance()) {
            showError("Couldn't create a vulkan instance");
            return false;
        }

        if (!createSurface()) {
            showError("Couldn't create window surface");
            return false;
        }

        if (!findPhysicalDevice()) {
            showError("Unable to find an appropriate physical device");
            return false;
        }

        if (!findGraphicsQueue()) {
            showError("Unable to find a compatible graphics queue");
            return false;
        }

        if (!createDevice()) {
            showError("Couldn't create the logical GPU device");
            return false;
        }

        if (!initializeVMA()) {
            showError("Unable to create Vulkan Memory Allocator");
            return false;
        }

        if (!createSwapchain()) {
            showError("Unable to create swapchain");
            return false;
        }

        if (!createShaders()) {
            showError("Error creating shader modules");
            return false;
        }

        if (!createDescriptorSets()) {
            showError("Error creating descriptor sets");
            return false;
        }

        if (!createGraphicsPipeline()) {
            showError("Unable to initialize the graphics pipeline");
            return false;
        }

        if (!createSyncResources()) {
            showError("Couldn't create the sync related resources");
            return false;
        }

        if (!createCommandBuffers()) {
            showError("Couldn't create command buffer objects");
            return false;
        }

        if (!createIndirectDrawBuffers()) {
            showError("Couldn't create buffers for indirect-drawing");
            return false;
        }

        return true;
    }

    private boolean createVulkanInstance() {
        @Cleanup val stack = MemoryStack.stackPush();

        // Initialize VK function pointers from SDL
        val vkGetInstanceProcAddr = SDL_Vulkan_GetVkGetInstanceProcAddr();
        VK.create(pName -> {
            // Unwrapping here because otherwise null terminator is included
            val nName = MemoryUtil.memAddress(pName);
            val name = MemoryUtil.memASCIISafe(nName);
            if ("vkGetInstanceProcAddr".equals(name)) {
                return vkGetInstanceProcAddr;
            }
            throw new IllegalArgumentException("Expected 'vkGetInstanceProcAddr', got: '%s'".formatted(name));
        });

        // Create the vulkan application instance
        val appInfo = VkApplicationInfo.calloc(stack)
                                       .sType$Default()
                                       .pApplicationName(stack.UTF8("My First Triangle"))
                                       .apiVersion(VulkanVersion);

        // Instance layers with just the validation layer
        final PointerBuffer layers;
        {
            val list = new ArrayDeque<String>();
            list.add("VK_LAYER_KHRONOS_validation");

            layers = stack.callocPointer(list.size());
            while (layers.hasRemaining()) {
                layers.put(stack.UTF8(list.pop()));
            }
            layers.flip();
        }

        // Instance extensions with what SDL needs plus debug utils
        final PointerBuffer extensions;
        {
            val list = new ArrayDeque<String>();
            list.add("VK_EXT_debug_utils");

            val sdlExtensions = Objects.requireNonNull(SDL_Vulkan_GetInstanceExtensions());
            while (sdlExtensions.hasRemaining()) {
                list.add(MemoryUtil.memASCII(sdlExtensions.get()));
            }

            extensions = stack.callocPointer(list.size());
            while (extensions.hasRemaining()) {
                extensions.put(stack.UTF8(list.pop()));
            }
            extensions.flip();
        }

        // Debug callback with everything enabled
        final VkDebugUtilsMessengerCreateInfoEXT instCreateInfoDbg;
        {
            val severity = VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT |
                           VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT |
                           VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT |
                           VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
            val type = VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
                       VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
                       VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
            m_debugCallback = VkDebugUtilsMessengerCallbackEXT.create(Application.this::debugCallback);
            instCreateInfoDbg = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack)
                                                                  .sType$Default()
                                                                  .flags(0)
                                                                  .messageSeverity(severity)
                                                                  .messageType(type)
                                                                  .pfnUserCallback(m_debugCallback);
        }

        val instCreateInfo = VkInstanceCreateInfo.calloc(stack)
                                                 .sType$Default()
                                                 .pNext(instCreateInfoDbg)
                                                 .pApplicationInfo(appInfo)
                                                 .ppEnabledLayerNames(layers)
                                                 .ppEnabledExtensionNames(extensions);

        val ptr = stack.callocPointer(1);
        if (vkCreateInstance(instCreateInfo, null, ptr) != VK_SUCCESS) {
            return false;
        }

        m_vulkanInstance = new VkInstance(ptr.get(0), instCreateInfo);
        return true;
    }

    private boolean createSurface() {
        assert m_window != NULL;
        assert m_vulkanInstance != null;

        @Cleanup val stack = MemoryStack.stackPush();
        val ptr = stack.callocLong(1);
        if (!SDL_Vulkan_CreateSurface(m_window, m_vulkanInstance, null, ptr)) {
            return false;
        }

        m_surface = ptr.get(0);
        return true;
    }

    private boolean findPhysicalDevice() {
        assert m_vulkanInstance != null;
        assert m_surface != NULL;

        @Cleanup val stack = MemoryStack.stackPush();

        // Try to find a suitable device, preferably a dedicated gpu
        {
            val list = new ArrayList<VkPhysicalDevice>();

            // Enumerate all physical devices
            val count = stack.callocInt(1);
            vkEnumeratePhysicalDevices(m_vulkanInstance, count, null);
            val ptr = stack.callocPointer(count.get(0));
            vkEnumeratePhysicalDevices(m_vulkanInstance, count, ptr);

            while (ptr.hasRemaining()) {
                list.add(new VkPhysicalDevice(ptr.get(), m_vulkanInstance));
            }

            if (list.isEmpty()) {
                // No devices found
                return false;
            }

            val props = VkPhysicalDeviceProperties.calloc(stack);
            // if you have issues, you can always just hardcode a GPU index while learning
            var selectedDev = list.getFirst(); // default to first GPU
            // look through list and see if a dGPU exists
            for (val dev : list) {
                vkGetPhysicalDeviceProperties(dev, props);
                // Select desired GPU by name :)
                if ("AMD Radeon RX 9070 XT (RADV GFX1201)".equals(props.deviceNameString())) {
                    selectedDev = dev;
                    break;
                }

                //                if (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                //                    selectedDev = dev;
                //                    break;
                //                }
            }

            m_physicalDevice = selectedDev;
        }
        assert m_physicalDevice != null;

        // Ensure the desired swapchain format is supported
        {
            val count = stack.callocInt(1);
            vkGetPhysicalDeviceSurfaceFormatsKHR(m_physicalDevice, m_surface, count, null);
            val formats = VkSurfaceFormatKHR.calloc(count.get(0), stack);
            vkGetPhysicalDeviceSurfaceFormatsKHR(m_physicalDevice, m_surface, count, formats);

            var isSupported = false;
            for (val format : formats) {
                if (format.format() == swapchainFormat) {
                    isSupported = true;
                    break;
                }
            }

            if (!isSupported) {
                showError("Requested swapchain format is not supported by the surface");
                return false;
            }
        }

        return true;
    }

    private boolean findGraphicsQueue() {
        // Eventually we'll have more complex queue lookup for presentation, etc.

        assert m_surface != NULL;
        assert m_physicalDevice != null;
        @Cleanup val stack = MemoryStack.stackPush();

        final int numQueues;
        final VkQueueFamilyProperties2.Buffer propsList;
        // Grab all the queue families
        {
            val count = stack.callocInt(1);
            vkGetPhysicalDeviceQueueFamilyProperties2(m_physicalDevice, count, null);

            propsList = VkQueueFamilyProperties2.calloc(count.get(0), stack);
            propsList.forEach(VkQueueFamilyProperties2::sType$Default);

            vkGetPhysicalDeviceQueueFamilyProperties2(m_physicalDevice, count, propsList);
            numQueues = count.get(0);
        }

        if (numQueues < 1) {
            // No queues found
            return false;
        }

        val hasPresentSupport = stack.callocInt(1);
        for (var i = 0; i < numQueues; i++) {
            // Ensure this queue has presentation support
            vkGetPhysicalDeviceSurfaceSupportKHR(m_physicalDevice, i, m_surface, hasPresentSupport);
            if (hasPresentSupport.get(0) != VK_TRUE) {
                continue;
            }

            // Ensure this is a GRAPHICS queue
            val props = propsList.get(i);
            val familyProps = props.queueFamilyProperties();
            if ((familyProps.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                m_gfxQueueFamIdx = i;
                return true;
            }
        }
        return false;
    }

    private boolean createDevice() {
        assert m_physicalDevice != null;
        assert m_gfxQueueFamIdx != -1;

        @Cleanup val stack = MemoryStack.stackPush();

        // Ensure the physical device supports what we need
        {
            // @formatter:off
            val feats   = VkPhysicalDeviceFeatures2.calloc(stack).sType$Default();
            val feats11 = VkPhysicalDeviceVulkan11Features.calloc(stack).sType$Default();
            val feats12 = VkPhysicalDeviceVulkan12Features.calloc(stack).sType$Default();
            val feats13 = VkPhysicalDeviceVulkan13Features.calloc(stack).sType$Default();
            val feats14 = VkPhysicalDeviceVulkan14Features.calloc(stack).sType$Default();
            // @formatter:on

            // LWJGL will prepend these into the chain
            feats.pNext(feats14);
            feats.pNext(feats13);
            feats.pNext(feats12);
            feats.pNext(feats11);

            // Query supported features
            vkGetPhysicalDeviceFeatures2(m_physicalDevice, feats);

            // @formatter:off
            var allSupported = true;
            allSupported &= feats13.dynamicRendering();
            allSupported &= feats13.synchronization2();
            allSupported &= feats12.timelineSemaphore();
            allSupported &= feats12.bufferDeviceAddress();
            allSupported &= feats12.scalarBlockLayout();
            allSupported &= feats12.descriptorIndexing();
            allSupported &= feats12.descriptorBindingSampledImageUpdateAfterBind();
            allSupported &= feats12.descriptorBindingPartiallyBound();
            allSupported &= feats12.runtimeDescriptorArray();
            allSupported &= feats.features().shaderInt64();
            allSupported &= feats.features().multiDrawIndirect();
            // @formatter:on

            // Check if what we need is supported
            if (!allSupported) {
                showError("Physical device doesn't meet the feature requirements");
                return false;
            }
        }

        // Produce a separate features struct chain for device creation
        final VkPhysicalDeviceFeatures2 enabledFeatures;
        {
            // @formatter:off
            val feats   = VkPhysicalDeviceFeatures2.calloc(stack).sType$Default();
            val feats11 = VkPhysicalDeviceVulkan11Features.calloc(stack).sType$Default();
            val feats12 = VkPhysicalDeviceVulkan12Features.calloc(stack).sType$Default();
            val feats13 = VkPhysicalDeviceVulkan13Features.calloc(stack).sType$Default();
            val feats14 = VkPhysicalDeviceVulkan14Features.calloc(stack).sType$Default();
            // @formatter:on

            // LWJGL will prepend these into the chain
            feats.pNext(feats14);
            feats.pNext(feats13);
            feats.pNext(feats12);
            feats.pNext(feats11);

            // Enabling only what we need
            // @formatter:off
            feats13.dynamicRendering(true);
            feats13.synchronization2(true);
            feats12.timelineSemaphore(true);
            feats12.bufferDeviceAddress(true);
            feats12.scalarBlockLayout(true);
            feats12.descriptorIndexing(true);
            feats12.descriptorBindingSampledImageUpdateAfterBind(true);
            feats12.descriptorBindingPartiallyBound(true);
            feats12.runtimeDescriptorArray(true);
            feats.features().shaderInt64(true);
            feats.features().multiDrawIndirect(true);
            // @formatter:on

            enabledFeatures = feats;
        }

        // Create a list of queue infos, but populate only the first one for now
        val gfxQueueInfos = VkDeviceQueueCreateInfo.calloc(1, stack);
        gfxQueueInfos.forEach(VkDeviceQueueCreateInfo::sType$Default);
        //noinspection resource
        gfxQueueInfos.get(0)
                     .queueFamilyIndex(m_gfxQueueFamIdx)
                     .pQueuePriorities(stack.floats(1F));

        // Device extension names, in particular for the swapchain
        final PointerBuffer extensions;
        {
            val list = new ArrayDeque<String>();
            list.add(VK_KHR_SWAPCHAIN_EXTENSION_NAME);

            extensions = stack.callocPointer(list.size());
            while (extensions.hasRemaining()) {
                extensions.put(stack.UTF8(list.pop()));
            }
            extensions.flip();
        }

        // Create the actual device
        {
            val devCreateInfo = VkDeviceCreateInfo.calloc(stack)
                                                  .sType$Default()
                                                  // Just append
                                                  .pNext(enabledFeatures.address())
                                                  .pQueueCreateInfos(gfxQueueInfos)
                                                  .ppEnabledExtensionNames(extensions)
                                                  .pEnabledFeatures(null);
            val ptr = stack.callocPointer(1);
            if (vkCreateDevice(m_physicalDevice, devCreateInfo, null, ptr) != VK_SUCCESS) {
                return false;
            }
            m_device = new VkDevice(ptr.get(0), m_physicalDevice, devCreateInfo);
        }

        // Grab the VkQueue object finally
        {
            val ptr = stack.callocPointer(1);
            vkGetDeviceQueue(m_device, m_gfxQueueFamIdx, 0, ptr);
            if (ptr.get(0) == NULL) {
                showError("Couldn't get the graphics queue");
                return false;
            }

            m_gfxQueue = new VkQueue(ptr.get(0), m_device);
        }
        return true;
    }

    private boolean initializeVMA() {
        assert m_vulkanInstance != null;
        assert m_physicalDevice != null;
        assert m_device != null;

        @Cleanup val stack = MemoryStack.stackPush();

        // See: https://github.com/LWJGL/lwjgl3/blob/8adda2367a10f0283ba7ac27a2222fcf7fc67212/modules/samples/src/test/java/org/lwjgl/demo/vulkan/khronos/HelloTriangle.java#L406-L416
        val vmaFuncInfo = VmaVulkanFunctions.calloc(stack)
                                            .set(m_vulkanInstance, m_device);

        val vmaAllocInfo = VmaAllocatorCreateInfo.calloc(stack)
                                                 .flags(VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT)
                                                 .physicalDevice(m_physicalDevice)
                                                 .device(m_device)
                                                 .pVulkanFunctions(vmaFuncInfo)
                                                 .instance(m_vulkanInstance)
                                                 .vulkanApiVersion(VulkanVersion);

        val ptr = stack.callocPointer(1);
        if (vmaCreateAllocator(vmaAllocInfo, ptr) != VK_SUCCESS) {
            return false;
        }
        m_vmaAllocator = ptr.get(0);
        return true;
    }

    private boolean createSwapchain() {
        assert m_physicalDevice != null;
        assert m_surface != NULL;
        assert m_device != null;
        assert m_vmaAllocator != NULL;

        @Cleanup val stack = MemoryStack.stackPush();

        m_swapchainWidth = m_width;
        m_swapchainHeight = m_height;

        // Create new swapchain
        {
            val surfaceCaps = VkSurfaceCapabilitiesKHR.calloc(stack);
            if (vkGetPhysicalDeviceSurfaceCapabilitiesKHR(m_physicalDevice, m_surface, surfaceCaps) != VK_SUCCESS) {
                showError("Couldn't get the surface capabilities");
                return false;
            }

            val swapchainInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                                                        .sType$Default()
                                                        .surface(m_surface)
                                                        .minImageCount(surfaceCaps.minImageCount())
                                                        .imageFormat(swapchainFormat)
                                                        .imageColorSpace(VK_COLORSPACE_SRGB_NONLINEAR_KHR)
                                                        .imageExtent(ext -> ext.set(m_swapchainWidth,
                                                                                    m_swapchainHeight))
                                                        .imageArrayLayers(1)
                                                        .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                                                        .preTransform(VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR)
                                                        .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                                                        .presentMode(VK_PRESENT_MODE_FIFO_KHR);
            val ptr = stack.callocLong(1);
            if (vkCreateSwapchainKHR(m_device, swapchainInfo, null, ptr) != VK_SUCCESS) {
                showError("Error creating swapchain");
                return false;
            }
            m_swapchain = ptr.get(0);
        }

        // Grab the swapchain images
        final int numImages;
        {
            val count = stack.callocInt(1);
            vkGetSwapchainImagesKHR(m_device, m_swapchain, count, null);
            val ptr = stack.callocLong(count.get(0));
            vkGetSwapchainImagesKHR(m_device, m_swapchain, count, ptr);
            numImages = count.get(0);

            m_swapchainImages = new long[numImages];
            ptr.get(m_swapchainImages);
        }

        // Create the swapchain image views
        m_swapchainImageViews = new long[numImages];
        {
            val imgViewInfo = VkImageViewCreateInfo.calloc(stack)
                                                   .sType$Default()
                                                   .viewType(VK_IMAGE_VIEW_TYPE_2D)
                                                   .format(swapchainFormat)
                                                   .subresourceRange(s -> s.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                                                                           .levelCount(1)
                                                                           .layerCount(1));
            val ptr = stack.callocLong(1);
            for (var i = 0; i < numImages; i++) {
                imgViewInfo.image(m_swapchainImages[i]);
                if (vkCreateImageView(m_device, imgViewInfo, null, ptr) != VK_SUCCESS) {
                    showError("Error creating swapchain image view");
                    return false;
                }
                m_swapchainImageViews[i] = ptr.get(0);
            }
        }

        // Semaphores used to signal render completion
        m_renderCompleteSemaphores = new long[numImages];
        {
            val semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack)
                                                     .sType$Default();
            val ptr = stack.callocLong(1);
            for (var i = 0; i < numImages; i++) {
                if (vkCreateSemaphore(m_device, semaphoreInfo, null, ptr) != VK_SUCCESS) {
                    showError("Error creating the render-complete semaphore");
                    return false;
                }
                m_renderCompleteSemaphores[i] = ptr.get(0);
            }
        }

        // Create depth image
        {
            val depthInfo = VkImageCreateInfo.calloc(stack)
                                             .sType$Default()
                                             .imageType(VK_IMAGE_TYPE_2D)
                                             .format(depthFormat)
                                             .extent(s -> s.set(m_swapchainWidth, m_swapchainHeight, 1))
                                             .mipLevels(1)
                                             .arrayLayers(1)
                                             .samples(VK_SAMPLE_COUNT_1_BIT)
                                             .tiling(VK_IMAGE_TILING_OPTIMAL)
                                             .usage(VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT)
                                             .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            val allocInfo = VmaAllocationCreateInfo.calloc(stack)
                                                   .flags(VMA_ALLOCATION_CREATE_DEDICATED_MEMORY_BIT)
                                                   .usage(VMA_MEMORY_USAGE_AUTO);

            val imgPtr = stack.callocLong(1);
            val allocPtr = stack.callocPointer(1);
            if (vmaCreateImage(m_vmaAllocator, depthInfo, allocInfo, imgPtr, allocPtr, null) != VK_SUCCESS) {
                showError("Error allocating depth image");
                return false;
            }

            m_depthImage = imgPtr.get(0);
            m_depthImageAllocation = allocPtr.get(0);
        }

        // Create depth image view
        {
            val depthImgViewInfo = VkImageViewCreateInfo.calloc(stack)
                                                        .sType$Default()
                                                        .image(m_depthImage)
                                                        .viewType(VK_IMAGE_VIEW_TYPE_2D)
                                                        .format(depthFormat)
                                                        .subresourceRange(s -> s.aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT)
                                                                                .levelCount(1)
                                                                                .layerCount(1));
            val ptr = stack.callocLong(1);
            if (vkCreateImageView(m_device, depthImgViewInfo, null, ptr) != VK_SUCCESS) {
                showError("Error creating depth image view");
                return false;
            }
            m_depthImageView = ptr.get(0);
        }

        return true;
    }

    private void destroySwapchain() {
        assert m_device != null;

        if (m_swapchainImageViews != null) {
            for (var i = 0; i < m_swapchainImageViews.length; i++) {
                var swapchainImgView = m_swapchainImageViews[i];
                if (swapchainImgView != NULL) {
                    vkDestroyImageView(m_device, swapchainImgView, null);
                    m_swapchainImageViews[i] = NULL;
                }
            }
            m_swapchainImageViews = null;
        }

        // Destroy render-complete semaphores
        if (m_renderCompleteSemaphores != null) {
            for (var i = 0; i < m_renderCompleteSemaphores.length; i++) {
                var semaphore = m_renderCompleteSemaphores[i];
                if (semaphore != NULL) {
                    vkDestroySemaphore(m_device, semaphore, null);
                    m_renderCompleteSemaphores[i] = NULL;
                }
            }
            m_renderCompleteSemaphores = null;
        }

        if (m_swapchain != NULL) {
            vkDestroySwapchainKHR(m_device, m_swapchain, null);
            m_swapchain = NULL;
        }

        if (m_depthImageView != NULL) {
            vkDestroyImageView(m_device, m_depthImageView, null);
            m_depthImageView = NULL;
        }

        if (m_depthImageAllocation != NULL) {
            assert m_depthImage != NULL;

            vmaDestroyImage(m_vmaAllocator, m_depthImage, m_depthImageAllocation);
            m_depthImage = NULL;
            m_depthImageAllocation = NULL;
        }
    }

    private @NativeType("VkShaderModule") long createShaderModule(String fileName,
                                                                  @NativeType("shaderc_shader_kind") int kind) {
        assert m_device != null;

        val shaderPath = "shaders/" + fileName;
        val src = readTextFile(shaderPath);
        if (src.isEmpty()) {
            showError("Specified shader file doesn't exist: " + shaderPath);
            return NULL;
        }

        // Compile the shader to SPIR-V
        IO.println("Compiling shader: %s".formatted(shaderPath));

        long compiler = NULL;
        long opts = NULL;
        long result = NULL;

        try (val stack = MemoryStack.stackPush()) {
            compiler = shaderc_compiler_initialize();
            opts = shaderc_compile_options_initialize();

            shaderc_compile_options_set_target_env(opts, shaderc_target_env_vulkan, shaderc_env_version_vulkan_1_4);
            shaderc_compile_options_set_target_spirv(opts, shaderc_spirv_version_1_6);
            shaderc_compile_options_set_optimization_level(opts, shaderc_optimization_level_performance);

            result = shaderc_compile_into_spv(compiler, src, kind, fileName, shaderEntryPoint, opts);

            if (shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
                IO.println("Shader Compilation Error: %s".formatted(shaderc_result_get_error_message(result)));
                return NULL;
            }

            // Memory here is managed by shaderc
            val spv = shaderc_result_get_bytes(result);
            assert spv != null;

            val moduleInfo = VkShaderModuleCreateInfo.calloc(stack)
                                                     .sType$Default()
                                                     .pCode(spv);
            val ptr = stack.callocLong(1);
            if (vkCreateShaderModule(m_device, moduleInfo, null, ptr) != VK_SUCCESS) {
                showError("Error creating shader module");
                return NULL;
            }
            return ptr.get(0);
        } finally {
            if (result != NULL) {
                shaderc_result_release(result);
            }
            if (opts != NULL) {
                shaderc_compile_options_release(opts);
            }
            if (compiler != NULL) {
                shaderc_compiler_release(compiler);
            }
        }
    }

    private boolean createShaders() {
        // Create the shader modules that we'll need for the graphics pipeline
        m_vertShader = createShaderModule("shader.vert", shaderc_vertex_shader);
        m_fragShader = createShaderModule("shader.frag", shaderc_fragment_shader);
        return m_vertShader != NULL && m_fragShader != NULL;
    }

    private boolean createDescriptorSets() {
        assert m_device != null;

        @Cleanup val stack = MemoryStack.stackPush();

        {
            val poolSizes = VkDescriptorPoolSize.calloc(1, stack);
            //noinspection resource
            poolSizes.get(0)
                     .type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                     .descriptorCount(MaxTextures);

            val poolInfo = VkDescriptorPoolCreateInfo.calloc(stack)
                                                     .sType$Default()
                                                     .flags(VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT)
                                                     .maxSets(1)
                                                     .pPoolSizes(poolSizes);
            val ptr = stack.callocLong(1);
            if (vkCreateDescriptorPool(m_device, poolInfo, null, ptr) != VK_SUCCESS) {
                showError("Unable to create descriptor pool");
                return false;
            }
            m_descPool = ptr.get(0);
        }

        // Global descriptor set
        {
            val bindings = VkDescriptorSetLayoutBinding.calloc(1, stack);
            //noinspection resource
            bindings.get(0)
                    .binding(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(MaxTextures)
                    .stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            val flags = stack.ints(VK_DESCRIPTOR_BINDING_PARTIALLY_BOUND_BIT |
                                   VK_DESCRIPTOR_BINDING_UPDATE_AFTER_BIND_BIT);
            val flagsInfo = VkDescriptorSetLayoutBindingFlagsCreateInfo.calloc(stack)
                                                                       .sType$Default()
                                                                       .pBindingFlags(flags);

            val layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack)
                                                            .sType$Default()
                                                            .pNext(flagsInfo)
                                                            .flags(VK_DESCRIPTOR_SET_LAYOUT_CREATE_UPDATE_AFTER_BIND_POOL_BIT)
                                                            .pBindings(bindings);
            val ptr = stack.callocLong(1);
            if (vkCreateDescriptorSetLayout(m_device, layoutInfo, null, ptr) != VK_SUCCESS) {
                showError("Unable to create descriptor set layout");
                return false;
            }
            m_globalDSLayout = ptr.get(0);
        }

        // Create the actual descriptor sets
        {
            val descSetAllocInfo = VkDescriptorSetAllocateInfo.calloc(stack)
                                                              .sType$Default()
                                                              .descriptorPool(m_descPool)
                                                              .pSetLayouts(stack.longs(m_globalDSLayout));
            val ptr = stack.callocLong(1);
            if (vkAllocateDescriptorSets(m_device, descSetAllocInfo, ptr) != VK_SUCCESS) {
                showError("Unable to allocate descriptor set");
                return false;
            }
            m_globalDescSet = ptr.get(0);
        }

        return true;
    }

    private boolean createGraphicsPipeline() {
        assert m_device != null;

        @Cleanup val stack = MemoryStack.stackPush();

        // Need to define a pipeline layout
        {
            val pushConstRange = VkPushConstantRange.calloc(1, stack)
                                                    .stageFlags(VK_SHADER_STAGE_VERTEX_BIT |
                                                                VK_SHADER_STAGE_FRAGMENT_BIT)
                                                    .offset(0)
                                                    .size((int) FrameConstants.$.sizeof());
            val pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                                                               .sType$Default()
                                                               .pSetLayouts(stack.longs(m_globalDSLayout))
                                                               .pPushConstantRanges(pushConstRange);
            val ptr = stack.callocLong(1);
            if (vkCreatePipelineLayout(m_device, pipelineLayoutInfo, null, ptr) != VK_SUCCESS) {
                showError("Unable to create the pipeline layout");
                return false;
            }
            m_pipelineLayout = ptr.get(0);
        }

        // Configure the shader stages struct
        final VkPipelineShaderStageCreateInfo.Buffer shaderStages;
        {
            val entryPointPtr = stack.UTF8(shaderEntryPoint);
            shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
            shaderStages.forEach(ci -> ci.sType$Default()
                                         .pName(entryPointPtr));
            //noinspection resource
            shaderStages.get(0)
                        .stage(VK_SHADER_STAGE_VERTEX_BIT)
                        .module(m_vertShader);
            //noinspection resource
            shaderStages.get(1)
                        .stage(VK_SHADER_STAGE_FRAGMENT_BIT)
                        .module(m_fragShader);
        }

        // Vertex pulling, don't define vertex input details
        val vertInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack)
                                                                .sType$Default();

        // Input assembly, we'll be drawing triangle lists
        val inputAssemblyInfo = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                                                                      .sType$Default()
                                                                      .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);

        // Depth/Stencil configuration
        val depthStencilInfo = VkPipelineDepthStencilStateCreateInfo.calloc(stack)
                                                                    .sType$Default()
                                                                    .depthTestEnable(true)
                                                                    .depthWriteEnable(true)
                                                                    .depthCompareOp(VK_COMPARE_OP_LESS)
                                                                    .stencilTestEnable(false);

        // Dynamic rendering allows to set this up...dynamically
        // We still need this struct though
        val viewportInfo = VkPipelineViewportStateCreateInfo.calloc(stack)
                                                            .sType$Default()
                                                            .viewportCount(1)
                                                            .pViewports(null)
                                                            .scissorCount(1)
                                                            .pScissors(null);

        // Rasterizer settings
        val rasterInfo = VkPipelineRasterizationStateCreateInfo.calloc(stack)
                                                               .sType$Default()
                                                               .polygonMode(VK_POLYGON_MODE_FILL)
                                                               .cullMode(VK_CULL_MODE_BACK_BIT)
                                                               .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
                                                               .lineWidth(1F);

        // No multisampling
        val multiSampleInfo = VkPipelineMultisampleStateCreateInfo.calloc(stack)
                                                                  .sType$Default()
                                                                  .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
        //Alpha-blending (disabled for now), still need attachment info and write mask
        val attachStates = VkPipelineColorBlendAttachmentState.calloc(1, stack);
        //noinspection resource
        attachStates.get(0)
                    .blendEnable(false)
                    .colorWriteMask(VK_COLOR_COMPONENT_R_BIT |
                                    VK_COLOR_COMPONENT_G_BIT |
                                    VK_COLOR_COMPONENT_B_BIT |
                                    VK_COLOR_COMPONENT_A_BIT);
        val blendInfo = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                                                           .sType$Default()
                                                           .pAttachments(attachStates);

        // Enable dynamic state
        val dynamicStateInfo = VkPipelineDynamicStateCreateInfo.calloc(stack)
                                                               .sType$Default()
                                                               .pDynamicStates(stack.ints(VK_DYNAMIC_STATE_VIEWPORT,
                                                                                          VK_DYNAMIC_STATE_SCISSOR));

        // Structure required for dynamic rendering
        val renderInfo = VkPipelineRenderingCreateInfo.calloc(stack)
                                                      .sType$Default()
                                                      .pColorAttachmentFormats(stack.ints(swapchainFormat))
                                                      .depthAttachmentFormat(depthFormat);

        // Create the graphics pipeline
        val pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
        pipelineInfo.forEach(VkGraphicsPipelineCreateInfo::sType$Default);
        pipelineInfo.get(0)
                    .pNext(renderInfo)
                    .pStages(shaderStages)
                    .pVertexInputState(vertInputInfo)
                    .pInputAssemblyState(inputAssemblyInfo)
                    .pViewportState(viewportInfo)
                    .pRasterizationState(rasterInfo)
                    .pMultisampleState(multiSampleInfo)
                    .pDepthStencilState(depthStencilInfo)
                    .pColorBlendState(blendInfo)
                    .pDynamicState(dynamicStateInfo)
                    .layout(m_pipelineLayout)
                    .renderPass(VK_NULL_HANDLE);

        val ptr = stack.callocLong(1);
        if (vkCreateGraphicsPipelines(m_device, NULL, pipelineInfo, null, ptr) != VK_SUCCESS) {
            showError("Error creating the pipeline");
            return false;
        }

        m_pipeline = ptr.get(0);
        return true;
    }

    private boolean createSyncResources() {
        assert m_device != null;

        @Cleanup val stack = MemoryStack.stackPush();

        {
            val semaphoreTypeInfo = VkSemaphoreTypeCreateInfo.calloc(stack)
                                                             .sType$Default()
                                                             .semaphoreType(VK_SEMAPHORE_TYPE_TIMELINE)
                                                             .initialValue(MaxFramesInFlight);
            val semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack)
                                                     .sType$Default()
                                                     .pNext(semaphoreTypeInfo);

            val ptr = stack.callocLong(1);
            if (vkCreateSemaphore(m_device, semaphoreInfo, null, ptr) != VK_SUCCESS) {
                showError("Unable to create the timeline semaphore");
                return false;
            }

            m_timelineSemaphore = ptr.get(0);
        }


        // Initialize frame resources
        m_frameResources = IntStream.range(0, MaxFramesInFlight)
                                    .mapToObj(_ -> new FrameResources())
                                    .toArray(FrameResources[]::new);

        // per-frame image-acquire semaphores
        val semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack)
                                                 .sType$Default();
        val ptr = stack.callocLong(1);
        for (val res : m_frameResources) {
            if (vkCreateSemaphore(m_device, semaphoreInfo, null, ptr) != VK_SUCCESS) {
                showError("Error creating the per-frame image-acquire semaphore");
                return false;
            }
            res.imageAcquiredSemaphore = ptr.get(0);
        }
        return true;
    }

    private boolean createCommandBuffers() {
        assert m_device != null;
        assert m_frameResources != null;

        @Cleanup val stack = MemoryStack.stackPush();

        // Create a command pool for single use command buffers
        {
            val poolInfo = VkCommandPoolCreateInfo.calloc(stack)
                                                  .sType$Default()
                                                  .flags(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT)
                                                  .queueFamilyIndex(m_gfxQueueFamIdx);
            val ptr = stack.callocLong(1);
            if (vkCreateCommandPool(m_device, poolInfo, null, ptr) != VK_SUCCESS) {
                showError("Unable to create command buffer pool");
                return false;
            }
            m_commandPool = ptr.get(0);
        }

        // We'll give each frame its own pool, faster cmd buffer resets this way
        {
            val poolInfo = VkCommandPoolCreateInfo.calloc(stack)
                                                  .sType$Default()
                                                  .queueFamilyIndex(m_gfxQueueFamIdx);
            val ptr = stack.callocLong(1);
            for (val res : m_frameResources) {
                if (vkCreateCommandPool(m_device, poolInfo, null, ptr) != VK_SUCCESS) {
                    showError("Unable to create command buffer pool");
                    return false;
                }
                res.commandPool = ptr.get(0);
            }
        }

        // Create a command buffer for each frame
        {
            val cmdAllocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                                                          .sType$Default()
                                                          .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                                                          .commandBufferCount(1);
            val ptr = stack.callocPointer(1);
            for (val res : m_frameResources) {
                cmdAllocInfo.commandPool(res.commandPool);
                if (vkAllocateCommandBuffers(m_device, cmdAllocInfo, ptr) != VK_SUCCESS) {
                    showError("Unable to allocate command buffer");
                    return false;
                }
                res.commandBuffer = new VkCommandBuffer(ptr.get(0), m_device);
            }
        }

        return true;
    }

    private boolean createIndirectDrawBuffers() {
        assert m_vmaAllocator != NULL;
        assert m_frameResources != null;
        assert m_nodeWorld != null;

        @Cleanup val stack = MemoryStack.stackPush();

        val indBuffPtr = stack.callocPointer(1);
        val riBuffPtr = stack.callocPointer(1);
        val maxNodes = m_nodeWorld.maxNodes();
        for (val res : m_frameResources) {
            // Indirect drawing buffer
            val indirectBuffByteSize = maxNodes * VkDrawIndexedIndirectCommand.SIZEOF;
            res.indirectDrawBuffer = createBuffer(VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT,
                                                  indirectBuffByteSize,
                                                  true,
                                                  VMA_MEMORY_USAGE_AUTO);

            // Map indirect draw buffer
            if (vmaMapMemory(m_vmaAllocator, res.indirectDrawBuffer.allocation, indBuffPtr) != VK_SUCCESS) {
                showError("Unable to map indirect draw buffer");
                return false;
            }
            res.indirectDrawPtr = VkDrawIndexedIndirectCommand.create(indBuffPtr.get(0), maxNodes);

            // Render item buffer (per-draw data)
            val renderItemByteSize = maxNodes * RenderItem.$.sizeof();
            res.renderItemBuffer = createBuffer(VK_BUFFER_USAGE_STORAGE_BUFFER_BIT |
                                                VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
                                                renderItemByteSize,
                                                true,
                                                VMA_MEMORY_USAGE_AUTO);

            // Map indirect draw buffer
            if (vmaMapMemory(m_vmaAllocator, res.renderItemBuffer.allocation, riBuffPtr) != VK_SUCCESS) {
                showError("Unable to map render item buffer");
                return false;
            }
            val segment = MemorySegment.ofAddress(riBuffPtr.get(0))
                                       .reinterpret(renderItemByteSize);
            res.renderItemPtr = RenderItem.$.array(segment);
        }
        return true;
    }

    private GPUBuffer createBuffer(@NativeType("VkBufferUsageFlags") int usage,
                                   long byteSize,
                                   boolean mappable,
                                   @NativeType("VmaMemoryUsage") int memoryUsage) {
        assert m_device != null;
        assert m_vmaAllocator != NULL;

        @Cleanup val stack = MemoryStack.stackPush();

        // Create buffer and vma allocation
        val gpuBuff = new GPUBuffer();
        {
            val buffInfo = VkBufferCreateInfo.calloc(stack)
                                             .sType$Default()
                                             .size(byteSize)
                                             .usage(usage)
                                             .sharingMode(VK_SHARING_MODE_EXCLUSIVE);
            val flags = mappable ? VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT : 0;
            val allocInfo = VmaAllocationCreateInfo.calloc(stack)
                                                   .flags(flags)
                                                   .usage(memoryUsage);

            val vkBufferPtr = stack.callocLong(1);
            val allocationPtr = stack.callocPointer(1);
            if (vmaCreateBuffer(m_vmaAllocator, buffInfo, allocInfo, vkBufferPtr, allocationPtr, null) != VK_SUCCESS) {
                return gpuBuff; // (gpuBuff.vkBuffer == NULL) -> Error
            }
            gpuBuff.vkBuffer = vkBufferPtr.get(0);
            gpuBuff.allocation = allocationPtr.get(0);
        }

        // BDA Send Device Pointer
        if ((usage & VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT) != 0) {
            val vertBdaInfo = VkBufferDeviceAddressInfo.calloc(stack)
                                                       .sType$Default()
                                                       .buffer(gpuBuff.vkBuffer);
            gpuBuff.deviceAddress = vkGetBufferDeviceAddress(m_device, vertBdaInfo);
        }
        return gpuBuff;
    }

    private int addBuffer(GPUBuffer buffer) {
        m_buffers.add(buffer);
        val bufferId = m_buffers.size();
        return bufferId;
    }

    private void mapCopyBufferData(GPUBuffer buffer, long bufferOffset, MemorySegment data) {
        assert m_vmaAllocator != NULL;

        @Cleanup val stack = MemoryStack.stackPush();

        // Map and write buffer data
        val buffPtr = stack.callocPointer(1);
        if (vmaMapMemory(m_vmaAllocator, buffer.allocation, buffPtr) != VK_SUCCESS) {
            showError("Unable to map buffer memory");
            return;
        }

        try {
            val buffData = MemorySegment.ofAddress(buffPtr.get(0) + bufferOffset)
                                        .reinterpret(data.byteSize());
            buffData.copyFrom(data);
        } finally {
            vmaUnmapMemory(m_vmaAllocator, buffer.allocation);
        }
    }

    private VkCommandBuffer startTransientCommandBuffer() {
        assert m_device != null;
        assert m_commandPool != NULL;

        @Cleanup val stack = MemoryStack.stackPush();

        // Allocate the transient command buffer
        final VkCommandBuffer commandBuffer;
        {
            val cmdAllocInfos = VkCommandBufferAllocateInfo.calloc(stack)
                                                           .sType$Default()
                                                           .commandPool(m_commandPool)
                                                           .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                                                           .commandBufferCount(1);

            val ptr = stack.callocPointer(1);
            if (vkAllocateCommandBuffers(m_device, cmdAllocInfos, ptr) != VK_SUCCESS) {
                showError("Unable to allocate command buffer");
                throw new IllegalStateException(); //return nullptr;
            }
            commandBuffer = new VkCommandBuffer(ptr.get(0), m_device);
        }

        // Begin the command buffer
        {
            val beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                                                    .sType$Default()
                                                    .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            if (vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS) {
                showError("Unable to begin command buffer");
                vkFreeCommandBuffers(m_device, m_commandPool, commandBuffer);
                throw new IllegalStateException(); //return nullptr;
            }
        }

        return commandBuffer;
    }

    private void submitTransientCommandBuffer(VkCommandBuffer commandBuffer) {
        assert m_device != null;
        assert m_gfxQueue != null;
        assert m_commandPool != NULL;

        @Cleanup val stack = MemoryStack.stackPush();

        vkEndCommandBuffer(commandBuffer);

        val submitInfo = VkSubmitInfo.calloc(stack)
                                     .sType$Default()
                                     .pCommandBuffers(stack.pointers(commandBuffer.address()));

        vkQueueSubmit(m_gfxQueue, submitInfo, NULL);
        vkQueueWaitIdle(m_gfxQueue);
        vkFreeCommandBuffers(m_device, m_commandPool, commandBuffer);
    }

    private Pair<Integer, GPUBuffer> createImage(VkCommandBuffer commandBuffer, Image cpuImg) {
        assert m_device != null;
        assert m_vmaAllocator != NULL;

        @Cleanup val stack = MemoryStack.stackPush();

        val width = cpuImg.width;
        val height = cpuImg.height;
        val imageFormat = VK_FORMAT_R8G8B8A8_SRGB;

        // Create vk image and allocation
        val gpuImg = new GPUImage();
        {
            val imageInfo = VkImageCreateInfo.calloc(stack)
                                             .sType$Default()
                                             .imageType(VK_IMAGE_TYPE_2D)
                                             .format(imageFormat)
                                             .extent(ext -> ext.set(width, height, 1))
                                             .mipLevels(1)
                                             .arrayLayers(1)
                                             .samples(VK_SAMPLE_COUNT_1_BIT)
                                             .tiling(VK_IMAGE_TILING_OPTIMAL)
                                             .usage(VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT)
                                             .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            val allocInfo = VmaAllocationCreateInfo.calloc(stack)
                                                   .usage(VMA_MEMORY_USAGE_AUTO);

            val imagePtr = stack.callocLong(1);
            val allocationPtr = stack.callocPointer(1);
            if (vmaCreateImage(m_vmaAllocator, imageInfo, allocInfo, imagePtr, allocationPtr, null) != VK_SUCCESS) {
                showError("Error creating image");
                throw new IllegalStateException();// return {0, GPUBuffer{}};
            }
            gpuImg.image = imagePtr.get(0);
            gpuImg.allocation = allocationPtr.get(0);
        }

        // Create image view
        {
            val imgViewInfo = VkImageViewCreateInfo.calloc(stack)
                                                   .sType$Default()
                                                   .image(gpuImg.image)
                                                   .viewType(VK_IMAGE_VIEW_TYPE_2D)
                                                   .format(imageFormat)
                                                   .subresourceRange(sr -> sr.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                                                                             .levelCount(1)
                                                                             .layerCount(1));

            val ptr = stack.callocLong(1);
            if (vkCreateImageView(m_device, imgViewInfo, null, ptr) != VK_SUCCESS) {
                showError("Error creating image view");
                throw new IllegalStateException();// return { 0, GPUBuffer{} };
            }
            gpuImg.imageView = ptr.get(0);
        }

        // Transition the image to transfer-DST
        {
            val transferBarrier = VkImageMemoryBarrier2.calloc(1, stack)
                                                       .sType$Default()
                                                       .srcStageMask(VK_PIPELINE_STAGE_2_NONE)
                                                       .srcAccessMask(VK_ACCESS_2_NONE)
                                                       .dstStageMask(VK_PIPELINE_STAGE_2_COPY_BIT)
                                                       .dstAccessMask(VK_ACCESS_2_TRANSFER_WRITE_BIT)
                                                       .oldLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                                                       .newLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
                                                       .image(gpuImg.image)
                                                       .subresourceRange((sr) -> sr.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                                                                                   .baseMipLevel(0)
                                                                                   .levelCount(1)
                                                                                   .baseArrayLayer(0)
                                                                                   .layerCount(1));
            val transferDepInfo = VkDependencyInfo.calloc(stack)
                                                  .sType$Default()
                                                  .pImageMemoryBarriers(transferBarrier);

            vkCmdPipelineBarrier2(commandBuffer, transferDepInfo);
        }

        // Create staging buffer and issue record copy operation
        final GPUBuffer stageBuff;
        {
            stageBuff = createBuffer(VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                     cpuImg.data.byteSize(),
                                     true,
                                     VMA_MEMORY_USAGE_AUTO_PREFER_HOST);
            mapCopyBufferData(stageBuff, 0, cpuImg.data);

            val buffImgCopy = VkBufferImageCopy.calloc(1, stack)
                                               .imageSubresource(sr -> sr.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                                                                         .mipLevel(0)
                                                                         .baseArrayLayer(0)
                                                                         .layerCount(1))
                                               .imageExtent(ext -> ext.set(width, height, 1));
            vkCmdCopyBufferToImage(commandBuffer,
                                   stageBuff.vkBuffer,
                                   gpuImg.image,
                                   VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                                   buffImgCopy);
        }

        // transition image for shader read/sampling
        {
            val shaderReadBarrier = VkImageMemoryBarrier2.calloc(1, stack)
                                                         .sType$Default()
                                                         .srcStageMask(VK_PIPELINE_STAGE_2_COPY_BIT)
                                                         .srcAccessMask(VK_ACCESS_2_TRANSFER_WRITE_BIT)
                                                         .dstStageMask(VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT)
                                                         .dstAccessMask(VK_ACCESS_2_SHADER_READ_BIT)
                                                         .oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
                                                         .newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                                                         .image(gpuImg.image)
                                                         .subresourceRange((sr) -> sr.aspectMask(
                                                                                             VK_IMAGE_ASPECT_COLOR_BIT)
                                                                                     .baseMipLevel(0)
                                                                                     .levelCount(1)
                                                                                     .baseArrayLayer(0)
                                                                                     .layerCount(1));
            val transferDepInfo = VkDependencyInfo.calloc(stack)
                                                  .sType$Default()
                                                  .pImageMemoryBarriers(shaderReadBarrier);

            vkCmdPipelineBarrier2(commandBuffer, transferDepInfo);
        }

        m_images.add(gpuImg);
        val imageId = m_images.size();
        return new Pair<>(imageId, stageBuff);
    }

    private void updateTextureDescriptors() {
        assert m_device != null;

        val numTextures = m_textures.size();

        @Cleanup val arena = Arena.ofConfined();

        // Create combined image & sampler descriptor writes for all textures
        val imageDescriptorsSeg = arena.allocate((long) VkDescriptorImageInfo.SIZEOF * numTextures,
                                                 VkDescriptorImageInfo.ALIGNOF);
        val imageDescriptors = VkDescriptorImageInfo.create(imageDescriptorsSeg.address(), numTextures);
        for (var i = 0; i < numTextures; i++) {
            val texture = m_textures.get(i);
            val sampler = m_samplers.get(texture.samplerId - 1);
            val imageView = m_images.get(texture.imageId - 1).imageView;
            //noinspection resource
            imageDescriptors.get(i)
                            .sampler(sampler)
                            .imageView(imageView)
                            .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        }

        val descSetWriteSeg = arena.allocate(VkWriteDescriptorSet.SIZEOF, VkWriteDescriptorSet.ALIGNOF);
        val descSetWrite = VkWriteDescriptorSet.create(descSetWriteSeg.address(), 1)
                                               .sType$Default()
                                               .dstSet(m_globalDescSet)
                                               .dstBinding(0)
                                               .dstArrayElement(0)
                                               .descriptorCount(numTextures)
                                               .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                                               .pImageInfo(imageDescriptors);
        vkUpdateDescriptorSets(m_device, descSetWrite, null);
    }

    private void render() {
        assert m_device != null;
        assert m_gfxQueue != null;
        assert m_frameResources != null;
        assert m_swapchainImages != null;
        assert m_swapchainImageViews != null;
        assert m_renderCompleteSemaphores != null;
        assert m_nodeWorld != null;

        @Cleanup val stack = MemoryStack.stackPush();

        // first check if our swapchain is still valid
        if (m_requireSwapchainRecreate) {
            vkDeviceWaitIdle(m_device);
            destroySwapchain();
            createSwapchain();
            m_requireSwapchainRecreate = false;
        }

        val frameResIndex = (int) (m_frameIndex++ % MaxFramesInFlight);
        val signalValue = m_nextSignalValue++;
        val waitValue = signalValue - MaxFramesInFlight;

        // Wait on timeline
        {
            val waitInfo = VkSemaphoreWaitInfo.calloc(stack)
                                              .sType$Default()
                                              .semaphoreCount(1)
                                              .pSemaphores(stack.longs(m_timelineSemaphore))
                                              .pValues(stack.longs(waitValue));
            vkWaitSemaphores(m_device, waitInfo, Long.MAX_VALUE);
        }

        // Get the resources for this frame
        final FrameResources res;
        {
            res = m_frameResources[frameResIndex];
            assert res.commandPool != NULL;
            assert res.commandBuffer != null;
            assert res.imageAcquiredSemaphore != NULL;
            assert res.indirectDrawBuffer != null;
            assert res.renderItemBuffer != null;
            assert res.indirectDrawPtr != null;
            assert res.renderItemPtr != null;
        }

        // Now it's safe to start recording commands
        vkResetCommandPool(m_device, res.commandPool, 0);

        // Figure out what image in the swapchain is next
        final int imageIndex;
        final long swapchainImage;
        final long swapchainImageView;
        final long renderCompleteSemaphore;
        {
            val ptr = stack.callocInt(1);
            val acquireResult = vkAcquireNextImageKHR(m_device,
                                                      m_swapchain,
                                                      Long.MAX_VALUE,
                                                      res.imageAcquiredSemaphore,
                                                      VK_NULL_HANDLE,
                                                      ptr);
            imageIndex = ptr.get(0);

            // Handle resize and out-of-date images, may need swapchain recreate
            switch (acquireResult) {
                case VK_ERROR_OUT_OF_DATE_KHR -> {
                    // Bail now
                    m_requireSwapchainRecreate = true;
                    return;
                }
                // Can render this frame, recreate next time around
                case VK_SUBOPTIMAL_KHR -> m_requireSwapchainRecreate = true;
            }

            swapchainImage = m_swapchainImages[imageIndex];
            swapchainImageView = m_swapchainImageViews[imageIndex];
            renderCompleteSemaphore = m_renderCompleteSemaphores[imageIndex];

            assert swapchainImage != NULL;
            assert swapchainImageView != NULL;
            assert renderCompleteSemaphore != NULL;
        }

        // Push root nodes to render-stack
        m_nodeRenderStack.clear();
        var nodeId = m_rootNodeId;
        while (nodeId != 0) {
            val node = m_nodeWorld.getNode(nodeId);
            m_nodeRenderStack.push(new Pair<>(node, new Matrix4d()));
            nodeId = node.nextSiblingId;
        }

        var drawIndex = 0;
        while (!m_nodeRenderStack.isEmpty()) {
            final Node node;
            final Matrix4dc parentTransform;
            {
                val pair = m_nodeRenderStack.pop();
                node = pair.fst();
                parentTransform = pair.snd();
            }

            val matWorld = parentTransform.mul(node.getTransform(), new Matrix4d());
            val wvp = m_viewProjMatrix.mul(matWorld, new Matrix4d());

            // Draw the associated mesh
            val meshId = node.meshId;
            if (meshId != 0) {
                val mesh = m_meshes.get(meshId - 1);
                for (val subMesh : mesh.subMeshes) {
                    // Indirect draw command
                    val indirectDrawPtr = res.indirectDrawPtr.get(drawIndex);
                    indirectDrawPtr.indexCount(subMesh.indexCount)
                                   .instanceCount(1)
                                   .firstIndex(subMesh.indexStart)
                                   .vertexOffset(subMesh.vertexStart)
                                   .firstInstance(drawIndex);

                    // Per render-item data
                    val renderItemPtr = res.renderItemPtr.get(drawIndex);
                    Utils.setMat4(wvp, renderItemPtr.wvp());
                    Utils.setMat4(matWorld, renderItemPtr.worldMatrix());
                    renderItemPtr.materialIndex(subMesh.materialId - 1);

                    drawIndex++;
                }
            }

            // Child nodes for processing
            var childNodeId = node.firstChildId;
            while (childNodeId != 0) {
                val child = m_nodeWorld.getNode(childNodeId);
                m_nodeRenderStack.push(new Pair<>(child, matWorld));
                childNodeId = child.nextSiblingId;
            }
        }

        // Begin recording commands
        val cmdBeginInfo = VkCommandBufferBeginInfo.calloc(stack)
                                                   .sType$Default()
                                                   .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
        vkBeginCommandBuffer(res.commandBuffer, cmdBeginInfo);

        // Transition the color and depth images
        final VkDependencyInfo depInfo;
        {
            val layoutBarriers = VkImageMemoryBarrier2.calloc(2, stack);
            layoutBarriers.forEach(VkImageMemoryBarrier2::sType$Default);
            //noinspection resource
            layoutBarriers.get(0) // Color Barrier
                          .srcStageMask(VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
                          .srcAccessMask(0)
                          .dstStageMask(VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
                          .dstAccessMask(VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT)
                          .oldLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                          .newLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                          .image(swapchainImage)
                          .subresourceRange(s -> s.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                                                  .baseMipLevel(0)
                                                  .levelCount(1)
                                                  .baseArrayLayer(0)
                                                  .layerCount(1));
            //noinspection resource
            layoutBarriers.get(1) // Depth Barrier
                          .srcStageMask(VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT)
                          .srcAccessMask(0)
                          // Both (EARLY_FRAGMENT_TESTS + LATE_FRAGMENT_TESTS) specified to control memory access at both stages (write)
                          // NOTE: COLOR_ATTACHMENT_OUTPUT added due to validation error
                          .dstStageMask(VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT |
                                        VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT |
                                        VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
                          .dstAccessMask(VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT)
                          .oldLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                          .newLayout(VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL)
                          .image(m_depthImage)
                          .subresourceRange(s -> s.aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT)
                                                  .baseMipLevel(0)
                                                  .levelCount(1)
                                                  .baseArrayLayer(0)
                                                  .layerCount(1));
            depInfo = VkDependencyInfo.calloc(stack)
                                      .sType$Default()
                                      .pImageMemoryBarriers(layoutBarriers);
        }
        vkCmdPipelineBarrier2(res.commandBuffer, depInfo);

        // Setup attachments (color and depth)
        final VkRenderingInfo renderingInfo;
        {
            val colorAttachInfos = VkRenderingAttachmentInfo.calloc(1, stack);
            colorAttachInfos.forEach(VkRenderingAttachmentInfo::sType$Default);
            //noinspection resource
            colorAttachInfos.get(0)
                            .imageView(swapchainImageView)
                            .imageLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                            .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR) // Clear the image
                            .storeOp(VK_ATTACHMENT_STORE_OP_STORE) // Keep data for presentation
                            .clearValue(clr -> clr.color(v -> v.float32(stack.floats(0.3F, 0.3F, 1F, 1F))));
            //                            .clearValue(clr -> clr.color(v -> v.float32(stack.floats(0.01F, 0.01F, 0.01F, 1F))));
            val depthAttachInfo = VkRenderingAttachmentInfo.calloc(stack)
                                                           .sType$Default();
            depthAttachInfo.imageView(m_depthImageView)
                           .imageLayout(VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL)
                           .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                           .storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                           .clearValue(clr -> clr.depthStencil(v -> v.depth(1F)
                                                                     .stencil(0)));

            renderingInfo = VkRenderingInfo.calloc(stack)
                                           .sType$Default()
                                           .renderArea(a -> a.offset(o -> o.set(0, 0))
                                                             .extent(ext -> ext.set(m_swapchainWidth,
                                                                                    m_swapchainHeight)))
                                           .layerCount(1)
                                           .pColorAttachments(colorAttachInfos)
                                           .pDepthAttachment(depthAttachInfo);
        }

        // Setup frame data
        vkCmdBindDescriptorSets(res.commandBuffer,
                                VK_PIPELINE_BIND_POINT_GRAPHICS,
                                m_pipelineLayout,
                                0,
                                stack.longs(m_globalDescSet),
                                null);

        // Push frame constants
        try (val segStack = SegmentStack.stackPush()) {
            val frameConsts = FrameConstants.$.allocate(segStack);
            val buf = FrameConstants.$.asSegment(frameConsts)
                                      .asByteBuffer();

            val vertBuffer = m_buffers.get(m_vertexBufferId - 1);
            val materialBuffer = m_buffers.get(m_matBufferId - 1);
            val renderItemBuffer = res.renderItemBuffer;
            frameConsts.vertexBufferAddress(vertBuffer.deviceAddress)
                       .materialBufferAddress(materialBuffer.deviceAddress)
                       .renderItemsAddress(renderItemBuffer.deviceAddress);

            val flags = VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT;
            vkCmdPushConstants(res.commandBuffer, m_pipelineLayout, flags, 0, buf);
        }

        val idxBuffer = m_buffers.get(m_indexBufferId - 1);
        vkCmdBindIndexBuffer(res.commandBuffer, idxBuffer.vkBuffer, 0, VK_INDEX_TYPE_UINT32);

        // Begin dynamic rendering
        vkCmdBeginRendering(res.commandBuffer, renderingInfo);
        {
            // Set viewport state
            val viewports = VkViewport.calloc(1, stack);
            //noinspection resource
            viewports.get(0)
                     .set(0F, m_swapchainHeight, m_swapchainWidth, -m_swapchainHeight, 0F, 1F);
            vkCmdSetViewport(res.commandBuffer, 0, viewports);

            // Set scissor state
            val scissors = VkRect2D.calloc(1, stack);
            //noinspection resource
            scissors.get(0)
                    .offset(o -> o.set(0, 0))
                    .extent(ext -> ext.set(m_swapchainWidth, m_swapchainHeight));
            vkCmdSetScissor(res.commandBuffer, 0, scissors);
            vkCmdBindPipeline(res.commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, m_pipeline);
            vkCmdDrawIndexedIndirect(res.commandBuffer,
                                     res.indirectDrawBuffer.vkBuffer,
                                     0,
                                     drawIndex,
                                     VkDrawIndexedIndirectCommand.SIZEOF);
        }
        // End dynamic rendering
        vkCmdEndRendering(res.commandBuffer);

        final VkDependencyInfo presentDepInfo;
        {
            val presentLayoutBarriers = VkImageMemoryBarrier2.calloc(1, stack)
                                                             .sType$Default();
            //noinspection resource
            presentLayoutBarriers.get(0)
                                 .srcStageMask(VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
                                 .srcAccessMask(VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT)
                                 .dstStageMask(VK_PIPELINE_STAGE_2_NONE) // Nothing is waiting, but the cache is flushed and layout is transition
                                 .dstAccessMask(0)
                                 .oldLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                                 .newLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
                                 .image(swapchainImage)
                                 .subresourceRange(s -> s.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                                                         .baseMipLevel(0)
                                                         .levelCount(1)
                                                         .baseArrayLayer(0)
                                                         .layerCount(1));
            presentDepInfo = VkDependencyInfo.calloc(stack)
                                             .sType$Default()
                                             .pImageMemoryBarriers(presentLayoutBarriers);
        }
        vkCmdPipelineBarrier2(res.commandBuffer, presentDepInfo);
        vkEndCommandBuffer(res.commandBuffer);

        final VkSubmitInfo2.Buffer submitInfos;
        {
            // Ensure swapchain image is actually available to start color output
            val imageAcquireWaitInfos = VkSemaphoreSubmitInfo.calloc(1, stack);
            imageAcquireWaitInfos.forEach(VkSemaphoreSubmitInfo::sType$Default);
            //noinspection resource
            imageAcquireWaitInfos.get(0)
                                 .semaphore(res.imageAcquiredSemaphore)
                                 // wait before drawing to image
                                 .stageMask(VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT);

            // Signal that the image can be presented
            val semaphoreSignals = VkSemaphoreSubmitInfo.calloc(2, stack);
            semaphoreSignals.forEach(VkSemaphoreSubmitInfo::sType$Default);
            //noinspection resource
            semaphoreSignals.get(0)
                            .semaphore(renderCompleteSemaphore)
                            // wait before drawing to image
                            .stageMask(VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT);
            //noinspection resource
            semaphoreSignals.get(1)
                            .semaphore(m_timelineSemaphore)
                            .value(signalValue)
                            // wait before drawing to image
                            .stageMask(VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT);

            val cmdSubmitInfos = VkCommandBufferSubmitInfo.calloc(1, stack);
            cmdSubmitInfos.forEach(VkCommandBufferSubmitInfo::sType$Default);
            //noinspection resource
            cmdSubmitInfos.get(0)
                          .commandBuffer(res.commandBuffer);

            submitInfos = VkSubmitInfo2.calloc(1, stack);
            submitInfos.forEach(VkSubmitInfo2::sType$Default);
            submitInfos.get(0)
                       .pWaitSemaphoreInfos(imageAcquireWaitInfos)
                       .pCommandBufferInfos(cmdSubmitInfos)
                       .pSignalSemaphoreInfos(semaphoreSignals);
        }
        vkQueueSubmit2(m_gfxQueue, submitInfos, VK_NULL_HANDLE);

        // Present the image
        val presentInfo = VkPresentInfoKHR.calloc(stack)
                                          .sType$Default()
                                          .pWaitSemaphores(stack.longs(renderCompleteSemaphore))
                                          .swapchainCount(1)
                                          .pSwapchains(stack.longs(m_swapchain))
                                          .pImageIndices(stack.ints(imageIndex))
                                          .pResults(null);
        vkQueuePresentKHR(m_gfxQueue, presentInfo);
    }

    private int debugCallback(int messageSeverity, int messageTypes, long pCallbackData, long pUserData) {
        //noinspection resource
        val callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        System.err.println(callbackData.pMessageString());
        return VK_FALSE;
    }

    private void loadGltf(String fileName) {
        val filePath = Paths.get(fileName);
        val fileDir = filePath.getParent();

        if (!Files.isRegularFile(filePath)) {
            IO.println("File doesn't exist: %s".formatted(fileName));
            return;
        }

        IO.println(" ** Loading GLTF: %s".formatted(fileName));
        // load and parse GLTF

        final GlTF model;
        try (val is = Files.newInputStream(filePath)) {
            val reader = new GltfReaderV2();
            model = reader.read(is);
        } catch (Exception e) {
            IO.println("Error parsing glTF file");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return;
        }

        final int[] imageIds;
        try (val arena = Arena.ofConfined()) { // frees image memory after uploading to VRAM
            val images = loadImages(arena, model, fileDir); // load images into RAM
            imageIds = uploadImages(images); // upload images to VRAM
        }

        val samplerIds = loadSamplers(model); // samplers required for shaders
        val textureIds = loadTextures(model, imageIds, samplerIds); // image/sampler combinations
        val materialIds = loadMaterials(model, textureIds); // materials reference textureIds

        final int[] meshIds;
        try (val arena = Arena.ofConfined()) {
            val gltfBuffers = model.getBuffers();
            val numBuffers = gltfBuffers.size();

            // Pre-map all the buffers and confine them to the scoped arena
            val buffers = new MemorySegment[numBuffers];
            for (var i = 0; i < numBuffers; i++) {
                val gltfBuffer = gltfBuffers.get(i);
                val bufferPath = fileDir.resolve(gltfBuffer.getUri());
                try (val fc = FileChannel.open(bufferPath, StandardOpenOption.READ)) {
                    buffers[i] = fc.map(FileChannel.MapMode.READ_ONLY, 0, gltfBuffer.getByteLength(), arena);
                } catch (Exception e) {
                    IO.println("Error parsing glTF file");
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    return;
                }
            }

            meshIds = loadMeshes(model, materialIds, buffers); // meshes/submeshes reference materials
        }

        val scenes = model.getScenes();
        val rootScene = scenes.get(Objects.requireNonNullElse(model.getScene(), 0));
        val sceneNodes = rootScene.getNodes();

        for (val nodeIndex : sceneNodes) {
            val nodeId = importNode(model, nodeIndex, 0, m_lastRootNodeId, meshIds);
            if (m_rootNodeId == 0) {
                // first root node
                m_rootNodeId = nodeId;
                m_lastRootNodeId = nodeId;
            } else {
                // subsequent root nodes, link to previous root node
                m_lastRootNodeId = nodeId;
            }
        }
    }

    private @Nullable Image[] loadImages(Arena arena, GlTF model, Path imageDir) {
        assert m_arena != null;

        val gltfImages = model.getImages();
        if (gltfImages == null || gltfImages.isEmpty()) {
            return new Image[0];
        }
        val numImages = gltfImages.size();
        val images = new Image[numImages];
        for (var i = 0; i < numImages; i++) {
            ByteBuffer stbiData = null;
            String imageName = null;
            try (val stack = MemoryStack.stackPush()) {
                val gltfImage = gltfImages.get(i);
                val imagePath = imageDir.resolve(gltfImage.getUri());
                imageName = imagePath.toString();
                IO.println("Loading image %d/%d: %s".formatted(i + 1, numImages, imageName));

                val widthPtr = stack.callocInt(1);
                val heightPtr = stack.callocInt(1);
                val channelsPtr = stack.callocInt(1);

                val fileName = imagePath.toAbsolutePath()
                                        .toString();
                stbiData = stbi_load(fileName, widthPtr, heightPtr, channelsPtr, 4);
                if (stbiData == null) {
                    throw new IllegalStateException("stbi gave no data");
                }

                val width = widthPtr.get(0);
                val height = heightPtr.get(0);
                val channels = channelsPtr.get(0);

                val data = arena.allocate(stbiData.remaining());
                data.copyFrom(MemorySegment.ofBuffer(stbiData));

                images[i] = new Image(width, height, channels, data);
            } catch (Exception e) {
                if (imageName == null) {
                    imageName = "Unknown at index: " + i + 1;
                }
                showError("Failed to load image: " + imageName);
            } finally {
                if (stbiData != null) {
                    stbi_image_free(stbiData);
                }
            }
        }
        return images;
    }

    private int[] uploadImages(@Nullable Image[] images) {
        assert m_vmaAllocator != NULL;

        val commandBuffer = startTransientCommandBuffer();

        val numImages = images.length;
        final @Nullable GPUBuffer[] stagingBuffers = new GPUBuffer[images.length];

        // Upload images to GPU textures
        val imageIds = new int[numImages];
        for (var i = 0; i < numImages; i++) {
            val image = images[i];
            if (image != null) {
                val pair = createImage(commandBuffer, image);
                val imageId = pair.fst();
                val stagingTexBuffer = pair.snd();
                imageIds[i] = imageId;
                stagingBuffers[i] = stagingTexBuffer;
            } else {
                imageIds[i] = m_whitePixelImageId; // fallback to white pixel texture
            }
        }

        submitTransientCommandBuffer(commandBuffer); // submit and wait

        // clean up the staging buffers
        for (val stageBuff : stagingBuffers) {
            if (stageBuff != null) {
                vmaDestroyBuffer(m_vmaAllocator, stageBuff.vkBuffer, stageBuff.allocation);
            }
        }
        return imageIds;
    }

    private int[] loadSamplers(GlTF model) {
        assert m_device != null;

        @Cleanup val stack = MemoryStack.stackPush();

        val gltfSamplers = model.getSamplers();
        if (gltfSamplers == null || gltfSamplers.isEmpty()) {
            return new int[0];
        }

        val numSamplers = gltfSamplers.size();
        val samplerIds = new int[numSamplers];

        val samplerInfo = VkSamplerCreateInfo.calloc(stack)
                                             .sType$Default()
                                             .compareEnable(false);
        val ptr = stack.callocLong(1);
        for (var i = 0; i < numSamplers; i++) {
            val gltfSampler = gltfSamplers.get(i);

            val magFilter = GltfModel.filterMap.getOrDefault(gltfSampler.getMagFilter(), GltfModel.defaultFilter);
            val minFilter = GltfModel.filterMap.getOrDefault(gltfSampler.getMinFilter(), GltfModel.defaultFilter);
            val wrapS = GltfModel.wrapMap.getOrDefault(gltfSampler.getWrapS(), GltfModel.defaultWrap);
            val wrapT = GltfModel.wrapMap.getOrDefault(gltfSampler.getWrapT(), GltfModel.defaultWrap);

            samplerInfo.magFilter(magFilter.fst())
                       .minFilter(minFilter.fst())
                       .mipmapMode(minFilter.snd())
                       .addressModeU(wrapS)
                       .addressModeV(wrapT)
                       .addressModeW(GltfModel.defaultWrap)
                       .minLod(0F)
                       .maxLod(minFilter.thr());

            if (vkCreateSampler(m_device, samplerInfo, null, ptr) != VK_SUCCESS) {
                showError("Unable to create texture sampler");
                // fallback texture's sampler
                val fallbackTexture = m_textures.getFirst();
                samplerIds[i] = fallbackTexture.samplerId;
            } else {
                m_samplers.add(ptr.get(0));
                samplerIds[i] = m_samplers.size();
            }
        }
        return samplerIds;
    }

    private int[] loadTextures(GlTF model, int[] imageIds, int[] samplerIds) {
        val gltfTextures = model.getTextures();
        if (gltfTextures == null || gltfTextures.isEmpty()) {
            return new int[0];
        }

        val numTextures = gltfTextures.size();
        assert m_textures.size() + numTextures <= MaxTextures : "Exceeding max texture count";

        val textureIds = new int[numTextures];
        for (var i = 0; i < numTextures; i++) {
            val gltfTexture = gltfTextures.get(i);
            val imageId = imageIds[gltfTexture.getSource()];
            val samplerId = samplerIds[gltfTexture.getSampler()];

            m_textures.add(new Texture(imageId, samplerId));

            textureIds[i] = m_textures.size();
        }
        return textureIds;
    }

    private int[] loadMaterials(GlTF model, int[] textureIds) {
        assert m_materials != null;

        val gltfMaterials = model.getMaterials();
        val numMaterials = gltfMaterials.size();
        assert numMaterials + m_matOffset < MaxMaterials : "Exceeding max material count";

        val materialIds = new int[numMaterials];
        for (var i = 0; i < numMaterials; i++) {
            val gltfMaterial = gltfMaterials.get(i);
            val pbr = gltfMaterial.getPbrMetallicRoughness();

            final int textureIndex;
            {
                val textureInfo = pbr.getBaseColorTexture();
                if (textureInfo != null) {
                    textureIndex = textureIds[textureInfo.getIndex()] - 1;
                } else {
                    textureIndex = m_whitePixelImageId;
                }
            }
            val color = Objects.requireNonNullElse(pbr.getBaseColorFactor(), pbr.defaultBaseColorFactor());

            m_materials.apply(m_matOffset, mat -> {
                mat.baseColor()
                   .x((float) color[0])
                   .y((float) color[1])
                   .z((float) color[2])
                   .w((float) color[3]);
                mat.textureIndex(textureIndex);
            });
            m_matOffset++;
            materialIds[i] = m_matOffset;
        }
        return materialIds;
    }

    private int[] loadMeshes(GlTF model, int[] materialIds, MemorySegment[] buffers) {
        assert m_vertices != null;
        assert m_indices != null;

        val gltfMeshes = model.getMeshes();
        val numMeshes = gltfMeshes.size();

        val meshIds = new int[numMeshes];

        // Load all gltf mesh and primitive data
        for (var i = 0; i < numMeshes; i++) {
            val gltfMesh = gltfMeshes.get(i);
            val gltfAccessors = model.getAccessors();
            val gltfBufferViews = model.getBufferViews();
            val gltfPrimitives = gltfMesh.getPrimitives();

            val name = Objects.requireNonNullElse(gltfMesh.getName(), "No Name");
            val numSubMeshes = gltfPrimitives.size();
            val mesh = new Mesh(name, numSubMeshes);

            for (var j = 0; j < numSubMeshes; j++) {
                val gltfPrimitive = gltfPrimitives.get(j);
                val subMesh = mesh.subMeshes[j];
                subMesh.materialId = materialIds[Objects.requireNonNullElse(gltfPrimitive.getMaterial(), 0)];
                subMesh.vertexStart = m_vertOffset;
                subMesh.indexStart = m_idxOffset;

                // Copy the vertex data
                val gltfAttributes = gltfPrimitive.getAttributes();
                {
                    val acc = Optional.ofNullable(gltfAttributes.get("POSITION"))
                                      .map(gltfAccessors::get)
                                      .orElseThrow();
                    val count = acc.getCount();
                    if (m_vertOffset + count > MaxNumVertices) {
                        throw new IllegalStateException("Not enough space to load vertices");
                    }
                    subMesh.vertexCount = count;

                    if (!"VEC3".equals(acc.getType())) {
                        throw new IllegalStateException();
                    }
                    if (!(acc.getComponentType() == TG3_COMPONENT_TYPE_FLOAT)) {
                        throw new IllegalStateException();
                    }

                    val accOffset = Objects.requireNonNullElseGet(acc.getByteOffset(), acc::defaultByteOffset);
                    val bufView = gltfBufferViews.get(acc.getBufferView());
                    val fullBuf = buffers[bufView.getBuffer()];
                    val slicedBuf = fullBuf.asSlice(Objects.requireNonNullElseGet(bufView.getByteOffset(),
                                                                                  bufView::defaultByteOffset) +
                                                    accOffset, bufView.getByteLength() - accOffset);

                    val attrArray = new StructArray<>(vec3.$, slicedBuf);

                    for (var k = 0; k < count; k++) {
                        val dst = m_vertices.get(m_vertOffset + k);
                        dst.position(attrArray.get(k));

                        // Applies Default vertex values
                        // @formatter:off
                        dst.color().x(1F).y(1F).z(1F);
                        dst.normal().x(0F).y(1F).z(0F); // TODO: Bad normals break lighting :(
                        dst.uv().x(0F).y(0F);
                        // @formatter:on
                    }
                }
                normal:
                {
                    val acc = Optional.ofNullable(gltfAttributes.get("NORMAL"))
                                      .map(gltfAccessors::get)
                                      .orElse(null);
                    if (acc == null) {
                        break normal;
                    }
                    val count = acc.getCount();
                    if (count != subMesh.vertexCount) {
                        throw new IllegalStateException();
                    }

                    if (!"VEC3".equals(acc.getType())) {
                        throw new IllegalStateException();
                    }
                    if (!(acc.getComponentType() == TG3_COMPONENT_TYPE_FLOAT)) {
                        throw new IllegalStateException();
                    }

                    val accOffset = Objects.requireNonNullElseGet(acc.getByteOffset(), acc::defaultByteOffset);
                    val bufView = gltfBufferViews.get(acc.getBufferView());
                    val fullBuf = buffers[bufView.getBuffer()];
                    val slicedBuf = fullBuf.asSlice(Objects.requireNonNullElseGet(bufView.getByteOffset(),
                                                                                  bufView::defaultByteOffset) +
                                                    accOffset, bufView.getByteLength() - accOffset);
                    val attrArray = new StructArray<>(vec3.$, slicedBuf);

                    for (var k = 0; k < count; k++) {
                        m_vertices.get(m_vertOffset + k)
                                  .normal(attrArray.get(k));
                    }
                }
                color:
                {
                    val acc = Optional.ofNullable(gltfAttributes.get("COLOR_0"))
                                      .map(gltfAccessors::get)
                                      .orElse(null);
                    if (acc == null) {
                        break color;
                    }
                    val count = acc.getCount();
                    if (count != subMesh.vertexCount) {
                        throw new IllegalStateException();
                    }

                    if (!"VEC3".equals(acc.getType()) && !"VEC4".equals(acc.getType())) {
                        throw new IllegalStateException();
                    }
                    if (!(acc.getComponentType() == TG3_COMPONENT_TYPE_FLOAT)) {
                        throw new IllegalStateException();
                    }

                    val accOffset = Objects.requireNonNullElseGet(acc.getByteOffset(), acc::defaultByteOffset);
                    val bufView = gltfBufferViews.get(acc.getBufferView());
                    val fullBuf = buffers[bufView.getBuffer()];
                    val slicedBuf = fullBuf.asSlice(Objects.requireNonNullElseGet(bufView.getByteOffset(),
                                                                                  bufView::defaultByteOffset) +
                                                    accOffset, bufView.getByteLength() - accOffset);

                    if ("VEC3".equals(acc.getType())) {
                        val attrArray = new StructArray<>(vec3.$, slicedBuf);
                        for (var k = 0; k < count; k++) {
                            m_vertices.get(m_vertOffset + k)
                                      .color(attrArray.get(k));
                        }
                    } else {
                        val attrArray = new StructArray<>(vec4.$, slicedBuf);
                        for (var k = 0; k < count; k++) {
                            val src = attrArray.get(k);
                            val dst = m_vertices.get(m_vertOffset + k)
                                                .color();
                            dst.x(src.x())
                               .y(src.y())
                               .z(src.z());
                        }
                    }
                }
                uv:
                {
                    val acc = Optional.ofNullable(gltfAttributes.get("TEXCOORD_0"))
                                      .map(gltfAccessors::get)
                                      .orElse(null);
                    if (acc == null) {
                        break uv;
                    }
                    val count = acc.getCount();
                    if (count != subMesh.vertexCount) {
                        throw new IllegalStateException();
                    }

                    if (!"VEC2".equals(acc.getType())) {
                        throw new IllegalStateException();
                    }
                    if (!(acc.getComponentType() == TG3_COMPONENT_TYPE_FLOAT)) {
                        throw new IllegalStateException();
                    }

                    val accOffset = Objects.requireNonNullElseGet(acc.getByteOffset(), acc::defaultByteOffset);
                    val bufView = gltfBufferViews.get(acc.getBufferView());
                    val fullBuf = buffers[bufView.getBuffer()];
                    val slicedBuf = fullBuf.asSlice(Objects.requireNonNullElseGet(bufView.getByteOffset(),
                                                                                  bufView::defaultByteOffset) +
                                                    accOffset, bufView.getByteLength() - accOffset);
                    val attrArray = new StructArray<>(vec2.$, slicedBuf);

                    for (var k = 0; k < count; k++) {
                        m_vertices.get(m_vertOffset + k)
                                  .uv(attrArray.get(k));
                    }
                }

                m_vertOffset += subMesh.vertexCount;

                // Copy index data
                {
                    val acc = Optional.ofNullable(gltfPrimitive.getIndices())
                                      .map(gltfAccessors::get)
                                      .orElseThrow();
                    val count = acc.getCount();
                    if (m_idxOffset + count > MaxNumIndices) {
                        throw new IllegalStateException("Not enough space for indices");
                    }
                    if (!(acc.getComponentType() == TG3_COMPONENT_TYPE_UNSIGNED_INT) &&
                        !(acc.getComponentType() == TG3_COMPONENT_TYPE_UNSIGNED_SHORT)) {
                        throw new IllegalStateException();
                    }

                    subMesh.indexStart = m_idxOffset;
                    subMesh.indexCount = count;

                    val accOffset = Objects.requireNonNullElseGet(acc.getByteOffset(), acc::defaultByteOffset);
                    val bufView = gltfBufferViews.get(acc.getBufferView());
                    val fullBuf = buffers[bufView.getBuffer()];
                    val slicedBuf = fullBuf.asSlice(Objects.requireNonNullElseGet(bufView.getByteOffset(),
                                                                                  bufView::defaultByteOffset) +
                                                    accOffset, bufView.getByteLength() - accOffset);

                    if (acc.getComponentType() == TG3_COMPONENT_TYPE_UNSIGNED_INT) {
                        MemorySegment.copy(slicedBuf, 0, m_indices, m_idxOffset, count);
                    } else {
                        for (var k = 0; k < count; k++) {
                            val index = slicedBuf.getAtIndex(FFM.uint16_t.layout(), k);
                            m_indices.setAtIndex(FFM.uint32_t.layout(), m_idxOffset + k, index);
                        }
                    }
                }
                m_idxOffset += subMesh.indexCount;
            }
            m_meshes.add(mesh);
            meshIds[i] = m_meshes.size();
        }
        return meshIds;
    }

    private int importNode(GlTF model, int nodeIndex, int parentId, int prevSiblingId, int[] meshIds) {
        assert m_nodeWorld != null;

        val gltfNodes = model.getNodes();
        val gltfCameras = model.getCameras();
        val gltfNode = gltfNodes.get(nodeIndex);

        final Node node;
        final int nodeId;
        {
            val pair = m_nodeWorld.createNode();
            node = pair.fst();
            nodeId = pair.snd();
        }
        node.parentId = parentId;

        tf:
        {
            val rawTransform = gltfNode.getMatrix();
            if (rawTransform != null) {
                node.setTransform(new Matrix4d(rawTransform));
                // Got a matrix, so we assume it's all we need.
                break tf;
            }
            // Go piece by piece i guess
            val rawTranslation = gltfNode.getTranslation();
            if (rawTranslation != null) {
                node.setTranslation(new Vector3d(rawTranslation));
            }
            val rawScale = gltfNode.getScale();
            if (rawScale != null) {
                node.setScale(new Vector3d(rawScale));
            }
            val rawRotation = gltfNode.getRotation();
            if (rawRotation != null) {
                node.setRotation(new Quaterniond(rawRotation));
            }
        }

        val gltfMeshIdx = gltfNode.getMesh();
        val gltfCameraIdx = gltfNode.getCamera();
        mesh_cam:
        if (gltfMeshIdx != null) {
            node.meshId = meshIds[gltfMeshIdx];
        } else if (gltfCameraIdx != null) {
            if (m_cameraNodeId != 0) {
                // TODO: Support multiple camera objects
                break mesh_cam;
            }
            val gltfCamera = gltfCameras.get(gltfCameraIdx);
            val perspective = gltfCamera.getPerspective();
            if (perspective == null) {
                break mesh_cam;
            }

            m_camera.fovY = perspective.getYfov();
            m_camera.nearPlane = perspective.getZnear();
            m_camera.farPlane = perspective.getZfar();
            m_cameraNodeId = nodeId;

            val nodeRotation = node.getRotation();
            m_camera.forward.set(0D, 0D, -1D)
                            .rotate(nodeRotation)
                            .normalize();
            m_camera.right.set(1D, 0D, 0D)
                          .rotate(nodeRotation)
                          .normalize();
            m_camera.up.set(m_camera.right)
                       .cross(m_camera.forward);
        }

        if (prevSiblingId != 0) {
            m_nodeWorld.getNode(prevSiblingId).nextSiblingId = nodeId;
        }

        // Iterate through child nodes
        val children = gltfNode.getChildren();
        if (children != null) {
            var lastChildId = 0;

            for (val childIndex : children) {
                lastChildId = importNode(model, childIndex, nodeId, lastChildId, meshIds);

                // set parent's first child id field
                if (node.firstChildId != 0) {
                    node.firstChildId = lastChildId;
                }
            }
        }

        return nodeId;
    }

    private void updateProjectionMatrix() {
        val aspectRatio = m_width / (double) m_height;
        m_matProj.perspective(Math.toRadians(85D), aspectRatio, 0.01F, 1000F);
    }

    private void updateViewMatrix() {
        assert m_nodeWorld != null;

        m_camera.yaw -= Math.toRadians(m_mouseXRel * m_mouseSensitivity);
        m_camera.pitch -= Math.toRadians(m_mouseYRel * m_mouseSensitivity);
        m_camera.pitch = Math.clamp(m_camera.pitch, -HALF_PI + 0.01F, HALF_PI - 0.01F);
        m_mouseXRel = 0;
        m_mouseYRel = 0;

        // camera and view matrix
        val camNode = m_nodeWorld.getNode(m_cameraNodeId);
        val camPosition = new Vector3d(camNode.getTranslation());
        if (m_camera.type == CameraType.orbit) {
            camPosition.set(Math.cos(m_camera.yaw) * Math.cos(m_camera.pitch),
                            Math.sin(m_camera.pitch),
                            Math.sin(m_camera.yaw) * Math.cos(m_camera.pitch))
                       .mul(m_camera.distance);
            camNode.setTranslation(camPosition);
            m_matView.setLookAt(camPosition, new Vector3d(0D, 0D, 0D), new Vector3d(0D, 1D, 0D));
        } else if (m_camera.type == CameraType.firstPerson) {
            val cameraQuat = new Quaterniond().rotateLocalX(m_camera.pitch)
                                              .rotateLocalY(m_camera.yaw);
            camNode.setRotation(cameraQuat);
            val is6DegFree = false;
            m_camera.forward.set(0D, 0D, -1D)
                            .rotate(cameraQuat)
                            .normalize();
            //noinspection ConstantValue
            m_camera.right.set(m_camera.forward)
                          .cross(is6DegFree ? m_camera.up : new Vector3d(0D, 1D, 0D))
                          .normalize();
            m_camera.up.set(m_camera.right)
                       .cross(m_camera.forward)
                       .normalize();
            m_matView.setLookAt(camPosition, new Vector3d(camPosition).add(m_camera.forward), m_camera.up);

            val is6DegFree2 = true;
            //noinspection ConstantValue
            val movementQuat = new Quaterniond().rotateLocalX(is6DegFree2 ? m_camera.pitch : 0D)
                                                .rotateLocalY(m_camera.yaw);
            m_moveDirection.set(0D, 0D, -1D)
                           .rotate(movementQuat);
        }
        m_matProj.mul(m_matView, m_viewProjMatrix);
    }

    @NoArgsConstructor
    static final class GPUImage {
        /*(VkImage)*/ long image = NULL;
        /*(VkImageView)*/ long imageView = NULL;
        /*(VmaAllocation)*/ long allocation = NULL;
    }

    @NoArgsConstructor
    static final class GPUBuffer {
        /*(VkBuffer)*/ long vkBuffer = NULL;
        /*(uint64_t)*/ long deviceAddress = NULL;
        /*(VmaAllocation)*/ long allocation = NULL;
    }

    @NoArgsConstructor
    static final class FrameResources {
        /*(VkCommandPool)*/ long commandPool = NULL;
        @Nullable VkCommandBuffer commandBuffer = null;
        /*(VkSemaphore)*/ long imageAcquiredSemaphore = NULL;
        @Nullable GPUBuffer indirectDrawBuffer = null;
        @Nullable GPUBuffer renderItemBuffer = null;
        VkDrawIndexedIndirectCommand.@Nullable Buffer indirectDrawPtr = null;
        @Nullable StructArray<RenderItem> renderItemPtr = null;
    }

    record Image(int width,
                 int height,
                 int channels,
                 MemorySegment data) {
    }

    record Texture(int imageId,
                   int samplerId) {
    }

    static final class Mesh {
        final String name;
        final SubMesh[] subMeshes;

        Mesh(String name, int numSubMeshes) {
            this.name = name;
            this.subMeshes = IntStream.range(0, numSubMeshes)
                                      .mapToObj(_ -> new SubMesh())
                                      .toArray(SubMesh[]::new);
        }
    }

    static final class SubMesh {
        int vertexStart = 0;
        int vertexCount = 0;
        int indexStart = 0;
        int indexCount = 0;
        int materialId = 0;
    }
}
