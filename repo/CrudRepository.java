package repo;


import model.Entity;
import java.util.*;
import java.util.function.Predicate;

public interface CrudRepository<T extends Entity> {
    void add(T obj);
    T get(UUID id);
    void remove(UUID id);
    List<T> all();
    List<T> filter(Predicate<T> predicate);
}