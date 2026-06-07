package com.nopunnygames.pbservice.entity;

import com.nopunnygames.pbservice.dto.DeckIdentityDto;
import com.nopunnygames.tanuki.core.entity.MasterEntity;
import com.nopunnygames.tanuki.core.filter.FilterConfig;
import com.nopunnygames.tanuki.core.filter.FilterType;
import com.nopunnygames.tanuki.core.filter.SourceType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Stable conceptual identity for a Power Bulletin deck.
 */
@Entity
@Table(name = "deck_identities")
@Getter
@Setter
public class DeckIdentity extends MasterEntity<DeckIdentity, DeckIdentityDto> {
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @OneToMany(mappedBy = "deckIdentity", cascade = CascadeType.ALL)
    private List<DeckVersion> versions = new ArrayList<>();

    /**
     * Creates an empty deck identity entity.
     */
    public DeckIdentity() {
    }

    @Override
    protected Class<DeckIdentityDto> getDtoClass() {
        return DeckIdentityDto.class;
    }

    @Override
    protected Class<DeckIdentity> getEntityClass() {
        return DeckIdentity.class;
    }

    @Override
    public DeckIdentityDto toDto() {
        DeckIdentityDto dto = new DeckIdentityDto();
        BeanUtils.copyProperties(this, dto, "versions");
        return dto;
    }

    @Override
    public DeckIdentityDto toCompleteDto() {
        DeckIdentityDto dto = toDto();
        dto.setVersions(versions.stream().map(DeckVersion::toDto).toList());
        return dto;
    }

    /**
     * Returns filter metadata for deck identities.
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
        return List.of("code", "name", "notes");
    }
}
