package io.github.lrzeszotarski.accountmanager.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private UUID eventId;
    private String type;
    private OffsetDateTime happenedAt;

    @ManyToOne
    private Account account;
}
