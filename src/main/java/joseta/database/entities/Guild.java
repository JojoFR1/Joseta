package joseta.database.entities;

import jakarta.persistence.*;

@Entity
public class Guild {
    @Id
    long id;
    @Column
    String name;

    protected Guild() {}

    public Guild(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
