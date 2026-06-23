package com.pixelmc.pixelmcmanager.maintenance;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public final class StopServerPlan {
    private final long createdAtMillis;
    private final Duration originalDelay;
    private final Set<Integer> minuteWarnings = new HashSet<>();
    private final Set<Integer> secondWarnings = new HashSet<>();
    private boolean maintenanceActive = false;

    public StopServerPlan(long createdAtMillis, Duration originalDelay) {
        this.createdAtMillis = createdAtMillis;
        this.originalDelay = originalDelay;
    }

    public long kickTimeMillis() {
        return createdAtMillis + originalDelay.toMillis();
    }

    public long stopTimeMillis() {
        return kickTimeMillis() + 15_000L;
    }

    public Duration originalDelay() {
        return originalDelay;
    }

    public boolean maintenanceActive() {
        return maintenanceActive;
    }

    public void activateMaintenance() {
        maintenanceActive = true;
    }

    public boolean markMinuteWarning(int minute) {
        return minuteWarnings.add(minute);
    }

    public boolean markSecondWarning(int second) {
        return secondWarnings.add(second);
    }
}
