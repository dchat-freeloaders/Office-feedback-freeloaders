package com.github.iauglov.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import static lombok.AccessLevel.NONE;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "feedbacks")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FeedBack {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Setter(NONE)
    private Integer id;
    private int satisfactionLevel;
    @Column(length = 1024)
    private String text;
    @ManyToOne
    private Event event;
    @ManyToOne
    private InternalUser user;

}
