import java.util.*;
import java.time.*;

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
    private boolean doublePortion = false;

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

    public boolean getPortinon() {
        return doublePortion;
    }

    public void setPortion(boolean arg) {
        this.doublePortion = arg;
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


class Side extends Entity {
    private double price;
    private List<Pizza> banPizzas = new ArrayList<>();

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

    public List<Pizza> getBanPizzas() {
        return banPizzas;
    }

    public void addBanPizza(Pizza pizza) {
        banPizzas.add(pizza);
    }
    
    public void removeBanPizza(Pizza pizza) {
        banPizzas.removeIf(piz -> piz.getId().equals(pizza.getId()));
    }
}


enum Size {
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
        this.name=name;
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

    public String getName() {
        return name;
    }

}


abstract class Slice extends Entity {
    protected List<Ingredient> ingredients = new ArrayList<>();
    protected Size size;
    protected double price;
    protected Side side;

    Slice (String name, Size size, Side side) {
        super(name);
        this.size = size;
        this.side = side;
    }

    public void addIngredient (Ingredient ingredient) {
        ingredients.add(ingredient);

    }

    public void removeIngredient (Ingredient ingredient) {
        ingredients.remove(ingredient);
    }

    public void setSide(Side side, UUID id){
        if (!side.getBanPizzas().stream().anyMatch(p -> p.getId().equals(id))) this.side=side;
        else throw new IllegalArgumentException("Нельзя добавить такой борт к этой пицце");
    }

}



enum Mode {
    BASIC("Целиком"),
    HALFS("Половинки"),
    PARTS("Кусочки");

    private final String name;

    Mode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

class Pizza extends Slice {
    private List<Slice> slices;
    private Base base;
    private Mode mode;

    Pizza (String name, Base base, Size size, Mode mode, Side side) {
        super(name, size, side);
        if (base == null) throw new IllegalArgumentException ("У пиццы должна быть основа");
        else {
            this.base = base;
            this.mode = mode;
            this.slices = new ArrayList<>(size.getAmount());
        }
    }
    
    public Base getBase() {
        return base;
    }

    public double getPrice() {
        double price = 0;
        price += base.getPrice(); //стоимость основы
        for (Slice slice : slices){
            for (Ingredient ingredient : slice.ingredients) {
                int t = 1;
                if (ingredient.getPortinon()) t = 2; // двойная порция
                price+=ingredient.getPrice() * t; // стоимость ингредиента
            }
            price+=side.getPrice(); // стоимость бортиков
        }

        return price;
    }

    public Mode getMode() {
        return mode;
    }

    public List<Slice> getSlices() {
        return slices;
    }

    public void setBase(Base base) {
        this.base = base;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSlices(List<Slice> slices) {
        this.slices = slices;
    }

    public void setSize(Size size) {
        this.size = size;
        List<Slice> copy = new ArrayList<>(slices);
        if (mode == Mode.PARTS) {
            slices = new ArrayList<>(size.getAmount());
        }
        else {
            for (int i = 1; i < slices.size()/2; i++) {
                slices.set(i, copy.get(0));
            }
            for (int i = slices.size()/2; i < slices.size()-1; i++) {
                slices.set(i, copy.get(copy.size()-1));
            }
        }
    }

    public void addIngredientsBasic(Ingredient ingr) {
        for (Slice slice : slices) {
            slice.addIngredient(ingr);
        }
    }

    public void addIngredientHalfs(Pizza pizzaA, Pizza pizzaB) {
        for (int i=0; i<slices.size()/2; i++) {
            slices.set(i, pizzaA.getSlices().get(0));
        }

        for (int i=slices.size()/2; i<slices.size(); i++) {
            slices.set(i, pizzaB.getSlices().get(0));
        }
    
    }

    public void addIngredientParts(Ingredient ingr, int a, int b) {
        for (int i = a-1; i<b; i++) {
             slices.get(i).addIngredient(ingr);
        }
    }

    public void addSideBasic(Side side) {
        try {
            for (Slice slice:slices){
                slice.setSide(side, id);
            }
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Нельзя добавить такой борт к этой пицце");
        }
    }

    public void addSideHalfs(Side side, String half) {
        try {
            if (half == "A"){
                for (int i=0; i<slices.size()/2; i++) {
                    slices.get(i).setSide(side, id);
                }
            }
            else {
                for (int i=slices.size()/2; i<slices.size(); i++) {
                    slices.get(i).setSide(side, id);
                }
            }
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Нельзя добавить такой борт к этой пицце");
        }
    }

    
}


class Person extends Entity {
    double bill = 0;
    public Person (String name) {
        super(name);
    }
}



class Order extends Entity {

    private Map<UUID, List<Person>> pizzas = new HashMap<>();
    private List<Person> guests = new ArrayList<>();
    private List<Pizza> newPizzas = new ArrayList<>();
    private String comment;
    private Instant time;

    public Order(String name) {
        super(name);
        this.time = Instant.now();
    }

    public List<Person> getGuests() {
        return guests;
    }

    public Map<UUID, List<Person>> getPizzas() {
        return pizzas;
    }

    public void removeGuest(Person guest) {
        guests.removeIf(g -> g.getId().equals(guest.getId()));
        for (List<Person> persons : pizzas.values()) {
            persons.removeIf(person -> guest.getId().equals(person.getId()));
        }
    
    }

    public void createGuest(Person guest) {
        guests.add(guest);
    }

    public void removeGuestFromPizza (Pizza pizza, Person guest) {
        pizzas.get(pizza.getId()).removeIf(g -> g.getId().equals(guest.getId()));
    }

    public void addGuestToPizza(Pizza pizza, Person guest) {
        pizzas.get(pizza.getId()).add(guest);

    }

    public void addPizza(Pizza pizza) {
        pizzas.put(pizza.getId(), new ArrayList<Person>());
    }

    public void removePizza(Pizza pizza) {
        pizzas.remove(pizza.getId());
    }

    public void createNewPizza(Pizza pizza) {
        newPizzas.add(pizza);
    }

    public List<Pizza> getNewPizzas() {
        return newPizzas;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        if (this.time.isAfter(time)) {
            throw new IllegalArgumentException("Нельзя сделать заказ в прошлом");
        } else {
            this.time = time;
        }
    }
}


