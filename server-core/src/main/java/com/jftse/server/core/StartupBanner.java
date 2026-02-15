package com.jftse.server.core;

import org.apache.logging.log4j.Logger;

public final class StartupBanner {
    private static String pad(Object value, int width) {
        return String.format("%-" + width + "s", value);
    }

    private static String jvmInfo() {
        String version = System.getProperty("java.version");
        String vmName = System.getProperty("java.vm.name");
        return version + " (" + vmName + ")";
    }

    private static String maxHeap() {
        long bytes = Runtime.getRuntime().maxMemory();
        long mb = bytes / (1024 * 1024);
        return mb + " MB";
    }

    private static String pid() {
        // works on Java 9+
        return String.valueOf(ProcessHandle.current().pid());
    }

    public static void print(
            Logger log,
            String serverName,
            int port,
            boolean useEpoll
    ) {
        log.info("""
                        
                        ***************************************
                        * {}*
                        * Port: {}*
                        * Transport: {}*
                        * PID: {}*
                        * JVM: {}*
                        * Max Heap: {}*
                        ***************************************""",
                pad(serverName + " successfully started!", 36),
                pad(port, 30),
                pad(useEpoll ? "Epoll" : "NIO", 25),
                pad(pid(), 31),
                pad(jvmInfo(), 31),
                pad(maxHeap(), 26)
        );
    }
}
