package com.ventooth.modernvk;

import lombok.NoArgsConstructor;
import lombok.val;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.system.MemoryStack;

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
import static org.lwjgl.system.MemoryUtil.NULL;

@NoArgsConstructor
public final class Application {
    //	constexpr static uint32_t VulkanVersion{ VK_API_VERSION_1_4 };
    //	constexpr static uint32_t MaxFramesInFlight{ 2 };
    //	constexpr static VkFormat swapchainFormat{ VK_FORMAT_B8G8R8A8_SRGB };
    //	constexpr static VkFormat depthFormat{ VK_FORMAT_D32_SFLOAT };
    //
    private long window = NULL;
    private int width = 1280;
    private int height = 720;
    private boolean running = false;
    //    uint64_t frameIndex = 0;
    //    uint64_t nextSignalValue = MaxFramesInFlight + 1;
    //
    //	// vulkan core
    //	VkInstance vulkanInstance = nullptr;
    //	VkPhysicalDevice physicalDevice = nullptr;
    //	VkDevice device = nullptr;
    //	VkSurfaceKHR surface = nullptr;
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
        try (val stack = MemoryStack.stackPush()) {
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
//
//        // cleanup Vulkan
//        if (surface) {
//            vkDestroySurfaceKHR(vulkanInstance, surface, nullptr);
//        }
//        if (device) {
//            vkDestroyDevice(device, nullptr);
//        }
//        if (vulkanInstance) {
//            vkDestroyInstance(vulkanInstance, nullptr);
//        }
//        volkFinalize();
//
        // cleanup SDL
        if (window != NULL) {
            SDL_DestroyWindow(window);
        }
        SDL_Quit();
    }

    private boolean initializeVulkan() {
        //        if (!createVulkanInstance()) {
        //            showError("Couldn't create a vulkan instance");
        //            return false;
        //        }
        //
        //        if (!createSurface()) {
        //            showError("Couldn't create window surface");
        //            return false;
        //        }
        //
        //        physicalDevice = findPhysicalDevice();
        //        if (!physicalDevice) {
        //            showError("Unable to find an appropriate physical device");
        //            return false;
        //        }
        //
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

    private void render() {

    }
}
