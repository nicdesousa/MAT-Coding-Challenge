package com.github.nicdesousa.telemetry.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RegisterForReflection
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Location {
    @JsonProperty("lat")
    private double latitude = 0D;
    @JsonProperty("long")
    private double longitude = 0D;
}
