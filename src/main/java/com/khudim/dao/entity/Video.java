package com.khudim.dao.entity;

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
    @Column(name = "video_id")
    private long id;
    private String name;
    private long date;
    private long contentId;
    private int width;
    private int height;
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "video_tags", joinColumns = {
            @JoinColumn(name = "video_id")}, inverseJoinColumns = {
            @JoinColumn(name = "tag_id")
    })
    private Set<Tags> videoTags = new HashSet<>(0);
}
