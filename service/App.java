package service;

import model.*;
import repo.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class App {
    private final Repository<Ingredient> ingredientRepo = new Repository<>();
    private final Repository<Base> baseRepo = new Repository<>();
    private final Repository<Side> sideRepo = new Repository<>();
    private final Repository<Pizza> pizzaRepo = new Repository<>();
    private final Repository<Order> orderRepo = new Repository<>();

    public List<Ingredient> getAllIngredients() { return ingredientRepo.all(); }
    public List<Base> getAllBases() { return baseRepo.all(); }
    public List<Side> getAllSides() { return sideRepo.all(); }
    public List<Pizza> getAllPizzas() { return pizzaRepo.all(); }
    public List<Order> getAllOrders() { return orderRepo.all(); }

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

    public void deleteBase(UUID id) { baseRepo.remove(id); }

    public void updateBasePrice(UUID baseId, double newPrice) {
        Base b = baseRepo.get(baseId);
        if (b == null) throw new IllegalArgumentException("Нет основы с id=" + baseId);
        b.setPrice(newPrice);
    }

    public Side createSide(String name) {
        Side s = new Side(name);
        sideRepo.add(s);
        return s;
    }

    public void deleteSide(UUID id) { sideRepo.remove(id); }

    public Pizza createPizza(String name, Base base, Size size, Mode mode, Side side) {
        Pizza p = new Pizza(name, base, size, mode, side);
        pizzaRepo.add(p);
        return p;
    }

    public Pizza createPizzaForOrderOnly(String name, Base base, Size size, Mode mode, Side side) {
        return new Pizza(name, base, size, mode, side);
    }

    public Order createOrder(String number) {
        Order o = new Order(number);
        orderRepo.add(o);
        return o;
    }

    public void seedDefaults() {
        Ingredient cheese = createIngredient("Сыр", 30);
        Ingredient tomato = createIngredient("Томаты", 20);
        Ingredient sausage = createIngredient("Колбаски", 35);

        Base classic = createClassicBase();
        Base thin = createNotClassicBase("Тонкая", 115);
        createNotClassicBase("Черная", 120);

        Side cheeseSide = createSide("Сырный");
        cheeseSide.addIngredient(cheese, 2);

        Side sausageSide = createSide("Колбасный");
        sausageSide.addIngredient(sausage, 1);

        Pizza cheesePizza = createPizza("Сырная", classic, Size.MEDIUM, Mode.BASIC, null);
        cheesePizza.addIngredientsBasic(cheese, 2);

        Pizza pepperoni = createPizza("Пепперони", thin, Size.MEDIUM, Mode.BASIC, null);
        pepperoni.addIngredientsBasic(cheese, 1);
        pepperoni.addIngredientsBasic(sausage, 2);

        Pizza margarita = createPizza("Маргарита", classic, Size.MEDIUM, Mode.BASIC, null);
        margarita.addIngredientsBasic(cheese, 1);
        margarita.addIngredientsBasic(tomato, 1);
    }

    public Pizza copyPizzaForOrder(Pizza src) {
        Pizza copy = createPizzaForOrderOnly(
                src.getName(),
                src.getBase(),
                src.getSize(),
                src.getMode(),
                null
        );

        List<Slice> srcSlices = src.getSlices();
        List<Slice> dstSlices = copy.getSlices();

        for (int i = 0; i < srcSlices.size(); i++) {
            Slice s = srcSlices.get(i);
            for (IngredientPortion ip : s.getIngredients()) {
                copy.addIngredientToSlice(i + 1, ip.ingredient(), ip.multiplier());
            }
            if (s.getSide() != null) copy.setSideToSlice(i + 1, s.getSide());
        }

        return copy;
    }

    // --------- Фильтры пицц ---------

    public List<Pizza> pizzasWithIngredient(Ingredient ingredient) {
        if (ingredient == null) throw new IllegalArgumentException("ingredient null");
        UUID ingrId = ingredient.getId();
        return pizzaRepo.filter(p ->
                p.getSlices().stream().anyMatch(sl ->
                        sl.getIngredients().stream().anyMatch(ip ->
                                ip.ingredient().getId().equals(ingrId)
                        )
                )
        );
    }

    public List<Pizza> pizzasByBase(Base base) {
        if (base == null) throw new IllegalArgumentException("base null");
        return pizzaRepo.filter(p -> p.getBase().getId().equals(base.getId()));
    }

    public List<Pizza> pizzasBySize(Size size) {
        if (size == null) throw new IllegalArgumentException("size null");
        return pizzaRepo.filter(p -> p.getSize() == size);
    }

    public List<Pizza> pizzasByMode(Mode mode) {
        if (mode == null) throw new IllegalArgumentException("mode null");
        return pizzaRepo.filter(p -> p.getMode() == mode);
    }

    // --------- Фильтры заказов ---------

    private List<Pizza> allPizzasOf(Order o) {
        List<Pizza> all = new ArrayList<>();
        all.addAll(o.getPizzasList());
        all.addAll(o.getCustomPizzas());
        return all;
    }

    public List<Order> ordersMoreThan(double minTotal) {
        return orderRepo.filter(o -> o.getTotalPrice() > minTotal);
    }

    public List<Order> ordersAtDay(LocalDate day, ZoneId zone) {
        return orderRepo.filter(o -> o.getTime().atZone(zone).toLocalDate().equals(day));
    }

    public List<Order> ordersWithPizza(Pizza pizza) {
        if (pizza == null) throw new IllegalArgumentException("pizza null");
        return orderRepo.filter(o ->
                allPizzasOf(o).stream().anyMatch(p -> p.getId().equals(pizza.getId()))
        );
    }

    public List<Order> ordersWithIngredient(Ingredient ingredient) {
        if (ingredient == null) throw new IllegalArgumentException("ingredient null");
        UUID ingrId = ingredient.getId();

        return orderRepo.filter(o ->
                allPizzasOf(o).stream().anyMatch(p ->
                        p.getSlices().stream().anyMatch(sl ->
                                sl.getIngredients().stream().anyMatch(ip ->
                                        ip.ingredient().getId().equals(ingrId)
                                )
                        )
                )
        );
    }

    public List<Order> ordersWithMinGuests(int n) {
        return orderRepo.filter(o -> o.getGuests().size() >= n);
    }
}