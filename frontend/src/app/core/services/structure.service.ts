import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StructureNode } from '../models/structure.model';

@Injectable({ providedIn: 'root' })
export class StructureService {
  constructor(private readonly http: HttpClient) {}

  roots(bookId: number): Observable<StructureNode[]> {
    return this.http.get<StructureNode[]>(`/api/books/${bookId}/structure`);
  }

  children(nodeId: number): Observable<StructureNode[]> {
    return this.http.get<StructureNode[]>(`/api/structure-nodes/${nodeId}/children`);
  }

  get(nodeId: number): Observable<StructureNode> {
    return this.http.get<StructureNode>(`/api/structure-nodes/${nodeId}`);
  }
}
