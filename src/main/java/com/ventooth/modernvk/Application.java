package com.ventooth.modernvk;

import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferSubmitInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
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
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties2;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderingAttachmentInfo;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;
import org.lwjgl.vulkan.VkSemaphoreTypeCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreWaitInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo2;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkViewport;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.IntStream;

import static com.ventooth.modernvk.Utils.readTextFile;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_QUIT;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_RESIZED;
import static org.lwjgl.sdl.SDLEvents.SDL_PollEvent;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_VIDEO;
import static org.lwjgl.sdl.SDLInit.SDL_InitSubSystem;
import static org.lwjgl.sdl.SDLInit.SDL_Quit;
import static org.lwjgl.sdl.SDLMessageBox.SDL_MESSAGEBOX_ERROR;
import static org.lwjgl.sdl.SDLMessageBox.SDL_ShowSimpleMessageBox;
import static org.lwjgl.sdl.SDLVideo.SDL_CreateWindow;
import static org.lwjgl.sdl.SDLVideo.SDL_DestroyWindow;
import static org.lwjgl.sdl.SDLVideo.SDL_WINDOW_RESIZABLE;
import static org.lwjgl.sdl.SDLVideo.SDL_WINDOW_VULKAN;
import static org.lwjgl.sdl.SDLVulkan.SDL_Vulkan_CreateSurface;
import static org.lwjgl.sdl.SDLVulkan.SDL_Vulkan_GetInstanceExtensions;
import static org.lwjgl.sdl.SDLVulkan.SDL_Vulkan_GetVkGetInstanceProcAddr;
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
import static org.lwjgl.util.vma.Vma.VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_AUTO;
import static org.lwjgl.util.vma.Vma.vmaCreateAllocator;
import static org.lwjgl.util.vma.Vma.vmaCreateImage;
import static org.lwjgl.util.vma.Vma.vmaDestroyAllocator;
import static org.lwjgl.util.vma.Vma.vmaDestroyImage;
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
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_SCISSOR;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_VIEWPORT;
import static org.lwjgl.vulkan.VK10.VK_FALSE;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;
import static org.lwjgl.vulkan.VK10.vkDestroyCommandPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroySemaphore;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceProperties;
import static org.lwjgl.vulkan.VK10.vkResetCommandPool;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceFeatures2;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceQueueFamilyProperties2;
import static org.lwjgl.vulkan.VK12.VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK12.VK_SEMAPHORE_TYPE_TIMELINE;
import static org.lwjgl.vulkan.VK12.vkWaitSemaphores;
import static org.lwjgl.vulkan.VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT;
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
    private static final int MaxFramesInFlight = 2;
    private static final String shaderEntryPoint = "main";

    private /*(SDL_Window*)*/ long window = NULL;
    private int width = 1280;
    private int height = 720;
    private long frameIndex = 0;
    private long nextSignalValue = MaxFramesInFlight + 1;

    // vulkan core
    private @Nullable VkDebugUtilsMessengerCallbackEXT debugCallback;
    private @Nullable VkInstance vulkanInstance = null;
    private /*(VkSurfaceKHR)*/ long surface = NULL;
    private @Nullable VkPhysicalDevice physicalDevice = null;
    private @Nullable VkDevice device = null;
    private /*(VmaAllocator)*/ long vmaAllocator = NULL;

    // queue related
    private int gfxQueueFamIdx = -1;
    private @Nullable VkQueue gfxQueue = null;

    // swapchain related
    private /*(VkSwapchainKHR)*/ long swapchain = NULL;
    private /*(VkImage[])*/ long @Nullable [] swapchainImages = null;
    private /*(VkImageView[])*/ long @Nullable [] swapchainImageViews = null;
    private /*(VkSemaphore[])*/ long @Nullable [] renderCompleteSemaphores = null;

    private boolean requireSwapchainRecreate = false;
    private int swapchainWidth = -1;
    private int swapchainHeight = -1;

    private /*(VkImage)*/ long depthImage = NULL;
    private /*(VkImageView)*/ long depthImageView = NULL;
    private /*(VmaAllocation)*/ long depthImageAllocation = NULL;

    // graphics pipeline related
    private /*(VkPipelineLayout)*/ long pipelineLayout = NULL;
    private /*(VkPipeline)*/ long pipeline = NULL;

    // shader resources
    private /*(VkShaderModule)*/ long vertShader = NULL;
    private /*(VkShaderModule)*/ long fragShader = NULL;

    // frame and synchronization resources
    private /*(VkSemaphore)*/ long timelineSemaphore = NULL;
    private FrameResources @Nullable [] frameResources = null;

    private void showError(String errorMessage) {
        SDL_ShowSimpleMessageBox(SDL_MESSAGEBOX_ERROR, "Error", errorMessage, window);
    }

    public boolean initialize() {
        SDL_InitSubSystem(SDL_INIT_VIDEO);
        window = SDL_CreateWindow("Vulkan Learning", width, height, SDL_WINDOW_VULKAN | SDL_WINDOW_RESIZABLE);
        if (window == NULL) {
            showError("Error creating window");
            return false;
        }

        return initializeVulkan();
    }

    public void run() {
        var running = true;
        @Cleanup val stack = MemoryStack.stackPush();

        val event = SDL_Event.calloc(stack);
        while (running) {
            while (SDL_PollEvent(event)) {
                if (event.type() == SDL_EVENT_QUIT) {
                    running = false;
                    break;
                } else if (event.type() == SDL_EVENT_WINDOW_RESIZED) {
                    val windowData = event.window();
                    width = windowData.data1();
                    height = windowData.data2();
                    break; // TODO: Should this be break?
                }
            }

            render();
        }
    }

    public void shutdown() {
        // wait in case resources are in use
        if (device != null) {
            vkDeviceWaitIdle(device);
        }

        // frame / sync object cleanup
        if (timelineSemaphore != NULL) {
            assert device != null;
            vkDestroySemaphore(device, timelineSemaphore, null);
        }

        if (frameResources != null) {
            assert device != null;
            for (val res : frameResources) {
                if (res.imageAcquiredSemaphore != NULL) {
                    vkDestroySemaphore(device, res.imageAcquiredSemaphore, null);
                    res.imageAcquiredSemaphore = NULL;
                }
                if (res.commandPool != NULL) {
                    vkDestroyCommandPool(device, res.commandPool, null); // destroys buffers implicitly
                    res.commandPool = NULL;
                    res.commandBuffer = null;
                }
            }
            frameResources = null;
        }

        // Pipeline cleanup
        if (pipelineLayout != NULL) {
            assert device != null;
            vkDestroyPipelineLayout(device, pipelineLayout, null);
            pipelineLayout = NULL;
        }
        if (pipeline != NULL) {
            assert device != null;
            vkDestroyPipeline(device, pipeline, null);
            pipeline = NULL;
        }

        // Cleanup shaders
        if (vertShader != NULL) {
            assert device != null;
            vkDestroyShaderModule(device, vertShader, null);
            vertShader = NULL;
        }
        if (fragShader != NULL) {
            assert device != null;
            vkDestroyShaderModule(device, fragShader, null);
            fragShader = NULL;
        }

        // Cleanup swapchain
        destroySwapchain();

        // VMA
        if (vmaAllocator != NULL) {
            vmaDestroyAllocator(vmaAllocator);
            vmaAllocator = NULL;
        }

        // Cleanup Vulkan
        gfxQueue = null;
        if (device != null) {
            vkDestroyDevice(device, null);
            device = null;
        }
        gfxQueueFamIdx = -1;
        physicalDevice = null;
        if (surface != NULL) {
            assert vulkanInstance != null;
            vkDestroySurfaceKHR(vulkanInstance, surface, null);
            surface = NULL;
        }
        if (vulkanInstance != null) {
            vkDestroyInstance(vulkanInstance, null);
            vulkanInstance = null;
        }
        if (debugCallback != null) {
            debugCallback.free();
            debugCallback = null;
        }
        VK.destroy();

        // Cleanup SDL
        if (window != NULL) {
            SDL_DestroyWindow(window);
            window = NULL;
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
            debugCallback = VkDebugUtilsMessengerCallbackEXT.create(Application.this::debugCallback);
            instCreateInfoDbg = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack)
                                                                  .sType$Default()
                                                                  .flags(0)
                                                                  .messageSeverity(severity)
                                                                  .messageType(type)
                                                                  .pfnUserCallback(debugCallback);
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

        vulkanInstance = new VkInstance(ptr.get(0), instCreateInfo);
        return true;
    }

    private boolean createSurface() {
        assert window != NULL;
        assert vulkanInstance != null;

        @Cleanup val stack = MemoryStack.stackPush();
        val ptr = stack.callocLong(1);
        if (!SDL_Vulkan_CreateSurface(window, vulkanInstance, null, ptr)) {
            return false;
        }

        surface = ptr.get(0);
        return true;
    }

    private boolean findPhysicalDevice() {
        assert vulkanInstance != null;
        assert surface != NULL;

        @Cleanup val stack = MemoryStack.stackPush();

        // Try to find a suitable device, preferably a dedicated gpu
        {
            val list = new ArrayList<VkPhysicalDevice>();

            // Enumerate all physical devices
            val count = stack.callocInt(1);
            vkEnumeratePhysicalDevices(vulkanInstance, count, null);
            val ptr = stack.callocPointer(count.get(0));
            vkEnumeratePhysicalDevices(vulkanInstance, count, ptr);

            while (ptr.hasRemaining()) {
                list.add(new VkPhysicalDevice(ptr.get(), vulkanInstance));
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
                if (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                    selectedDev = dev;
                    break;
                }
            }

            physicalDevice = selectedDev;
        }
        assert physicalDevice != null;

        // Ensure the desired swapchain format is supported
        {
            val count = stack.callocInt(1);
            vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, count, null);
            val formats = VkSurfaceFormatKHR.calloc(count.get(0), stack);
            vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, count, formats);

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

        assert surface != NULL;
        assert physicalDevice != null;
        @Cleanup val stack = MemoryStack.stackPush();

        final int numQueues;
        final VkQueueFamilyProperties2.Buffer propsList;
        // Grab all the queue families
        {
            val count = stack.callocInt(1);
            vkGetPhysicalDeviceQueueFamilyProperties2(physicalDevice, count, null);

            propsList = VkQueueFamilyProperties2.calloc(count.get(0), stack);
            propsList.forEach(VkQueueFamilyProperties2::sType$Default);

            vkGetPhysicalDeviceQueueFamilyProperties2(physicalDevice, count, propsList);
            numQueues = count.get(0);
        }

        if (numQueues < 1) {
            // No queues found
            return false;
        }

        val hasPresentSupport = stack.callocInt(1);
        for (var i = 0; i < numQueues; i++) {
            // Ensure this queue has presentation support
            vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, hasPresentSupport);
            if (hasPresentSupport.get(0) != VK_TRUE) {
                continue;
            }

            // Ensure this is a GRAPHICS queue
            val props = propsList.get(i);
            val familyProps = props.queueFamilyProperties();
            if ((familyProps.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                gfxQueueFamIdx = i;
                return true;
            }
        }
        return false;
    }

    private boolean createDevice() {
        assert physicalDevice != null;
        assert gfxQueueFamIdx != -1;

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
            vkGetPhysicalDeviceFeatures2(physicalDevice, feats);

            // Check if what we need is supported
            if (!feats13.dynamicRendering() || !feats13.synchronization2() || !feats12.timelineSemaphore()) {
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
            feats13.dynamicRendering(true);
            feats13.synchronization2(true);
            feats12.timelineSemaphore(true);

            enabledFeatures = feats;
        }

        // Create a list of queue infos, but populate only the first one for now
        val gfxQueueInfos = VkDeviceQueueCreateInfo.calloc(1, stack);
        gfxQueueInfos.forEach(VkDeviceQueueCreateInfo::sType$Default);
        //noinspection resource
        gfxQueueInfos.get(0)
                     .queueFamilyIndex(gfxQueueFamIdx)
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
            if (vkCreateDevice(physicalDevice, devCreateInfo, null, ptr) != VK_SUCCESS) {
                return false;
            }
            device = new VkDevice(ptr.get(0), physicalDevice, devCreateInfo);
        }

        // Grab the VkQueue object finally
        {
            val ptr = stack.callocPointer(1);
            vkGetDeviceQueue(device, gfxQueueFamIdx, 0, ptr);
            if (ptr.get(0) == NULL) {
                showError("Couldn't get the graphics queue");
                return false;
            }

            gfxQueue = new VkQueue(ptr.get(0), device);
        }
        return true;
    }

    private boolean initializeVMA() {
        assert vulkanInstance != null;
        assert physicalDevice != null;
        assert device != null;

        @Cleanup val stack = MemoryStack.stackPush();

        // See: https://github.com/LWJGL/lwjgl3/blob/8adda2367a10f0283ba7ac27a2222fcf7fc67212/modules/samples/src/test/java/org/lwjgl/demo/vulkan/khronos/HelloTriangle.java#L406-L416
        val vmaFuncInfo = VmaVulkanFunctions.calloc(stack)
                                            .set(vulkanInstance, device);

        val vmaAllocInfo = VmaAllocatorCreateInfo.calloc(stack)
                                                 .flags(VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT)
                                                 .physicalDevice(physicalDevice)
                                                 .device(device)
                                                 .pVulkanFunctions(vmaFuncInfo)
                                                 .instance(vulkanInstance)
                                                 .vulkanApiVersion(VulkanVersion);

        val ptr = stack.callocPointer(1);
        if (vmaCreateAllocator(vmaAllocInfo, ptr) != VK_SUCCESS) {
            return false;
        }
        vmaAllocator = ptr.get(0);
        return true;
    }

    private boolean createSwapchain() {
        assert physicalDevice != null;
        assert surface != NULL;
        assert device != null;
        assert vmaAllocator != NULL;

        @Cleanup val stack = MemoryStack.stackPush();

        swapchainWidth = width;
        swapchainHeight = height;

        // Create new swapchain
        {
            val surfaceCaps = VkSurfaceCapabilitiesKHR.calloc(stack);
            if (vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, surfaceCaps) != VK_SUCCESS) {
                showError("Couldn't get the surface capabilities");
                return false;
            }

            val swapchainInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                                                        .sType$Default()
                                                        .surface(surface)
                                                        .minImageCount(surfaceCaps.minImageCount())
                                                        .imageFormat(swapchainFormat)
                                                        .imageColorSpace(VK_COLORSPACE_SRGB_NONLINEAR_KHR)
                                                        .imageExtent(ext -> ext.set(swapchainWidth, swapchainHeight))
                                                        .imageArrayLayers(1)
                                                        .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                                                        .preTransform(VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR)
                                                        .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                                                        .presentMode(VK_PRESENT_MODE_FIFO_KHR);
            val ptr = stack.callocLong(1);
            if (vkCreateSwapchainKHR(device, swapchainInfo, null, ptr) != VK_SUCCESS) {
                showError("Error creating swapchain");
                return false;
            }
            swapchain = ptr.get(0);
        }

        // Grab the swapchain images
        final int numImages;
        {
            val count = stack.callocInt(1);
            vkGetSwapchainImagesKHR(device, swapchain, count, null);
            val ptr = stack.callocLong(count.get(0));
            vkGetSwapchainImagesKHR(device, swapchain, count, ptr);
            numImages = count.get(0);

            swapchainImages = new long[numImages];
            ptr.get(swapchainImages);
        }

        // Create the swapchain image views
        swapchainImageViews = new long[numImages];
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
                imgViewInfo.image(swapchainImages[i]);
                if (vkCreateImageView(device, imgViewInfo, null, ptr) != VK_SUCCESS) {
                    showError("Error creating swapchain image view");
                    return false;
                }
                swapchainImageViews[i] = ptr.get(0);
            }
        }

        // Semaphores used to signal render completion
        renderCompleteSemaphores = new long[numImages];
        {
            val semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack)
                                                     .sType$Default();
            val ptr = stack.callocLong(1);
            for (var i = 0; i < numImages; i++) {
                if (vkCreateSemaphore(device, semaphoreInfo, null, ptr) != VK_SUCCESS) {
                    showError("Error creating the render-complete semaphore");
                    return false;
                }
                renderCompleteSemaphores[i] = ptr.get(0);
            }
        }

        // Create depth image
        {
            val depthInfo = VkImageCreateInfo.calloc(stack)
                                             .sType$Default()
                                             .imageType(VK_IMAGE_TYPE_2D)
                                             .format(depthFormat)
                                             .extent(s -> s.set(swapchainWidth, swapchainHeight, 1))
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
            if (vmaCreateImage(vmaAllocator, depthInfo, allocInfo, imgPtr, allocPtr, null) != VK_SUCCESS) {
                showError("Error allocating depth image");
                return false;
            }

            depthImage = imgPtr.get(0);
            depthImageAllocation = allocPtr.get(0);
        }

        // Create depth image view
        {
            val depthImgViewInfo = VkImageViewCreateInfo.calloc(stack)
                                                        .image(depthImage)
                                                        .viewType(VK_IMAGE_VIEW_TYPE_2D)
                                                        .format(depthFormat)
                                                        .subresourceRange(s -> s.aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT)
                                                                                .levelCount(1)
                                                                                .layerCount(1));
            val ptr = stack.callocLong(1);
            if (vkCreateImageView(device, depthImgViewInfo, null, ptr) != VK_SUCCESS) {
                showError("Error creating depth image view");
                return false;
            }
            depthImageView = ptr.get(0);
        }

        return true;
    }

    private void destroySwapchain() {
        assert device != null;

        if (swapchainImageViews != null) {
            for (var i = 0; i < swapchainImageViews.length; i++) {
                var swapchainImgView = swapchainImageViews[i];
                if (swapchainImgView != NULL) {
                    vkDestroyImageView(device, swapchainImgView, null);
                    swapchainImageViews[i] = NULL;
                }
            }
            swapchainImageViews = null;
        }

        // Destroy render-complete semaphores
        if (renderCompleteSemaphores != null) {
            for (var i = 0; i < renderCompleteSemaphores.length; i++) {
                var semaphore = renderCompleteSemaphores[i];
                if (semaphore != NULL) {
                    vkDestroySemaphore(device, semaphore, null);
                    renderCompleteSemaphores[i] = NULL;
                }
            }
            renderCompleteSemaphores = null;
        }

        if (swapchain != NULL) {
            vkDestroySwapchainKHR(device, swapchain, null);
            swapchain = NULL;
        }

        if (depthImageView != NULL) {
            vkDestroyImageView(device, depthImageView, null);
            depthImageView = NULL;
        }

        if (depthImageAllocation != NULL) {
            assert depthImage != NULL;

            vmaDestroyImage(vmaAllocator, depthImage, depthImageAllocation);
            depthImage = NULL;
            depthImageAllocation = NULL;
        }
    }

    private @NativeType("VkShaderModule") long createShaderModule(String fileName,
                                                                  @NativeType("shaderc_shader_kind") int kind) {
        assert device != null;

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
            if (vkCreateShaderModule(device, moduleInfo, null, ptr) != VK_SUCCESS) {
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
        vertShader = createShaderModule("shader.vert", shaderc_vertex_shader);
        fragShader = createShaderModule("shader.frag", shaderc_fragment_shader);
        return vertShader != NULL && fragShader != NULL;
    }

    private boolean createGraphicsPipeline() {
        assert device != null;

        @Cleanup val stack = MemoryStack.stackPush();

        // Need to define a pipeline layout
        {
            val pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                                                               .sType$Default()
                                                               .setLayoutCount(0);
            val ptr = stack.callocLong(1);
            if (vkCreatePipelineLayout(device, pipelineLayoutInfo, null, ptr) != VK_SUCCESS) {
                showError("Unable to create the pipeline layout");
                return false;
            }
            pipelineLayout = ptr.get(0);
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
                        .module(vertShader);
            //noinspection resource
            shaderStages.get(1)
                        .stage(VK_SHADER_STAGE_FRAGMENT_BIT)
                        .module(fragShader);
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
                    .layout(pipelineLayout)
                    .renderPass(VK_NULL_HANDLE);

        val ptr = stack.callocLong(1);
        if (vkCreateGraphicsPipelines(device, NULL, pipelineInfo, null, ptr) != VK_SUCCESS) {
            showError("Error creating the pipeline");
            return false;
        }

        pipeline = ptr.get(0);
        return true;
    }

    private boolean createSyncResources() {
        assert device != null;

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
            if (vkCreateSemaphore(device, semaphoreInfo, null, ptr) != VK_SUCCESS) {
                showError("Unable to create the timeline semaphore");
                return false;
            }

            timelineSemaphore = ptr.get(0);
        }


        // Initialize frame resources
        frameResources = IntStream.range(0, MaxFramesInFlight)
                                  .mapToObj(_ -> new FrameResources())
                                  .toArray(FrameResources[]::new);

        // per-frame image-acquire semaphores
        val semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack)
                                                 .sType$Default();
        val ptr = stack.callocLong(1);
        for (val res : frameResources) {
            if (vkCreateSemaphore(device, semaphoreInfo, null, ptr) != VK_SUCCESS) {
                showError("Error creating the per-frame image-acquire semaphore");
                return false;
            }
            res.imageAcquiredSemaphore = ptr.get(0);
        }
        return true;
    }

    private boolean createCommandBuffers() {
        assert device != null;
        assert frameResources != null;

        @Cleanup val stack = MemoryStack.stackPush();

        // We'll give each frame its own pool, faster cmd buffer resets this way
        {
            val poolInfo = VkCommandPoolCreateInfo.calloc(stack)
                                                  .sType$Default()
                                                  .queueFamilyIndex(gfxQueueFamIdx);
            val ptr = stack.callocLong(1);
            for (val res : frameResources) {
                if (vkCreateCommandPool(device, poolInfo, null, ptr) != VK_SUCCESS) {
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
            for (val res : frameResources) {
                cmdAllocInfo.commandPool(res.commandPool);
                if (vkAllocateCommandBuffers(device, cmdAllocInfo, ptr) != VK_SUCCESS) {
                    showError("Unable to allocate command buffer");
                    return false;
                }
                res.commandBuffer = new VkCommandBuffer(ptr.get(0), device);
            }
        }

        return true;
    }

    private void render() {
        assert device != null;
        assert gfxQueue != null;
        assert frameResources != null;
        assert swapchainImages != null;
        assert swapchainImageViews != null;
        assert renderCompleteSemaphores != null;

        @Cleanup val stack = MemoryStack.stackPush();

        // first check if our swapchain is still valid
        if (requireSwapchainRecreate) {
            vkDeviceWaitIdle(device);
            destroySwapchain();
            createSwapchain();
            requireSwapchainRecreate = false;
        }

        val frameResIndex = (int) (frameIndex++ % MaxFramesInFlight);
        val signalValue = nextSignalValue++;
        val waitValue = signalValue - MaxFramesInFlight;

        // Wait on timeline
        {
            val waitInfo = VkSemaphoreWaitInfo.calloc(stack)
                                              .sType$Default()
                                              .semaphoreCount(1)
                                              .pSemaphores(stack.longs(timelineSemaphore))
                                              .pValues(stack.longs(waitValue));
            vkWaitSemaphores(device, waitInfo, Long.MAX_VALUE);
        }

        // Get the resources for this frame
        final FrameResources res;
        {
            res = frameResources[frameResIndex];
            assert res.commandPool != NULL;
            assert res.commandBuffer != null;
            assert res.imageAcquiredSemaphore != NULL;
        }

        // Now it's safe to start recording commands
        vkResetCommandPool(device, res.commandPool, 0);

        // Figure out what image in the swapchain is next
        final int imageIndex;
        final long swapchainImage;
        final long swapchainImageView;
        final long renderCompleteSemaphore;
        {
            val ptr = stack.callocInt(1);
            val acquireResult = vkAcquireNextImageKHR(device,
                                                      swapchain,
                                                      Long.MAX_VALUE,
                                                      res.imageAcquiredSemaphore,
                                                      VK_NULL_HANDLE,
                                                      ptr);
            imageIndex = ptr.get(0);

            // Handle resize and out-of-date images, may need swapchain recreate
            switch (acquireResult) {
                case VK_ERROR_OUT_OF_DATE_KHR -> {
                    // Bail now
                    requireSwapchainRecreate = true;
                    return;
                }
                // Can render this frame, recreate next time around
                case VK_SUBOPTIMAL_KHR -> requireSwapchainRecreate = true;
            }

            swapchainImage = swapchainImages[imageIndex];
            swapchainImageView = swapchainImageViews[imageIndex];
            renderCompleteSemaphore = renderCompleteSemaphores[imageIndex];

            assert swapchainImage != NULL;
            assert swapchainImageView != NULL;
            assert renderCompleteSemaphore != NULL;
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
                          // Both specified to control memory access at both stages (write)
                          .dstStageMask(VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT |
                                        VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT)
                          .dstAccessMask(VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT)
                          .oldLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                          .newLayout(VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL)
                          .image(depthImage)
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
                            .clearValue(clr -> clr.color(v -> v.float32(stack.floats(0.01F, 0.01F, 0.01F, 1F))));
            val depthAttachInfo = VkRenderingAttachmentInfo.calloc(stack)
                                                           .sType$Default();
            depthAttachInfo.imageView(depthImageView)
                           .imageLayout(VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL)
                           .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                           .storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                           .clearValue(clr -> clr.depthStencil(v -> v.depth(1F)
                                                                     .stencil(0)));

            renderingInfo = VkRenderingInfo.calloc(stack)
                                           .sType$Default()
                                           .renderArea(a -> a.offset(o -> o.set(0, 0))
                                                             .extent(ext -> ext.set(swapchainWidth, swapchainHeight)))
                                           .layerCount(1)
                                           .pColorAttachments(colorAttachInfos)
                                           .pDepthAttachment(depthAttachInfo);
        }

        // Begin dynamic rendering
        vkCmdBeginRendering(res.commandBuffer, renderingInfo);
        {
            // Set viewport state
            val viewports = VkViewport.calloc(1, stack);
            //noinspection resource
            viewports.get(0)
                     .set(0F, 0F, swapchainWidth, swapchainHeight, 0F, 1F);
            vkCmdSetViewport(res.commandBuffer, 0, viewports);

            // Set scissor state
            val scissors = VkRect2D.calloc(1, stack);
            //noinspection resource
            scissors.get(0)
                    .offset(o -> o.set(0, 0))
                    .extent(ext -> ext.set(swapchainWidth, swapchainHeight));
            vkCmdSetScissor(res.commandBuffer, 0, scissors);

            // Draw our triangle
            vkCmdBindPipeline(res.commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
            vkCmdDraw(res.commandBuffer, 3, 1, 0, 0);
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
                            .semaphore(timelineSemaphore)
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
        vkQueueSubmit2(gfxQueue, submitInfos, VK_NULL_HANDLE);

        // Present the image
        val presentInfo = VkPresentInfoKHR.calloc(stack)
                                          .sType$Default()
                                          .pWaitSemaphores(stack.longs(renderCompleteSemaphore))
                                          .swapchainCount(1)
                                          .pSwapchains(stack.longs(swapchain))
                                          .pImageIndices(stack.ints(imageIndex))
                                          .pResults(null);
        vkQueuePresentKHR(gfxQueue, presentInfo);
    }

    private int debugCallback(int messageSeverity, int messageTypes, long pCallbackData, long pUserData) {
        //noinspection resource
        val callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        System.err.println(callbackData.pMessageString());
        return VK_FALSE;
    }

    @NoArgsConstructor
    static final class FrameResources {
        /*(VkCommandPool)*/ long commandPool = NULL;
        @Nullable VkCommandBuffer commandBuffer = null;
        /*(VkSemaphore)*/ long imageAcquiredSemaphore = NULL;
    }
}
