package model;

public enum Size {
    SMALL(20, 6, 0.8, "Маленькая"),
    MEDIUM(30, 8, 1.0, "Средняя"),
    LARGE(40, 12, 1.2, "Большая");

    private final int diameter;
    private final int amount;
    private final double k;
    private final String name;

    Size(int diameter, int amount, double k, String name) {
        this.diameter = diameter;
        this.amount = amount;
        this.k = k;
        this.name = name;
    }

    public int getDiameter() { return diameter; }
    public int getAmount() { return amount; }
    public double getK() { return k; }
    public String getName() { return name; }
}