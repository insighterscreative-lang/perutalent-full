import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditarPerfilEmpleado } from './editar-perfil-empleado';

describe('EditarPerfilEmpleado', () => {
  let component: EditarPerfilEmpleado;
  let fixture: ComponentFixture<EditarPerfilEmpleado>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditarPerfilEmpleado],
    }).compileComponents();

    fixture = TestBed.createComponent(EditarPerfilEmpleado);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
