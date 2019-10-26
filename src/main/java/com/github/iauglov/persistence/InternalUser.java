package com.github.iauglov.persistence;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InternalUser {

    @Id
    private Integer id;
    private boolean isAdmin;
    private String name;

}
