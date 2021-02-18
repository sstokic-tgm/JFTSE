package com.jftse.emulator.common.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

@Getter
@Setter
@Audited
@MappedSuperclass
public class AbstractBaseModel extends AbstractIdBaseModel {
    private Date created;
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
