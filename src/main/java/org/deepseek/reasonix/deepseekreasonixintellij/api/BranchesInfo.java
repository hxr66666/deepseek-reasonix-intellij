package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchesInfo {
    @JsonProperty("branches")
    public List<BranchItem> branches;
    @JsonProperty("tree")
    public String tree;

    public List<BranchItem> getBranches() {
        return branches;
    }

    public void setBranches(List<BranchItem> branches) {
        this.branches = branches;
    }

    public String getTree() {
        return tree;
    }

    public void setTree(String tree) {
        this.tree = tree;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BranchItem {
        @JsonProperty("id")
        public String id;
        @JsonProperty("created_at")
        public String createdAt;
        @JsonProperty("updated_at")
        public String updatedAt;
        @JsonProperty("path")
        public String path;
        @JsonProperty("ModTime")
        public String modTime;
        @JsonProperty("Preview")
        public String preview;
        @JsonProperty("Turns")
        public int turns;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getModTime() {
            return modTime;
        }

        public void setModTime(String modTime) {
            this.modTime = modTime;
        }

        public String getPreview() {
            return preview;
        }

        public void setPreview(String preview) {
            this.preview = preview;
        }

        public int getTurns() {
            return turns;
        }

        public void setTurns(int turns) {
            this.turns = turns;
        }
    }
}
