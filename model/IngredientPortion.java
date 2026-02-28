package model;

public record IngredientPortion(Ingredient ingredient, int multiplier) {
    public IngredientPortion {
        if (ingredient == null) throw new IllegalArgumentException("ingredient null");
        if (multiplier != 1 && multiplier != 2) throw new IllegalArgumentException("multiplier must be 1 or 2");
    }

    public double cost() { return ingredient.getPrice() * multiplier; }
}