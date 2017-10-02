package com.khudim.dao.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "tags")
@EqualsAndHashCode(exclude = {"videos"})
@ToString(exclude = "videos")
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private long id;
    private String tag;
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "videoTags")
    private Set<Video> videos = new HashSet<>(0);

    public Tags(String tag) {
        this.tag = tag;
    }

}
