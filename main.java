import java.util.*;

abstract class Entity {
    protected String name;
    protected UUID id = UUID.randomUUID();

    public Entity (String name) {
        this.name = name;
    }

    public UUID getId () {
        return id;
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


class Side extends {
    private double price;
    Side (String name, double price) {
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


public enum Size {
    SMALL(20, 6, 0.8),
    MEDIUM(30, 8, 1.0),
    LARGE(40, 12, 1.2);

    private final int diameter;
    private final int amount;
    private final double k;

    Size(int diameter, int amount, double k) {
        this.diameter = diameter;
        this.amount = amount;
        this.k = k;
    }

    public int getDiameter() {
        return diameter;
    }

    public int getAmount() {
        return amount;
    }

    public double getK() {
        return k;
    }

}


abstract class Slice extends Entity {
    protected List<Ingredient> ingredients = new ArrayList<>();
    protected Size size;
    protected double price;

    Slice (String name, Size size) {
        super(name);
        this.size = size;
    }

    public void addIngredient (Ingredient ingredient) {
        ingredients.add(ingredient);

    }

    public void removeIngredient (Ingredient ingredient) {
        ingredients.remove(ingredient);
    }

}



public enum Mode {
    BASIC,
    HALFS,
    PARTS;
}

class Pizza extends Slice {
    private List<Slice> slices = new ArrayList<>();
    private Base base;
    private double price = 0;
    private Mode mode;
    


    Pizza (String name, Base base, Size size, Mode mode) {
        super(name, size);
        if (name == null) throw new IllegalArgumentException ("У пиццы должна быть основа");
        else {
            this.base = base;
            this.mode = mode;
            price += base.price * size.getK;
        }
    }

    



}


