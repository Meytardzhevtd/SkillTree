package com.skilltree.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_course", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    private List<Task> tasks;

    // add getters!!!
}