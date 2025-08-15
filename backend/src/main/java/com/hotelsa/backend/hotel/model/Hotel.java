package com.hotelsa.backend.hotel.model;

import com.hotelsa.backend.user.model.User;
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
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
