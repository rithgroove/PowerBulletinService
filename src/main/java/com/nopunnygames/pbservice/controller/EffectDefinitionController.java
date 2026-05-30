package com.nopunnygames.pbservice.controller;

import com.nopunnygames.pbservice.dto.EffectDefinitionDto;
import com.nopunnygames.pbservice.entity.EffectDefinition;
import com.nopunnygames.pbservice.repository.EffectDefinitionRepository;
import com.nopunnygames.pbservice.service.EffectDefinitionService;
import com.nopunnygames.tanuki.core.controller.MasterController;
import com.nopunnygames.tanuki.core.exception.ObjectNotFoundException;
import com.nopunnygames.tanuki.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for effect definitions.
 */
@RestController
@RequestMapping("/effects")
public class EffectDefinitionController extends MasterController<EffectDefinition, UUID, EffectDefinitionDto> {
    private final EffectDefinitionService service;
    private final EffectDefinitionRepository repository;

    /**
     * Creates the effect definition controller.
     *
     * @param service effect definition service
     * @param repository effect definition repository
     */
    public EffectDefinitionController(EffectDefinitionService service, EffectDefinitionRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @Override
    protected EffectDefinitionService getService() {
        return service;
    }

    /**
     * Reads an effect definition by stable code.
     *
     * @param code stable code
     * @return effect definition response
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<EffectDefinitionDto>> getByCode(@PathVariable String code) {
        EffectDefinition effect = repository.findByCode(code)
                .orElseThrow(() -> new ObjectNotFoundException("Effect definition " + code + " not found"));
        return ResponseEntity.ok(new ApiResponse<>(200, effect.toCompleteDto()));
    }

    @Override
    protected String getFeatureCode() {
        return "EFFECT_DEFINITION";
    }
}
