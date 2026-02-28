package model;

public class Ingredient extends Entity {
    private double price;

    public Ingredient(String name, double price) {
        super(name);
        setPrice(price);
    }

    public double getPrice() { return price; }

    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Цена не может быть отрицательной");
        this.price = price;
    }
}