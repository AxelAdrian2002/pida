import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, DatabaseStatusPayload } from '../models/models';

@Injectable({ providedIn: 'root' })
export class DatabaseStatusService {
  private readonly API = '/api/bases';

  constructor(private http: HttpClient) {}

  getStatus(): Observable<ApiResponse<DatabaseStatusPayload>> {
    return this.http.get<ApiResponse<DatabaseStatusPayload>>(`${this.API}/status`);
  }
}
