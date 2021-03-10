package com.kotori316.limiter.capability;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import com.kotori316.limiter.TestSpawn;

public enum RuleType {
    DEFAULT("Default", "Defaults") {
        @Override
        public Set<TestSpawn> getRules(LMSHandler handler) {
            return handler.getDefaultConditions();
        }

        @Override
        public void removeAll(LMSHandler lmsHandler) {
            lmsHandler.clearDefaultConditions();
        }

        @Override
        public void add(LMSHandler lmsHandler, TestSpawn testSpawn) {
            lmsHandler.addDefaultCondition(testSpawn);
        }
    },
    DENY("Deny", "Denies") {
        @Override
        public Set<TestSpawn> getRules(LMSHandler handler) {
            return handler.getDenyConditions();
        }

        @Override
        public void removeAll(LMSHandler lmsHandler) {
            lmsHandler.clearDenyConditions();
        }

        @Override
        public void add(LMSHandler lmsHandler, TestSpawn testSpawn) {
            lmsHandler.addDenyCondition(testSpawn);
        }
    },
    FORCE("Force", "Forces") {
        @Override
        public Set<TestSpawn> getRules(LMSHandler handler) {
            return handler.getForceConditions();
        }

        @Override
        public void removeAll(LMSHandler lmsHandler) {
            lmsHandler.clearForceConditions();
        }

        @Override
        public void add(LMSHandler lmsHandler, TestSpawn testSpawn) {
            lmsHandler.addForceCondition(testSpawn);
        }
    };
    private final String text;
    private final String commandName;

    RuleType(String text, String commandName) {
        this.text = text;
        this.commandName = commandName;
    }

    public String getText() {
        return text;
    }

    public String getCommandName() {
        return commandName;
    }

    public abstract Set<TestSpawn> getRules(LMSHandler handler);

    public abstract void removeAll(LMSHandler lmsHandler);

    public abstract void add(LMSHandler lmsHandler, TestSpawn testSpawn);

    public final void addAll(LMSHandler lmsHandler, Collection<TestSpawn> spawns) {
        this.removeAll(lmsHandler);
        spawns.forEach(t -> this.add(lmsHandler, t));
    }

    public String saveName() {
        return getText().toLowerCase(Locale.ROOT);
    }

}
