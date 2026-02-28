package model;

public enum Mode {
    BASIC("Целиком"),
    HALFS("Половинки"),
    PARTS("Кусочки");

    private final String name;

    Mode(String name) { this.name = name; }

    public String getName() { return name; }
}