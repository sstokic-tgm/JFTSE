package com.jftse.entities.database.model.messenger;

import com.jftse.entities.database.model.item.Product;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;

@Getter
@Setter
@Audited
@Entity
public class Gift extends AbstractMessage {
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;
}