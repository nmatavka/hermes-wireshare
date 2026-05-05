package org.limewire.ui.compose.integration;

public final class ComposeEventDispatchErrorCatcher {
    public void handle(Throwable problem) {
        CoreComposeRuntimeErrorService.handleEventDispatchThrowable(problem);
    }
}
