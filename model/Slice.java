package model;

import java.util.*;

public abstract class Slice extends Entity {
    private final List<IngredientPortion> ingredients = new ArrayList<>();
    private Size size;
    private Side side;

    protected Slice(String name, Size size, Side side) {
        super(name);
        this.size = size;
        this.side = side;
    }

    public final Size getSize() { return size; }
    protected final void setSizeInternal(Size size) { this.size = size; }

    public final Side getSide() { return side; }

    public final List<IngredientPortion> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    public final void addIngredient(Ingredient ingredient, int mult) {
        ingredients.add(new IngredientPortion(ingredient, mult));
    }

    public final void removeIngredient(UUID ingredientId) {
        ingredients.removeIf(p -> p.ingredient().getId().equals(ingredientId));
    }

    public final void setSide(Side side, java.util.UUID pizzaId) {
        if (side == null) { this.side = null; return; }
        if (!side.getBanPizzaIds().contains(pizzaId)) this.side = side;
        else throw new IllegalArgumentException("Нельзя добавить такой борт к этой пицце");
    }

    protected final void clearIngredientsInternal() { ingredients.clear(); }

    protected final void addIngredientInternal(Ingredient ingredient, int mult) {
        ingredients.add(new IngredientPortion(ingredient, mult));
    }
}