package com.jftse.entities.database.model.messenger;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Getter
@Setter
@Audited
@MappedSuperclass
public class AbstractMessage extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private Player sender;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private Player receiver;

    private String message;
    private Boolean seen;
    private Byte useTypeOption = 0;
}
