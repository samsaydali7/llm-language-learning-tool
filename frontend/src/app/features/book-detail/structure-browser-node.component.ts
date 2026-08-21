import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StructureNode } from '../../core/models/structure.model';
import { StructureService } from '../../core/services/structure.service';

/** Click-to-browse variant of the structure tree (as opposed to the checkbox-based scope selector one). */
@Component({
  selector: 'app-structure-browser-node',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, MatProgressSpinnerModule, StructureBrowserNodeComponent],
  template: `
    <div class="node" [class.node--selected]="selectedId === node.id">
      <button mat-icon-button class="node__toggle" [class.node__toggle--hidden]="!node.hasChildren" (click)="toggleExpand()">
        @if (loadingChildren) {
          <mat-spinner diameter="16"></mat-spinner>
        } @else {
          <mat-icon>{{ expanded ? 'expand_more' : 'chevron_right' }}</mat-icon>
        }
      </button>
      <button class="node__label" (click)="select.emit(node)">
        {{ node.title }}
        @if (node.startPage) {
          <span class="node__page">p.{{ node.startPage }}</span>
        }
      </button>
    </div>
    @if (expanded && children.length) {
      <div class="node__children">
        @for (child of children; track child.id) {
          <app-structure-browser-node [node]="child" [selectedId]="selectedId" (select)="select.emit($event)" />
        }
      </div>
    }
  `,
  styles: [`
    .node { display: flex; align-items: center; gap: 0.1rem; }
    .node--selected > .node__label { color: var(--brand); font-weight: 600; }
    .node__toggle { width: 26px; height: 26px; line-height: 26px; flex-shrink: 0; }
    .node__toggle--hidden { visibility: hidden; }
    .node__label {
      background: none; border: none; text-align: left; cursor: pointer; padding: 0.25rem 0.4rem;
      border-radius: var(--radius-sm); font: inherit; color: inherit; flex: 1;
    }
    .node__label:hover { background: var(--surface-2); }
    .node__page { margin-left: 0.4rem; font-size: 0.75rem; color: var(--text-muted); }
    .node__children { margin-left: 1.5rem; border-left: 1px dashed var(--border-subtle); padding-left: 0.4rem; }
  `],
})
export class StructureBrowserNodeComponent {
  @Input({ required: true }) node!: StructureNode;
  @Input() selectedId: number | null = null;
  @Output() select = new EventEmitter<StructureNode>();

  expanded = false;
  loadingChildren = false;
  children: StructureNode[] = [];

  constructor(private readonly structureService: StructureService) {}

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
