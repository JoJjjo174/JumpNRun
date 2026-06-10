package at.jonathans.jumpNRun;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PluginVersion(@JsonProperty("version_number") String versionNumber) { }
