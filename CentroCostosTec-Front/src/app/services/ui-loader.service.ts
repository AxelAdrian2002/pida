import { Injectable } from '@angular/core';
import { BehaviorSubject, distinctUntilChanged, map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UiLoaderService {
  private readonly pendingRequests$ = new BehaviorSubject<number>(0);

  readonly loading$ = this.pendingRequests$.pipe(
    map((count) => count > 0),
    distinctUntilChanged()
  );

  show(): void {
    this.pendingRequests$.next(this.pendingRequests$.value + 1);
  }

  hide(): void {
    const nextValue = Math.max(0, this.pendingRequests$.value - 1);
    this.pendingRequests$.next(nextValue);
  }
}
