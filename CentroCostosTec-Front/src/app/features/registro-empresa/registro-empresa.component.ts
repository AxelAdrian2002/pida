import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import * as L from 'leaflet';
import { ESTADOS_MX, MUNICIPIOS_POR_ESTADO } from './direccion-catalogo';
import { CP_COLONIAS_POR_CP, CP_ESTADO_POR_CP } from './direccion-cp-catalogo';

const ALCALDIAS_CDMX: string[] = [
  'Alvaro Obregon',
  'Azcapotzalco',
  'Benito Juarez',
  'Coyoacan',
  'Cuajimalpa de Morelos',
  'Cuauhtemoc',
  'Gustavo A. Madero',
  'Iztacalco',
  'Iztapalapa',
  'La Magdalena Contreras',
  'Miguel Hidalgo',
  'Milpa Alta',
  'Tlahuac',
  'Tlalpan',
  'Venustiano Carranza',
  'Xochimilco'
];

@Component({
  selector: 'app-registro-empresa',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
  <div class="registro-bg d-flex justify-content-center align-items-start py-5 min-vh-100">
    <div class="card shadow-lg p-4" style="width: 780px; max-width: 98vw;">

      <!-- Encabezado -->
      <div class="text-center mb-4">
        <h4 class="fw-bold text-primary">Registro de Empresa</h4>
        <p class="text-muted small mb-0">Completa los datos para crear tu cuenta en la plataforma</p>
      </div>

      <!-- Éxito -->
      <div *ngIf="registroExitoso" class="alert alert-success text-center py-4">
        <i class="bi bi-check-circle-fill fs-2 text-success d-block mb-2"></i>
        <h5>¡Empresa registrada exitosamente!</h5>
        <p class="mb-1">Tu corporativo es: <strong>{{ codigoEmpresaCreada }}</strong></p>
        <p class="mb-3 text-muted small">Hemos enviado tus datos de acceso al correo registrado.</p>
        <a routerLink="/login" class="btn btn-primary">Ir al Login</a>
      </div>

      <form *ngIf="!registroExitoso" [formGroup]="form" (ngSubmit)="onSubmit()">

        <!-- Sección: Datos de la empresa -->
        <h6 class="text-secondary border-bottom pb-1 mb-3 mt-2">Datos de la Empresa</h6>
        <div class="row g-3">
          <div class="col-md-8">
            <label class="form-label">Nombre de la empresa <span class="text-danger">*</span></label>
            <input type="text" class="form-control" formControlName="nombreEmpresa" placeholder="Ej. Soluciones ABC S.A. de C.V.">
            <div class="text-danger small" *ngIf="f['nombreEmpresa'].invalid && submitted">Campo obligatorio</div>
          </div>
          <div class="col-md-4">
            <label class="form-label">RFC <span class="text-danger">*</span></label>
            <input type="text" class="form-control text-uppercase" formControlName="rfc" placeholder="Ej. SABC850101XY2" maxlength="20">
            <div class="text-danger small" *ngIf="f['rfc'].invalid && submitted">Campo obligatorio</div>
          </div>
          <div class="col-md-8">
            <label class="form-label">Razón Social</label>
            <input type="text" class="form-control" formControlName="razonSocial" placeholder="Razón social completa">
          </div>
          <div class="col-md-4">
            <label class="form-label">Sitio Web</label>
            <input type="text" class="form-control" formControlName="sitioWeb" placeholder="https://miweb.com">
          </div>
          <div class="col-md-6">
            <label class="form-label">Email de contacto</label>
            <input type="email" class="form-control" formControlName="emailEmpresa" placeholder="contacto@empresa.com">
          </div>
          <div class="col-md-6">
            <label class="form-label">Teléfono</label>
            <input type="text" class="form-control" formControlName="telefonoEmpresa" placeholder="55 1234 5678">
          </div>
        </div>

        <!-- Sección: Identidad visual -->
        <h6 class="text-secondary border-bottom pb-1 mb-3 mt-4">Identidad Visual</h6>
        <div class="row g-3 align-items-end">
          <div class="col-md-3">
            <label class="form-label">Color primario</label>
            <div class="d-flex align-items-center gap-2">
              <input type="color" class="form-control form-control-color" formControlName="colorPrimario" style="width:50px;height:38px;">
              <input type="text" class="form-control form-control-sm font-monospace" formControlName="colorPrimario" maxlength="20" placeholder="#1a73e8">
            </div>
          </div>
          <div class="col-md-3">
            <label class="form-label">Color secundario</label>
            <div class="d-flex align-items-center gap-2">
              <input type="color" class="form-control form-control-color" formControlName="colorSecundario" style="width:50px;height:38px;">
              <input type="text" class="form-control form-control-sm font-monospace" formControlName="colorSecundario" maxlength="20" placeholder="#fbbc04">
            </div>
          </div>
          <div class="col-md-6">
            <label class="form-label">Logo de la empresa</label>
            <div class="d-flex gap-2 align-items-center">
              <input type="file" class="form-control" accept="image/*" (change)="onLogoSelected($event)">
              <button type="button" class="btn btn-outline-primary" (click)="subirLogoSeleccionado()" [disabled]="!logoFile || subiendoLogo">
                {{ subiendoLogo ? 'Subiendo...' : 'Subir logo' }}
              </button>
            </div>
            <div class="form-text" *ngIf="logoSubidoMsg">{{ logoSubidoMsg }}</div>
          </div>
          <!-- Preview de colores -->
          <div class="col-12">
            <div class="rounded p-3 d-flex align-items-center gap-3"
                 [style.background]="form.get('colorPrimario')?.value || '#1a73e8'">
              <div class="rounded-circle"
                   [style.background]="form.get('colorSecundario')?.value || '#fbbc04'"
                   style="width:40px;height:40px;"></div>
              <img *ngIf="form.get('logoUrl')?.value" [src]="form.get('logoUrl')?.value"
                   style="height:40px;object-fit:contain;" alt="Logo">
              <span class="text-white fw-bold">{{ form.get('nombreEmpresa')?.value || 'Vista previa' }}</span>
            </div>
          </div>
        </div>

        <!-- Sección: Dirección -->
        <h6 class="text-secondary border-bottom pb-1 mb-3 mt-4">Dirección</h6>
        <div class="row g-3">
          <div class="col-md-4">
            <label class="form-label">Estado</label>
            <select class="form-select" formControlName="estado">
              <option value="">Selecciona un estado</option>
              <option *ngFor="let estadoItem of estadosMx" [value]="estadoItem">{{ estadoItem }}</option>
            </select>
          </div>
          <div class="col-md-4">
            <label class="form-label">{{ municipioLabel }}</label>
            <ng-container *ngIf="municipiosDisponibles.length > 0; else municipioLibre">
              <select class="form-select" formControlName="municipio">
                <option value="">Selecciona {{ municipioLabel.toLowerCase() }}</option>
                <option *ngFor="let municipioItem of municipiosDisponibles" [value]="municipioItem">{{ municipioItem }}</option>
              </select>
            </ng-container>
            <ng-template #municipioLibre>
              <input type="text" class="form-control" formControlName="municipio" [placeholder]="municipioLabel + ' (captura manual)'">
            </ng-template>
          </div>
          <div class="col-md-4">
            <label class="form-label">Código postal</label>
            <input type="text" class="form-control" formControlName="codigoPostal" maxlength="5" placeholder="Ej. 03100">
            <div class="text-danger small" *ngIf="f['codigoPostal'].invalid && f['codigoPostal'].touched">Debe tener 5 dígitos</div>
            <div class="text-warning small" *ngIf="cpNoCoincideConEstado">
              El codigo postal no corresponde al estado seleccionado. Para este CP, el estado esperado es: {{ estadoEsperadoPorCp }}.
            </div>
          </div>
          <div class="col-md-6">
            <label class="form-label">Colonia</label>
            <ng-container *ngIf="coloniasDisponibles.length > 0; else coloniaLibre">
              <select class="form-select" formControlName="colonia">
                <option value="">Selecciona colonia</option>
                <option *ngFor="let coloniaItem of coloniasDisponibles" [value]="coloniaItem">{{ coloniaItem }}</option>
              </select>
              <div class="form-text">Colonias sugeridas según el código postal.</div>
            </ng-container>
            <ng-template #coloniaLibre>
              <input type="text" class="form-control" formControlName="colonia" placeholder="Colonia (captura manual)">
              <div class="form-text">Si no aparece una lista, captura la colonia manualmente.</div>
            </ng-template>
          </div>
          <div class="col-md-6">
            <label class="form-label">País</label>
            <input type="text" class="form-control" formControlName="pais" placeholder="México">
          </div>
          <div class="col-md-6">
            <label class="form-label">Calle</label>
            <input type="text" class="form-control" formControlName="calle">
          </div>
          <div class="col-md-3">
            <label class="form-label">Núm. exterior</label>
            <input type="text" class="form-control" formControlName="numeroExterior">
          </div>
          <div class="col-md-3">
            <label class="form-label">Núm. interior</label>
            <input type="text" class="form-control" formControlName="numeroInterior">
          </div>
          <div class="col-12 d-flex justify-content-between align-items-center">
            <small class="text-muted">Selecciona un punto en el mapa para autocompletar dirección o escribe dirección para ubicarla.</small>
            <button type="button" class="btn btn-sm btn-outline-secondary" (click)="onMapaBuscarDesdeCampos()">Ubicar en mapa</button>
          </div>
          <div class="col-12">
            <div #mapContainer class="registro-map"></div>
          </div>
        </div>

        <!-- Sección: Administrador -->
        <h6 class="text-secondary border-bottom pb-1 mb-3 mt-4">Cuenta del Administrador</h6>
        <div class="row g-3">
          <div class="col-md-12">
            <label class="form-label">Nombre completo <span class="text-danger">*</span></label>
            <input type="text" class="form-control" formControlName="adminNombre" placeholder="Nombre del administrador de la empresa">
            <div class="text-danger small" *ngIf="f['adminNombre'].invalid && submitted">Campo obligatorio</div>
          </div>
          <div class="col-md-6">
            <label class="form-label">Correo del administrador <span class="text-danger">*</span></label>
            <input type="email" class="form-control" formControlName="adminEmail" placeholder="admin@miempresa.com">
            <div class="text-danger small" *ngIf="f['adminEmail'].invalid && submitted">Correo válido obligatorio</div>
          </div>
          <div class="col-md-6">
            <label class="form-label">Contraseña <span class="text-danger">*</span></label>
            <input type="password" class="form-control" formControlName="adminPassword" placeholder="Mínimo 6 caracteres" autocomplete="new-password">
            <div class="text-danger small" *ngIf="f['adminPassword'].invalid && submitted">Mínimo 6 caracteres</div>
          </div>
        </div>

        <!-- Error global -->
        <div class="alert alert-danger py-2 mt-3" *ngIf="errorMsg">{{ errorMsg }}</div>

        <!-- Botones -->
        <div class="d-flex justify-content-between align-items-center mt-4 pt-2 border-top">
          <a routerLink="/login" class="text-muted small text-decoration-none">
            ← Volver al login
          </a>
          <button type="submit" class="btn btn-primary px-4" [disabled]="loading">
            <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
            Registrar Empresa
          </button>
        </div>

      </form>
    </div>
  </div>
  `,
  styles: [`
    .registro-bg {
      background: linear-gradient(135deg, #f0f4ff 0%, #e8f5e9 100%);
    }

    .registro-map {
      width: 100%;
      height: 320px;
      border: 1px solid #ced4da;
      border-radius: .75rem;
      overflow: hidden;
    }
  `]
})
export class RegistroEmpresaComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('mapContainer') mapContainerRef?: ElementRef<HTMLDivElement>;
  form: FormGroup;
  loading = false;
  submitted = false;
  errorMsg = '';
  logoSubidoMsg = '';
  logoFile: File | null = null;
  subiendoLogo = false;
  registroExitoso = false;
  codigoEmpresaCreada = '';
  readonly estadosMx = ESTADOS_MX;
  municipiosDisponibles: string[] = [];
  coloniasDisponibles: string[] = [];
  municipioLabel = 'Municipio';
  private coloniasPorCp: Record<string, string[]> = {};
  private estadoPorCp: Record<string, string> = {};
  private map?: L.Map;
  private marker?: L.CircleMarker;
  private geocodeTimer: ReturnType<typeof setTimeout> | null = null;
  private mapReady = false;
  private blockFieldDrivenGeocode = false;
  private readonly initialCoords: [number, number] = [19.4326, -99.1332];
  cpNoCoincideConEstado = false;
  estadoEsperadoPorCp = '';

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.form = this.fb.group({
      // Empresa
      nombreEmpresa:    ['', [Validators.required, Validators.maxLength(180)]],
      rfc:              ['', [Validators.required, Validators.maxLength(20)]],
      razonSocial:      [''],
      colorPrimario:    ['#1a73e8'],
      colorSecundario:  ['#fbbc04'],
      logoUrl:          [''],
      emailEmpresa:     ['', [Validators.email]],
      telefonoEmpresa:  [''],
      sitioWeb:         [''],
      // Dirección
      calle:            [''],
      numeroExterior:   [''],
      numeroInterior:   [''],
      colonia:          [''],
      municipio:        [''],
      estado:           [''],
      pais:             ['México'],
      codigoPostal:     ['', [Validators.maxLength(5), Validators.pattern(/^$|^\d{5}$/)]],
      // Admin
      adminNombre:      ['', Validators.required],
      adminEmail:       ['', [Validators.required, Validators.email]],
      adminPassword:    ['', [Validators.required, Validators.minLength(6)]]
    });

    this.form.get('estado')?.valueChanges.subscribe((estado) => {
      this.actualizarMunicipios(String(estado ?? ''));
      this.actualizarColoniasPorCp(String(this.form.get('codigoPostal')?.value ?? ''));
    });

    this.form.get('codigoPostal')?.valueChanges.subscribe((cp) => {
      this.actualizarColoniasPorCp(String(cp ?? ''));
    });

    [
      'calle',
      'numeroExterior',
      'colonia',
      'municipio',
      'estado',
      'codigoPostal',
      'pais'
    ].forEach(campo => {
      this.form.get(campo)?.valueChanges.subscribe(() => {
        if (!this.blockFieldDrivenGeocode) {
          this.programarGeocodificacionDesdeCampos();
        }
      });
    });

    this.actualizarMunicipios(String(this.form.get('estado')?.value ?? ''));
  }

  ngOnInit(): void {
    this.coloniasPorCp = CP_COLONIAS_POR_CP;
    this.estadoPorCp = CP_ESTADO_POR_CP;
    this.actualizarColoniasPorCp(String(this.form.get('codigoPostal')?.value ?? ''));
  }

  ngAfterViewInit(): void {
    this.inicializarMapa();
  }

  ngOnDestroy(): void {
    if (this.geocodeTimer) {
      clearTimeout(this.geocodeTimer);
    }
    if (this.map) {
      this.map.remove();
    }
  }

  get f() { return this.form.controls; }

  private actualizarMunicipios(estado: string): void {
    this.municipioLabel = estado === 'Ciudad de Mexico' ? 'Alcaldia' : 'Municipio';
    if (estado === 'Ciudad de Mexico') {
      this.municipiosDisponibles = ALCALDIAS_CDMX;
    } else {
      this.municipiosDisponibles = MUNICIPIOS_POR_ESTADO[estado] ?? [];
    }

    const municipioActual = String(this.form.get('municipio')?.value ?? '').trim();
    if (municipioActual && this.municipiosDisponibles.length > 0 && !this.municipiosDisponibles.includes(municipioActual)) {
      this.form.patchValue({ municipio: '' }, { emitEvent: false });
    }
  }

  private actualizarColoniasPorCp(codigoPostal: string): void {
    const cpNormalizado = codigoPostal.replace(/\D/g, '').slice(0, 5);
    if (codigoPostal !== cpNormalizado) {
      this.form.patchValue({ codigoPostal: cpNormalizado }, { emitEvent: false });
    }

    if (cpNormalizado.length < 5) {
      this.coloniasDisponibles = [];
      this.cpNoCoincideConEstado = false;
      this.estadoEsperadoPorCp = '';
      return;
    }

    this.coloniasDisponibles = this.coloniasPorCp[cpNormalizado] ?? [];
    if (this.coloniasDisponibles.length === 0) {
      const cpSinCeros = String(Number(cpNormalizado));
      this.coloniasDisponibles = this.coloniasPorCp[cpSinCeros] ?? [];
    }

    const estadoSeleccionado = String(this.form.get('estado')?.value ?? '').trim();
    const estadoEsperado = String(this.estadoPorCp[cpNormalizado] ?? '').trim();
    this.estadoEsperadoPorCp = estadoEsperado;
    this.cpNoCoincideConEstado = !!estadoSeleccionado && !!estadoEsperado && estadoSeleccionado !== estadoEsperado;

    const coloniaActual = String(this.form.get('colonia')?.value ?? '').trim();
    if (coloniaActual && this.coloniasDisponibles.length > 0 && !this.coloniasDisponibles.includes(coloniaActual)) {
      this.form.patchValue({ colonia: '' }, { emitEvent: false });
    }
  }

  private inicializarMapa(): void {
    if (!this.mapContainerRef || this.mapReady) {
      return;
    }

    this.map = L.map(this.mapContainerRef.nativeElement, {
      zoomControl: true
    }).setView(this.initialCoords, 5);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);

    this.marker = L.circleMarker(this.initialCoords, {
      radius: 8,
      color: '#0d6efd',
      fillColor: '#0d6efd',
      fillOpacity: 0.65,
      weight: 2
    }).addTo(this.map);

    this.map.on('click', (e: L.LeafletMouseEvent) => {
      const lat = Number(e.latlng.lat.toFixed(6));
      const lng = Number(e.latlng.lng.toFixed(6));
      this.moverMarcador(lat, lng, true);
      this.reverseGeocode(lat, lng);
    });

    this.mapReady = true;
    this.programarGeocodificacionDesdeCampos();
  }

  private moverMarcador(lat: number, lng: number, centrar: boolean): void {
    if (!this.map || !this.marker) {
      return;
    }

    this.marker.setLatLng([lat, lng]);
    if (centrar) {
      this.map.setView([lat, lng], Math.max(this.map.getZoom(), 16));
    }
  }

  private programarGeocodificacionDesdeCampos(): void {
    if (!this.mapReady) {
      return;
    }

    const texto = this.construirDireccionLibre();
    if (texto.length < 8) {
      return;
    }

    if (this.geocodeTimer) {
      clearTimeout(this.geocodeTimer);
    }

    this.geocodeTimer = setTimeout(() => {
      this.geocodificarDireccion(texto);
    }, 700);
  }

  private construirDireccionLibre(): string {
    const calle = String(this.form.get('calle')?.value || '').trim();
    const numeroExterior = String(this.form.get('numeroExterior')?.value || '').trim();
    const colonia = String(this.form.get('colonia')?.value || '').trim();
    const municipio = String(this.form.get('municipio')?.value || '').trim();
    const estado = String(this.form.get('estado')?.value || '').trim();
    const codigoPostal = String(this.form.get('codigoPostal')?.value || '').trim();
    const pais = String(this.form.get('pais')?.value || '').trim() || 'Mexico';

    return [
      `${calle} ${numeroExterior}`.trim(),
      colonia,
      municipio,
      estado,
      codigoPostal,
      pais
    ].filter(Boolean).join(', ');
  }

  private geocodificarDireccion(query: string): void {
    this.http.get<any[]>('https://nominatim.openstreetmap.org/search', {
      params: {
        q: query,
        format: 'jsonv2',
        addressdetails: '1',
        countrycodes: 'mx',
        limit: '1'
      }
    }).subscribe({
      next: res => {
        if (!Array.isArray(res) || res.length === 0) {
          return;
        }

        const first = res[0];
        const lat = Number(first?.lat);
        const lng = Number(first?.lon);
        if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
          return;
        }

        this.moverMarcador(lat, lng, true);
      }
    });
  }

  private reverseGeocode(lat: number, lng: number): void {
    this.http.get<any>('https://nominatim.openstreetmap.org/reverse', {
      params: {
        lat: String(lat),
        lon: String(lng),
        format: 'jsonv2',
        addressdetails: '1'
      }
    }).subscribe({
      next: res => {
        const address = res?.address || {};
        const patch: Record<string, string> = {};

        const road = String(address.road || '').trim();
        const houseNumber = String(address.house_number || '').trim();
        const suburb = String(address.suburb || address.neighbourhood || '').trim();
        const city = String(address.city || address.town || address.village || address.municipality || '').trim();
        const state = String(address.state || '').trim();
        const postcode = String(address.postcode || '').trim();
        const country = String(address.country || 'Mexico').trim();

        if (road) patch['calle'] = road;
        if (houseNumber) patch['numeroExterior'] = houseNumber;
        if (suburb) patch['colonia'] = suburb;
        if (city) patch['municipio'] = city;
        if (state) patch['estado'] = state;
        if (postcode) patch['codigoPostal'] = postcode.replace(/\D/g, '').slice(0, 5);
        if (country) patch['pais'] = country;

        this.blockFieldDrivenGeocode = true;
        this.form.patchValue(patch);
        this.blockFieldDrivenGeocode = false;

        this.actualizarMunicipios(String(this.form.get('estado')?.value ?? ''));
        this.actualizarColoniasPorCp(String(this.form.get('codigoPostal')?.value ?? ''));
      }
    });
  }

  onSubmit(): void {
    this.submitted = true;
    this.errorMsg = '';
    if (this.form.invalid) return;
    this.loading = true;

    this.http.post<any>('/api/auth/registrar-empresa', this.form.value).subscribe({
      next: (res) => {
        this.codigoEmpresaCreada = res?.datos?.codigoEmpresa ?? '';
        this.registroExitoso = true;
        this.loading = false;
      },
      error: (err) => {
        this.errorMsg = err?.error?.mensaje ?? err?.error?.message ?? 'Error al registrar la empresa. Intenta de nuevo.';
        this.loading = false;
      }
    });
  }

  onLogoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.logoFile = input.files && input.files.length > 0 ? input.files[0] : null;
    this.logoSubidoMsg = '';
  }

  subirLogoSeleccionado(): void {
    if (!this.logoFile) {
      return;
    }

    const formData = new FormData();
    formData.append('file', this.logoFile);
    this.subiendoLogo = true;
    this.logoSubidoMsg = '';

    this.http.post<any>('/api/auth/media/upload', formData).subscribe({
      next: (res) => {
        const url = String(res?.datos?.url || '');
        if (url) {
          this.form.patchValue({ logoUrl: url });
          this.logoSubidoMsg = 'Logo subido correctamente.';
        } else {
          this.errorMsg = 'No se recibió la URL del logo.';
        }
      },
      error: (err) => {
        this.errorMsg = err?.error?.mensaje ?? 'No fue posible subir el logo.';
      },
      complete: () => {
        this.subiendoLogo = false;
      }
    });
  }

  onMapaBuscarDesdeCampos(): void {
    this.programarGeocodificacionDesdeCampos();
  }
}
