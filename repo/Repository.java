package repo;


import model.Entity;
import java.util.*;
import java.util.function.Predicate;

public class Repository<T extends Entity> implements CrudRepository<T> {
    private final Map<UUID, T> data = new HashMap<>();

    public void add(T obj) { data.put(obj.getId(), obj); }
    public T get(UUID id) { return data.get(id); }
    public void remove(UUID id) { data.remove(id); }
    public List<T> all() { return new ArrayList<>(data.values()); }
    public List<T> filter(Predicate<T> predicate) { return data.values().stream().filter(predicate).toList(); }
}