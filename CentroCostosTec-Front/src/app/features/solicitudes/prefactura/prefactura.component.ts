import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SolicitudService } from '../../../services/solicitud.service';
import { DataSharingService } from '../../../services/data-sharing.service';

@Component({
  selector: 'app-prefactura',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './prefactura.component.html',
  styleUrls: ['./prefactura.component.css']
})
export class PrefacturaComponent implements OnInit {
  pedido: any = null;
  tipoPedido: 'dispersion' | 'stock' | 'tarjeta' | 'adicional' = 'dispersion';
  loading = false;
  guardado = false;
  exitoMsg = '';
  errorMsg = '';
  condicionBandera = false;
  hoy = new Date();
  postGuardado = false;

  constructor(
    private readonly dataSharingService: DataSharingService,
    private readonly SolicitudService: SolicitudService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      const tipo = (params.get('tipo') || '').toLowerCase();
      if (tipo === 'stock' || tipo === 'tarjeta' || tipo === 'adicional' || tipo === 'dispersion') {
        this.tipoPedido = tipo;
      }
    });

    const inicial = this.dataSharingService.obtenerRequestActual();
    if (inicial) {
      this.pedido = typeof inicial === 'string' ? JSON.parse(inicial) : inicial;
    }

    this.dataSharingService.request$.subscribe(data => {
      if (data) {
        this.pedido = typeof data === 'string' ? JSON.parse(data) : data;
      }
    });
  }

  guardar(): void {
    if (!this.pedido) {
      this.errorMsg = 'No hay prefactura para guardar.';
      return;
    }

    if (this.pedido?.pedidoCliente && !this.condicionBandera) {
      this.errorMsg = 'Debes aceptar las condiciones para continuar.';
      return;
    }

    this.loading = true;
    this.exitoMsg = '';
    this.errorMsg = '';

    const montoResuelto = this.resolverMontoPedido(this.pedido);
    const payload = {
      ...this.pedido,
      montoTotal: montoResuelto
    };

    this.SolicitudService.guardarPedido(this.tipoPedido, payload).subscribe({
      next: (res: any) => {
        const pedidoId = Number((res.datos as any)?.idPedido ?? (res.datos as any)?.pedidoid ?? 0);
        const prefacturaId = Number(this.pedido?.prefactura?.prefacturaid ?? this.pedido?.prefacturaId ?? 0);

        this.SolicitudService.recuperarVistaPrefacturaGuardado(
          Number.isFinite(pedidoId) && pedidoId > 0 ? pedidoId : undefined,
          Number.isFinite(prefacturaId) && prefacturaId > 0 ? prefacturaId : undefined
        ).subscribe({
          next: (vistaRes) => {
            this.loading = false;
            this.guardado = true;
            this.postGuardado = true;

            const vista = this.SolicitudService.normalizarPayloadPrefactura(vistaRes?.datos);
            this.pedido = vista || this.pedido;
            this.dataSharingService.enviarRequest(this.pedido);
            this.exitoMsg = `Pedido guardado. ID: ${pedidoId || (this.pedido?.pedidoid ?? '-')}`;
          },
          error: () => {
            // Si falla la recuperación, mostramos al menos el ID guardado y conservamos la prefactura actual.
            this.loading = false;
            this.guardado = true;
            this.postGuardado = true;
            this.pedido = {
              ...this.pedido,
              pedidoid: pedidoId > 0 ? pedidoId : this.pedido?.pedidoid ?? 0
            };
            this.dataSharingService.enviarRequest(this.pedido);
            this.exitoMsg = `Pedido guardado. ID: ${pedidoId || (this.pedido?.pedidoid ?? '-')}`;
          }
        });
      },
      error: (err: any) => {
        this.loading = false;
        this.errorMsg = err.error?.mensaje || 'Error al guardar pedido.';
      }
    });
  }

  cancelar(): void {
    this.volver();
  }

  aceptar(): void {
    this.volver();
  }

  imprimir(): void {
    window.print();
  }

  volver(): void {
    this.router.navigate(['/solicitudes']);
  }

  resolverMontoPedido(pedido: any): number {
    const raw =
      pedido?.montoTotal ??
      pedido?.prefactura?.total ??
      pedido?.total ??
      0;

    const parsed = Number(raw);
    return Number.isFinite(parsed) ? parsed : 0;
  }
}


