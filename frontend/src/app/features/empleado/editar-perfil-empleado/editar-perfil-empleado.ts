import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { EmpleadoService } from 'src/app/core/services/empleado';
import { CatalogoService } from 'src/app/core/services/catalogo';

import { EmpleadoRequest, EmpleadoResponse } from 'src/app/core/models/empleado';
import { CatalogoItem } from 'src/app/core/models/catalogo';

@Component({
  selector: 'app-editar-perfil-empleado',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './editar-perfil-empleado.html',
  styleUrl: './editar-perfil-empleado.scss'
})
export class EditarPerfilEmpleadoComponent implements OnInit {

  cargando = true;
  guardando = false;

  mensajeError = '';
  mensajeExito = '';

  fechaMaximaNacimiento: string = this.calcularFechaMaximaNacimiento();

  idDepartamentoSeleccionado = 0;
  idProvinciaSeleccionada = 0;

  departamentos: CatalogoItem[] = [];
  provincias: CatalogoItem[] = [];
  distritos: CatalogoItem[] = [];

  categorias: CatalogoItem[] = [];
  habilidades: CatalogoItem[] = [];
  herramientas: CatalogoItem[] = [];
  modalidades: CatalogoItem[] = [];

  busquedaCategorias = '';
  busquedaHabilidades = '';
  busquedaHerramientas = '';
  busquedaModalidades = '';

  tiposDocumento = ['DNI', 'Carnet de extranjería', 'Pasaporte'];
  generos = ['Masculino', 'Femenino', 'Otro', 'Prefiero no decirlo'];
  nacionalidades = ['Peruana', 'Extranjera'];
  idiomasDisponibles = ['Español', 'Inglés', 'Portugués', 'Francés'];

  perfil: EmpleadoRequest = {
    nombre: '',
    apellido: '',
    tipoDoc: '',
    numDoc: '',
    fechaNacimiento: '',
    genero: '',
    telefono: '',
    idDistrito: 0,
    nacionalidad: '',
    descripcion: '',
    curriculum: '',
    fotoPerfil: '',
    idiomas: [],
    disponibilidadEquipo: '',
    habilidadesId: [],
    categoriasId: [],
    herramientasId: [],
    modalidadesId: []
  };

  constructor(
    private empleadoService: EmpleadoService,
    private catalogoService: CatalogoService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarCatalogosYPerfil();
  }

  cargarCatalogosYPerfil(): void {
    this.cargando = true;
    this.mensajeError = '';

    this.catalogoService.listarDepartamentos().subscribe({
      next: (departamentosRes) => {
        this.departamentos = departamentosRes.data;

        this.catalogoService.listarCategorias().subscribe({
          next: (categoriasRes) => {
            this.categorias = categoriasRes.data;

            this.catalogoService.listarHabilidades().subscribe({
              next: (habilidadesRes) => {
                this.habilidades = habilidadesRes.data;

                this.catalogoService.listarHerramientas().subscribe({
                  next: (herramientasRes) => {
                    this.herramientas = herramientasRes.data;

                    this.catalogoService.listarModalidades().subscribe({
                      next: (modalidadesRes) => {
                        this.modalidades = modalidadesRes.data;
                        this.cargarPerfil();
                      },
                      error: () => this.errorCatalogos()
                    });
                  },
                  error: () => this.errorCatalogos()
                });
              },
              error: () => this.errorCatalogos()
            });
          },
          error: () => this.errorCatalogos()
        });
      },
      error: () => this.errorCatalogos()
    });
  }

  private errorCatalogos(): void {
    this.cargando = false;
    this.mensajeError = 'No se pudieron cargar los catálogos';
    this.cdr.detectChanges();
  }

  cargarPerfil(): void {
    this.empleadoService.obtenerPerfil().subscribe({
      next: (response) => {
        this.mapearResponseAFormulario(response.data);
      },
      error: (error) => {
        this.cargando = false;

        if (error.status === 404) {
          this.router.navigate(['/empleado/crear-perfil']);
          this.cdr.detectChanges();
          return;
        }

        this.mensajeError = error?.error?.message || 'No se pudo cargar el perfil';
        this.cdr.detectChanges();
      }
    });
  }

  private mapearResponseAFormulario(data: EmpleadoResponse): void {
    this.idDepartamentoSeleccionado = data.idDepartamento || 0;
    this.idProvinciaSeleccionada = data.idProvincia || 0;

    this.perfil = {
      nombre: data.nombre || '',
      apellido: data.apellido || '',
      tipoDoc: data.tipoDoc || '',
      numDoc: data.numDoc || '',
      fechaNacimiento: data.fechaNacimiento || '',
      genero: data.genero || '',
      telefono: data.telefono || '',
      idDistrito: data.idDistrito || 0,
      nacionalidad: data.nacionalidad || '',
      descripcion: data.descripcion || '',
      curriculum: data.curriculum || '',
      fotoPerfil: data.fotoPerfil || '',
      idiomas: data.idiomas || [],
      disponibilidadEquipo: data.disponibilidadEquipo || '',
      habilidadesId: this.obtenerIdsPorNombres(this.habilidades, data.habilidades),
      categoriasId: this.obtenerIdsPorNombres(this.categorias, data.categorias),
      herramientasId: this.obtenerIdsPorNombres(this.herramientas, data.herramientas),
      modalidadesId: this.obtenerIdsPorNombres(this.modalidades, data.modalidades)
    };

    if (this.idDepartamentoSeleccionado) {
      this.catalogoService.listarProvincias(this.idDepartamentoSeleccionado).subscribe({
        next: (res) => {
          this.provincias = res.data;

          if (this.idProvinciaSeleccionada) {
            this.catalogoService.listarDistritos(this.idProvinciaSeleccionada).subscribe({
              next: (distritosRes) => {
                this.distritos = distritosRes.data;
                this.cargando = false;
                this.cdr.detectChanges();
              },
              error: () => {
                this.cargando = false;
                this.mensajeError = 'No se pudieron cargar los distritos';
                this.cdr.detectChanges();
              }
            });
          } else {
            this.cargando = false;
            this.cdr.detectChanges();
          }
        },
        error: () => {
          this.cargando = false;
          this.mensajeError = 'No se pudieron cargar las provincias';
          this.cdr.detectChanges();
        }
      });
    } else {
      this.cargando = false;
      this.cdr.detectChanges();
    }
  }

  cambiarDepartamento(): void {
    this.idProvinciaSeleccionada = 0;
    this.perfil.idDistrito = 0;

    this.provincias = [];
    this.distritos = [];

    if (!this.idDepartamentoSeleccionado) {
      return;
    }

    this.catalogoService.listarProvincias(this.idDepartamentoSeleccionado).subscribe({
      next: (res) => {
        this.provincias = res.data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.mensajeError = 'No se pudieron cargar las provincias';
        this.cdr.detectChanges();
      }
    });
  }

  cambiarProvincia(): void {
    this.perfil.idDistrito = 0;
    this.distritos = [];

    if (!this.idProvinciaSeleccionada) {
      return;
    }

    this.catalogoService.listarDistritos(this.idProvinciaSeleccionada).subscribe({
      next: (res) => {
        this.distritos = res.data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.mensajeError = 'No se pudieron cargar los distritos';
        this.cdr.detectChanges();
      }
    });
  }

  private obtenerIdsPorNombres(
    lista: CatalogoItem[],
    nombres: string[] | undefined
  ): number[] {
    if (!nombres) {
      return [];
    }

    return lista
      .filter(item => nombres.includes(item.nombre))
      .map(item => item.id);
  }

  filtrarCatalogo(lista: CatalogoItem[], texto: string): CatalogoItem[] {
    const termino = texto.trim().toLowerCase();

    if (!termino) {
      return lista;
    }

    return lista.filter(item =>
      item.nombre.toLowerCase().includes(termino)
    );
  }

  categoriasFiltradas(): CatalogoItem[] {
    return this.filtrarCatalogo(this.categorias, this.busquedaCategorias);
  }

  habilidadesFiltradas(): CatalogoItem[] {
    return this.filtrarCatalogo(this.habilidades, this.busquedaHabilidades);
  }

  herramientasFiltradas(): CatalogoItem[] {
    return this.filtrarCatalogo(this.herramientas, this.busquedaHerramientas);
  }

  modalidadesFiltradas(): CatalogoItem[] {
    return this.filtrarCatalogo(this.modalidades, this.busquedaModalidades);
  }

  toggleString(lista: string[] | undefined, valor: string): string[] {
    const actual = lista ?? [];

    if (actual.includes(valor)) {
      return actual.filter(item => item !== valor);
    }

    return [...actual, valor];
  }

  toggleNumber(lista: number[] | undefined, valor: number): number[] {
    const actual = lista ?? [];

    if (actual.includes(valor)) {
      return actual.filter(item => item !== valor);
    }

    return [...actual, valor];
  }

  estaSeleccionadoString(lista: string[] | undefined, valor: string): boolean {
    return lista?.includes(valor) ?? false;
  }

  estaSeleccionadoNumber(lista: number[] | undefined, valor: number): boolean {
    return lista?.includes(valor) ?? false;
  }

  cambiarIdioma(idioma: string): void {
    this.perfil.idiomas = this.toggleString(this.perfil.idiomas, idioma);
  }

  cambiarCategoria(id: number): void {
    this.perfil.categoriasId = this.toggleNumber(this.perfil.categoriasId, id);
  }

  cambiarHabilidad(id: number): void {
    this.perfil.habilidadesId = this.toggleNumber(this.perfil.habilidadesId, id);
  }

  cambiarHerramienta(id: number): void {
    this.perfil.herramientasId = this.toggleNumber(this.perfil.herramientasId, id);
  }

  cambiarModalidad(id: number): void {
    this.perfil.modalidadesId = this.toggleNumber(this.perfil.modalidadesId, id);
  }

  guardarCambios(): void {
    this.mensajeError = '';
    this.mensajeExito = '';

    this.limpiarCamposTexto();

    if (!this.validarFormulario()) {
      return;
    }

    this.guardando = true;

    this.empleadoService.editarPerfil(this.perfil).subscribe({
      next: () => {
        this.guardando = false;
        this.mensajeExito = 'Perfil actualizado exitosamente';

        this.cdr.detectChanges();

        setTimeout(() => {
          this.router.navigate(['/empleado/perfil']);
        }, 700);
      },
      error: (error) => {
        this.guardando = false;
        this.mensajeError = error?.error?.message || 'Error al actualizar el perfil';
        this.cdr.detectChanges();
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/empleado/perfil']);
  }

  cambiarTipoDocumento(): void {
    this.perfil.numDoc = '';
  }

  obtenerMaxLengthDocumento(): number {
    if (this.perfil.tipoDoc === 'DNI') {
      return 8;
    }

    if (this.perfil.tipoDoc === 'Carnet de extranjería') {
      return 12;
    }

    if (this.perfil.tipoDoc === 'Pasaporte') {
      return 12;
    }

    return 12;
  }

  obtenerPlaceholderDocumento(): string {
    if (this.perfil.tipoDoc === 'DNI') {
      return 'Ejemplo: 70123456';
    }

    if (this.perfil.tipoDoc === 'Carnet de extranjería') {
      return 'Ejemplo: 001234567';
    }

    if (this.perfil.tipoDoc === 'Pasaporte') {
      return 'Ejemplo: AB123456';
    }

    return 'Selecciona primero el tipo de documento';
  }

  private validarFormulario(): boolean {
    if (
      !this.perfil.nombre ||
      !this.perfil.apellido ||
      !this.perfil.tipoDoc ||
      !this.perfil.numDoc ||
      !this.perfil.fechaNacimiento ||
      !this.perfil.genero ||
      !this.perfil.telefono ||
      !this.idDepartamentoSeleccionado ||
      !this.idProvinciaSeleccionada ||
      !this.perfil.idDistrito ||
      !this.perfil.nacionalidad
    ) {
      this.mensajeError = 'Completa todos los campos obligatorios';
      return false;
    }

    if (!this.esMayorDeEdad(this.perfil.fechaNacimiento)) {
      this.mensajeError = 'Debes ser mayor de edad para tener un perfil de empleado';
      return false;
    }

    if (!this.validarDocumento()) {
      this.mensajeError = this.obtenerMensajeDocumentoInvalido();
      return false;
    }

    return true;
  }

  private validarDocumento(): boolean {
    const tipoDoc = this.perfil.tipoDoc;
    const numDoc = this.perfil.numDoc?.trim() || '';

    if (tipoDoc === 'DNI') {
      return /^[0-9]{8}$/.test(numDoc);
    }

    if (tipoDoc === 'Carnet de extranjería') {
      return /^[A-Za-z0-9]{9,12}$/.test(numDoc);
    }

    if (tipoDoc === 'Pasaporte') {
      return /^[A-Za-z0-9]{6,12}$/.test(numDoc);
    }

    return false;
  }

  private obtenerMensajeDocumentoInvalido(): string {
    if (this.perfil.tipoDoc === 'DNI') {
      return 'El DNI debe tener exactamente 8 dígitos numéricos';
    }

    if (this.perfil.tipoDoc === 'Carnet de extranjería') {
      return 'El carnet de extranjería debe tener entre 9 y 12 caracteres alfanuméricos';
    }

    if (this.perfil.tipoDoc === 'Pasaporte') {
      return 'El pasaporte debe tener entre 6 y 12 caracteres alfanuméricos';
    }

    return 'El tipo de documento no es válido';
  }

  private esMayorDeEdad(fechaNacimiento: string): boolean {
    if (!fechaNacimiento) {
      return false;
    }

    const nacimiento = new Date(`${fechaNacimiento}T00:00:00`);
    const hoy = new Date();

    let edad = hoy.getFullYear() - nacimiento.getFullYear();
    const diferenciaMes = hoy.getMonth() - nacimiento.getMonth();

    if (
      diferenciaMes < 0 ||
      (diferenciaMes === 0 && hoy.getDate() < nacimiento.getDate())
    ) {
      edad--;
    }

    return edad >= 18;
  }

  private calcularFechaMaximaNacimiento(): string {
    const hoy = new Date();

    const fechaMaxima = new Date(
      hoy.getFullYear() - 18,
      hoy.getMonth(),
      hoy.getDate()
    );

    return fechaMaxima.toISOString().split('T')[0];
  }

  private limpiarCamposTexto(): void {
    this.perfil.nombre = this.perfil.nombre.trim();
    this.perfil.apellido = this.perfil.apellido.trim();
    this.perfil.numDoc = this.perfil.numDoc.trim();
    this.perfil.telefono = this.perfil.telefono.trim();
  }
}