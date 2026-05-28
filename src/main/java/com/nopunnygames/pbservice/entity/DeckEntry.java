package com.nopunnygames.pbservice.entity;

import com.nopunnygames.pbservice.dto.DeckEntryDto;
import com.nopunnygames.tanuki.core.entity.BaseEntity;
import com.nopunnygames.tanuki.core.filter.FilterConfig;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;

/**
 * Link between a deck version and a card print set.
 */
@Entity
@Table(
        name = "deck_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"deck_version_id", "card_print_set_id"})
)
@Getter
@Setter
public class DeckEntry extends BaseEntity<DeckEntry, DeckEntryDto> {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_version_id", nullable = false)
    private DeckVersion deckVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_print_set_id", nullable = false)
    private CardPrintSet cardPrintSet;

    @Column(nullable = false)
    private int quantity = 1;

    /**
     * Creates an empty deck entry entity.
     */
    public DeckEntry() {
    }

    @Override
    protected Class<DeckEntryDto> getDtoClass() {
        return DeckEntryDto.class;
    }

    @Override
    protected Class<DeckEntry> getEntityClass() {
        return DeckEntry.class;
    }

    @Override
    public DeckEntryDto toDto() {
        DeckEntryDto dto = new DeckEntryDto();
        BeanUtils.copyProperties(this, dto, "deckVersion", "cardPrintSet");
        if (deckVersion != null) {
            dto.setDeckVersionId(deckVersion.getId());
        }
        if (cardPrintSet != null) {
            dto.setCardPrintSetId(cardPrintSet.getId());
            dto.setPrintSet(cardPrintSet.toDto());
        }
        return dto;
    }

    /**
     * Returns no filter metadata for deck entries.
     *
     * @return empty filter metadata
     */
    public static List<FilterConfig> getFilterConfigs() {
        return Collections.emptyList();
    }

    /**
     * Returns no searchable fields for deck entries.
     *
     * @return empty searchable fields
     */
    public static List<String> getSearchableFields() {
        return Collections.emptyList();
    }
}
