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
@Entity
public class Friend extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "friend_id", referencedColumnName = "id")
    private Player friend;

    private EFriendshipState EFriendshipState;
}