/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuthService);
    sessionStorage.clear();
    localStorage.clear();
  });

  afterEach(() => {
    sessionStorage.clear();
    localStorage.clear();
  });

  it('debe detectar usuario autenticado cuando existe token', () => {
    localStorage.setItem('cc_token', 'token_demo');

    expect(service.isLoggedIn()).toBeTrue();
    expect(service.getToken()).toBe('token_demo');
  });

  it('debe resolver rol ADMIN por prefijo de rol', () => {
    localStorage.setItem('cc_user', JSON.stringify({ rol: 'ADMINISTRADOR' }));

    expect(service.getRol()).toBe('ADMIN');
  });

  it('debe limpiar credenciales con logout', () => {
    sessionStorage.setItem('cc_token', 'abc');
    sessionStorage.setItem('cc_user', JSON.stringify({ username: 'u1' }));

    service.logout();

    expect(sessionStorage.getItem('cc_token')).toBeNull();
    expect(sessionStorage.getItem('cc_user')).toBeNull();
    expect(localStorage.getItem('cc_token')).toBeNull();
    expect(localStorage.getItem('cc_user')).toBeNull();
  });
});
