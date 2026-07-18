import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearPerfilEmpleador } from './crear-perfil-empleador';

describe('CrearPerfilEmpleador', () => {
  let component: CrearPerfilEmpleador;
  let fixture: ComponentFixture<CrearPerfilEmpleador>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearPerfilEmpleador],
    }).compileComponents();

    fixture = TestBed.createComponent(CrearPerfilEmpleador);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
