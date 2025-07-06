package com.jftse.entities.database.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

@Getter
@Setter
@Audited
@MappedSuperclass
public class AbstractBaseModel extends AbstractIdBaseModel {
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    private Date created;
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date modified;

    @PrePersist
    @PreUpdate
    protected void prePersist() {
        if(this.created == null)
            this.created = new Date();

        if(this.modified == null || this.modified.before(new Date()))
            this.modified = new Date();
    }
}
