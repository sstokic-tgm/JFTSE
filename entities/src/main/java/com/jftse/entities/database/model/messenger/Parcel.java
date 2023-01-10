package com.jftse.entities.database.model.messenger;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Getter
@Setter
@Audited
@Entity
public class Parcel extends AbstractMessage {
    private Integer gold;
    private EParcelType EParcelType;

    // ITEM INFO
    private String category;
    private Integer itemIndex;
    private Integer itemCount;
    private String useType;
}