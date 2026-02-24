package com.exometric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SystemMetrics {

    private static long lastCpuUsage = 0;
    private static long lastCpuTime = 0;

    public static Long getMemoryUsageBytes() {
        try {
            File cgroupV2 = new File("/sys/fs/cgroup/memory.current");
            if (cgroupV2.exists()) {
                return Long.parseLong(readFile(cgroupV2).trim());
            }
            File cgroupV1 = new File("/sys/fs/cgroup/memory/memory.usage_in_bytes");
            if (cgroupV1.exists()) {
                return Long.parseLong(readFile(cgroupV1).trim());
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public static Double getCpuUsagePercent() {
        try {
            long currentCpuUsage = 0;
            long currentTime = System.currentTimeMillis();

            File cpuStat = new File("/sys/fs/cgroup/cpu.stat");
            if (cpuStat.exists()) {
                String content = readFile(cpuStat);
                for (String line : content.split("\n")) {
                    if (line.startsWith("usage_usec")) {
                        currentCpuUsage = Long.parseLong(line.split("\\s+")[1]) * 1000L; // convert to nanos
                        break;
                    }
                }
            } else {
                File cpuacct = new File("/sys/fs/cgroup/cpuacct/cpuacct.usage");
                if (cpuacct.exists()) {
                    currentCpuUsage = Long.parseLong(readFile(cpuacct).trim()); // usually nanos
                }
            }

            if (currentCpuUsage > 0) {
                if (lastCpuUsage > 0 && lastCpuTime > 0) {
                    long cpuDelta = currentCpuUsage - lastCpuUsage;
                    long timeDelta = currentTime - lastCpuTime;
                    if (timeDelta > 0) {
                        double percentage = (cpuDelta / (double)(timeDelta * 1000000L)) * 100.0;
                        lastCpuUsage = currentCpuUsage;
                        lastCpuTime = currentTime;
                        return percentage;
                    }
                }
                lastCpuUsage = currentCpuUsage;
                lastCpuTime = currentTime;
                return null;
            }
            
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static String getActiveNetworkInterface() {
        File netDir = new File("/sys/class/net");
        if (netDir.exists() && netDir.isDirectory()) {
            File[] interfaces = netDir.listFiles();
            if (interfaces != null) {
                for (File iface : interfaces) {
                    if (!iface.getName().equals("lo")) {
                        try {
                            String operstate = readFile(new File(iface, "operstate")).trim();
                            if ("up".equals(operstate)) {
                                return iface.getName();
                            }
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }
        }
        return "eth0"; // fallback
    }

    public static Long[] getNetworkMetricsBytes() {
        String iface = getActiveNetworkInterface();
        Long rx = null;
        Long tx = null;
        try {
            File rxFile = new File("/sys/class/net/" + iface + "/statistics/rx_bytes");
            if (rxFile.exists()) {
                rx = Long.parseLong(readFile(rxFile).trim());
            }
            File txFile = new File("/sys/class/net/" + iface + "/statistics/tx_bytes");
            if (txFile.exists()) {
                tx = Long.parseLong(readFile(txFile).trim());
            }
        } catch (Exception e) {
            // ignore
        }
        return new Long[]{ rx, tx };
    }

    public static Long getDiskUsageBytes() {
        try {
            File currentDir = new File(".");
            long totalSpace = currentDir.getTotalSpace();
            long freeSpace = currentDir.getUsableSpace();
            return totalSpace - freeSpace;
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public static Long getUptimeSeconds() {
        try {
            return java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        } catch (Exception e) {
            return null;
        }
    }

    private static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
