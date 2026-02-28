package model;

public class NotClassicBase extends Base {
    private static double checkedPrice(double price) {
        double maxPrice = Base.getClassicBasePrice() * 1.2;
        if (price > maxPrice) {
            throw new IllegalArgumentException(
                    String.format("Стоимость не должна превышать %.2f руб. (20%% от классической)", maxPrice)
            );
        }
        if (price <= 0) throw new IllegalArgumentException("Цена основы должна быть > 0");
        return price;
    }

    public NotClassicBase(String name, double price) {
        super(name, checkedPrice(price));
    }

    @Override
    public void setPrice(double price) {
        double maxPrice = Base.getClassicBasePrice() * 1.2;
        if (price > maxPrice) throw new IllegalArgumentException(
                String.format("Стоимость не должна превышать %.2f руб. (20%% от классической)", maxPrice));
        if (price <= 0) throw new IllegalArgumentException("Цена основы должна быть > 0");
        super.setPrice(price);
    }
}