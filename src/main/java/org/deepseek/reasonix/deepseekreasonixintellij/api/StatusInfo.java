package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusInfo implements Serializable {
    @JsonProperty("label")
    public String label;
    @JsonProperty("running")
    public boolean running;
    @JsonProperty("plan")
    public boolean plan;
    @JsonProperty("bypass")
    public boolean bypass;
    @JsonProperty("cwd")
    public String cwd;
    @JsonProperty("used")
    public int used;
    @JsonProperty("window")
    public int window;
    @JsonProperty("cacheHit")
    public int cacheHit;
    @JsonProperty("cacheMiss")
    public int cacheMiss;
    @JsonProperty("lastUsage")
    public WireUsage lastUsage;
    @JsonProperty("balance")
    public Balance balance;
    @JsonProperty("jobs")
    public List<Job> jobs;

    public String getBalanceDisplay() {
        if (balance != null && balance.infos != null && !balance.infos.isEmpty()) {
            BalanceInfo info = balance.infos.get(0);
            return info.currency + " " + info.amount;
        }
        return balance != null ? balance.display : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Balance {
        @JsonProperty("available")
        public boolean available;
        @JsonProperty("infos")
        public List<BalanceInfo> infos;
        @JsonProperty("display")
        public String display;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BalanceInfo {
        @JsonProperty("currency")
        public String currency;
        @JsonProperty("amount")
        public double amount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Job {
        @JsonProperty("id")
        public String id;
        @JsonProperty("kind")
        public String kind;
        @JsonProperty("label")
        public String label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isPlan() {
        return plan;
    }

    public void setPlan(boolean plan) {
        this.plan = plan;
    }

    public boolean isBypass() {
        return bypass;
    }

    public void setBypass(boolean bypass) {
        this.bypass = bypass;
    }

    public String getCwd() {
        return cwd;
    }

    public void setCwd(String cwd) {
        this.cwd = cwd;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public int getWindow() {
        return window;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public int getCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(int cacheHit) {
        this.cacheHit = cacheHit;
    }

    public int getCacheMiss() {
        return cacheMiss;
    }

    public void setCacheMiss(int cacheMiss) {
        this.cacheMiss = cacheMiss;
    }

    public WireUsage getLastUsage() {
        return lastUsage;
    }

    public void setLastUsage(WireUsage lastUsage) {
        this.lastUsage = lastUsage;
    }

    public Balance getBalance() {
        return balance;
    }

    public void setBalance(Balance balance) {
        this.balance = balance;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    @Override
    public String toString() {
        return "StatusInfo{" +
                "label='" + label + '\'' +
                ", running=" + running +
                ", plan=" + plan +
                ", bypass=" + bypass +
                ", cwd='" + cwd + '\'' +
                ", used=" + used +
                ", window=" + window +
                ", cacheHit=" + cacheHit +
                ", cacheMiss=" + cacheMiss +
                ", lastUsage=" + lastUsage +
                ", balance=" + balance +
                ", jobs=" + jobs +
                '}';
    }
}
