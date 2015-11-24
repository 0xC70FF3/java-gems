package org.javagems.core.benchmark;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BenchmarkTool {
    private static final BenchmarkTool INSTANCE = new BenchmarkTool(new DefaultSystemClockManager());

    protected interface SystemClockManager {
        public long getTimestamp();
    }

    private static class DefaultSystemClockManager implements SystemClockManager {
        public long getTimestamp() {
            return System.nanoTime();
        }
    }

    private static class Clock {
        private SystemClockManager clockManager;
        private long ellapsedTime;
        private long timestamp;
        private boolean running;

        public Clock(SystemClockManager clockManager) {
            this.clockManager = clockManager;
            this.reset();
        }

        public long getEllapsedTime() {
            return ellapsedTime;
        }

        public void reset() {
            this.ellapsedTime = 0l;
            this.timestamp = 0l;
            this.running = false;
        }

        public void start() {
            this.timestamp = this.clockManager.getTimestamp();
            this.running = true;
        }

        public void stop() {
            if (this.running) {
                this.ellapsedTime += (this.clockManager.getTimestamp() - this.timestamp);
                this.running = false;
            }
        }
    }

    private static class Process {
        private String name;
        private long trueEllapedTime;
        private Map<String, Process> subProcesses;
        private int depth;

        public Process(String name, int depth) {
            this.depth = depth;
            this.name = name;
            this.trueEllapedTime = 0l;
            this.subProcesses = new LinkedHashMap<String, Process>();
        }

        public void setTrueEllapsedTime(long ellapsedTime) {
            this.trueEllapedTime = ellapsedTime;
        }

        private long getEllapsedTime() {
            if (this.trueEllapedTime != 0) {
                return trueEllapedTime;
            } else {
                long subTime = 0;
                for (Entry<String, Process> e : this.subProcesses.entrySet()) {
                    subTime += e.getValue().getEllapsedTime();
                }
                return subTime;
            }
        }

        public Map<String, Process> getSubProcesses() {
            return this.subProcesses;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%s [%05dms]\n", this.name, this.getEllapsedTime() / 1000000l));
            for (Entry<String, Process> e : this.subProcesses.entrySet()) {
                for (int i = 0; i < this.depth; i++) {
                    sb.append("    ");
                }
                sb.append("`-- ").append(e.getValue());
            }
            return sb.toString();
        }
    }

    private Map<String, Clock> clocks;
    private SystemClockManager clockManager;

    public static BenchmarkTool getInstance() {
        return BenchmarkTool.INSTANCE;
    }

    protected BenchmarkTool(SystemClockManager clockManager) {
        this.clockManager = clockManager;
        this.clocks = new LinkedHashMap<String, Clock>();
    }

    public void start(String key) {
        if (!this.clocks.containsKey(key)) {
            this.clocks.put(key, new Clock(this.clockManager));
        }
        this.clocks.get(key).start();
    }

    public void stop(String key) {
        if (this.clocks.containsKey(key)) {
            this.clocks.get(key).stop();
        }
    }

    public void clear() {
        this.clocks.clear();
    }

    public void reset(String key) {
        if (!this.clocks.containsKey(key)) {
            this.clocks.put(key, new Clock(this.clockManager));
        }
        this.clocks.get(key).reset();
    }

    @Override
    public String toString() {
        Map<String, Process> processes = new LinkedHashMap<String, Process>();
        for (Entry<String, Clock> e : this.clocks.entrySet()) {
            e.getValue().stop(); // stop all
            String[] keys = e.getKey().split(":");
            Map<String, Process> current = processes;
            for (int i = 0; i < keys.length; i++) {
                if (!current.containsKey(keys[i])) {
                    current.put(keys[i], new Process(keys[i], i));
                }
                Process process = current.get(keys[i]);
                if (i == keys.length - 1) { // end process
                    process.setTrueEllapsedTime(e.getValue().getEllapsedTime());
                }
                current = process.getSubProcesses();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("***** Benchmark Result *****\n");
        for (Entry<String, Process> e : processes.entrySet()) {
            sb.append(e.getValue());
        }
        return sb.toString();
    }
}
