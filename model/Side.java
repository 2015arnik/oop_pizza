package model;

import java.util.*;

public class Side extends Entity {
    private final List<IngredientPortion> ingredients = new ArrayList<>();
    private final List<UUID> banPizzaIds = new ArrayList<>();

    public Side(String name) { super(name); }

    public void addIngredient(Ingredient ingredient, int mult) {
        ingredients.add(new IngredientPortion(ingredient, mult));
    }

    public void removeIngredient(UUID ingredientId) {
        ingredients.removeIf(p -> p.ingredient().getId().equals(ingredientId));
    }

    public List<IngredientPortion> getIngredients() {
        return Collections.unmodifiableList(ingredients);
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

    public List<UUID> getBanPizzaIds() {
        return Collections.unmodifiableList(banPizzaIds);
    }
}