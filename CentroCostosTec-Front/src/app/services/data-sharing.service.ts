import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DataSharingService {
  private readonly storageKey = 'prefactura_request';
  private readonly requestSource = new BehaviorSubject<any>(this.readFromStorage());
  readonly request$ = this.requestSource.asObservable();

  enviarRequest(data: any): void {
    this.writeToStorage(data);
    this.requestSource.next(data);
  }

  obtenerRequestActual(): any {
    return this.requestSource.value;
  }

  limpiarRequest(): void {
    sessionStorage.removeItem(this.storageKey);
    this.requestSource.next(null);
  }

  private readFromStorage(): any {
    try {
      const raw = sessionStorage.getItem(this.storageKey);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  private writeToStorage(data: any): void {
    try {
      sessionStorage.setItem(this.storageKey, JSON.stringify(data));
    } catch {
      // Ignorar fallo de persistencia para no bloquear el flujo principal.
    }
  }
}
