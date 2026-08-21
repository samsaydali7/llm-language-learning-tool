package com.languagelearning.structure.api;

import com.languagelearning.structure.entity.StructureNode;

public class StructureDtos {

    private StructureDtos() {
    }

    public record StructureNodeResponse(
            Long id,
            Long parentId,
            String type,
            String title,
            Integer orderIndex,
            Integer startPage,
            Integer endPage,
            boolean hasChildren) {

        public static StructureNodeResponse from(StructureNode node, boolean hasChildren) {
            return new StructureNodeResponse(
                    node.getId(),
                    node.getParent() != null ? node.getParent().getId() : null,
                    node.getType().name(),
                    node.getTitle(),
                    node.getOrderIndex(),
                    node.getStartPage(),
                    node.getEndPage(),
                    hasChildren);
        }
    }
}
