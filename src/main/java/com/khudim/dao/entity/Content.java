package com.khudim.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@EqualsAndHashCode(exclude = {"contentTags", "video"})
@ToString(exclude = {"contentTags", "video"})
@JsonIgnoreProperties({"image", "path", "length", "video", "storage", "contentTags"})
public class Content implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private byte[] image;
    private String path;
    private long length;
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "content", cascade = CascadeType.ALL)
    private Video video;
    private String storage;
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "contents", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Set<Tags> contentTags = new HashSet<>(0);
}
