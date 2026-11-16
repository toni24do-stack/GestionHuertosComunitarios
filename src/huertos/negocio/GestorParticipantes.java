package huertos.negocio;

import huertos.modelo.Cosecha;
import huertos.modelo.EstadoLabor;
import huertos.modelo.EstadoParticipante;
import huertos.modelo.Huerto;
import huertos.modelo.Labor;
import huertos.modelo.Participante;
import huertos.modelo.RolHuerto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorParticipantes {
    public static final int DIAS_INACTIVIDAD = 30;

    private final Huerto huerto;

    public GestorParticipantes(Huerto huerto) {
        this.huerto = huerto;
    }

    public Participante registrar(String cedula, String nombres, String apellidos, String telefono,
                                  RolHuerto rol, LocalDate fechaIngreso, LocalDate hoy) {
        String ced = Formato.texto(cedula, "Cédula");
        if (!ValidadorCedula.esValida(ced)) {
            throw new ReglaNegocioException("La cédula " + ced + " no es válida (dígito verificador incorrecto "
                    + "o formato distinto de 10 dígitos).");
        }
        if (buscarPorCedula(ced) != null) {
            throw new ReglaNegocioException("Ya existe un participante con la cédula " + ced + ".");
        }
        String nom = Formato.texto(nombres, "Nombres");
        String ape = Formato.texto(apellidos, "Apellidos");
        String tel = validarTelefono(telefono);
        if (rol == null) {
            throw new ReglaNegocioException("Seleccione el rol en el huerto.");
        }
        if (fechaIngreso == null) {
            throw new ReglaNegocioException("El campo «Fecha de ingreso» es obligatorio.");
        }
        if (fechaIngreso.isAfter(hoy)) {
            throw new ReglaNegocioException("La fecha de ingreso no puede ser posterior a hoy.");
        }
        Participante p = new Participante(ced, nom, ape, tel, rol, fechaIngreso);
        huerto.getParticipantes().add(p);
        return p;
    }

    public List<Participante> buscar(String texto) {
        List<Participante> resultado = new ArrayList<>();
        String filtro = "";
        if (texto != null) {
            filtro = texto.trim().toLowerCase();
        }
        for (Participante p : huerto.getParticipantes()) {
            if (filtro.isEmpty() || p.getCedula().contains(filtro)
                    || p.getNombreCompleto().toLowerCase().contains(filtro)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    public Participante buscarPorCedula(String cedula) {
        for (Participante p : huerto.getParticipantes()) {
            if (p.getCedula().equals(cedula)) {
                return p;
            }
        }
        return null;
    }

    public Participante obtener(String cedula) {
        String ced = Formato.texto(cedula, "Cédula");
        Participante p = buscarPorCedula(ced);
        if (p == null) {
            throw new ReglaNegocioException("No existe un participante con la cédula " + ced + ".");
        }
        return p;
    }

    public List<Participante> getActivos() {
        List<Participante> activos = new ArrayList<>();
        for (Participante p : huerto.getParticipantes()) {
            if (p.estaActivo()) {
                activos.add(p);
            }
        }
        return activos;
    }

    public void actualizar(String cedula, String nombres, String apellidos, String telefono, RolHuerto rol) {
        Participante p = obtener(cedula);
        String nom = Formato.texto(nombres, "Nombres");
        String ape = Formato.texto(apellidos, "Apellidos");
        String tel = validarTelefono(telefono);
        p.setNombres(nom);
        p.setApellidos(ape);
        p.setTelefono(tel);
        p.setRol(rol);
    }

    public void cambiarEstado(String cedula, EstadoParticipante estado) {
        obtener(cedula).setEstado(estado);
    }

    public String motivoNoEliminable(Participante p) {
        for (Labor l : huerto.getLabores()) {
            if (l.getResponsable() == p && l.getEstado() != EstadoLabor.CANCELADA) {
                return "tiene labores registradas (pendientes o completadas)";
            }
        }
        for (Cosecha c : huerto.getCosechas()) {
            if (c.estaRepartida() && c.getReparto().incluye(p)) {
                return "aparece en el reparto de una cosecha";
            }
        }
        return null;
    }

    public void eliminar(String cedula) {
        Participante p = obtener(cedula);
        String motivo = motivoNoEliminable(p);
        if (motivo != null) {
            throw new ReglaNegocioException("No se puede eliminar a " + p.getNombreCompleto() + " porque " + motivo
                    + ". Solo se puede marcar como «Inactivo».");
        }
        huerto.getParticipantes().remove(p);
    }

    public List<ResumenParticipacion> calcularParticipacion(LocalDate desde, LocalDate hasta, LocalDate hoy) {
        if (desde == null || hasta == null) {
            throw new ReglaNegocioException("Ingrese las fechas de inicio y fin del período.");
        }
        if (hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        aplicarReglaInactividad(hoy);

        List<Participante> lista = huerto.getParticipantes();
        int[] numeroLabores = new int[lista.size()];
        double[] horas = new double[lista.size()];
        double totalHoras = 0;
        for (Labor l : huerto.getLabores()) {
            if (l.getEstado() == EstadoLabor.COMPLETADA
                    && !l.getFechaRealizacion().isBefore(desde) && !l.getFechaRealizacion().isAfter(hasta)) {
                int i = lista.indexOf(l.getResponsable());
                if (i >= 0) {
                    numeroLabores[i]++;
                    horas[i] = horas[i] + l.getHorasTrabajadas();
                    totalHoras = totalHoras + l.getHorasTrabajadas();
                }
            }
        }
        List<ResumenParticipacion> resultado = new ArrayList<>();
        for (int i = 0; i < lista.size(); i++) {
            double porcentaje = 0;
            if (totalHoras > 0) {
                porcentaje = horas[i] / totalHoras * 100;
            }
            resultado.add(new ResumenParticipacion(lista.get(i), numeroLabores[i], horas[i], porcentaje));
        }
        return resultado;
    }

    public int aplicarReglaInactividad(LocalDate hoy) {
        LocalDate limite = hoy.minusDays(DIAS_INACTIVIDAD);
        int cambios = 0;
        for (Participante p : huerto.getParticipantes()) {
            if (p.getRol() == RolHuerto.PARTICIPANTE && p.estaActivo() && !p.getFechaIngreso().isAfter(limite)) {
                boolean trabajo = false;
                for (Labor l : huerto.getLabores()) {
                    if (l.getResponsable() == p && l.getEstado() == EstadoLabor.COMPLETADA
                            && !l.getFechaRealizacion().isBefore(limite)) {
                        trabajo = true;
                    }
                }
                if (!trabajo) {
                    p.setEstado(EstadoParticipante.INACTIVO);
                    cambios++;
                }
            }
        }
        return cambios;
    }

    private String validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return "";
        }
        String tel = telefono.trim();
        if (!tel.matches("0\\d{8,9}")) {
            throw new ReglaNegocioException("El teléfono debe empezar con 0 y tener 9 o 10 dígitos.");
        }
        return tel;
    }
}
