import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { EmpleadorService } from 'src/app/core/services/empleador';
import { CatalogoService } from 'src/app/core/services/catalogo';

import { EmpleadorRequest, EmpleadorResponse } from 'src/app/core/models/empleador';
import { CatalogoItem } from 'src/app/core/models/catalogo';

import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-editar-perfil-empleador',
  standalone: true,
  imports: [CommonModule, FormsModule, TopbarComponent],
  templateUrl: './editar-perfil-empleador.html',
  styleUrl: './editar-perfil-empleador.scss'
})
export class EditarPerfilEmpleadorComponent implements OnInit {

  cargando = true;
  guardando = false;

  mensajeError = '';
  mensajeExito = '';

  logoSeleccionado: File | null = null;
  nombreLogoSeleccionado = '';
  previewLogo = '';

  categorias: CatalogoItem[] = [];
  busquedaCategorias = '';

  tiposEmpleador = ['Empresa', 'Persona natural'];
  tiposDocumento = ['RUC', 'DNI', 'Carnet de extranjería', 'Pasaporte'];

  perfil: EmpleadorRequest = {
    tipoEmpleador: '',
    nombreComercial: '',
    razonSocial: '',
    tipoDoc: '',
    numDoc: '',
    logoEmpleador: '',
    descripcionNegocio: '',
    aniosOperacion: 0,
    sitioWeb: '',
    correoContacto: '',
    telefonoContacto: '',
    categoriasId: []
  };

  constructor(
    private empleadorService: EmpleadorService,
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

    this.catalogoService.listarCategorias().subscribe({
      next: (res) => {
        this.categorias = res.data;
        this.cargarPerfil();
      },
      error: () => {
        this.cargando = false;
        this.mensajeError = 'No se pudieron cargar las categorías';
        this.cdr.detectChanges();
      }
    });
  }

  cargarPerfil(): void {
    this.empleadorService.obtenerPerfil().subscribe({
      next: (response) => {
        this.mapearResponseAFormulario(response.data);
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar perfil empleador:', error);

        this.cargando = false;

        if (error.status === 404) {
          this.router.navigate(['/empleador/crear-perfil']);
          this.cdr.detectChanges();
          return;
        }

        if (error.status === 401 || error.status === 403) {
          this.mensajeError = 'No estás autorizado. Inicia sesión nuevamente.';
          this.cdr.detectChanges();
          return;
        }

        this.mensajeError = error?.error?.message || 'No se pudo cargar el perfil del empleador';
        this.cdr.detectChanges();
      }
    });
  }

  private mapearResponseAFormulario(data: EmpleadorResponse): void {
    this.perfil = {
      tipoEmpleador: data.tipoEmpleador || '',
      nombreComercial: data.nombreComercial || '',
      razonSocial: data.razonSocial || '',
      tipoDoc: data.tipoDoc || '',
      numDoc: data.numDoc || '',
      logoEmpleador: data.logoEmpleador || '',
      descripcionNegocio: data.descripcionNegocio || '',
      aniosOperacion: data.aniosOperacion || 0,
      sitioWeb: data.sitioWeb || '',
      correoContacto: data.correoContacto || '',
      telefonoContacto: data.telefonoContacto || '',
      categoriasId: this.obtenerIdsPorNombres(this.categorias, data.categorias)
    };
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

  cambiarTipoEmpleador(): void {
    if (this.perfil.tipoEmpleador === 'Empresa') {
      this.perfil.tipoDoc = 'RUC';
    }

    if (this.perfil.tipoEmpleador === 'Persona natural') {
      this.perfil.tipoDoc = 'DNI';
    }

    this.perfil.numDoc = '';
  }

  cambiarTipoDocumento(): void {
    this.perfil.numDoc = '';
  }

  obtenerMaxLengthDocumento(): number {
    if (this.perfil.tipoDoc === 'RUC') {
      return 11;
    }

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
    if (this.perfil.tipoDoc === 'RUC') {
      return 'Ejemplo: 20609999999';
    }

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

  toggleNumber(lista: number[] | undefined, valor: number): number[] {
    const actual = lista ?? [];

    if (actual.includes(valor)) {
      return actual.filter(item => item !== valor);
    }

    return [...actual, valor];
  }

  estaSeleccionadoNumber(lista: number[] | undefined, valor: number): boolean {
    return lista?.includes(valor) ?? false;
  }

  cambiarCategoria(id: number): void {
    this.perfil.categoriasId = this.toggleNumber(this.perfil.categoriasId, id);
  }

  guardarCambios(): void {
    this.mensajeError = '';
    this.mensajeExito = '';

    this.limpiarCamposTexto();

    if (!this.validarFormulario()) {
      return;
    }

    this.guardando = true;

    this.empleadorService.editarPerfil(this.perfil, this.logoSeleccionado).subscribe({
      next: () => {
        this.guardando = false;
        this.mensajeExito = 'Perfil de empleador actualizado exitosamente';

        this.cdr.detectChanges();

        setTimeout(() => {
          this.router.navigate(['/empleador/perfil']);
        }, 700);
      },
      error: (error) => {
        this.guardando = false;
        this.mensajeError = error?.error?.message || 'Error al actualizar el perfil de empleador';
        this.cdr.detectChanges();
      }
    });
  }

  onLogoSeleccionado(event: Event): void {
    this.mensajeError = '';

    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0] ?? null;

    if (!archivo) {
      this.logoSeleccionado = null;
      this.nombreLogoSeleccionado = '';
      this.previewLogo = '';
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
      this.logoSeleccionado = null;
      this.nombreLogoSeleccionado = '';
      this.previewLogo = '';
      this.mensajeError = 'El logo debe ser JPG, PNG o WEBP';
      input.value = '';
      this.cdr.detectChanges();
      return;
    }

    const maxSizeBytes = 2 * 1024 * 1024;

    if (archivo.size > maxSizeBytes) {
      this.logoSeleccionado = null;
      this.nombreLogoSeleccionado = '';
      this.previewLogo = '';
      this.mensajeError = 'El logo no debe superar los 2 MB';
      input.value = '';
      this.cdr.detectChanges();
      return;
    }

    this.logoSeleccionado = archivo;
    this.nombreLogoSeleccionado = archivo.name;
    this.previewLogo = URL.createObjectURL(archivo);
    this.cdr.detectChanges();
  }

  quitarLogoSeleccionado(): void {
    this.logoSeleccionado = null;
    this.nombreLogoSeleccionado = '';

    if (this.previewLogo) {
      URL.revokeObjectURL(this.previewLogo);
    }

    this.previewLogo = '';
  }

  cancelar(): void {
    this.router.navigate(['/empleador/perfil']);
  }

  private validarFormulario(): boolean {
    if (
      !this.perfil.tipoEmpleador ||
      !this.perfil.nombreComercial ||
      !this.perfil.razonSocial ||
      !this.perfil.tipoDoc ||
      !this.perfil.numDoc ||
      this.perfil.aniosOperacion === null ||
      this.perfil.aniosOperacion === undefined
    ) {
      this.mensajeError = 'Completa todos los campos obligatorios';
      return false;
    }

    if (this.perfil.aniosOperacion < 0) {
      this.mensajeError = 'Los años de operación no pueden ser negativos';
      return false;
    }

    if (!this.validarDocumento()) {
      this.mensajeError = this.obtenerMensajeDocumentoInvalido();
      return false;
    }

    if (!this.validarContactoOpcional()) {
      return false;
    }

    return true;
  }

  private validarDocumento(): boolean {
    const tipoDoc = this.perfil.tipoDoc;
    const numDoc = this.perfil.numDoc?.trim() || '';

    if (tipoDoc === 'RUC') {
      return /^[0-9]{11}$/.test(numDoc);
    }

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
    if (this.perfil.tipoDoc === 'RUC') {
      return 'El RUC debe tener exactamente 11 dígitos numéricos';
    }

    if (this.perfil.tipoDoc === 'DNI') {
      return 'El DNI debe tener exactamente 8 dígitos numéricos';
    }

    if (this.perfil.tipoDoc === 'Carnet de extranjería') {
      return 'El carnet de extranjería debe tener entre 9 y 12 caracteres alfanuméricos';
    }

    if (this.perfil.tipoDoc === 'Pasaporte') {
      return 'El pasaporte debe tener entre 6 y 12 caracteres alfanuméricos';
    }

    return 'Tipo de documento no válido';
  }

  private validarContactoOpcional(): boolean {
    const correo = this.perfil.correoContacto?.trim() || '';
    const telefono = this.perfil.telefonoContacto?.trim() || '';
    const sitioWeb = this.perfil.sitioWeb?.trim() || '';

    if (correo && !/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(correo)) {
      this.mensajeError = 'El correo de contacto no tiene un formato válido';
      return false;
    }

    if (telefono && !/^[0-9+\s]{6,20}$/.test(telefono)) {
      this.mensajeError = 'El teléfono de contacto no tiene un formato válido';
      return false;
    }

    if (sitioWeb && !/^(https?:\/\/)?([\w-]+\.)+[\w-]{2,}(\/.*)?$/.test(sitioWeb)) {
      this.mensajeError = 'El sitio web no tiene un formato válido';
      return false;
    }

    return true;
  }

  private limpiarCamposTexto(): void {
    this.perfil.tipoEmpleador = this.perfil.tipoEmpleador.trim();
    this.perfil.nombreComercial = this.perfil.nombreComercial.trim();
    this.perfil.razonSocial = this.perfil.razonSocial.trim();
    this.perfil.tipoDoc = this.perfil.tipoDoc.trim();
    this.perfil.numDoc = this.perfil.numDoc.trim();

    this.perfil.logoEmpleador = this.perfil.logoEmpleador?.trim() || '';
    this.perfil.descripcionNegocio = this.perfil.descripcionNegocio?.trim() || '';
    this.perfil.sitioWeb = this.perfil.sitioWeb?.trim() || '';
    this.perfil.correoContacto = this.perfil.correoContacto?.trim() || '';
    this.perfil.telefonoContacto = this.perfil.telefonoContacto?.trim() || '';
  }
}