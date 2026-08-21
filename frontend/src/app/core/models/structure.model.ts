export type StructureNodeType = 'BOOK' | 'VOLUME' | 'CHAPTER' | 'SECTION' | 'SUBSECTION';

export interface StructureNode {
  id: number;
  parentId: number | null;
  type: StructureNodeType;
  title: string;
  orderIndex: number;
  startPage: number | null;
  endPage: number | null;
  hasChildren: boolean;
}
