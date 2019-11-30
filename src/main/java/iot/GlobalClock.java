package iot;

import util.TimeHelper;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

/**
 * N.B. This clock store the triggers in a stack stack (list LIFO based on trigger uid)
 */
public class GlobalClock {

    private static long nextTriggerUid = 0;

    /**
     * A representation of time.
     */
    private LocalTime time;

    private Map<LocalTime, List<Trigger>> triggers;

    public GlobalClock() {
        time = LocalTime.of(0,0);
        triggers = new HashMap<>();
    }

    /**
     * Returns the current time.
     * @return The current time.
     */
    public LocalTime getTime() {
        return time;
    }

    /**
     * Increases the time with a given amount of milliseconds.
     * @param milliSeconds
     * @post Increases the time with a given amount of milliseconds.
     */
    public void tick(long milliSeconds) {
        for (long i = milliSeconds; i > 0; i--) {
            this.time = this.time.plus(1, ChronoUnit.MILLIS);
            fireTrigger();
        }
    }

    /**
     * Resets the time.
     * @post time is set to 0
     * @post all events are removed
     */
    public void reset() {
        this.time = LocalTime.of(0,0);
        triggers = new HashMap<>();
    }

    public boolean containsTriggers(LocalTime time) {
        return triggers.containsKey(time);
    }

    public long addTrigger(LocalTime time, Supplier<LocalTime> trigger) {
        var trig = new Trigger(trigger);
        addTrigger(TimeHelper.roundToMilli(time), trig);
        return trig.getUid();
    }

    public long addTriggerOneShot(LocalTime time, Runnable trigger) {
        return addTrigger(time, () -> {
            trigger.run();
            return LocalTime.of(0,0);
        });
    }

    private void addTrigger(LocalTime time, Trigger trigger) {
        if (containsTriggers(time)) {
            triggers.get(time).add(0,trigger);
        } else {
            List<Trigger> newTriggers = new ArrayList<>(List.of(trigger));
            triggers.put(time,newTriggers);
        }
    }

    public boolean removeTrigger(long triggerId) {
        for (Map.Entry<LocalTime, List<Trigger>> e: triggers.entrySet()) {
            if (e.getValue().removeIf(p -> p.getUid() == triggerId)) {
                return true;
            }
        }
        return false;
    }

    private void fireTrigger() {
        var triggersToFire = triggers.get(getTime());
        if (triggersToFire != null) {
            //Here you have to leave the normal 'for' because you can remove element from the list during the iteration
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < triggersToFire.size(); i++) {
                var trigger = triggersToFire.get(i);
                LocalTime newTime = trigger.getCallback().get();
                if (newTime.isAfter(getTime())) {
                    addTrigger(newTime, trigger);
                }
            }
            triggers.remove(getTime());
        }
    }

    private static class Trigger {

        private final long uid;
        private final Supplier<LocalTime> callback;

        public Trigger(Supplier<LocalTime> callback) {
            uid = nextTriggerUid++;
            this.callback = callback;
        }

        public long getUid() {
            return uid;
        }

        public Supplier<LocalTime> getCallback() {
            return callback;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Trigger trigger = (Trigger) o;
            return Objects.equals(getUid(), trigger.getUid());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getUid());
        }
    }
}
