package com.hotelsa.backend.hotel.model;

import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.user.model.User;
import com.hotelsa.backend.guest.model.Guest;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String city;
    private String country;
    private String phone;
    private String description;

    // Relación: un hotel puede tener muchos usuarios
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<User> users = new ArrayList<>();

    // Relación: un hotel puede tener muchas habitaciones
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Room> rooms = new ArrayList<>();

    // Relación: un hotel puede tener muchos huéspedes (Guests)
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Guest> guests = new ArrayList<>();

    public void addUser(User user) {
        if (users == null) {
            users = new ArrayList<>();
        }
        users.add(user);
        user.setHotel(this); // Mantiene consistencia bidireccional
    }

    public void removeUser(User user) {
        if (users != null) {
            users.remove(user);
            user.setHotel(null);
        }
    }

    public void addRoom(Room room) {
        if (rooms == null) {
            rooms = new ArrayList<>();
        }
        rooms.add(room);
        room.setHotel(this); // Mantiene consistencia bidireccional
    }

    public void removeRoom(Room room) {
        if (rooms != null) {
            rooms.remove(room);
            room.setHotel(null);
        }
    }

    public void addGuest(Guest guest) {
        if (guests == null) {
            guests = new ArrayList<>();
        }
        guests.add(guest);
        guest.setHotel(this);
    }

    public void removeGuest(Guest guest) {
        if (guests != null) {
            guests.remove(guest);
            guest.setHotel(null);
        }
    }
}
