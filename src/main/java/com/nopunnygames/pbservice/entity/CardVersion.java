package com.nopunnygames.pbservice.entity;

import com.nopunnygames.pbservice.dto.CardVersionDto;
import com.nopunnygames.pbservice.enums.CardType;
import com.nopunnygames.tanuki.core.entity.MasterEntity;
import com.nopunnygames.tanuki.core.filter.FilterConfig;
import com.nopunnygames.tanuki.core.filter.FilterType;
import com.nopunnygames.tanuki.core.filter.SourceType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Gameplay/rules version for one card identity.
 */
@Entity
@Table(name = "card_versions")
@Getter
@Setter
public class CardVersion extends MasterEntity<CardVersion, CardVersionDto> {
    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_identity_id", nullable = false)
    private CardIdentity cardIdentity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "effect_definition_id", nullable = false)
    private EffectDefinition effectDefinition;

    @Column(name = "version_name", nullable = false)
    private String versionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @Column(name = "effect_text", nullable = false, columnDefinition = "TEXT")
    private String effectText;

    @OneToMany(mappedBy = "cardVersion", cascade = CascadeType.ALL)
    private List<CardPrintSet> printSets = new ArrayList<>();

    /**
     * Creates an empty card version entity.
     */
    public CardVersion() {
    }

    @Override
    protected Class<CardVersionDto> getDtoClass() {
        return CardVersionDto.class;
    }

    @Override
    protected Class<CardVersion> getEntityClass() {
        return CardVersion.class;
    }

    @Override
    public CardVersionDto toDto() {
        CardVersionDto dto = new CardVersionDto();
        BeanUtils.copyProperties(this, dto, "cardIdentity", "effectDefinition", "printSets");
        if (cardIdentity != null) {
            dto.setCardIdentityId(cardIdentity.getId());
        }
        if (effectDefinition != null) {
            dto.setEffectDefinitionId(effectDefinition.getId());
            dto.setEffectDefinition(effectDefinition.toDto());
        }
        return dto;
    }

    @Override
    public CardVersionDto toCompleteDto() {
        CardVersionDto dto = toDto();
        dto.setPrintSets(printSets.stream().map(CardPrintSet::toDto).toList());
        return dto;
    }

    /**
     * Returns filter metadata for card versions.
     *
     * @return filter metadata
     */
    public static List<FilterConfig> getFilterConfigs() {
        return List.of(
                new FilterConfig("status", "Status", FilterType.CHECKBOXES, SourceType.DATABASE_DISTINCT, null, null, null, null, true),
                new FilterConfig("cardType", "Card Type", FilterType.CHECKBOXES, SourceType.STATIC, null, List.of("ACTION", "REACTION", "ON_DISCARD"), true)
        );
    }

    /**
     * Returns fields used for keyword search.
     *
     * @return searchable fields
     */
    public static List<String> getSearchableFields() {
        return List.of("code", "versionName", "effectText");
    }
}
