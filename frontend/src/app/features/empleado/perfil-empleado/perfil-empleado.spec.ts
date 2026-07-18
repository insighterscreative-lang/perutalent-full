import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PerfilEmpleado } from './perfil-empleado';

describe('PerfilEmpleado', () => {
  let component: PerfilEmpleado;
  let fixture: ComponentFixture<PerfilEmpleado>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PerfilEmpleado],
    }).compileComponents();

    fixture = TestBed.createComponent(PerfilEmpleado);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
