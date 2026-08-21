package com.languagelearning.extraction.model;

import com.languagelearning.structure.entity.StructureNodeType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * In-memory representation of one node of a book's detected structure, before it is persisted as
 * a {@code StructureNode}. Mutable so a post-processing pass can fill in {@code endPage} once the
 * whole tree (and therefore each node's neighbors) is known.
 */
@Getter
@Setter
public class ExtractedNode {

    private final String title;
    private final StructureNodeType type;
    private final Integer startPage;
    private Integer endPage;
    private final List<ExtractedNode> children;

    public ExtractedNode(String title, StructureNodeType type, Integer startPage, List<ExtractedNode> children) {
        this.title = title;
        this.type = type;
        this.startPage = startPage;
        this.children = children != null ? children : new ArrayList<>();
    }
}
