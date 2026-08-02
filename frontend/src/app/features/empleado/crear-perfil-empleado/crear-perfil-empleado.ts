import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { EmpleadoService } from 'src/app/core/services/empleado';
import { CatalogoService } from 'src/app/core/services/catalogo';

import { EmpleadoRequest } from 'src/app/core/models/empleado';
import { CatalogoItem } from 'src/app/core/models/catalogo';

@Component({
  selector: 'app-crear-perfil-empleado',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crear-perfil-empleado.html',
  styleUrl: './crear-perfil-empleado.scss'
})
export class CrearPerfilEmpleadoComponent implements OnInit {

  cargando = false;
  cargandoCatalogos = false;

  mensajeError = '';
  mensajeExito = '';

  cvSeleccionado: File | null = null;
  nombreCvSeleccionado = '';

  fotoPerfilSeleccionada: File | null = null;
  nombreFotoPerfilSeleccionada = '';
  previewFotoPerfil = '';

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
    this.cargarCatalogosIniciales();
  }

  cargarCatalogosIniciales(): void {
    this.cargandoCatalogos = true;
    this.mensajeError = '';

    this.catalogoService.listarDepartamentos().subscribe({
      next: (res) => {
        this.departamentos = res.data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.mensajeError = 'No se pudieron cargar los departamentos';
        this.cargandoCatalogos = false;
        this.cdr.detectChanges();
      }
    });

    this.catalogoService.listarCategorias().subscribe({
      next: (res) => {
        this.categorias = res.data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.mensajeError = 'No se pudieron cargar las categorías';
        this.cargandoCatalogos = false;
        this.cdr.detectChanges();
      }
    });

    this.catalogoService.listarHabilidades().subscribe({
      next: (res) => {
        this.habilidades = res.data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.mensajeError = 'No se pudieron cargar las habilidades';
        this.cargandoCatalogos = false;
        this.cdr.detectChanges();
      }
    });

    this.catalogoService.listarHerramientas().subscribe({
      next: (res) => {
        this.herramientas = res.data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.mensajeError = 'No se pudieron cargar las herramientas';
        this.cargandoCatalogos = false;
        this.cdr.detectChanges();
      }
    });

    this.catalogoService.listarModalidades().subscribe({
      next: (res) => {
        this.modalidades = res.data;
        this.cargandoCatalogos = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.mensajeError = 'No se pudieron cargar las modalidades';
        this.cargandoCatalogos = false;
        this.cdr.detectChanges();
      }
    });
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

  crearPerfil(): void {
    this.mensajeError = '';
    this.mensajeExito = '';

    this.limpiarCamposTexto();

    if (!this.validarFormulario()) {
      return;
    }

    this.cargando = true;

    this.empleadoService.crearPerfil(this.perfil, this.cvSeleccionado, this.fotoPerfilSeleccionada).subscribe({
      next: () => {
        this.cargando = false;
        this.mensajeExito = 'Perfil creado exitosamente';

        this.cdr.detectChanges();

        setTimeout(() => {
          this.router.navigate(['/empleado/perfil']);
        }, 800);
      },
      error: (error) => {
        this.cargando = false;
        this.mensajeError = error?.error?.message || 'Error al crear el perfil';
        this.cdr.detectChanges();
      }
    });
  }


  onCvSeleccionado(event: Event): void {
    this.mensajeError = '';

    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0] ?? null;

    if (!archivo) {
      this.cvSeleccionado = null;
      this.nombreCvSeleccionado = '';
      return;
    }

    const esPdf = archivo.type === 'application/pdf'
      || archivo.name.toLowerCase().endsWith('.pdf');

    if (!esPdf) {
      this.cvSeleccionado = null;
      this.nombreCvSeleccionado = '';
      this.mensajeError = 'El CV debe ser un archivo PDF';
      input.value = '';
      this.cdr.detectChanges();
      return;
    }

    const maxSizeBytes = 5 * 1024 * 1024;

    if (archivo.size > maxSizeBytes) {
      this.cvSeleccionado = null;
      this.nombreCvSeleccionado = '';
      this.mensajeError = 'El CV no debe superar los 5 MB';
      input.value = '';
      this.cdr.detectChanges();
      return;
    }

    this.cvSeleccionado = archivo;
    this.nombreCvSeleccionado = archivo.name;
    this.cdr.detectChanges();
  }

  quitarCvSeleccionado(): void {
    this.cvSeleccionado = null;
    this.nombreCvSeleccionado = '';
  }

  onFotoPerfilSeleccionada(event: Event): void {
    this.mensajeError = '';

    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0] ?? null;

    if (!archivo) {
      this.fotoPerfilSeleccionada = null;
      this.nombreFotoPerfilSeleccionada = '';
      this.previewFotoPerfil = '';
      return;
    }

    const nombre = archivo.name.toLowerCase();
    const tipo = archivo.type;

    const esImagenValida = tipo === 'image/jpeg'
      || tipo === 'image/jpg'
      || tipo === 'image/png'
      || tipo === 'image/webp'
      || nombre.endsWith('.jpg')
      || nombre.endsWith('.jpeg')
      || nombre.endsWith('.png')
      || nombre.endsWith('.webp');

    if (!esImagenValida) {
      this.fotoPerfilSeleccionada = null;
      this.nombreFotoPerfilSeleccionada = '';
      this.previewFotoPerfil = '';
      this.mensajeError = 'La foto de perfil debe ser JPG, PNG o WEBP';
      input.value = '';
      this.cdr.detectChanges();
      return;
    }

    const maxSizeBytes = 2 * 1024 * 1024;

    if (archivo.size > maxSizeBytes) {
      this.fotoPerfilSeleccionada = null;
      this.nombreFotoPerfilSeleccionada = '';
      this.previewFotoPerfil = '';
      this.mensajeError = 'La foto de perfil no debe superar los 2 MB';
      input.value = '';
      this.cdr.detectChanges();
      return;
    }

    this.fotoPerfilSeleccionada = archivo;
    this.nombreFotoPerfilSeleccionada = archivo.name;
    this.previewFotoPerfil = URL.createObjectURL(archivo);
    this.cdr.detectChanges();
  }

  quitarFotoPerfilSeleccionada(): void {
    this.fotoPerfilSeleccionada = null;
    this.nombreFotoPerfilSeleccionada = '';

    if (this.previewFotoPerfil) {
      URL.revokeObjectURL(this.previewFotoPerfil);
    }

    this.previewFotoPerfil = '';
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
      this.mensajeError = 'Debes ser mayor de edad para crear un perfil';
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