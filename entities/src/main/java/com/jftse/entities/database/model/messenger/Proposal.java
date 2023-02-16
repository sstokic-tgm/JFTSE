package com.jftse.entities.database.model.messenger;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Getter
@Setter
@Audited
@Entity
public class Proposal extends AbstractMessage {
        // ITEM INFO
    private String category;
    private Integer itemIndex;
}