package com.khudim.dao.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Beaver.
 */
@Entity
@Table
@Data
public class Video implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private long date;
    private int width;
    private int height;
    @PrimaryKeyJoinColumn
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Content content;

}
