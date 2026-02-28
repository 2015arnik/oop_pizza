package model;

import java.util.*;

public class Pizza extends Slice {
    private List<Slice> slices;
    private Base base;
    private Mode mode;

    private void initSlices() {
        slices = new ArrayList<>();
        for (int i = 0; i < getSize().getAmount(); i++) {
            slices.add(new Slice(getName() + " кусок " + (i + 1), getSize(), getSide()) {});
        }
    }

    public Pizza(String name, Base base, Size size, Mode mode, Side side) {
        super(name, size, side);
        if (base == null) throw new IllegalArgumentException("У пиццы должна быть основа");
        this.base = base;
        this.mode = mode;
        initSlices();
    }

    public Base getBase() { return base; }
    public Mode getMode() { return mode; }

    public List<Slice> getSlices() {
        return Collections.unmodifiableList(slices);
    }

    public void setBase(Base base) {
        if (base == null) throw new IllegalArgumentException("base null");
        this.base = base;
    }

    public void setMode(Mode mode) {
        if (mode == null) throw new IllegalArgumentException("mode null");
        this.mode = mode;
    }

    public void setSize(Size size) {
        if (size == null) throw new IllegalArgumentException("size null");
        setSizeInternal(size);
        initSlices();
    }

    private void copyIngredientsFromSlice(Slice from, Slice to) {
        to.clearIngredientsInternal();
        for (IngredientPortion ip : from.getIngredients()) {
            to.addIngredientInternal(ip.ingredient(), ip.multiplier());
        }
    }

    public void addIngredientsBasic(Ingredient ingr, int mult) {
        for (Slice slice : slices) slice.addIngredient(ingr, mult);
    }

    public void addIngredientToSlice(int sliceNumber1Based, Ingredient ingr, int mult) {
        if (sliceNumber1Based < 1 || sliceNumber1Based > slices.size())
            throw new IllegalArgumentException("Неверный номер куска");
        slices.get(sliceNumber1Based - 1).addIngredient(ingr, mult);
    }

    public void applyHalfsFrom(Pizza pizzaA, Pizza pizzaB) {
        if (pizzaA.getSlices().size() != pizzaB.getSlices().size())
            throw new IllegalArgumentException("Разные размеры (разное число кусков).");

        int mid = slices.size() / 2;

        for (int i = 0; i < mid; i++) copyIngredientsFromSlice(pizzaA.getSlices().get(i), slices.get(i));
        for (int i = mid; i < slices.size(); i++) copyIngredientsFromSlice(pizzaB.getSlices().get(i), slices.get(i));
    }

    public void addIngredientParts(Ingredient ingr, int mult, int a, int b) {
        if (a < 1 || b > slices.size() || a > b) throw new IllegalArgumentException("Неверный диапазон кусков");
        for (int i = a - 1; i <= b - 1; i++) slices.get(i).addIngredient(ingr, mult);
    }

    public void addSideBasic(Side side) {
        for (Slice slice : slices) slice.setSide(side, getId());
    }

    public void addSideHalfs(Side side, String half) {
        int mid = slices.size() / 2;

        if ("A".equals(half)) {
            for (int i = 0; i < mid; i++) slices.get(i).setSide(side, getId());
        } else if ("B".equals(half)) {
            for (int i = mid; i < slices.size(); i++) slices.get(i).setSide(side, getId());
        } else {
            throw new IllegalArgumentException("half должен быть A или B");
        }
    }

    public void addSideParts(Side side, int a, int b) {
        if (a < 1 || b > slices.size() || a > b) throw new IllegalArgumentException("Неверный диапазон");
        for (int i = a - 1; i <= b - 1; i++) slices.get(i).setSide(side, getId());
    }

    public void setSideToSlice(int sliceNumber1Based, Side side) {
        if (sliceNumber1Based < 1 || sliceNumber1Based > slices.size())
            throw new IllegalArgumentException("Неверный номер куска");
        slices.get(sliceNumber1Based - 1).setSide(side, getId());
    }

    public void removeIngredientEverywhere(UUID ingredientId) {
        for (Slice sl : slices) sl.removeIngredient(ingredientId);
    }

    public void clearIngredientsEverywhere() {
        for (Slice sl : slices) sl.clearIngredientsInternal();
    }

    public double getPrice() {
        double total = 0;
        total += base.getPrice();

        for (Slice slice : slices) {
            for (IngredientPortion ip : slice.getIngredients()) total += ip.cost();
        }

        total += slices.stream()
                .map(Slice::getSide)
                .filter(Objects::nonNull)
                .distinct()
                .mapToDouble(Side::getPrice)
                .sum();

        return total;
    }

    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("Пицца: ").append(getName())
                .append(", размер: ").append(getSize().getName())
                .append(", основа: ").append(base.getName())
                .append(", режим: ").append(mode.getName())
                .append(", цена: ").append(String.format("%.2f", getPrice()))
                .append("\n");

        for (int i = 0; i < slices.size(); i++) {
            Slice sl = slices.get(i);
            sb.append("  Кусок ").append(i + 1).append(": ");
            sb.append("бортик=").append(sl.getSide() == null ? "нет" : sl.getSide().getName()).append("; ");
            sb.append("ингредиенты=[");

            List<IngredientPortion> ingrList = sl.getIngredients();
            for (int j = 0; j < ingrList.size(); j++) {
                IngredientPortion ip = ingrList.get(j);
                sb.append(ip.ingredient().getName()).append("x").append(ip.multiplier());
                if (j < ingrList.size() - 1) sb.append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}