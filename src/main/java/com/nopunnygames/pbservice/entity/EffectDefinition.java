package com.nopunnygames.pbservice.entity;

import com.nopunnygames.pbservice.dto.EffectDefinitionDto;
import com.nopunnygames.tanuki.core.entity.MasterEntity;
import com.nopunnygames.tanuki.core.filter.FilterConfig;
import com.nopunnygames.tanuki.core.filter.FilterType;
import com.nopunnygames.tanuki.core.filter.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Metadata for a Python-side executable effect.
 */
@Entity
@Table(name = "effect_definitions")
@Getter
@Setter
public class EffectDefinition extends MasterEntity<EffectDefinition, EffectDefinitionDto> {
    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "python_class", nullable = false)
    private String pythonClass;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Creates an empty effect definition entity.
     */
    public EffectDefinition() {
    }

    @Override
    protected Class<EffectDefinitionDto> getDtoClass() {
        return EffectDefinitionDto.class;
    }

    @Override
    protected Class<EffectDefinition> getEntityClass() {
        return EffectDefinition.class;
    }

    /**
     * Returns filter metadata for effect definitions.
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
        return List.of("code", "pythonClass", "name", "description");
    }
}
