import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, timeout } from 'rxjs';

import { environment } from 'src/enviroments/enviroment';
import { Oferta, OfertaRequest } from 'src/app/core/models/oferta';
import { OfertaService } from 'src/app/core/services/oferta.service';

interface CatalogoDTO {
  id: number;
  nombre: string;
}

interface FiltrosOfertasResponse {
  categorias: CatalogoDTO[];
  modalidades: CatalogoDTO[];
  experiencias: CatalogoDTO[];
  rangosSalario?: unknown[];
}

interface ApiResponse<T> {
  message: string;
  data: T;
}

@Component({
  selector: 'app-crear-oferta',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crear-oferta.html',
  styleUrl: './crear-oferta.scss'
})
export class CrearOferta implements OnInit {

  private readonly baseUrl = environment.apiUrl;

  modoEdicion = false;
  idOfertaEditando?: number;
  estadoOferta = '';

  catalogosCargados = false;
  cargando = false;
  cargandoOferta = false;

  mensajeError = '';
  mensajeExito = '';

  fechaMinimaPostulacion = '';

  oferta: OfertaRequest = {
    titulo: '',
    codigoInterno: '',
    descripcion: '',
    tareasEspecificas: '',
    cantidadDuracion: null,
    montoTotal: null,
    fechaTerminoPostulacion: '',
    idCategoria: 0,
    idMod: 0,
    idDistrito: 0,
    idExperienciaRequerida: 0,
    idDuracion: 0,
    habilidadesId: []
  };

  categorias: CatalogoDTO[] = [];
  modalidades: CatalogoDTO[] = [];
  experiencias: CatalogoDTO[] = [];
  tiposDuracion: CatalogoDTO[] = [];

  departamentos: CatalogoDTO[] = [];
  provincias: CatalogoDTO[] = [];
  distritos: CatalogoDTO[] = [];

  habilidades: CatalogoDTO[] = [];

  idDepartamentoSeleccionado = 0;
  idProvinciaSeleccionada = 0;

  busquedaHabilidades = '';

  constructor(
    private http: HttpClient,
    private ofertaService: OfertaService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.fechaMinimaPostulacion = this.obtenerFechaActual();

    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      this.modoEdicion = true;
      this.idOfertaEditando = Number(idParam);
    }

    this.cargarCatalogos();
  }

  get tituloPagina(): string {
    return this.modoEdicion ? 'Editar oferta laboral' : 'Crear oferta laboral';
  }

  get descripcionPagina(): string {
    return this.modoEdicion
      ? 'Actualiza la informacion de tu oferta laboral activa.'
      : 'Publica una oportunidad laboral para que los empleados puedan postular.';
  }

  get textoBotonPrincipal(): string {
    if (this.cargando) {
      return this.modoEdicion ? 'Guardando...' : 'Publicando...';
    }

    return this.modoEdicion ? 'Guardar cambios' : 'Crear oferta';
  }

  get puedeGestionarEstado(): boolean {
    return this.modoEdicion && this.estadoOferta === 'ABIERTA';
  }

  get mostrarUpgradeOfertas(): boolean {
    const mensaje = this.normalizarMensaje(this.mensajeError);

    return mensaje.includes('limite de ofertas activas') ||
      mensaje.includes('límite de ofertas activas') ||
      mensaje.includes('has alcanzado el limite de ofertas') ||
      mensaje.includes('has alcanzado el límite de ofertas');
  }

  cargarCatalogos(): void {
    this.catalogosCargados = false;
    this.mensajeError = '';

    forkJoin({
      categorias: this.http.get<ApiResponse<CatalogoDTO[]>>(`${this.baseUrl}catalogos/categorias`),
      modalidades: this.http.get<ApiResponse<CatalogoDTO[]>>(`${this.baseUrl}catalogos/modalidades`),
      experiencias: this.http.get<ApiResponse<CatalogoDTO[]>>(`${this.baseUrl}catalogos/experiencias`),
      departamentos: this.http.get<ApiResponse<CatalogoDTO[]>>(`${this.baseUrl}catalogos/departamentos`),
      habilidades: this.http.get<ApiResponse<CatalogoDTO[]>>(`${this.baseUrl}catalogos/habilidades`),
      tiposDuracion: this.http.get<ApiResponse<CatalogoDTO[]>>(`${this.baseUrl}catalogos/tipos-duracion`)
    }).subscribe({
      next: (response) => {
        this.categorias = response.categorias.data ?? [];
        this.modalidades = response.modalidades.data ?? [];
        this.experiencias = response.experiencias.data ?? [];

        this.departamentos = response.departamentos.data ?? [];
        this.habilidades = response.habilidades.data ?? [];
        this.tiposDuracion = response.tiposDuracion.data ?? [];

        this.catalogosCargados = true;
        this.cdr.detectChanges();

        if (this.modoEdicion && this.idOfertaEditando) {
          this.cargarOfertaParaEditar(this.idOfertaEditando);
        }
      },
      error: (error) => {
        console.error('ERROR CARGANDO CATALOGOS:', error);

        const status = error.status;
        const url = error.url || 'URL no disponible';

        if (status === 0) {
          this.mensajeError =
            'No se pudo conectar con el servidor. Verifica que el backend esté encendido y que el puerto coincida con environment.apiUrl.';
        } else if (status === 401 || status === 403) {
          this.mensajeError =
            'No estás autorizado para cargar los catálogos. Revisa SecurityConfig.';
        } else {
          this.mensajeError =
            `No se pudieron cargar los catálogos. Error ${status} en ${url}`;
        }

        this.catalogosCargados = true;
        this.cdr.detectChanges();
      }
    });
  }

  cargarOfertaParaEditar(idOferta: number): void {
    this.cargandoOferta = true;

    this.ofertaService.getOfertaById(idOferta).subscribe({
      next: (oferta: Oferta) => {
        this.estadoOferta = oferta.estadoOferta;

        this.oferta = {
          titulo: oferta.titulo,
          codigoInterno: oferta.codigoInterno,
          descripcion: oferta.descripcion,
          tareasEspecificas: oferta.tareasEspecificas,
          cantidadDuracion: oferta.cantidadDuracion,
          montoTotal: oferta.montoTotal,
          fechaTerminoPostulacion: oferta.fechaTerminoPostulacion,
          idCategoria: oferta.idCategoria,
          idMod: oferta.idMod,
          idDistrito: oferta.idDistrito,
          idExperienciaRequerida: oferta.idExperienciaRequerida,
          idDuracion: oferta.idDuracion,
          habilidadesId: oferta.habilidades?.map(h => h.id) ?? []
        };

        this.cargarUbicacionInicial(oferta.idDistrito);

        this.cargandoOferta = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('ERROR CARGANDO OFERTA:', error);

        this.mensajeError = 'No se pudo cargar la oferta para editar.';
        this.cargandoOferta = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarUbicacionInicial(idDistrito: number): void {
    if (!this.buscarDistritoEnCatalogosActuales(idDistrito)) {
      this.oferta.idDistrito = idDistrito;
    }
  }

  buscarDistritoEnCatalogosActuales(idDistrito: number): CatalogoDTO | undefined {
    return this.distritos.find(d => d.id === idDistrito);
  }

  cambiarDepartamento(): void {
    this.idProvinciaSeleccionada = 0;
    this.oferta.idDistrito = 0;

    this.provincias = [];
    this.distritos = [];

    if (!this.idDepartamentoSeleccionado) {
      return;
    }

    this.http.get<ApiResponse<CatalogoDTO[]>>(
      `${this.baseUrl}catalogos/provincias?departamentoId=${this.idDepartamentoSeleccionado}`
    ).subscribe({
      next: (response) => {
        this.provincias = response.data ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('ERROR CARGANDO PROVINCIAS:', error);
        this.mensajeError = 'No se pudieron cargar las provincias.';
        this.cdr.detectChanges();
      }
    });
  }

  cambiarProvincia(): void {
    this.oferta.idDistrito = 0;
    this.distritos = [];

    if (!this.idProvinciaSeleccionada) {
      return;
    }

    this.http.get<ApiResponse<CatalogoDTO[]>>(
      `${this.baseUrl}catalogos/distritos?provinciaId=${this.idProvinciaSeleccionada}`
    ).subscribe({
      next: (response) => {
        this.distritos = response.data ?? [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('ERROR CARGANDO DISTRITOS:', error);
        this.mensajeError = 'No se pudieron cargar los distritos.';
        this.cdr.detectChanges();
      }
    });
  }

  habilidadesFiltradas(): CatalogoDTO[] {
    const busqueda = this.busquedaHabilidades.trim().toLowerCase();

    if (!busqueda) {
      return this.habilidades;
    }

    return this.habilidades.filter(habilidad =>
      habilidad.nombre.toLowerCase().includes(busqueda)
    );
  }

  cambiarHabilidad(idHabilidad: number): void {
    if (this.oferta.habilidadesId.includes(idHabilidad)) {
      this.oferta.habilidadesId = this.oferta.habilidadesId.filter(id => id !== idHabilidad);
    } else {
      this.oferta.habilidadesId.push(idHabilidad);
    }
  }

  estaSeleccionadoNumber(lista: number[], id: number): boolean {
    return lista.includes(id);
  }

  guardarOferta(): void {
    this.mensajeError = '';
    this.mensajeExito = '';

    if (!this.validarFormulario()) {
      this.cdr.detectChanges();
      return;
    }

    this.cargando = true;
    this.cdr.detectChanges();

    if (this.modoEdicion && this.idOfertaEditando) {
      this.editarOferta();
    } else {
      this.crearOferta();
    }
  }

  crearOferta(): void {
    this.ofertaService.crearOferta(this.oferta)
      .pipe(timeout(15000))
      .subscribe({
        next: (response) => {
          this.mensajeExito = response.message || 'Oferta laboral creada exitosamente.';
          this.mensajeError = '';
          this.cargando = false;
          this.cdr.detectChanges();

          setTimeout(() => {
            this.router.navigate(['/empleador/perfil']);
          }, 800);
        },
        error: (error) => this.manejarErrorGuardado(error)
      });
  }

  editarOferta(): void {
    if (!this.idOfertaEditando) {
      return;
    }

    this.ofertaService.editarOferta(this.idOfertaEditando, this.oferta)
      .pipe(timeout(15000))
      .subscribe({
        next: (response) => {
          this.mensajeExito = response.message || 'Oferta laboral actualizada exitosamente.';
          this.mensajeError = '';
          this.cargando = false;
          this.cdr.detectChanges();

          setTimeout(() => {
            this.router.navigate(['/empleador/mis-ofertas']);
          }, 800);
        },
        error: (error) => this.manejarErrorGuardado(error)
      });
  }

  finalizarOferta(): void {
    if (!this.idOfertaEditando) {
      return;
    }

    if (!confirm('¿Seguro que quieres finalizar esta oferta? Ya no recibirá postulantes nuevos.')) {
      return;
    }

    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.cdr.detectChanges();

    this.ofertaService.finalizarOferta(this.idOfertaEditando).subscribe({
      next: (response) => {
        this.estadoOferta = response.data.estadoOferta;
        this.mensajeExito = 'Oferta finalizada exitosamente.';
        this.cargando = false;
        this.cdr.detectChanges();

        setTimeout(() => {
          this.router.navigate(['/empleador/mis-ofertas']);
        }, 800);
      },
      error: (error) => this.manejarErrorGuardado(error)
    });
  }

  manejarErrorGuardado(error: any): void {
    console.error('ERROR GUARDANDO OFERTA:', error);

    this.cargando = false;
    this.mensajeExito = '';

    if (error.name === 'TimeoutError') {
      this.mensajeError = 'El servidor tardó demasiado. Intenta nuevamente.';
    } else if (error.status === 409) {
      this.mensajeError = error.error?.message || 'No se puede realizar esta acción por el estado actual.';
    } else if (error.status === 403) {
      this.mensajeError = error.error?.message || 'No tienes permiso para modificar esta oferta.';
    } else if (error.status === 401) {
      this.mensajeError = 'Tu sesión expiró. Inicia sesión nuevamente.';
    } else if (error.status === 400) {
      this.mensajeError = error.error?.message || 'Hay datos inválidos en el formulario.';
    } else if (error.status === 0) {
      this.mensajeError = 'No se pudo conectar con el servidor. Intenta nuevamente.';
    } else {
      this.mensajeError =
        error.error?.message ||
        error.error?.mensaje ||
        error.error?.error ||
        'No se pudo guardar la oferta laboral.';
    }

    this.cdr.detectChanges();
  }

  irSuscripciones(): void {
    this.router.navigate(['/suscripciones']);
  }

  cancelar(): void {
    this.router.navigate([
      this.modoEdicion ? '/empleador/mis-ofertas' : '/empleador/perfil'
    ]);
  }

  validarFormulario(): boolean {
    if (!this.oferta.titulo.trim()) {
      this.mensajeError = 'El título es obligatorio.';
      return false;
    }

    if (!this.oferta.codigoInterno.trim()) {
      this.mensajeError = 'El código interno es obligatorio.';
      return false;
    }

    if (!this.oferta.descripcion.trim()) {
      this.mensajeError = 'La descripción es obligatoria.';
      return false;
    }

    if (!this.oferta.tareasEspecificas.trim()) {
      this.mensajeError = 'Las tareas específicas son obligatorias.';
      return false;
    }

    if (!this.oferta.idCategoria) {
      this.mensajeError = 'Selecciona una categoría.';
      return false;
    }

    if (!this.oferta.idMod) {
      this.mensajeError = 'Selecciona una modalidad.';
      return false;
    }

    if (!this.oferta.idExperienciaRequerida) {
      this.mensajeError = 'Selecciona la experiencia requerida.';
      return false;
    }

    if (!this.oferta.idDuracion) {
      this.mensajeError = 'Selecciona el tipo de duración.';
      return false;
    }

    if (!this.oferta.idDistrito) {
      this.mensajeError = 'Selecciona un distrito.';
      return false;
    }

    if (!this.oferta.cantidadDuracion || this.oferta.cantidadDuracion <= 0) {
      this.mensajeError = 'La cantidad de duración debe ser mayor a 0.';
      return false;
    }

    if (!this.oferta.montoTotal || this.oferta.montoTotal <= 0) {
      this.mensajeError = 'El monto total debe ser mayor a 0.';
      return false;
    }

    if (!this.oferta.fechaTerminoPostulacion) {
      this.mensajeError = 'Selecciona la fecha límite de postulación.';
      return false;
    }

    return true;
  }

  obtenerFechaActual(): string {
    const hoy = new Date();

    return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`;
  }

  private normalizarMensaje(texto: string): string {
    return (texto || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim();
  }
}