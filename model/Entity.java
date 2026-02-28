package model;

import java.util.UUID;

public abstract class Entity {
    private String name;
    private final UUID id = UUID.randomUUID();

    protected Entity(String name) {
        this.name = name;
    }

    public final UUID getId() { return id; }
    public final String getName() { return name; }
    public final void setName(String name) { this.name = name; }
}