package model;

public abstract class Base extends Entity {
    private double price;
    private static double classicBasePrice = 100.0;

    public static double getClassicBasePrice() { return classicBasePrice; }

    public static void setClassicBasePrice(double price) {
        if (price <= 0) throw new IllegalArgumentException("Цена основы должна быть > 0");
        classicBasePrice = price;
    }

    protected Base(String name, double price) {
        super(name);
        setPrice(price);
    }

    public final double getPrice() { return price; }

    public void setPrice(double price) {
        if (price <= 0) throw new IllegalArgumentException("Цена основы должна быть > 0");
        this.price = price;
    }
}