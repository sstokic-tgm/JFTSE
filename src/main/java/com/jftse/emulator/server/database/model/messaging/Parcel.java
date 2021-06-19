package com.jftse.emulator.server.database.model.messaging;

import com.jftse.emulator.common.model.AbstractBaseModel;
import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;

@Getter
@Setter
@Audited
@Entity
public class Parcel extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private Player sender;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private Player receiver;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    private String message;
    private Integer amount;
    private Integer gold;
    private ParcelType parcelType;
}