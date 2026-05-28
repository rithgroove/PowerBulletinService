package com.nopunnygames.pbservice.controller;

import com.nopunnygames.tanuki.core.controller.MasterController;
import com.nopunnygames.tanuki.core.entity.MasterEntity;

/**
 * Master controller variant used while pb-service is consumed by local CMS tooling.
 *
 * @param <E> entity type
 * @param <ID> entity identifier type
 * @param <Dto> DTO type
 */
public abstract class PublicMasterController<E extends MasterEntity<E, Dto>, ID, Dto>
        extends MasterController<E, ID, Dto> {
    /**
     * Creates a public master controller.
     */
    protected PublicMasterController() {
    }

    @Override
    protected boolean isPublicPermission(String requiredPermission) {
        return true;
    }
}
