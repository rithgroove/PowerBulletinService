package com.nopunnygames.pbservice.controller;

import com.nopunnygames.tanuki.core.controller.BaseController;
import com.nopunnygames.tanuki.core.entity.BaseEntity;

/**
 * Base controller variant used while pb-service is consumed by local CMS tooling.
 *
 * @param <E> entity type
 * @param <ID> entity identifier type
 * @param <Dto> DTO type
 */
public abstract class PublicBaseController<E extends BaseEntity<E, Dto>, ID, Dto>
        extends BaseController<E, ID, Dto> {
    /**
     * Creates a public base controller.
     */
    protected PublicBaseController() {
    }

    @Override
    protected boolean isPublicPermission(String requiredPermission) {
        return true;
    }
}
