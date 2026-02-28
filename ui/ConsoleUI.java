package ui;

import model.*;
import service.App;

import java.time.*;
import java.util.*;

public class ConsoleUI {
    private final App app;
    private final Scanner sc = new Scanner(System.in);

    private Order currentOrder = null;

    private List<Ingredient> lastIngredients = List.of();
    private List<Base> lastBases = List.of();
    private List<Side> lastSides = List.of();
    private List<Pizza> lastPizzas = List.of();
    private List<Order> lastOrders = List.of();

    public ConsoleUI(App app) { this.app = app; }

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
                        ZoneId zone = ZoneId.systemDefault();
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
                        printPizzaList(app.pizzasWithIngredient(ingr));
                    }
                    case 2 -> {
                        Size size = chooseSize();
                        printPizzaList(app.pizzasBySize(size));
                    }
                    case 3 -> {
                        Base base = chooseBaseFromRepo();
                        printPizzaList(app.pizzasByBase(base));
                    }
                    case 4 -> {
                        Mode mode = chooseMode();
                        printPizzaList(app.pizzasByMode(mode));
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
                    i + 1, p.getName(), p.getPrice(), p.getBase().getName(), p.getMode().getName());
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
                    i + 1, o.getName(), o.getTotalPrice(), o.getTime());
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
        lastIngredients = app.getAllIngredients().stream()
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
        lastIngredients = app.getAllIngredients().stream()
                .sorted(Comparator.comparing(Ingredient::getName))
                .toList();

        if (lastIngredients.isEmpty()) throw new IllegalArgumentException("Ингредиентов нет.");

        System.out.println("--- Ингредиенты ---");
        for (int i = 0; i < lastIngredients.size(); i++) {
            Ingredient ingr = lastIngredients.get(i);
            System.out.printf("%d) %s | %.2f%n", i + 1, ingr.getName(), ingr.getPrice());
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
            System.out.println("5) Удалить основу (по номеру)");
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
                    case 5 -> {
                        printBases();
                        if (lastBases.isEmpty()) throw new IllegalArgumentException("Основ нет.");
                        int n = readInt("Номер основы: ");
                        Base b = pickByNumber(lastBases, n, "основа");
                        app.deleteBase(b.getId());
                        System.out.println("Основа удалена.");
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
        lastBases = app.getAllBases().stream()
                .sorted(Comparator.comparing(Base::getName))
                .toList();

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
            System.out.println("4) Удалить борт");
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
                    case 4 -> {
                        printSides();
                        if (lastSides.isEmpty()) throw new IllegalArgumentException("Бортов нет");
                        int n = readInt("Номер борта");
                        Side s = pickByNumber(lastSides, n, "борт");
                        app.deleteSide(s.getId());
                        System.out.println("Борт удалён.");
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
        lastSides = app.getAllSides().stream()
                .sorted(Comparator.comparing(Side::getName))
                .toList();

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
        lastSides = app.getAllSides().stream()
                .sorted(Comparator.comparing(Side::getName))
                .toList();

        if (lastSides.isEmpty()) throw new IllegalArgumentException("Бортов нет.");

        System.out.println("--- Бортики ---");
        for (int i = 0; i < lastSides.size(); i++) {
            Side s = lastSides.get(i);
            System.out.printf("%d) %s | цена=%.2f%n", i + 1, s.getName(), s.getPrice());
        }
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
            System.out.println("14) Создать пиццу из кусков");
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

                        Pizza combo = app.createPizza(name, pizzaA.getBase(), pizzaA.getSize(), Mode.HALFS, null);
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

    private void printPizzas() {
        lastPizzas = app.getAllPizzas().stream()
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
            System.out.println("12) Удалить пиццу из текущего заказа");
            System.out.println("13) Удалить пиццу из текущего заказа");
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

                        Pizza copy = app.copyPizzaForOrder(p);
                        o.addPizza(copy);
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
                            boolean isCustom = o.getCustomPizzas().stream().anyMatch(cp -> cp.getId().equals(p.getId()));
                            String tag = isCustom ? "[Кастомная] " : "";
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

                        Pizza p = app.createPizzaForOrderOnly(name, a.getBase(), a.getSize(), Mode.HALFS, null);
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
                    case 12 -> {
                        Order o = ensureCurrentOrder();
                        List<Pizza> all = new ArrayList<>();
                        all.addAll(o.getPizzasList());
                        all.addAll(o.getCustomPizzas());
                        if (all.isEmpty()) throw new IllegalArgumentException("В заказе нет пицц.");

                        System.out.println("--- Пиццы в заказе ---");
                        for (int i = 0; i < all.size(); i++) {
                            System.out.printf("%d) %s | %.2f%n", i + 1, all.get(i).getName(), all.get(i).getPrice());
                        }
                        int n = readInt("Номер пиццы: ");
                        Pizza p = pickByNumber(all, n, "пицца");

                        o.removeAnyPizza(p);
                        System.out.println("Пицца удалена из заказа.");
                    }
                    case 13 -> {
                        Order o = ensureCurrentOrder();

                        List<Pizza> all = new ArrayList<>();
                        all.addAll(o.getPizzasList());
                        all.addAll(o.getCustomPizzas());
                        if (all.isEmpty()) throw new IllegalArgumentException("В заказе нет пицц.");

                        System.out.println("--- Пиццы в заказе ---");
                        for (int i = 0; i < all.size(); i++) {
                            Pizza p = all.get(i);
                            System.out.printf("%d) %s | размер=%s%n", i + 1, p.getName(), p.getSize().getName());
                        }

                        int n = readInt("Номер пиццы: ");
                        Pizza p = pickByNumber(all, n, "пицца");

                        Size newSize = readEnum("Новый размер (SMALL/MEDIUM/LARGE): ", Size.class);
                        p.setSize(newSize);

                        System.out.println("Ок. Новый размер: " + newSize.getName());
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
        lastOrders = app.getAllOrders().stream()
                .sorted(Comparator.comparing(Order::getTime))
                .toList();

        if (lastOrders.isEmpty()) {
            System.out.println("(пусто)");
            return;
        }
        System.out.println("--- Заказы ---");
        for (int i = 0; i < lastOrders.size(); i++) {
            Order o = lastOrders.get(i);
            System.out.printf("%d) №%s | %s | %.2f%n", i + 1, o.getName(), o.getTime(), o.getTotalPrice());
        }
    }

    // ---------- Выборы из репозиториев ----------
    private Ingredient chooseIngredientFromRepo() {
        List<Ingredient> list = app.getAllIngredients().stream()
                .sorted(Comparator.comparing(Ingredient::getName))
                .toList();
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
        List<Base> list = app.getAllBases().stream()
                .sorted(Comparator.comparing(Base::getName))
                .toList();
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
        for (int i = 0; i < arr.length; i++) System.out.printf("%d) %s%n", i + 1, arr[i].getName());
        int n = readInt("Номер размера: ");
        if (n < 1 || n > arr.length) throw new IllegalArgumentException("Неверный номер размера");
        return arr[n - 1];
    }

    private Mode chooseMode() {
        System.out.println("--- Режимы ---");
        Mode[] arr = Mode.values();
        for (int i = 0; i < arr.length; i++) System.out.printf("%d) %s%n", i + 1, arr[i].getName());
        int n = readInt("Номер режима: ");
        if (n < 1 || n > arr.length) throw new IllegalArgumentException("Неверный номер режима");
        return arr[n - 1];
    }

    private Pizza choosePizzaFromCatalog() {
        List<Pizza> list = app.getAllPizzas().stream()
                .sorted(Comparator.comparing(Pizza::getName))
                .toList();
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
        List<Order> list = app.getAllOrders().stream()
                .sorted(Comparator.comparing(Order::getTime))
                .toList();
        if (list.isEmpty()) throw new IllegalArgumentException("Заказов нет. Сначала создайте заказ.");

        System.out.println("--- Заказы ---");
        for (int i = 0; i < list.size(); i++) {
            Order o = list.get(i);
            System.out.printf("%d) №%s | %s | %.2f%n", i + 1, o.getName(), o.getTime(), o.getTotalPrice());
        }

        int n = readInt("Номер заказа: ");
        return pickByNumber(list, n, "заказ");
    }

    private Base chooseBase() { return chooseBaseFromRepo(); }

    private Side chooseSide() {
        List<Side> list = app.getAllSides().stream()
                .sorted(Comparator.comparing(Side::getName))
                .toList();
        if (list.isEmpty()) throw new IllegalArgumentException("Бортов нет. Сначала создайте борт.");

        System.out.println("--- Бортики ---");
        for (int i = 0; i < list.size(); i++) {
            Side s = list.get(i);
            System.out.printf("%d) %s | цена=%.2f%n", i + 1, s.getName(), s.getPrice());
        }
        int n = readInt("Номер бортика: ");
        return pickByNumber(list, n, "борт");
    }

    private Ingredient chooseIngredient() { return chooseIngredientFromRepo(); }

    private Ingredient chooseIngredientOrZero() {
        List<Ingredient> list = app.getAllIngredients().stream()
                .sorted(Comparator.comparing(Ingredient::getName))
                .toList();
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

    // ---------- Utils ----------
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
}
