package com.bootcamp.capabilityservice.infrastructure.output.persistence.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad R2DBC para la tabla capability_technologies (relación many-to-many).
 */
@Table("capability_technologies")
public class CapabilityTechnologyEntity {

    @Column("capability_id")
    private Long capabilityId;

    @Column("technology_id")
    private Long technologyId;

    public CapabilityTechnologyEntity() {
    }

    public CapabilityTechnologyEntity(Long capabilityId, Long technologyId) {
        this.capabilityId = capabilityId;
        this.technologyId = technologyId;
    }

    public Long getCapabilityId() {
        return capabilityId;
    }

    public void setCapabilityId(Long capabilityId) {
        this.capabilityId = capabilityId;
    }

    public Long getTechnologyId() {
        return technologyId;
    }

    public void setTechnologyId(Long technologyId) {
        this.technologyId = technologyId;
    }
}
