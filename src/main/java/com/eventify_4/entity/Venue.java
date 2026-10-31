package com.eventify_4.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "address", nullable = false, length = 80)
    private String address;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "city", nullable = false, length = 80)
    private String city;

    @Column(name = "status", nullable = false)
    private boolean status;

    // Un lugar puede tener muchos eventos (1:N)
    @OneToMany(mappedBy = "venue", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Event> events = new ArrayList<>();

    public Venue(Long id, String name, String address, Integer capacity) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.capacity = capacity;
    }
}
