import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearPerfilEmpleado } from './crear-perfil-empleado';

describe('CrearPerfilEmpleado', () => {
  let component: CrearPerfilEmpleado;
  let fixture: ComponentFixture<CrearPerfilEmpleado>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearPerfilEmpleado],
    }).compileComponents();

    fixture = TestBed.createComponent(CrearPerfilEmpleado);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
