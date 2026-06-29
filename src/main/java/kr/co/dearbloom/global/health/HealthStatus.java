package kr.co.dearbloom.global.health;

public enum HealthStatus {
    CONNECTED("Connected"),
    DISCONNECTED("Not Connected");

    private final String label;
    HealthStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
