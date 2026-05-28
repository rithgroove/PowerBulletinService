package com.nopunnygames.pbservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

/**
 * One ordered power value inside a card print set.
 */
@Entity
@Table(
        name = "card_print_set_powers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"card_print_set_id", "power_order"})
)
@Getter
@Setter
public class CardPrintSetPower {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_print_set_id", nullable = false)
    private CardPrintSet cardPrintSet;

    @Column(name = "power_order", nullable = false)
    private int powerOrder;

    @Column(nullable = false)
    private int power;

    /**
     * Creates an empty power row.
     */
    public CardPrintSetPower() {
    }
}
