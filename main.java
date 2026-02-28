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

record IngredientPortion(Ingredient ingredient, int multiplier) {
    public IngredientPortion {
        if (ingredient == null) throw new IllegalArgumentException("ingredient null");
        if (multiplier != 1 && multiplier != 2) throw new IllegalArgumentException("multiplier must be 1 or 2");
    }

    public double cost() {
        return ingredient.getPrice() * multiplier;
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
    private final List<IngredientPortion> ingredients = new ArrayList<>();
    private final List<UUID> banPizzaIds = new ArrayList<>();

    Side(String name) {
        super(name);
    }

    public void addIngredient(Ingredient ingredient, int mult) {
        ingredients.add(new IngredientPortion(ingredient, mult));
    }

    public void removeIngredient(UUID ingredientId) {
        ingredients.removeIf(p -> p.ingredient().getId().equals(ingredientId));
    }

    public List<IngredientPortion> getIngredients() {
        return ingredients;
    }

    public double getPrice() {
        double total = 0;
        for (IngredientPortion ip : ingredients) total += ip.cost();
        return total;
    }

    public void addBanPizza(Pizza pizza) { banPizzaIds.add(pizza.getId()); }

    public void removeBanPizza(Pizza pizza) {
        banPizzaIds.removeIf(id -> id.equals(pizza.getId()));
    }

    public List<UUID> getBanPizzaIds() { return banPizzaIds; }
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
    protected List<IngredientPortion> ingredients = new ArrayList<>();
    protected Size size;
    protected Side side;

    Slice (String name, Size size, Side side) {
        super(name);
        this.size = size;
        this.side = side;
    }

    public void addIngredient(Ingredient ingredient, int mult) {
    ingredients.add(new IngredientPortion(ingredient, mult));
}

    public void removeIngredient(UUID ingredientId) {
        ingredients.removeIf(p -> p.ingredient().getId().equals(ingredientId));
    }

    public void setSide(Side side, UUID id){
        if (!side.getBanPizzaIds().stream().anyMatch(bannedId -> bannedId.equals(id))) this.side = side;
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

    private void initSlices() {
    slices = new ArrayList<>();
    for (int i = 0; i < size.getAmount(); i++) {
        slices.add(new Slice(this.name + " кусок " + (i + 1), size, side) {});
    }
}

    Pizza (String name, Base base, Size size, Mode mode, Side side) {
        super(name, size, side);
        if (base == null) throw new IllegalArgumentException ("У пиццы должна быть основа");
        else {
            this.base = base;
            this.mode = mode;
            this.slices = new ArrayList<>(size.getAmount());
            initSlices();
        }
    }
    
    public Base getBase() {
        return base;
    }

    public double getPrice() {
        double total = 0;

        // основа
        total += base.getPrice();

        // ингредиенты по кускам
        for (Slice slice : slices) {
            for (IngredientPortion ip : slice.ingredients) {
                total += ip.cost();
            }
        }

        // бортики: суммируем разные бортики, встречающиеся на кусках
        total += slices.stream()
                .map(s -> s.side)
                .filter(Objects::nonNull)
                .distinct()
                .mapToDouble(Side::getPrice)
                .sum();

        return total;
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

    public void setSlices(List<Slice> slices) {
        this.slices = slices;
    }

    private void copyIngredientsFromSlice(Slice from, Slice to) {
        to.ingredients.clear();
        to.ingredients.addAll(from.ingredients);
    }

    public void setSize(Size size) {
        this.size = size;
        initSlices();
    }

    public void addIngredientsBasic(Ingredient ingr, int mult) {
        for (Slice slice : slices) {
            slice.addIngredient(ingr, mult);
        }
}

    public void applyHalfsFrom(Pizza pizzaA, Pizza pizzaB) {
        int mid = slices.size() / 2;

        Slice a0 = pizzaA.getSlices().get(0);
        Slice b0 = pizzaB.getSlices().get(0);

        for (int i = 0; i < mid; i++) copyIngredientsFromSlice(a0, slices.get(i));
        for (int i = mid; i < slices.size(); i++) copyIngredientsFromSlice(b0, slices.get(i));
    }

    public void addIngredientParts(Ingredient ingr, int mult, int a, int b) {
        if (a < 1 || b > slices.size() || a > b) throw new IllegalArgumentException("Неверный диапазон кусков");
        for (int i = a - 1; i <= b - 1; i++) {
            slices.get(i).addIngredient(ingr, mult);
        }
}

    public void addSideBasic(Side side) {
        for (Slice slice : slices) slice.setSide(side, id);
    }

    public void addSideHalfs(Side side, String half) {
        int mid = slices.size() / 2;

        if ("A".equals(half)) {
            for (int i = 0; i < mid; i++) slices.get(i).setSide(side, id);
        } else if ("B".equals(half)) {
            for (int i = mid; i < slices.size(); i++) slices.get(i).setSide(side, id);
        } else {
            throw new IllegalArgumentException("half должен быть A или B");
        }
    }

    public void addSideParts(Side side, int a, int b) {
        if (a < 1 || b > slices.size() || a > b) throw new IllegalArgumentException("Неверный диапазон");
        for (int i = a - 1; i <= b - 1; i++) slices.get(i).setSide(side, id);
    }





    public String describe() {
    StringBuilder sb = new StringBuilder();
    sb.append("Пицца: ").append(name)
      .append(", размер: ").append(size.getName())
      .append(", основа: ").append(base.getName())
      .append(", режим: ").append(mode.getName())
      .append(", цена: ").append(String.format("%.2f", getPrice()))
      .append("\n");

    for (int i = 0; i < slices.size(); i++) {
        Slice sl = slices.get(i);
        sb.append("  Кусок ").append(i + 1).append(": ");
        sb.append("бортик=").append(sl.side == null ? "нет" : sl.side.getName()).append("; ");
        sb.append("ингредиенты=[");
        for (int j = 0; j < sl.ingredients.size(); j++) {
            IngredientPortion ip = sl.ingredients.get(j);
            sb.append(ip.ingredient().getName()).append("x").append(ip.multiplier());
            if (j < sl.ingredients.size() - 1) sb.append(", ");
        }
        sb.append("]\n");
    }
    return sb.toString();
}

    
}


class Person extends Entity {
    public Person (String name) {
        super(name);
    }
}



class Order extends Entity {

    private List<Pizza> pizzasList = new ArrayList<>();
    private Map<UUID, List<Person>> pizzaGuests = new HashMap<>();
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

    public List<Pizza> getPizzasList() {
        return pizzasList;
    }

    public Map<UUID, List<Person>> getPizzaGuests() {
        return pizzaGuests;
    }

    public void removeGuest(Person guest) {
        guests.removeIf(g -> g.getId().equals(guest.getId()));
        for (List<Person> persons : pizzaGuests.values()) {
            persons.removeIf(person -> guest.getId().equals(person.getId()));
        }
    
    }

    public void createGuest(Person guest) {
        guests.add(guest);
    }

    public void addGuestToPizza(Pizza pizza, Person guest) {
        pizzaGuests.get(pizza.getId()).add(guest);
    }

    public void removeGuestFromPizza(Pizza pizza, Person guest) {
        pizzaGuests.get(pizza.getId()).removeIf(g -> g.getId().equals(guest.getId()));
    }

    public void addPizza(Pizza pizza) {
        pizzasList.add(pizza);
        pizzaGuests.put(pizza.getId(), new ArrayList<>());
    }

    public void removePizza(Pizza pizza) {
        pizzasList.removeIf(p -> p.getId().equals(pizza.getId()));
        pizzaGuests.remove(pizza.getId());
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

    public double getTotalPrice() {
        double total = 0;
        for (Pizza p : pizzasList) total += p.getPrice();
        return total;
    }

    public Map<UUID, Double> splitBills() {
        Map<UUID, Double> bills = new HashMap<>();
        for (Person g : guests) bills.put(g.getId(), 0.0);

        for (Pizza pizza : pizzasList) {
            List<Person> eaters = pizzaGuests.getOrDefault(pizza.getId(), List.of());
            if (eaters.isEmpty()) continue;

            double price = pizza.getPrice();
            int n = eaters.size();

            double shareRaw = price / n;

            double share = Math.floor(shareRaw * 100.0) / 100.0;
            double sum = share * n;

            double diff = Math.round((price - sum) * 100.0) / 100.0;

            for (Person e : eaters) {
                bills.put(e.getId(), bills.get(e.getId()) + share);
            }

            if (diff > 0) {
                Person first = eaters.get(0);
                bills.put(first.getId(), bills.get(first.getId()) + diff);
            }
        }

        return bills;
    }





    public String describe() {
    StringBuilder sb = new StringBuilder();
    sb.append("Заказ №").append(name).append("\n");
    sb.append("Время: ").append(time).append("\n");
    sb.append("Комментарий: ").append(comment == null ? "-" : comment).append("\n\n");

    for (Pizza p : pizzasList) {
        sb.append(p.describe()).append("\n");
    }

    sb.append("Итого: ").append(String.format("%.2f", getTotalPrice())).append("\n");

    Map<UUID, Double> bills = splitBills();
    sb.append("Делёж по гостям:\n");
    for (Person g : guests) {
        sb.append("  ").append(g.getName()).append(": ")
          .append(String.format("%.2f", bills.getOrDefault(g.getId(), 0.0)))
          .append("\n");
    }
    return sb.toString();
}
        
}












class Repository<T extends Entity> {
    private final Map<UUID, T> data = new HashMap<>();

    public void add(T obj) { data.put(obj.getId(), obj); }
    public T get(UUID id) { return data.get(id); }
    public void remove(UUID id) { data.remove(id); }

    public List<T> all() { return new ArrayList<>(data.values()); }

    public List<T> filter(java.util.function.Predicate<T> predicate) {
        return data.values().stream().filter(predicate).toList();
    }
}

class App {
    Repository<Ingredient> ingredientRepo = new Repository<>();
    Repository<Base> baseRepo = new Repository<>();
    Repository<Side> sideRepo = new Repository<>();
    Repository<Pizza> pizzaRepo = new Repository<>();
    Repository<Order> orderRepo = new Repository<>();

    public Ingredient createIngredient(String name, double price) {
        Ingredient i = new Ingredient(name, price);
        ingredientRepo.add(i);
        return i;
    }

    public void updateIngredientPrice(UUID id, double newPrice) {
        Ingredient i = ingredientRepo.get(id);
        if (i == null) throw new IllegalArgumentException("Нет ингредиента с id=" + id);
        i.setPrice(newPrice);
    }

    public void deleteIngredient(UUID id) { ingredientRepo.remove(id); }

    public Base createClassicBase() {
        Base b = new ClassicBase();
        baseRepo.add(b);
        return b;
    }

    public Base createNotClassicBase(String name, double price) {
        Base b = new NotClassicBase(name, price);
        baseRepo.add(b);
        return b;
    }

    public Side createSide(String name) {
        Side s = new Side(name);
        sideRepo.add(s);
        return s;
    }

    public Pizza createPizza(String name, Base base, Size size, Mode mode, Side side) {
        Pizza p = new Pizza(name, base, size, mode, side);
        pizzaRepo.add(p);
        return p;
    }

    public Order createOrder(String number) {
        Order o = new Order(number);
        orderRepo.add(o);
        return o;
    }



    // --------- Для фильтров ---------
    List<Pizza> withCheese = pizzaRepo.filter(p ->
        p.getSlices().stream().anyMatch(sl ->
            sl.ingredients.stream().anyMatch(ip -> ip.ingredient().getName().equals("Сыр"))
        )
    );

    double X = 500;
    List<Order> expensiveOrders = orderRepo.filter(o -> o.getTotalPrice() > X);

    ZoneId zone = ZoneId.of("Europe/Warsaw");
    LocalDate day = LocalDate.of(2026, 2, 25);

    List<Order> ordersAtDay = orderRepo.filter(o ->
        o.getTime().atZone(zone).toLocalDate().equals(day)
    );

}



public class Main {
    public static void main(String[] args) {
        App app = new App();

        // 1) ингредиенты
        Ingredient cheese = app.createIngredient("Сыр", 30);
        Ingredient tomato = app.createIngredient("Томаты", 20);
        Ingredient sausage = app.createIngredient("Сосиски", 35);

        // 2) основы
        Base classic = app.createClassicBase();
        Base thin = app.createNotClassicBase("Тонкое тесто", 115);

        // 3) бортики (из ингредиентов)
        Side cheeseSide = app.createSide("Сырный борт");
        cheeseSide.addIngredient(cheese, 2); // двойная порция сыра

        Side sausageSide = app.createSide("Сосисочный борт");
        sausageSide.addIngredient(sausage, 1);

        // 4) пиццы в каталоге
        Pizza margarita = app.createPizza("Маргарита", classic, Size.MEDIUM, Mode.BASIC, null);
        margarita.addIngredientsBasic(cheese, 1);
        margarita.addIngredientsBasic(tomato, 1);

        Pizza meat = app.createPizza("Мясная", thin, Size.MEDIUM, Mode.BASIC, null);
        meat.addIngredientsBasic(sausage, 2);

        // 5) тест: разные бортики на половины одной пиццы
        Pizza combo = app.createPizza("Комбо", classic, Size.MEDIUM, Mode.HALFS, null);
        combo.applyHalfsFrom(margarita, meat);
        combo.addSideHalfs(cheeseSide, "A");
        combo.addSideHalfs(sausageSide, "B");

        // 6) заказ + гости
        Order order = app.createOrder("0001");
        Person alice = new Person("Алиса");
        Person bob = new Person("Боб");
        order.createGuest(alice);
        order.createGuest(bob);

        order.addPizza(combo);
        order.addGuestToPizza(combo, alice);
        order.addGuestToPizza(combo, bob);

        // 7) вывод
        System.out.println(order.describe());

        // 8) фильтр-демо: пиццы с сыром
        List<Pizza> withCheese = app.pizzaRepo.filter(p ->
            p.getSlices().stream().anyMatch(sl ->
                sl.ingredients.stream().anyMatch(ip -> ip.ingredient().getName().equals("Сыр"))
            )
        );
        System.out.println("Пиццы с сыром: " + withCheese.stream().map(Pizza::getName).toList());
    }
}