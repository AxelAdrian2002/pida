import { FormBuilder } from '@angular/forms';

import { RegistroEmpresaComponent } from './registro-empresa.component';

describe('RegistroEmpresaComponent - validaciones de formulario', () => {
  it('debe marcar municipio y colonia como obligatorios', () => {
    const httpMock = jasmine.createSpyObj('HttpClient', ['get', 'post']);
    const routerMock = jasmine.createSpyObj('Router', ['navigate']);
    const component = new RegistroEmpresaComponent(new FormBuilder(), httpMock, routerMock);

    component.form.patchValue({
      nombreEmpresa: 'Empresa Test',
      rfc: 'RFC123456789',
      adminNombre: 'Admin Test',
      adminEmail: 'admin@test.com',
      adminPassword: '123456',
      municipio: '',
      colonia: ''
    });

    expect(component.form.get('municipio')?.valid).toBeFalse();
    expect(component.form.get('colonia')?.valid).toBeFalse();

    component.form.patchValue({
      municipio: 'Benito Juarez',
      colonia: 'Del Valle'
    });

    expect(component.form.get('municipio')?.valid).toBeTrue();
    expect(component.form.get('colonia')?.valid).toBeTrue();
  });
});
