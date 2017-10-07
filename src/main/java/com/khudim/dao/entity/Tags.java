package com.khudim.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "tags")
@EqualsAndHashCode(exclude = {"videos", "id"})
@ToString(exclude = "videos")
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String tag;
    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST})
    @JoinTable(name = "video_tags", joinColumns = {
            @JoinColumn(name = "tag_id")}, inverseJoinColumns = {
            @JoinColumn(name = "video_id")
    })
    private Set<Video> videos = new HashSet<>(0);

    public Tags(String tag) {
        this.tag = tag;
    }

    public void addVideo(Video video) {
        if (videos.contains(video)) {
            return;
        }
        videos.add(video);
    }
}
