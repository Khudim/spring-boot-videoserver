package com.khudim.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Beaver.
 */
@Entity
@Table
@Data
@EqualsAndHashCode(exclude = {"videoTags"})
@ToString(exclude = {"videoTags"})
public class Video implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private long date;
    private long contentId;
    private int width;
    private int height;
    @JsonIgnore
    private String storage;
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "videos", cascade = CascadeType.MERGE)
    private Set<Tags> videoTags = new HashSet<>(0);
}
