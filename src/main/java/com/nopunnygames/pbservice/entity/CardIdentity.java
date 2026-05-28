package com.nopunnygames.pbservice.entity;

import com.nopunnygames.pbservice.dto.CardIdentityDto;
import com.nopunnygames.pbservice.enums.Faction;
import com.nopunnygames.tanuki.core.entity.MasterEntity;
import com.nopunnygames.tanuki.core.filter.FilterConfig;
import com.nopunnygames.tanuki.core.filter.FilterType;
import com.nopunnygames.tanuki.core.filter.SourceType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Stable conceptual identity for a Power Bulletin card.
 */
@Entity
@Table(name = "card_identities")
@Getter
@Setter
public class CardIdentity extends MasterEntity<CardIdentity, CardIdentityDto> {
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "character_name", nullable = false)
    private String characterName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Faction faction;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "cardIdentity", cascade = CascadeType.ALL)
    private List<CardVersion> versions = new ArrayList<>();

    /**
     * Creates an empty card identity entity.
     */
    public CardIdentity() {
    }

    @Override
    protected Class<CardIdentityDto> getDtoClass() {
        return CardIdentityDto.class;
    }

    @Override
    protected Class<CardIdentity> getEntityClass() {
        return CardIdentity.class;
    }

    @Override
    public CardIdentityDto toDto() {
        CardIdentityDto dto = new CardIdentityDto();
        BeanUtils.copyProperties(this, dto, "versions");
        return dto;
    }

    @Override
    public CardIdentityDto toCompleteDto() {
        CardIdentityDto dto = toDto();
        dto.setVersions(versions.stream().map(CardVersion::toDto).toList());
        return dto;
    }

    /**
     * Returns filter metadata for card identities.
     *
     * @return filter metadata
     */
    public static List<FilterConfig> getFilterConfigs() {
        return List.of(
                new FilterConfig("status", "Status", FilterType.CHECKBOXES, SourceType.DATABASE_DISTINCT, null, null, null, null, true),
                new FilterConfig("faction", "Faction", FilterType.CHECKBOXES, SourceType.STATIC, null, List.of("HERO", "VILLAIN", "CIVILIAN"), true)
        );
    }

    /**
     * Returns fields used for keyword search.
     *
     * @return searchable fields
     */
    public static List<String> getSearchableFields() {
        return List.of("code", "name", "characterName", "notes");
    }
}
