abstract class Entity {
    protected String name;

    public Entity (String name) {
        this.name = name;
    }

    public  String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }
}

class Ingredient extends Entity {
    private double price;

    public Ingredient (String name, double price) {
        super(name);
        this.price = price;
    }

    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }

}

abstract class Base extends Entity {
    protected double price;
    protected static final double  CLASSIC_BASE_PRICE = 100.0;

    public Base (String name, double price) {
        super(name);
        this.price = price;
    }

    public double getPrice () {
        return price;
    } 

    public void setPrice (double price) {
        this.price = price;
    }
}

class ClassicBase extends Base {
    public ClassicBase () {
        super("Классическая", CLASSIC_BASE_PRICE);
    }

    @Override
    public void setPrice(double price) {
        this.price = CLASSIC_BASE_PRICE;
    }
}

class NotClassicBase extends Base {
        private static double checkedPrice(double price) {
        double maxPrice = CLASSIC_BASE_PRICE * 1.2;
        if (price > maxPrice) {
            throw new IllegalArgumentException(
                String.format("Стоимость не должна превышать %.2f руб. (20%% от классической)", maxPrice)
            );
        }
        return price;
    }

    public NotClassicBase(String name, double price) {
        super(name, checkedPrice(price));
    }

    @Override
    public void setPrice (double price) {
        double maxPrice = CLASSIC_BASE_PRICE * 1.2;
        if (price > maxPrice) throw new IllegalArgumentException(
                String.format("Стоимость не должна превышать %.2f руб. (20%% от классической)", maxPrice));
        else this.price = price;
    }
}



