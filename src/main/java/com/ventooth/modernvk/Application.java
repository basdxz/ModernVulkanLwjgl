package com.ventooth.modernvk;

import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan14Features;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties2;
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
import static org.lwjgl.util.vma.Vma.VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT;
import static org.lwjgl.util.vma.Vma.vmaCreateAllocator;
import static org.lwjgl.util.vma.Vma.vmaDestroyAllocator;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.VK_FALSE;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceProperties;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceFeatures2;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceQueueFamilyProperties2;
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
    private @Nullable VkDevice device = null;
    private /*(VmaAllocator)*/ long vmaAllocator = NULL;
    //
    // queue related
    private int gfxQueueFamIdx = -1;
    private @Nullable VkQueue gfxQueue = null;
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
                                                  .pNext(enabledFeatures)
                                                  .pQueueCreateInfos(gfxQueueInfos)
                                                  .ppEnabledExtensionNames(extensions);
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
        assert device != null;
        assert physicalDevice != null;

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

    private void render() {

    }

    private int debugCallback(int messageSeverity, int messageTypes, long pCallbackData, long pUserData) {
        //noinspection resource
        val callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        System.err.println(callbackData.pMessageString());
        return VK_FALSE;
    }
}
