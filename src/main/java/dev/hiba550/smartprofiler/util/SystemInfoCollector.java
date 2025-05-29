    }
    
    /**
     * Gets GPU information using various detection methods
     */
    private static String getGpuInfo() {
        try {
            // Try to get GPU info from system properties first
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("windows")) {
                return getGpuInfoWindows();
            } else if (osName.contains("linux")) {
                return getGpuInfoLinux();
            } else if (osName.contains("mac")) {
                return getGpuInfoMac();
            }
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.debug("Could not detect GPU info", e);
        }
        
        return "Unknown GPU";
    }
    
    private static String getGpuInfoWindows() {
        try {
            // Use WMI query for Windows GPU detection
            Process process = Runtime.getRuntime().exec(
                "wmic path win32_VideoController get name /format:list"
            );
            
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Name=") && !line.equals("Name=")) {
                        return line.substring(5).trim();
                    }
                }
            }
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.debug("Failed to get Windows GPU info", e);
        }
        
        return "Unknown GPU (Windows)";
    }
    
    private static String getGpuInfoLinux() {
        try {
            // Try reading from /proc/driver/nvidia/version for NVIDIA
            java.nio.file.Path nvidiaPath = java.nio.file.Paths.get("/proc/driver/nvidia/version");
            if (java.nio.file.Files.exists(nvidiaPath)) {
                java.util.List<String> lines = java.nio.file.Files.readAllLines(nvidiaPath);
                if (!lines.isEmpty()) {
                    return "NVIDIA GPU (Driver: " + lines.get(0) + ")";
                }
            }
            
            // Try lspci command
            Process process = Runtime.getRuntime().exec("lspci | grep -i vga");
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
                
                String line = reader.readLine();
                if (line != null && line.contains(":")) {
                    return line.substring(line.indexOf(":") + 1).trim();
                }
            }
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.debug("Failed to get Linux GPU info", e);
        }
        
        return "Unknown GPU (Linux)";
    }
    
    private static String getGpuInfoMac() {
        try {
            // Use system_profiler for macOS
            Process process = Runtime.getRuntime().exec(
                "system_profiler SPDisplaysDataType -detailLevel mini"
            );
            
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().endsWith(":") && !line.contains("Displays")) {
                        return line.trim().replace(":", "");
                    }
                }
            }
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.debug("Failed to get macOS GPU info", e);
        }
        
        return "Unknown GPU (macOS)";
    }
    
    /**
     * Detects Java version and vendor information
     */
    public static JavaInfo detectJavaInfo() {
        String version = System.getProperty("java.version");
        String vendor = System.getProperty("java.vendor");
        String vmName = System.getProperty("java.vm.name");
        String vmVersion = System.getProperty("java.vm.version");
        
        // Parse major version number
        int majorVersion = parseMajorVersion(version);
        
        // Determine if it's a recommended version for MC 1.21.5
        boolean isRecommended = majorVersion >= 21;
        
        return new JavaInfo(version, vendor, vmName, vmVersion, majorVersion, isRecommended);
    }
    
    private static int parseMajorVersion(String version) {
        try {
            if (version.startsWith("1.")) {
                // Old format: 1.8.0_XXX
                return Integer.parseInt(version.substring(2, version.indexOf(".", 2)));
            } else {
                // New format: 17.0.1, 21.0.2
                return Integer.parseInt(version.substring(0, version.indexOf(".")));
            }
        } catch (Exception e) {
            return 8; // Default to Java 8 if parsing fails
        }
    }
    
    /**
     * Detects system performance characteristics
     */
    public static SystemPerformanceInfo detectSystemPerformance() {
        try {
            // Get OS-specific performance info
            com.sun.management.OperatingSystemMXBean osBean = 
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            
            double cpuLoad = osBean.getProcessCpuLoad();
            long freeMemory = osBean.getFreePhysicalMemorySize();
            long totalMemory = osBean.getTotalPhysicalMemorySize();
            
            // Calculate memory usage percentage
            double memoryUsage = ((double) (totalMemory - freeMemory) / totalMemory) * 100;
            
            // Detect if system is under load
            boolean isUnderLoad = cpuLoad > 0.8 || memoryUsage > 85;
            
            return new SystemPerformanceInfo(
                cpuLoad,
                memoryUsage,
                freeMemory,
                totalMemory,
                isUnderLoad
            );
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Error detecting system performance", e);
            return SystemPerformanceInfo.getDefault();
        }
    }
}