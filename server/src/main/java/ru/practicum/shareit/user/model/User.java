package ru.practicum.shareit.user.model;

import lombok.*;
import org.hibernate.Hibernate;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
@Generated
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    @Column(name = "user_name")
    private String name;
    @Column(name = "email")
    private String email;
    @OneToMany(mappedBy = "owner")
    @ToString.Exclude
    private Set<Item> items;
    @OneToMany(mappedBy = "booker")
    @ToString.Exclude
    private Set<Booking> bookings;
    @OneToMany(mappedBy = "author")
    @ToString.Exclude
    private Set<Comment> comments;
    @OneToMany(mappedBy = "requester")
    @ToString.Exclude
    private Set<ItemRequest> requests;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
