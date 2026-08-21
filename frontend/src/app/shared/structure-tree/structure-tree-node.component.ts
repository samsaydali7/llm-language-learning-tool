import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StructureNode } from '../../core/models/structure.model';
import { StructureService } from '../../core/services/structure.service';

/**
 * One node in a book's structure tree, lazily loading its children on first expand. Recursively
 * renders itself for descendants, and reports checkbox toggles up via (toggle) so the containing
 * scope selector can track the full set of selected node ids.
 */
@Component({
  selector: 'app-structure-tree-node',
  standalone: true,
  imports: [MatCheckboxModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule, StructureTreeNodeComponent],
  template: `
    <div class="node">
      <button mat-icon-button class="node__toggle" [class.node__toggle--hidden]="!node.hasChildren" (click)="toggleExpand()">
        @if (loadingChildren) {
          <mat-spinner diameter="16"></mat-spinner>
        } @else {
          <mat-icon>{{ expanded ? 'expand_more' : 'chevron_right' }}</mat-icon>
        }
      </button>
      <mat-checkbox [checked]="selectedIds.has(node.id)" (change)="toggle.emit(node.id)">
        {{ node.title }}
        @if (pageRangeLabel()) {
          <span class="node__page">{{ pageRangeLabel() }}</span>
        }
      </mat-checkbox>
    </div>
    @if (expanded && children.length) {
      <div class="node__children">
        @for (child of children; track child.id) {
          <app-structure-tree-node [node]="child" [selectedIds]="selectedIds" (toggle)="toggle.emit($event)" />
        }
      </div>
    }
  `,
  styles: [`
    .node { display: flex; align-items: center; gap: 0.15rem; }
    .node__toggle { width: 28px; height: 28px; line-height: 28px; }
    .node__toggle--hidden { visibility: hidden; }
    .node__page { margin-left: 0.4rem; font-size: 0.75rem; color: var(--text-muted); }
    .node__children { margin-left: 1.6rem; border-left: 1px dashed var(--border-subtle); padding-left: 0.5rem; }
  `],
})
export class StructureTreeNodeComponent implements OnInit {
  @Input({ required: true }) node!: StructureNode;
  @Input({ required: true }) selectedIds!: Set<number>;
  @Output() toggle = new EventEmitter<number>();

  expanded = false;
  loadingChildren = false;
  children: StructureNode[] = [];

  constructor(private readonly structureService: StructureService) {}

  ngOnInit(): void {}

  pageRangeLabel(): string {
    if (!this.node.startPage) {
      return '';
    }
    if (this.node.endPage && this.node.endPage !== this.node.startPage) {
      return `p.${this.node.startPage}-${this.node.endPage}`;
    }
    return `p.${this.node.startPage}`;
  }

  toggleExpand(): void {
    if (!this.node.hasChildren) {
      return;
    }
    this.expanded = !this.expanded;
    if (this.expanded && this.children.length === 0) {
      this.loadingChildren = true;
      this.structureService.children(this.node.id).subscribe((children) => {
        this.children = children;
        this.loadingChildren = false;
      });
    }
  }
}
