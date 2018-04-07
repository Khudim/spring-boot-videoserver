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
@EqualsAndHashCode(exclude = {"contents", "id"})
@ToString(exclude = "contents")
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String tag;
    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST})
    @JoinTable(name = "content_tags", joinColumns = {
            @JoinColumn(name = "tag_id")}, inverseJoinColumns = {
            @JoinColumn(name = "content_id")
    })
    private Set<Content> contents = new HashSet<>(0);

    public Tags(String tag) {
        this.tag = tag;
    }

    public void addContent(Content content) {
        if (contents.contains(content)) {
            return;
        }
        contents.add(content);
    }
}
