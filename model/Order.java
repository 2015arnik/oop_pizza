package model;

import java.time.*;
import java.util.*;

public class Order extends Entity {
    private final List<Pizza> pizzasList = new ArrayList<>();
    private final Map<UUID, List<Person>> pizzaGuests = new HashMap<>();
    private final List<Person> guests = new ArrayList<>();
    private final List<Pizza> customPizzas = new ArrayList<>();
    private String comment;
    private Instant time;

    public Order(String name) {
        super(name);
        this.time = Instant.now();
    }

    public List<Person> getGuests() { return Collections.unmodifiableList(guests); }
    public List<Pizza> getPizzasList() { return Collections.unmodifiableList(pizzasList); }
    public List<Pizza> getCustomPizzas() { return Collections.unmodifiableList(customPizzas); }

    public Map<UUID, List<Person>> getPizzaGuestsSnapshot() {
        Map<UUID, List<Person>> snap = new HashMap<>();
        for (var e : pizzaGuests.entrySet()) {
            snap.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
        }
        return Collections.unmodifiableMap(snap);
    }

    public List<Person> getGuestsForPizza(UUID pizzaId) {
        return Collections.unmodifiableList(pizzaGuests.getOrDefault(pizzaId, List.of()));
    }

    public void removeGuest(Person guest) {
        guests.removeIf(g -> g.getId().equals(guest.getId()));
        for (List<Person> persons : pizzaGuests.values()) {
            persons.removeIf(person -> guest.getId().equals(person.getId()));
        }
    }

    public void createGuest(Person guest) { guests.add(guest); }

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

    public void addCustomPizza(Pizza pizza) {
        customPizzas.add(pizza);
        pizzaGuests.put(pizza.getId(), new ArrayList<>());
    }

    public void removeAnyPizza(Pizza pizza) {
        pizzasList.removeIf(p -> p.getId().equals(pizza.getId()));
        customPizzas.removeIf(p -> p.getId().equals(pizza.getId()));
        pizzaGuests.remove(pizza.getId());
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Instant getTime() { return time; }

    public void setTime(Instant time) {
        if (time == null) throw new IllegalArgumentException("time == null");
        if (time.isBefore(Instant.now())) throw new IllegalArgumentException("Нельзя сделать заказ в прошлом");
        this.time = time;
    }

    public void postponeTo(LocalDate date, LocalTime time, ZoneId zone) {
        if (date == null || time == null || zone == null) {
            throw new IllegalArgumentException("Дата/время/зона не должны быть null");
        }
        Instant newInstant = LocalDateTime.of(date, time).atZone(zone).toInstant();
        setTime(newInstant);
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

            for (Person e : eaters) bills.put(e.getId(), bills.get(e.getId()) + share);

            if (diff > 0) {
                Person first = eaters.get(0);
                bills.put(first.getId(), bills.get(first.getId()) + diff);
            }
        }
        return bills;
    }

    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("Заказ №").append(getName()).append("\n");
        sb.append("Время: ").append(time).append("\n");
        sb.append("Комментарий: ").append(comment == null ? "-" : comment).append("\n\n");

        for (Pizza p : pizzasList) sb.append(p.describe()).append("\n");

        if (!customPizzas.isEmpty()) {
            sb.append("\nКастомные пиццы:\n");
            for (Pizza p : customPizzas) sb.append("[Кастомная] ").append(p.describe()).append("\n");
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