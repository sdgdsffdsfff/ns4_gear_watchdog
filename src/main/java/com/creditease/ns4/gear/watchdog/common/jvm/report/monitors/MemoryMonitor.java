package com.creditease.ns4.gear.watchdog.common.jvm.report.monitors;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author outman
 * @description Memory monitoring class
 * @date 2019/3/5
 */
public final class MemoryMonitor {

    private MemoryMonitor() {
        //Utility Class
    }

    public enum Type {
        All, Heap, NonHeap
    }

    public static class Report {

        private static final String USAGE_INIT = "usage.init";
        private static final String USAGE_COMMITTED = "usage.committed";
        private static final String USAGE_USED = "usage.used";
        private static final String USAGE_MAX = "usage.max";
        private static final String PEAK_INIT = "peak.init";
        private static final String PEAK_COMMITTED = "peak.committed";
        private static final String PEAK_USED = "peak.used";
        private static final String PEAK_MAX = "peak.max";

        private Map<String, Map<String, Object>> heapMap = new HashMap<>();
        private Map<String, Map<String, Object>> nonHeapMap = new HashMap<>();

        private Report() {
        }

        public Map<String, Map<String, Object>> getHeap() {
            return heapMap;
        }

        public Map<String, Map<String, Object>> getNonHeap() {
            return nonHeapMap;
        }

        void addMemoryBeanInfo(MemoryPoolMXBean bean) {
            Map<String, Map<String, Object>> memoryMap = bean.getType().equals(MemoryType.HEAP) ? heapMap : nonHeapMap;
            Map<String, Object> beanMap = memoryMap.get(bean.getName());
            if (beanMap == null) {
                beanMap = new HashMap<>();
                memoryMap.put(bean.getName(), beanMap);
            }
            addUsage(beanMap, bean.getUsage());
            addPeak(beanMap, bean.getPeakUsage());
        }

        private static void addUsage(Map<String, Object> map, MemoryUsage usage) {
            map.put(USAGE_INIT, usage.getInit());
            map.put(USAGE_COMMITTED, usage.getCommitted());
            map.put(USAGE_USED, usage.getUsed());
            map.put(USAGE_MAX, usage.getMax());
        }

        private static void addPeak(Map<String, Object> map, MemoryUsage peak) {
            map.put(PEAK_INIT, peak.getInit());
            map.put(PEAK_COMMITTED, peak.getCommitted());
            map.put(PEAK_USED, peak.getUsed());
            map.put(PEAK_MAX, peak.getMax());
        }
    }

    public static Report detect(Type selectType) {
        List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();
        Report report = new Report();
        for (MemoryPoolMXBean bean : beans) {
            if (selectType.equals(Type.All) || !filterPool(bean.getType(), selectType)) {
                report.addMemoryBeanInfo(bean);
            }
        }
        return report;
    }

    private static boolean filterPool(MemoryType type, Type selectType) {
        return ((selectType.equals(Type.NonHeap) && type.equals(MemoryType.HEAP))
                || (selectType.equals(Type.Heap) && type.equals(MemoryType.NON_HEAP)));
    }
}
