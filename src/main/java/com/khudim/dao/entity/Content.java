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
public class Content implements Serializable{
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private byte[] image;
    private String path;
}
