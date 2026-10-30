package com.ventooth.modernvk;

import lombok.val;
import org.lwjgl.system.Configuration;

public final class Main {
    static {
        // Track leaks
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
        // Prefer the SDL function provider
        Configuration.VULKAN_EXPLICIT_INIT.set(true);
    }

    private Main() {
        throw new UnsupportedOperationException();
    }

    static void main() {
        Structs.initialize();

        val app = new Application();
        if (app.initialize()) {
            app.loadData();
            app.run();
        }
        app.shutdown();
    }
}
