package com.ventooth.modernvk;

import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;

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
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.VK10.VK_FALSE;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceProperties;
import static org.lwjgl.vulkan.VK14.VK_API_VERSION_1_4;

@NoArgsConstructor
public final class Application {
    private static final int VulkanVersion = VK_API_VERSION_1_4;
    private static final int swapchainFormat = VK_FORMAT_B8G8R8A8_SRGB;
    //	constexpr static uint32_t MaxFramesInFlight{ 2 };
    //	constexpr static VkFormat depthFormat{ VK_FORMAT_D32_SFLOAT };

    private /*(SDL_Window *)*/ long window = NULL;
    private int width = 1280;
    private int height = 720;
    private boolean running = false;
    //    uint64_t frameIndex = 0;
    //    uint64_t nextSignalValue = MaxFramesInFlight + 1;

    // vulkan core
    private @Nullable VkDebugUtilsMessengerCallbackEXT debugCallback;
    private @Nullable VkInstance vulkanInstance = null;
    private /*(VkSurfaceKHR)*/ long surface = NULL;
    private @Nullable VkPhysicalDevice physicalDevice = null;
    //	VkDevice device = nullptr;
    //	VmaAllocator vmaAllocator = nullptr;
    //
    //	// queue related
    //	uint32_t gfxQueueFamIdx = UINT32_MAX;
    //	VkQueue gfxQueue = nullptr;
    //
    //	// swapchain related
    //	VkSwapchainKHR swapchain = nullptr;
    //	std::vector<VkImage> swapchainImages;
    //	std::vector<VkImageView> swapchainImageViews;
    //	std::vector<VkSemaphore> renderCompleteSemaphores;
    //	bool requireSwapchainRecreate = false;
    //	uint32_t swapchainWidth = 0;
    //	uint32_t swapchainHeight = 0;
    //
    //	VkImage depthImage = nullptr;
    //	VkImageView depthImageView = nullptr;
    //	VmaAllocation depthImageAllocation = nullptr;
    //
    //	// graphics pipeline related
    //	VkPipelineLayout pipelineLayout = nullptr;
    //	VkPipeline pipeline = nullptr;
    //
    //	// shader resources
    //	VkShaderModule vertShader = nullptr;
    //	VkShaderModule fragShader = nullptr;
    //
    //	// frame and synchronization resources
    //	VkSemaphore timelineSemaphore = nullptr;
    //	std::array<FrameResources, MaxFramesInFlight> frameResources;

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

        if (!initializeVulkan()) {
            return false;
        }

        return true;
    }

    public void run() {
        running = true;
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
                    break;
                }
            }

            render();
        }
    }

    public void shutdown() {
        //        // wait in case resources are in use
        //        vkDeviceWaitIdle(device);
        //
        //        // frame / sync object cleanup
        //        if (timelineSemaphore) {
        //            vkDestroySemaphore(device, timelineSemaphore, nullptr);
        //        }
        //        for (auto & res :frameResources)
        //        {
        //            vkDestroySemaphore(device, res.imageAcquiredSemaphore, nullptr);
        //            vkDestroyCommandPool(device, res.commandPool, nullptr); // destroys buffers implicitly
        //        }
        //
        //        // pipeline cleanup
        //        if (pipelineLayout) {
        //            vkDestroyPipelineLayout(device, pipelineLayout, nullptr);
        //        }
        //        if (pipeline) {
        //            vkDestroyPipeline(device, pipeline, nullptr);
        //        }
        //
        //        // cleanup shaders
        //        if (vertShader) {
        //            vkDestroyShaderModule(device, vertShader, nullptr);
        //        }
        //        if (fragShader) {
        //            vkDestroyShaderModule(device, fragShader, nullptr);
        //        }
        //
        //        // cleanup swapchain
        //        destroySwapchain();
        //
        //        // VMA
        //        if (vmaAllocator) {
        //            vmaDestroyAllocator(vmaAllocator);
        //        }

        // cleanup Vulkan
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

        // cleanup SDL
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
        //        if (!findGraphicsQueue()) {
        //            showError("Unable to find a compatible graphics queue");
        //            return false;
        //        }
        //
        //        if (!createDevice(physicalDevice)) {
        //            showError("Couldn't create the logical GPU device");
        //            return false;
        //        }
        //
        //        if (!initializeVMA()) {
        //            showError("Unable to create Vulkan Memory Allocator");
        //            return false;
        //        }
        //
        //        if (!createSwapchain(width, height)) {
        //            showError("Unable to create swapchain");
        //            return false;
        //        }
        //
        //        if (!createShaders()) {
        //            showError("Error creating shader modules");
        //            return false;
        //        }
        //
        //        pipeline = createGraphicsPipeline();
        //        if (!pipeline) {
        //            showError("Unable to initialize the graphics pipeline");
        //            return false;
        //        }
        //
        //        if (!createSyncResources()) {
        //            showError("Couldn't create the sync related resources");
        //            return false;
        //        }
        //
        //        if (!createCommandBuffers()) {
        //            showError("Couldn't create command buffer objects");
        //            return false;
        //        }

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

        @Cleanup val stack = MemoryStack.stackPush();

        // Try to find a suitable device, preferably a dedicated gpu
        final VkPhysicalDevice selectedDev;
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
            var tempDev = list.getFirst(); // default to first GPU
            // look through list and see if a dGPU exists
            for (val dev : list) {
                vkGetPhysicalDeviceProperties(dev, props);
                if (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                    tempDev = dev;
                    break;
                }
            }

            selectedDev = tempDev;
        }

        // Ensure the desired swapchain format is supported
        {
            val count = stack.callocInt(1);
            vkGetPhysicalDeviceSurfaceFormatsKHR(selectedDev, surface, count, null);
            val formats = VkSurfaceFormatKHR.calloc(count.get(0), stack);
            vkGetPhysicalDeviceSurfaceFormatsKHR(selectedDev, surface, count, formats);

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

        physicalDevice = selectedDev;
        return true;
    }

    private void render() {

    }

    private int debugCallback(int messageSeverity, int messageTypes, long pCallbackData, long pUserData) {
        //noinspection resource
        val callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        System.err.println(callbackData.pMessageString());
        return VK_FALSE;
    }
}
