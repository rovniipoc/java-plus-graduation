package ru.practicum.ewm.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Entity
@Table(name = "similarities")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
public class Similarity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_a_id", nullable = false)
    private long eventAId;

    @Column(name = "event_b_id", nullable = false)
    private long eventBId;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
}