package com.jftse.entities.database.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "Metrics",
        indexes = {
                @Index(name = "idx_metric_name", columnList = "name"),
                @Index(name = "idx_metric_name_server", columnList = "name, serverType")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "serverType"})
        }
)
public class Metric extends AbstractBaseModel {
    private String name;
    private Long value;
    private ServerType serverType;
    private Long timestamp;
}
