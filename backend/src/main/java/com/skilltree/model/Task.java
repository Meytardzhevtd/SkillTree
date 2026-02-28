package com.skilltree.model;

import jakarta.persistence.*;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_module", nullable = false)
    private Module module;

    @Column(nullable = false)
    private String content; // only text now!!!

    // add getters!!!
}