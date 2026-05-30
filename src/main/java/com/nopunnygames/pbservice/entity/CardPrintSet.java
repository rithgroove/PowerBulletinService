package com.nopunnygames.pbservice.entity;

import com.nopunnygames.pbservice.dto.CardPrintSetDto;
import com.nopunnygames.tanuki.core.entity.MasterEntity;
import com.nopunnygames.tanuki.core.filter.FilterConfig;
import com.nopunnygames.tanuki.core.filter.FilterType;
import com.nopunnygames.tanuki.core.filter.SourceType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Exact physical print distribution for a card version.
 */
@Entity
@Table(name = "card_print_sets")
@Getter
@Setter
public class CardPrintSet extends MasterEntity<CardPrintSet, CardPrintSetDto> {
    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_version_id", nullable = false)
    private CardVersion cardVersion;

    @OneToMany(mappedBy = "cardPrintSet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("powerOrder ASC")
    private List<CardPrintSetPower> powers = new ArrayList<>();

    /**
     * Creates an empty card print set entity.
     */
    public CardPrintSet() {
    }

    @Override
    protected Class<CardPrintSetDto> getDtoClass() {
        return CardPrintSetDto.class;
    }

    @Override
    protected Class<CardPrintSet> getEntityClass() {
        return CardPrintSet.class;
    }

    @Override
    public CardPrintSetDto toDto() {
        CardPrintSetDto dto = new CardPrintSetDto();
        BeanUtils.copyProperties(this, dto, "cardVersion", "powers");
        if (cardVersion != null) {
            dto.setCardVersionId(cardVersion.getId());
        }
        dto.setPowers(powers.stream().map(CardPrintSetPower::getPower).toList());
        return dto;
    }

    /**
     * Replaces the ordered power rows for this print set.
     *
     * @param orderedPowers explicit ordered power values
     */
    public void replacePowers(List<Integer> orderedPowers) {
        for (int index = 0; index < orderedPowers.size(); index++) {
            CardPrintSetPower power;
            if (index < powers.size()) {
                power = powers.get(index);
            } else {
                power = new CardPrintSetPower();
                power.setCardPrintSet(this);
                powers.add(power);
            }
            power.setPowerOrder(index + 1);
            power.setPower(orderedPowers.get(index));
        }
        while (powers.size() > orderedPowers.size()) {
            powers.remove(powers.size() - 1);
        }
    }

    /**
     * Returns filter metadata for card print sets.
     *
     * @return filter metadata
     */
    public static List<FilterConfig> getFilterConfigs() {
        return List.of(new FilterConfig("status", "Status", FilterType.CHECKBOXES, SourceType.DATABASE_DISTINCT, null, null, null, null, true));
    }

    /**
     * Returns fields used for keyword search.
     *
     * @return searchable fields
     */
    public static List<String> getSearchableFields() {
        return List.of("code");
    }
}
