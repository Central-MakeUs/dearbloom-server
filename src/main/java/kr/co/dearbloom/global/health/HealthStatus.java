package kr.co.dearbloom.global.health;

import lombok.Getter;

@Getter
public enum HealthStatus {
    CONNECTED("Connected"),
    DISCONNECTED("Not Connected");

    private final String label;
    HealthStatus(String label) { this.label = label; }
}
