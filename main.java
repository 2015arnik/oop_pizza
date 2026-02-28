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
    protected static double classicBasePrice = 100.0;

    public static double getClassicBasePrice() {
        return classicBasePrice;
    }

    public static void setClassicBasePrice(double price) {
        if (price <= 0) throw new IllegalArgumentException("Цена основы должна быть > 0");
        classicBasePrice = price;
    }

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
        super("Классическая", Base.getClassicBasePrice());
    }

    @Override
    public void setPrice(double price) {
        Base.setClassicBasePrice(price);
        this.price = Base.getClassicBasePrice();
    }
}

class NotClassicBase extends Base {
    private static double checkedPrice(double price) {
        double maxPrice = Base.getClassicBasePrice() * 1.2;
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
        double maxPrice = Base.getClassicBasePrice() * 1.2;
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
        if (pizzaA.getSlices().size() != pizzaB.getSlices().size())
            throw new IllegalArgumentException("Разные размеры (разное число кусков).");

        int mid = slices.size() / 2;

        for (int i = 0; i < mid; i++) {
            copyIngredientsFromSlice(pizzaA.getSlices().get(i), slices.get(i));
        }
        for (int i = mid; i < slices.size(); i++) {
            copyIngredientsFromSlice(pizzaB.getSlices().get(i), slices.get(i));
        }
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


    public void addIngredientToSlice(int sliceNumber1Based, Ingredient ingr, int mult) {
        if (sliceNumber1Based < 1 || sliceNumber1Based > slices.size())
            throw new IllegalArgumentException("Неверный номер куска");
        slices.get(sliceNumber1Based - 1).addIngredient(ingr, mult);
    }

    public void setSideToSlice(int sliceNumber1Based, Side side) {
        if (sliceNumber1Based < 1 || sliceNumber1Based > slices.size())
            throw new IllegalArgumentException("Неверный номер куска");
        slices.get(sliceNumber1Based - 1).setSide(side, id);
    }

    public void removeIngredientEverywhere(UUID ingredientId) {
        for (Slice sl : slices) {
            sl.removeIngredient(ingredientId);
        }
    }

    public void clearIngredientsEverywhere() {
        for (Slice sl : slices) {
            sl.ingredients.clear();
        }
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
    private List<Pizza> customPizzas = new ArrayList<>();
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
        pizzaGuests.computeIfAbsent(pizza.getId(), k -> new ArrayList<>()).add(guest);
    }

    public void removeGuestFromPizza(Pizza pizza, Person guest) {
        pizzaGuests.getOrDefault(pizza.getId(), new ArrayList<>())
                .removeIf(g -> g.getId().equals(guest.getId()));
    }

    public void addPizza(Pizza pizza) {
        pizzasList.add(pizza);
        pizzaGuests.put(pizza.getId(), new ArrayList<>());
    }

    public void removePizza(Pizza pizza) {
        pizzasList.removeIf(p -> p.getId().equals(pizza.getId()));
        pizzaGuests.remove(pizza.getId());
    }

    public void addCustomPizza(Pizza pizza) {
        customPizzas.add(pizza);
        pizzaGuests.put(pizza.getId(), new ArrayList<>());
    }

    public List<Pizza> getCustomPizzas() {
        return customPizzas;
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
        if (time == null) throw new IllegalArgumentException("time == null");
        if (time.isBefore(Instant.now())) throw new IllegalArgumentException("Нельзя сделать заказ в прошлом");
        this.time = time;
    }

    public double getTotalPrice() {
        double total = 0;
        for (Pizza p : pizzasList) total += p.getPrice();
        for (Pizza p : customPizzas) total += p.getPrice();
        return total;
    }

    public Map<UUID, Double> splitBills() {
        Map<UUID, Double> bills = new HashMap<>();
        for (Person g : guests) bills.put(g.getId(), 0.0);
        List<Pizza> allPizzas = new ArrayList<>();
        allPizzas.addAll(pizzasList);
        allPizzas.addAll(customPizzas);

        for (Pizza pizza : allPizzas) {
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

    public void postponeTo(LocalDate date, LocalTime time, ZoneId zone) {
        if (date == null || time == null || zone == null) {
            throw new IllegalArgumentException("Дата/время/зона не должны быть null");
        }
        Instant newInstant = LocalDateTime.of(date, time).atZone(zone).toInstant();
        setTime(newInstant); // у тебя уже есть проверка "не в прошлое"
    }




    public String describe() {
    StringBuilder sb = new StringBuilder();
    sb.append("Заказ №").append(name).append("\n");
    sb.append("Время: ").append(time).append("\n");
    sb.append("Комментарий: ").append(comment == null ? "-" : comment).append("\n\n");

    for (Pizza p : pizzasList) {
        sb.append(p.describe()).append("\n");
    }
    if (!customPizzas.isEmpty()) {
        sb.append("\nКастомные пиццы:\n");
        for (Pizza p : customPizzas) {
            sb.append("[Кастомная] ").append(p.describe()).append("\n");
        }
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




interface CrudRepository<T extends Entity> {
    void add(T obj);
    T get(UUID id);
    void remove(UUID id);
    List<T> all();
    List<T> filter(java.util.function.Predicate<T> predicate);
}

class Repository<T extends Entity> implements CrudRepository<T> {
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

    public Pizza createPizzaForOrderOnly(String name, Base base, Size size, Mode mode, Side side) {
        return new Pizza(name, base, size, mode, side); // НЕ добавляем в pizzaRepo
    }

    public void updateBasePrice(UUID baseId, double newPrice) {
        Base b = baseRepo.get(baseId);
        if (b == null) throw new IllegalArgumentException("Нет основы с id=" + baseId);
        b.setPrice(newPrice);
    }

    public void deleteBase(UUID id) { baseRepo.remove(id); }


    public void seedDefaults() {
        // Ингредиенты
        Ingredient cheese = createIngredient("Сыр", 30);
        Ingredient tomato = createIngredient("Томаты", 20);
        Ingredient sausage = createIngredient("Колбаски", 35);

        // Основы
        Base classic = createClassicBase();
        Base thin = createNotClassicBase("Тонкая", 115);
        Base black = createNotClassicBase("Черная", 120);

        // Бортики
        Side cheeseSide = createSide("Сырный");
        cheeseSide.addIngredient(cheese, 2);

        Side sausageSide = createSide("Колбасный");
        sausageSide.addIngredient(sausage, 1);

        // Пиццы
        Pizza cheesePizza = createPizza("Сырная", classic, Size.MEDIUM, Mode.BASIC, null);
        cheesePizza.addIngredientsBasic(cheese, 2);

        Pizza pepperoni = createPizza("Пепперони", thin, Size.MEDIUM, Mode.BASIC, null);
        pepperoni.addIngredientsBasic(cheese, 1);
        pepperoni.addIngredientsBasic(sausage, 2);

        Pizza margarita = createPizza("Маргарита", classic, Size.MEDIUM, Mode.BASIC, null);
        margarita.addIngredientsBasic(cheese, 1);
        margarita.addIngredientsBasic(tomato, 1);
    }
    

    public List<Pizza> pizzasWithIngredient(Ingredient ingredient) {
        if (ingredient == null) throw new IllegalArgumentException("ingredient null");
        return pizzaRepo.filter(p ->
            p.getSlices().stream().anyMatch(sl ->
                sl.ingredients.stream().anyMatch(ip ->
                    ip.ingredient().getId().equals(ingredient.getId())
                )
            )
        );
    }

    // Пиццы по основе (по объекту Base)
    public List<Pizza> pizzasByBase(Base base) {
        if (base == null) throw new IllegalArgumentException("base null");
        return pizzaRepo.filter(p -> p.getBase().getId().equals(base.getId()));
    }

    // Пиццы по размеру
    public List<Pizza> pizzasBySize(Size size) {
        if (size == null) throw new IllegalArgumentException("size null");
        return pizzaRepo.filter(p -> p.size == size);
    }

    // Пиццы по режиму
    public List<Pizza> pizzasByMode(Mode mode) {
        if (mode == null) throw new IllegalArgumentException("mode null");
        return pizzaRepo.filter(p -> p.getMode() == mode);
    }

    // все пиццы в заказе (каталог + кастом)
    private List<Pizza> allPizzasOf(Order o) {
        List<Pizza> all = new ArrayList<>();
        all.addAll(o.getPizzasList());
        all.addAll(o.getCustomPizzas());
        return all;
    }

    // 1) Заказы дороже X
    public List<Order> ordersMoreThan(double minTotal) {
        return orderRepo.filter(o -> o.getTotalPrice() > minTotal);
    }

    // 2) Заказы на дату (по зоне)
    public List<Order> ordersAtDay(LocalDate day, ZoneId zone) {
        return orderRepo.filter(o -> o.getTime().atZone(zone).toLocalDate().equals(day));
    }

    // 3) Заказы, где есть пицца (по объекту Pizza из каталога)
    public List<Order> ordersWithPizza(Pizza pizza) {
        if (pizza == null) throw new IllegalArgumentException("pizza null");
        return orderRepo.filter(o ->
            allPizzasOf(o).stream().anyMatch(p -> p.getId().equals(pizza.getId()))
        );
    }

    // 4) Заказы, где есть ингредиент (по объекту Ingredient)
    public List<Order> ordersWithIngredient(Ingredient ingredient) {
        if (ingredient == null) throw new IllegalArgumentException("ingredient null");
        UUID ingrId = ingredient.getId();

        return orderRepo.filter(o ->
            allPizzasOf(o).stream().anyMatch(p ->
                p.getSlices().stream().anyMatch(sl ->
                    sl.ingredients.stream().anyMatch(ip ->
                        ip.ingredient().getId().equals(ingrId)
                    )
                )
            )
        );
    }

    // 5) Заказы с минимум N гостями
    public List<Order> ordersWithMinGuests(int n) {
        return orderRepo.filter(o -> o.getGuests().size() >= n);
    }

}


class ConsoleUI {
    private final App app;
    private final Scanner sc = new Scanner(System.in);

    private Order currentOrder = null;

    private List<Ingredient> lastIngredients = List.of();
    private List<Base> lastBases = List.of();
    private List<Side> lastSides = List.of();
    private List<Pizza> lastPizzas = List.of();
    private List<Order> lastOrders = List.of();

    ConsoleUI(App app) {
        this.app = app;
    }

    public void run() {
        while (true) {
            System.out.println("\n=== Главное меню ===");
            System.out.println("1) Ингредиенты");
            System.out.println("2) Основы");
            System.out.println("3) Бортики");
            System.out.println("4) Пиццы");
            System.out.println("5) Заказы");
            System.out.println("6) Фильтры");
            System.out.println("0) Выход");

            int c = readInt("Выбор: ");

            try {
                switch (c) {
                    case 1 -> ingredientsMenu();
                    case 2 -> basesMenu();
                    case 3 -> sidesMenu();
                    case 4 -> pizzasMenu();
                    case 5 -> ordersMenu();
                    case 6 -> filtersMenu();
                    case 0 -> { System.out.println("Пока!"); return; }
                    default -> System.out.println("Нет такого пункта.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }


    //------ Фильтры --------

    private void filtersMenu() {
        while (true) {
            System.out.println("\n=== Фильтры ===");
            System.out.println("1) Фильтры пицц");
            System.out.println("2) Фильтры заказов");
            System.out.println("0) Назад");

            int c = readInt("Выбор: ");
            try {
                switch (c) {
                    case 1 -> filtersPizzasMenu();
                    case 2 -> filtersOrdersMenu();
                    case 0 -> { return; }
                    default -> System.out.println("Нет такого пункта.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void filtersOrdersMenu() {
        while (true) {
            System.out.println("\n=== Фильтры заказов ===");
            System.out.println("1) Заказы дороже суммы");
            System.out.println("2) Заказы на дату");
            System.out.println("3) Заказы с пиццей из каталога (выбор по номеру)");
            System.out.println("4) Заказы с ингредиентом (выбор по номеру)");
            System.out.println("5) Заказы с минимум N гостями");
            System.out.println("0) Назад");

            int c = readInt("Выбор: ");

            try {
                switch (c) {
                    case 1 -> {
                        double x = readDouble("Сумма от: ");
                        printOrderList(app.ordersMoreThan(x));
                    }
                    case 2 -> {
                        LocalDate day = readDate("Дата");
                        ZoneId zone = ZoneId.of("Europe/Warsaw");
                        printOrderList(app.ordersAtDay(day, zone));
                    }
                    case 3 -> {
                        Pizza p = choosePizzaFromCatalog(); 
                        printOrderList(app.ordersWithPizza(p));
                    }
                    case 4 -> {
                        Ingredient ingr = chooseIngredientFromRepo();
                        printOrderList(app.ordersWithIngredient(ingr));
                    }
                    case 5 -> {
                        int n = readInt("Минимум гостей: ");
                        printOrderList(app.ordersWithMinGuests(n));
                    }
                    case 0 -> { return; }
                    default -> System.out.println("Нет такого пункта.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }
    private void filtersPizzasMenu() {
        while (true) {
            System.out.println("\n=== Фильтры пицц ===");
            System.out.println("1) Пиццы с ингредиентом (выбор из списка)");
            System.out.println("2) Пиццы по размеру (выбор из списка)");
            System.out.println("3) Пиццы по основе (выбор из списка)");
            System.out.println("4) Пиццы по режиму (выбор из списка)");
            System.out.println("0) Назад");

            int c = readInt("Выбор: ");
            try {
                switch (c) {
                    case 1 -> {
                        Ingredient ingr = chooseIngredientFromRepo();
                        List<Pizza> res = app.pizzasWithIngredient(ingr);
                        printPizzaList(res);
                    }
                    case 2 -> {
                        Size size = chooseSize();
                        List<Pizza> res = app.pizzasBySize(size);
                        printPizzaList(res);
                    }
                    case 3 -> {
                        Base base = chooseBaseFromRepo();
                        List<Pizza> res = app.pizzasByBase(base);
                        printPizzaList(res);
                    }
                    case 4 -> {
                        Mode mode = chooseMode();
                        List<Pizza> res = app.pizzasByMode(mode);
                        printPizzaList(res);
                    }
                    case 0 -> { return; }
                    default -> System.out.println("Нет такого пункта.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void printPizzaList(List<Pizza> list) {
        if (list.isEmpty()) {
            System.out.println("Ничего не найдено.");
            return;
        }
        System.out.println("--- Найденные пиццы ---");
        for (int i = 0; i < list.size(); i++) {
            Pizza p = list.get(i);
            System.out.printf("%d) %s | %.2f | %s | %s%n",
                    i + 1,
                    p.getName(),
                    p.getPrice(),
                    p.getBase().getName(),
                    p.getMode().getName());
        }
    }

    private void printOrderList(List<Order> list) {
        if (list.isEmpty()) {
            System.out.println("Ничего не найдено.");
            return;
        }
        System.out.println("--- Найденные заказы ---");
        for (int i = 0; i < list.size(); i++) {
            Order o = list.get(i);
            System.out.printf("%d) №%s | %.2f | %s%n",
                    i + 1,
                    o.getName(),
                    o.getTotalPrice(),
                    o.getTime());
        }
    }
    // ---------- Ингредиенты ----------
    private void ingredientsMenu() {
        while (true) {
            System.out.println("\n=== Ингредиенты ===");
            System.out.println("1) Создать");
            System.out.println("2) Показать список (с номерами)");
            System.out.println("3) Изменить цену (по номеру из списка)");
            System.out.println("4) Удалить (по номеру из списка)");
            System.out.println("0) Назад");

            int c = readInt("Выбор: ");
            try {
                switch (c) {
                    case 1 -> {
                        String name = readString("Название: ");
                        double price = readDouble("Цена: ");
                        Ingredient i = app.createIngredient(name, price);
                        System.out.println("Создан: " + i.getName() + " id=" + i.getId());
                    }
                    case 2 -> printIngredients();
                    case 3 -> {
                        ensureIngredientsListed();
                        int n = readInt("Номер ингредиента: ");
                        Ingredient i = pickByNumber(lastIngredients, n, "ингредиент");
                        double price = readDouble("Новая цена: ");
                        app.updateIngredientPrice(i.getId(), price);
                        System.out.println("Ок.");
                    }
                    case 4 -> {
                        ensureIngredientsListed();
                        int n = readInt("Номер ингредиента: ");
                        Ingredient i = pickByNumber(lastIngredients, n, "ингредиент");
                        app.deleteIngredient(i.getId());
                        System.out.println("Удалено.");
                    }
                    case 0 -> { return; }
                    default -> System.out.println("Нет такого пункта.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void printIngredients() {
        lastIngredients = app.ingredientRepo.all().stream()
                .sorted(Comparator.comparing(Ingredient::getName))
                .toList();
        if (lastIngredients.isEmpty()) {
            System.out.println("(пусто)");
            return;
        }
        System.out.println("--- Ингредиенты ---");
        for (int i = 0; i < lastIngredients.size(); i++) {
            Ingredient ingr = lastIngredients.get(i);
            System.out.printf("%d) %s | %.2f | %s%n", i + 1, ingr.getName(), ingr.getPrice(), ingr.getId());
        }
    }

    private void ensureIngredientsListed() {
        lastIngredients = app.ingredientRepo.all();

        if (lastIngredients.isEmpty()) {
            throw new IllegalArgumentException("Ингредиентов нет.");
        }

        System.out.println("--- Ингредиенты ---");
        for (int i = 0; i < lastIngredients.size(); i++) {
            Ingredient ingr = lastIngredients.get(i);
            System.out.printf("%d) %s | %.2f%n",
                    i + 1,
                    ingr.getName(),
                    ingr.getPrice());
        }
    }

    // ---------- Основы ----------
    private void basesMenu() {
        while (true) {
            System.out.println("\n=== Основы ===");
            System.out.println("1) Создать классическую");
            System.out.println("2) Создать неклассическую");
            System.out.println("3) Показать список (с номерами)");
            System.out.println("4) Изменить цену основы (по номеру)");
            System.out.println("0) Назад");

            int c = readInt("Выбор: ");
            try {
                switch (c) {
                    case 1 -> {
                        Base b = app.createClassicBase();
                        System.out.println("Создана: " + b.getName() + " цена=" + b.getPrice() + " id=" + b.getId());
                    }
                    case 2 -> {
                        String name = readString("Название: ");
                        double price = readDouble("Цена: ");
                        Base b = app.createNotClassicBase(name, price);
                        System.out.println("Создана: " + b.getName() + " цена=" + b.getPrice() + " id=" + b.getId());
                    }
                    case 3 -> printBases();
                    case 4 -> {
                        printBases();
                        if (lastBases.isEmpty()) throw new IllegalArgumentException("Основ нет.");
                        int n = readInt("Номер основы: ");
                        Base b = pickByNumber(lastBases, n, "основа");
                        double newPrice = readDouble("Новая цена: ");
                        app.updateBasePrice(b.getId(), newPrice);
                        System.out.println("Ок. Новая цена: " + String.format("%.2f", b.getPrice()));
                    }
                    case 0 -> { return; }
                    default -> System.out.println("Нет такого пункта.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void printBases() {
        lastBases = app.baseRepo.all();
        if (lastBases.isEmpty()) {
            System.out.println("(пусто)");
            return;
        }
        System.out.println("--- Основы ---");
        for (int i = 0; i < lastBases.size(); i++) {
            Base b = lastBases.get(i);
            System.out.printf("%d) %s | %.2f | %s%n", i + 1, b.getName(), b.getPrice(), b.getId());
        }
    }

    // ---------- Бортики ----------
    private void sidesMenu() {
        while (true) {
            System.out.println("\n=== Бортики ===");
            System.out.println("1) Создать борт");
            System.out.println("2) Показать список (с номерами)");
            System.out.println("3) Добавить ингредиент в борт (по номерам)");
            System.out.println("0) Назад");

            int c = readInt("Выбор: ");
            try {
                switch (c) {
                    case 1 -> {
                        String name = readString("Название борта: ");
                        Side s = app.createSide(name);
                        System.out.println("Создан: " + s.getName() + " id=" + s.getId());
                    }
                    case 2 -> printSides();
                    case 3 -> {
                        ensureSidesListed();
                        ensureIngredientsListed();

                        int sideN = readInt("Номер борта: ");
                        Side side = pickByNumber(lastSides, sideN, "борт");

                        int ingrN = readInt("Номер ингредиента: ");
                        Ingredient ingr = pickByNumber(lastIngredients, ingrN, "ингредиент");

                        int mult = readInt("Порция (1 или 2): ");
                        side.addIngredient(ingr, mult);

                        System.out.println("Ок. Цена борта: " + String.format("%.2f", side.getPrice()));
                    }
                    case 0 -> { return; }
                    default -> System.out.println("Нет такого пункта.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void printSides() {
        lastSides = app.sideRepo.all();
        if (lastSides.isEmpty()) {
            System.out.println("(пусто)");
            return;
        }
        System.out.println("--- Бортики ---");
        for (int i = 0; i < lastSides.size(); i++) {
            Side s = lastSides.get(i);
            System.out.printf("%d) %s | цена=%.2f | %s%n", i + 1, s.getName(), s.getPrice(), s.getId());
        }
    }

    private void ensureSidesListed() {
        if (lastSides.isEmpty()) {
            System.out.println("Сначала покажу список бортов.");
            printSides();
        }
        if (lastSides.isEmpty()) throw new IllegalArgumentException("Бортов нет.");
    }

    // ---------- Пиццы ----------
    private void pizzasMenu() {
        while (true) {
            System.out.println("\n=== Пиццы ===");
            System.out.println("1) Создать пиццу (в каталог)");
            System.out.println("2) Показать список (с номерами)");
            System.out.println("3) Добавить ингредиент на всю пиццу (по номерам)");
            System.out.println("4) Добавить ингредиент на 1 кусок (по номерам)");
            System.out.println("5) Показать описание пиццы (по номеру)");
            System.out.println("6) Добавить бортик на всю пиццу");
            System.out.println("7) Добавить бортик на половину (A/B)");
            System.out.println("8) Добавить бортик на диапазон кусков");
            System.out.println("9) Добавить бортик на 1 кусок");
            System.out.println("10) Удалить ингредиент из пиццы (везде)");
            System.out.println("11) Очистить начинку пиццы");
            System.out.println("12) Изменить размер пиццы");
            System.out.println("13) Создать пиццу из половинок (2 пиццы из каталога)");
            System.out.println("14) Создать пиццу из кусуов");
            System.out.println("0) Назад");

            int c = readInt("Выбор: ");
            try {
                switch (c) {
                    case 1 -> {
                        String name = readString("Название: ");
                        Base base = chooseBase();
                        Size size = readEnum("Размер (SMALL/MEDIUM/LARGE): ", Size.class);

                        Pizza p = app.createPizza(name, base, size, Mode.BASIC, null);
                        System.out.println("Создана пицца (BASIC): " + p.getName() + " id=" + p.getId());
                    }
                    case 2 -> printPizzas();
                    case 3 -> {
                        Pizza p = choosePizzaFromCatalog();
                        Ingredient ingr = chooseIngredient();
                        int mult = readInt("Порция (1 или 2): ");

                        p.addIngredientsBasic(ingr, mult);
                        System.out.println("Ок.");
                    }
                    case 4 -> {
                        Pizza p = choosePizzaFromCatalog();
                        Ingredient ingr = chooseIngredient();

                        int slice = readInt("Номер куска (1.." + p.getSlices().size() + "): ");
                        int mult = readInt("Порция (1 или 2): ");

                        p.addIngredientToSlice(slice, ingr, mult);
                        System.out.println("Ок. Добавлено на кусок " + slice + ".");
                    }
                    case 5 -> {
                        ensurePizzasListed();
                        int pN = readInt("Номер пиццы: ");
                        Pizza p = pickByNumber(lastPizzas, pN, "пицца");
                        System.out.println(p.describe());
                    }
                    case 6 -> {
                        Pizza p = choosePizzaFromCatalog();
                        Side side = chooseSide();
                        p.addSideBasic(side);
                        System.out.println("Ок.");
                    }
                    case 7 -> {
                        Pizza p = choosePizzaFromCatalog();
                        Side side = chooseSide();
                        String half = readString("Половина (A или B): ").toUpperCase();
                        p.addSideHalfs(side, half);
                        System.out.println("Ок.");
                    }
                    case 8 -> {
                        Pizza p = choosePizzaFromCatalog();
                        Side side = chooseSide();
                        int a = readInt("С какого куска (a): ");
                        int b = readInt("По какой кусок (b): ");
                        p.addSideParts(side, a, b);
                        System.out.println("Ок.");
                    }
                    case 9 -> {
                        Pizza p = choosePizzaFromCatalog();
                        Side side = chooseSide();
                        int slice = readInt("Номер куска: ");
                        p.setSideToSlice(slice, side);
                        System.out.println("Ок.");
                    }
                    case 10 -> {
                        Pizza p = choosePizzaFromCatalog();
                        Ingredient ingr = chooseIngredient();
                        p.removeIngredientEverywhere(ingr.getId());
                        System.out.println("Ок. Ингредиент удалён из всех кусков.");
                    }

                    case 11 -> {
                        Pizza p = choosePizzaFromCatalog();
                        p.clearIngredientsEverywhere();
                        System.out.println("Ок. Начинка очищена.");
                    }

                    case 12 -> {
                        Pizza p = choosePizzaFromCatalog();
                        Size newSize = readEnum("Новый размер (SMALL/MEDIUM/LARGE): ", Size.class);

                        System.out.println("Внимание: при смене размера начинка и бортики сбросятся.");
                        p.setSize(newSize);

                        System.out.println("Ок. Новый размер: " + newSize.getName());
                    }
                    case 13 -> {
                        String name = readString("Название новой пиццы: ");

                        System.out.println("Выбери пиццу для половины A:");
                        Pizza pizzaA = choosePizzaFromCatalog();

                        System.out.println("Выбери пиццу для половины B:");
                        Pizza pizzaB = choosePizzaFromCatalog();

                        if (pizzaA.getSlices().size() != pizzaB.getSlices().size()) {
                            throw new IllegalArgumentException("Нельзя склеить пиццы разных размеров (разное число кусков).");
                        }

                        Base base = pizzaA.getBase();
                        Size size = pizzaA.size;
                        Side side = null;

                        Pizza combo = app.createPizza(name, base, size, Mode.HALFS, side);
                        combo.applyHalfsFrom(pizzaA, pizzaB);

                        System.out.println("Готово! Создана пицца из половинок:");
                        System.out.println(combo.describe());
                    }
                    case 14 -> {
                        String name = readString("Название: ");
                        Base base = chooseBase();
                        Size size = readEnum("Размер (SMALL/MEDIUM/LARGE): ", Size.class);

                        Pizza p = app.createPizza(name, base, size, Mode.PARTS, null);

                        System.out.println("Добавляй ингредиенты на диапазоны. 0 - закончить.");
                        while (true) {
                            Ingredient ingr = chooseIngredientOrZero();
                            if (ingr == null) break;
                            int mult = readInt("Порция (1/2): ");
                            int a = readInt("a: ");
                            int b = readInt("b: ");
                            p.addIngredientParts(ingr, mult, a, b);
                            System.out.println("Добавлено.");
                        }

                        System.out.println("Готово:\n" + p.describe());
                    }
                    case 0 -> { return; }
                    default -> System.out.println("Нет такого пункта.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private Ingredient chooseIngredientFromRepo() {
    List<Ingredient> list = app.ingredientRepo.all();
    if (list.isEmpty()) throw new IllegalArgumentException("Ингредиентов нет.");

    System.out.println("--- Ингредиенты ---");
    for (int i = 0; i < list.size(); i++) {
        Ingredient x = list.get(i);
        System.out.printf("%d) %s | %.2f%n", i + 1, x.getName(), x.getPrice());
    }
    int n = readInt("Номер ингредиента: ");
    return pickByNumber(list, n, "ингредиент");
}

private Base chooseBaseFromRepo() {
    List<Base> list = app.baseRepo.all();
    if (list.isEmpty()) throw new IllegalArgumentException("Основ нет.");

    System.out.println("--- Основы ---");
    for (int i = 0; i < list.size(); i++) {
        Base b = list.get(i);
        System.out.printf("%d) %s | %.2f%n", i + 1, b.getName(), b.getPrice());
    }
    int n = readInt("Номер основы: ");
    return pickByNumber(list, n, "основа");
}

private Size chooseSize() {
    System.out.println("--- Размеры ---");
    Size[] arr = Size.values();
    for (int i = 0; i < arr.length; i++) {
        System.out.printf("%d) %s%n", i + 1, arr[i].getName());
    }
    int n = readInt("Номер размера: ");
    if (n < 1 || n > arr.length) throw new IllegalArgumentException("Неверный номер размера");
    return arr[n - 1];
}

    private Mode chooseMode() {
        System.out.println("--- Режимы ---");
        Mode[] arr = Mode.values();
        for (int i = 0; i < arr.length; i++) {
            System.out.printf("%d) %s%n", i + 1, arr[i].getName());
        }
        int n = readInt("Номер режима: ");
        if (n < 1 || n > arr.length) throw new IllegalArgumentException("Неверный номер режима");
        return arr[n - 1];
    }

    private void printPizzas() {
        lastPizzas = app.pizzaRepo.all().stream()
                .sorted(Comparator.comparingDouble(Pizza::getPrice))
                .toList();
        if (lastPizzas.isEmpty()) {
            System.out.println("(пусто)");
            return;
        }
        System.out.println("--- Пиццы ---");
        for (int i = 0; i < lastPizzas.size(); i++) {
            Pizza p = lastPizzas.get(i);
            System.out.printf("%d) %s | %.2f | %s%n", i + 1, p.getName(), p.getPrice(), p.getId());
        }
    }

    private void ensurePizzasListed() {
        if (lastPizzas.isEmpty()) {
            System.out.println("Сначала покажу список пицц.");
            printPizzas();
        }
        if (lastPizzas.isEmpty()) throw new IllegalArgumentException("Пицц нет.");
    }

    // ---------- Заказы ----------
    private void ordersMenu() {
        while (true) {
            System.out.println("\n=== Заказы ===");
            System.out.println("1) Создать заказ (сделать текущим)");
            System.out.println("2) Показать список (с номерами)");
            System.out.println("3) Выбрать текущий заказ (по номеру из списка)");
            System.out.println("4) Добавить пиццу из каталога в текущий заказ (по номерам)");
            System.out.println("5) Добавить гостя в текущий заказ");
            System.out.println("6) Назначить гостя на пиццу (по номерам)");
            System.out.println("7) Показать текущий заказ");
            System.out.println("8) Создать кастомную пиццу в текущем заказе");
            System.out.println("9) Кастомная пицца из половинок (в заказ)");
            System.out.println("10) Кастомная пицца по кускам (в заказ)");
            System.out.println("11) Отложить текущий заказ на дату/время");
            System.out.println("0) Назад");

            int c = readInt("Выбор: ");
            try {
                switch (c) {
                    case 1 -> {
                        String number = readString("Номер заказа: ");
                        Order o = app.createOrder(number);
                        currentOrder = o;
                        System.out.println("Создан заказ: id=" + o.getId() + " (текущий)");
                    }
                    case 2 -> printOrders();
                    case 3 -> {
                        currentOrder = chooseOrder();
                        System.out.println("Текущий заказ: №" + currentOrder.getName());
                    }
                    case 4 -> {
                        Order o = ensureCurrentOrder();
                        ensurePizzasListed();

                        int pN = readInt("Номер пиццы из каталога: ");
                        Pizza p = pickByNumber(lastPizzas, pN, "пицца");

                        o.addPizza(p);
                        System.out.println("Пицца добавлена в заказ.");
                    }
                    case 5 -> {
                        Order o = ensureCurrentOrder();
                        String guestName = readString("Имя гостя: ");
                        o.createGuest(new Person(guestName));
                        System.out.println("Гость добавлен.");
                    }
                    case 6 -> {
                        Order o = ensureCurrentOrder();

                        List<Pizza> allPizzas = new ArrayList<>();
                        allPizzas.addAll(o.getPizzasList());
                        allPizzas.addAll(o.getCustomPizzas());

                        if (allPizzas.isEmpty()) throw new IllegalArgumentException("В заказе нет пицц.");
                        if (o.getGuests().isEmpty()) throw new IllegalArgumentException("В заказе нет гостей.");

                        
                        System.out.println("--- Пиццы в заказе ---");
                        for (int i = 0; i < allPizzas.size(); i++) {
                            Pizza p = allPizzas.get(i);
                            String tag = o.getCustomPizzas().stream().anyMatch(cp -> cp.getId().equals(p.getId()))
                                    ? "[Кастомная] " : "";
                            System.out.printf("%d) %s%s | %.2f%n", i + 1, tag, p.getName(), p.getPrice());
                        }

                        
                        System.out.println("--- Гости ---");
                        List<Person> guests = o.getGuests();
                        for (int i = 0; i < guests.size(); i++) {
                            Person g = guests.get(i);
                            System.out.printf("%d) %s%n", i + 1, g.getName());
                        }

                        int pN = readInt("Номер пиццы (из заказа): ");
                        Pizza p = pickByNumber(allPizzas, pN, "пицца");

                        int gN = readInt("Номер гостя: ");
                        Person g = pickByNumber(guests, gN, "гость");

                        o.addGuestToPizza(p, g);
                        System.out.println("Назначено.");
                    }
                    case 7 -> {
                        Order o = ensureCurrentOrder();
                        System.out.println(o.describe());
                    }
                    case 8 -> {
                        Order o = ensureCurrentOrder();

                        String name = readString("Название кастомной пиццы: ");

                        Base base = chooseBase();

                        Size size = readEnum("Размер (SMALL/MEDIUM/LARGE): ", Size.class);
                        Pizza p = app.createPizzaForOrderOnly(name, base, size, Mode.BASIC, null);

                        System.out.println("Добавим ингредиенты. 0 — закончить.");
                        while (true) {
                            Ingredient ingr = chooseIngredientOrZero();
                            if (ingr == null) break;
                            int mult = readInt("Порция (1 или 2): ");
                            p.addIngredientsBasic(ingr, mult);
                            System.out.println("Добавлено.");
                        }
                        System.out.println("Добавить бортик? 1-да / 0-нет");
                        int ans = readInt("Выбор: ");
                        if (ans == 1) {
                            Side side = chooseSide(); 
                            System.out.println("Как применить бортик?");
                            System.out.println("1) На всю пиццу");
                            System.out.println("2) На половину");
                            System.out.println("3) На диапазон кусков");
                            System.out.println("4) На один кусок");
                            int how = readInt("Выбор: ");

                            switch (how) {
                                case 1 -> p.addSideBasic(side);
                                case 2 -> {
                                    String half = readString("Половина (A/B): ").toUpperCase();
                                    p.addSideHalfs(side, half);
                                }
                                case 3 -> {
                                    int a = readInt("a: ");
                                    int b = readInt("b: ");
                                    p.addSideParts(side, a, b);
                                }
                                case 4 -> {
                                    int k = readInt("Номер куска: ");
                                    p.setSideToSlice(k, side);
                                }
                                default -> System.out.println("Пропущено.");
                            }
                        }

                        o.addCustomPizza(p);

                        System.out.println("Кастомная пицца добавлена в заказ. Цена: " + String.format("%.2f", p.getPrice()));
                    }
                    case 9 -> {
                        Order o = ensureCurrentOrder();
                        String name = readString("Название кастомной HALF-пиццы: ");

                        System.out.println("Половина A (выбери пиццу из каталога):");
                        Pizza a = choosePizzaFromCatalog();

                        System.out.println("Половина B (выбери пиццу из каталога):");
                        Pizza b = choosePizzaFromCatalog();

                        if (a.getSlices().size() != b.getSlices().size())
                            throw new IllegalArgumentException("Пиццы разных размеров — нельзя склеить.");

                        Pizza p = app.createPizzaForOrderOnly(name, a.getBase(), a.size, Mode.HALFS, null);
                        p.applyHalfsFrom(a, b);

                        o.addCustomPizza(p);
                        System.out.println("Готово. Добавлено в заказ:\n" + p.describe());
                    }
                    case 10 -> {
                        Order o = ensureCurrentOrder();
                        String name = readString("Название кастомной PARTS-пиццы: ");
                        Base base = chooseBase();
                        Size size = readEnum("Размер (SMALL/MEDIUM/LARGE): ", Size.class);

                        Pizza p = app.createPizzaForOrderOnly(name, base, size, Mode.PARTS, null);

                        System.out.println("Добавляй ингредиенты на диапазоны. 0 - закончить.");
                        while (true) {
                            Ingredient ingr = chooseIngredientOrZero();
                            if (ingr == null) break;
                            int mult = readInt("Порция (1/2): ");
                            int a = readInt("a: ");
                            int b = readInt("b: ");
                            p.addIngredientParts(ingr, mult, a, b);
                            System.out.println("Добавлено.");
                        }

                        o.addCustomPizza(p);
                        System.out.println("Готово. Добавлено в заказ:\n" + p.describe());
                    }
                    case 11 -> {
                        Order o = ensureCurrentOrder();

                        ZoneId zone = ZoneId.systemDefault(); 
                        LocalDate date = readDate("Дата");
                        LocalTime time = readTime("Время");

                        o.postponeTo(date, time, zone);

                        System.out.println("Готово. Новое время заказа: " + o.getTime().atZone(zone));
                    }
                    case 0 -> { return; }
                    default -> System.out.println("Нет такого пункта.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void printOrders() {
        lastOrders = app.orderRepo.all().stream()
                .sorted(Comparator.comparing(Order::getTime))
                .toList();
        if (lastOrders.isEmpty()) {
            System.out.println("(пусто)");
            return;
        }
        System.out.println("--- Заказы ---");
        for (int i = 0; i < lastOrders.size(); i++) {
            Order o = lastOrders.get(i);
            System.out.printf("%d) №%s | %s | %s%n", i + 1, o.getName(), o.getTime(), o.getId());
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            String s = sc.nextLine().trim();
            try { return LocalDate.parse(s); }
            catch (Exception e) { System.out.println("Неверный формат даты. Пример: 2026-03-01"); }
        }
    }

    private LocalTime readTime(String prompt) {
        while (true) {
            System.out.print(prompt + " (HH:MM): ");
            String s = sc.nextLine().trim();
            try { return LocalTime.parse(s); }
            catch (Exception e) { System.out.println("Неверный формат времени. Пример: 18:30"); }
        }
    }

    private Order ensureCurrentOrder() {
        if (currentOrder == null) throw new IllegalArgumentException("Нет текущего заказа. Создайте или выберите заказ.");
        return currentOrder;
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) { System.out.println("Введите число."); }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim().replace(',', '.');
            try { return Double.parseDouble(s); }
            catch (NumberFormatException e) { System.out.println("Введите число."); }
        }
    }

    private String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("Пусто. Повторите.");
        }
    }

    private <E extends Enum<E>> E readEnum(String prompt, Class<E> enumClass) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim().toUpperCase();
            try { return Enum.valueOf(enumClass, s); }
            catch (IllegalArgumentException e) {
                System.out.println("Неверно. Допустимо: " + Arrays.toString(enumClass.getEnumConstants()));
            }
        }
    }

    private static <T> T pickByNumber(List<T> list, int number1Based, String what) {
        if (number1Based < 1 || number1Based > list.size())
            throw new IllegalArgumentException("Неверный номер (" + what + ").");
        return list.get(number1Based - 1);
    }


    private Ingredient chooseIngredient() {
        List<Ingredient> list = app.ingredientRepo.all();
        if (list.isEmpty()) throw new IllegalArgumentException("Ингредиентов нет. Сначала создайте ингредиент.");

        System.out.println("--- Ингредиенты ---");
        for (int i = 0; i < list.size(); i++) {
            Ingredient x = list.get(i);
            System.out.printf("%d) %s | %.2f%n", i + 1, x.getName(), x.getPrice());
        }

        int n = readInt("Номер ингредиента: ");
        return pickByNumber(list, n, "ингредиент");
    }

    private Pizza choosePizzaFromCatalog() {
        List<Pizza> list = app.pizzaRepo.all();
        if (list.isEmpty()) throw new IllegalArgumentException("Пицц нет. Сначала создайте пиццу.");

        System.out.println("--- Пиццы ---");
        for (int i = 0; i < list.size(); i++) {
            Pizza p = list.get(i);
            System.out.printf("%d) %s | %.2f%n", i + 1, p.getName(), p.getPrice());
        }

        int n = readInt("Номер пиццы: ");
        return pickByNumber(list, n, "пицца");
    }

    private Order chooseOrder() {
        List<Order> list = app.orderRepo.all();
        if (list.isEmpty()) throw new IllegalArgumentException("Заказов нет. Сначала создайте заказ.");

        System.out.println("--- Заказы ---");
        for (int i = 0; i < list.size(); i++) {
            Order o = list.get(i);
            System.out.printf("%d) №%s | %s | %.2f%n",
                    i + 1,
                    o.getName(),
                    o.getTime(),
                    o.getTotalPrice());
        }

        int n = readInt("Номер заказа: ");
        return pickByNumber(list, n, "заказ");
    }

    private Base chooseBase() {
        List<Base> list = app.baseRepo.all();
        if (list.isEmpty()) throw new IllegalArgumentException("Основ нет. Сначала создайте основу.");

        System.out.println("--- Основы ---");
        for (int i = 0; i < list.size(); i++) {
            Base b = list.get(i);
            System.out.printf("%d) %s | %.2f%n", i + 1, b.getName(), b.getPrice());
        }
        int n = readInt("Номер основы: ");
        return pickByNumber(list, n, "основа");
    }

    private Side chooseSide() {
        List<Side> list = app.sideRepo.all();
        if (list.isEmpty()) throw new IllegalArgumentException("Бортов нет. Сначала создайте борт.");

        System.out.println("--- Бортики ---");
        for (int i = 0; i < list.size(); i++) {
            Side s = list.get(i);
            System.out.printf("%d) %s | цена=%.2f%n", i + 1, s.getName(), s.getPrice());
        }
        int n = readInt("Номер бортика: ");
        return pickByNumber(list, n, "борт");
    }

    private Ingredient chooseIngredientOrZero() {
        List<Ingredient> list = app.ingredientRepo.all();
        if (list.isEmpty()) throw new IllegalArgumentException("Ингредиентов нет. Сначала создайте ингредиент.");

        System.out.println("--- Ингредиенты (0 - закончить) ---");
        for (int i = 0; i < list.size(); i++) {
            Ingredient x = list.get(i);
            System.out.printf("%d) %s | %.2f%n", i + 1, x.getName(), x.getPrice());
        }

        int n = readInt("Номер ингредиента: ");
        if (n == 0) return null;

        return pickByNumber(list, n, "ингредиент");
    }
}


public class Main {
    public static void main(String[] args) {
        App app = new App();
        app.seedDefaults();
        new ConsoleUI(app).run();
    }
}