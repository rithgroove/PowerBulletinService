package com.nopunnygames.pbservice.entity;

import com.nopunnygames.pbservice.dto.DeckVersionDto;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Historical version of a Power Bulletin deck.
 */
@Entity
@Table(name = "deck_versions")
@Getter
@Setter
public class DeckVersion extends MasterEntity<DeckVersion, DeckVersionDto> {
    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_identity_id", nullable = false)
    private DeckIdentity deckIdentity;

    @Column(name = "version_name", nullable = false)
    private String versionName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "deckVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeckEntry> entries = new ArrayList<>();

    /**
     * Creates an empty deck version entity.
     */
    public DeckVersion() {
    }

    @Override
    protected Class<DeckVersionDto> getDtoClass() {
        return DeckVersionDto.class;
    }

    @Override
    protected Class<DeckVersion> getEntityClass() {
        return DeckVersion.class;
    }

    @Override
    public DeckVersionDto toDto() {
        DeckVersionDto dto = new DeckVersionDto();
        BeanUtils.copyProperties(this, dto, "deckIdentity", "entries");
        if (deckIdentity != null) {
            dto.setDeckIdentityId(deckIdentity.getId());
        }
        return dto;
    }

    @Override
    public DeckVersionDto toCompleteDto() {
        DeckVersionDto dto = toDto();
        dto.setEntries(entries.stream().map(DeckEntry::toDto).toList());
        return dto;
    }

    /**
     * Returns filter metadata for deck versions.
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
        return List.of("code", "versionName", "notes");
    }
}
