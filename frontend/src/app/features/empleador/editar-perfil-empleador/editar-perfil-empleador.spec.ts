import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditarPerfilEmpleador } from './editar-perfil-empleador';

describe('EditarPerfilEmpleador', () => {
  let component: EditarPerfilEmpleador;
  let fixture: ComponentFixture<EditarPerfilEmpleador>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditarPerfilEmpleador],
    }).compileComponents();

    fixture = TestBed.createComponent(EditarPerfilEmpleador);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
