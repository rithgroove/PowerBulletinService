package com.nopunnygames.pbservice.service;

import com.nopunnygames.pbservice.dto.EffectDefinitionDto;
import com.nopunnygames.pbservice.entity.EffectDefinition;
import com.nopunnygames.pbservice.repository.EffectDefinitionRepository;
import com.nopunnygames.tanuki.core.exception.ValidationError;
import com.nopunnygames.tanuki.core.service.MasterService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for Python effect metadata CRUD operations.
 */
@Service
public class EffectDefinitionService extends MasterService<EffectDefinition, UUID, EffectDefinitionDto> {
    private final EffectDefinitionRepository repository;

    /**
     * Creates the effect definition service.
     *
     * @param repository effect definition repository
     */
    public EffectDefinitionService(EffectDefinitionRepository repository) {
        this.repository = repository;
    }

    @Override
    protected EffectDefinitionRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<EffectDefinition> getEntityClass() {
        return EffectDefinition.class;
    }

    @Override
    protected Class<EffectDefinitionDto> getDtoClass() {
        return EffectDefinitionDto.class;
    }

    @Override
    protected List<ValidationError> createEntityValidation(EffectDefinitionDto dto) {
        return validate(dto);
    }

    @Override
    protected List<ValidationError> updateEntityValidation(EffectDefinitionDto dto) {
        return validate(dto);
    }

    private List<ValidationError> validate(EffectDefinitionDto dto) {
        List<ValidationError> errors = new ArrayList<>();
        if (isBlank(dto.getCode())) errors.add(new ValidationError("code", "Code is required."));
        if (isBlank(dto.getPythonClass())) errors.add(new ValidationError("pythonClass", "Python class is required."));
        if (isBlank(dto.getName())) errors.add(new ValidationError("name", "Name is required."));
        if (isBlank(dto.getDescription())) errors.add(new ValidationError("description", "Description is required."));
        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
