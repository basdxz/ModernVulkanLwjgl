package com.ventooth.modernvk;

import lombok.val;

public final class Main {
    private Main() {
        throw new UnsupportedOperationException();
    }

    static void main() {
        val app = new Application();
        if (app.initialize()) {
            app.run();
        }
        app.shutdown();
    }
}
