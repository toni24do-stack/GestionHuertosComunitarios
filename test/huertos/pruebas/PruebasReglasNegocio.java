package huertos.pruebas;

import huertos.modelo.CausaPerdida;
import huertos.modelo.Cosecha;
import huertos.modelo.Cultivo;
import huertos.modelo.EstadoCultivo;
import huertos.modelo.EstadoLabor;
import huertos.modelo.EstadoParticipante;
import huertos.modelo.Huerto;
import huertos.modelo.ItemReparto;
import huertos.modelo.Labor;
import huertos.modelo.Parcela;
import huertos.modelo.Participante;
import huertos.modelo.Reparto;
import huertos.modelo.RolHuerto;
import huertos.modelo.TipoLabor;
import huertos.modelo.TipoRiego;
import huertos.negocio.AgendaSemanal;
import huertos.negocio.DatosEjemplo;
import huertos.negocio.GestorCosechas;
import huertos.negocio.GestorCultivos;
import huertos.negocio.GestorLabores;
import huertos.negocio.GestorParticipantes;
import huertos.negocio.ReglaNegocioException;
import huertos.negocio.ReporteProduccion;
import huertos.negocio.ResumenParticipacion;
import huertos.negocio.ValidadorCedula;

import java.time.LocalDate;
import java.util.List;

public class PruebasReglasNegocio {
    private static final LocalDate HOY = LocalDate.of(2026, 11, 16);
    private static int aprobadas = 0;
    private static int fallidas = 0;

    private static Huerto huerto;
    private static GestorParticipantes participantes;
    private static GestorCultivos cultivos;
    private static GestorLabores labores;
    private static GestorCosechas cosechas;

    public static void main(String[] args) {
        System.out.println("=== Pruebas de reglas de negocio – Huertos comunitarios ===\n");

        prueba("RF-01 Cédula válida pasa la validación", () -> verificar(ValidadorCedula.esValida("1712345675")));
        prueba("RF-01 Cédula con dígito verificador incorrecto no es válida",
                () -> verificar(!ValidadorCedula.esValida("1712345678")));
        prueba("RF-01 Registra participante activo con 0 horas", () -> {
            Participante p = participantes.registrar("0102030400", "Ana", "Pérez", "", RolHuerto.PARTICIPANTE, HOY, HOY);
            verificar(p.estaActivo() && p.getHorasAcumuladas() == 0);
        });
        prueba("RF-01 Rechaza cédula repetida", () -> esperarError(() -> participantes.registrar("1712345675", "X",
                "Y", "", RolHuerto.PARTICIPANTE, HOY, HOY)));
        prueba("RF-01 Rechaza nombres vacíos", () -> esperarError(() -> participantes.registrar("0102030400", " ",
                "Pérez", "", RolHuerto.PARTICIPANTE, HOY, HOY)));
        prueba("RF-02 Busca por nombre", () -> verificar(participantes.buscar("rosa").size() == 1));
        prueba("RF-02 No elimina a quien tiene labores registradas",
                () -> esperarError(() -> participantes.eliminar("1709876542")));
        prueba("RF-02 Elimina a quien no tiene labores", () -> {
            participantes.registrar("0102030400", "Ana", "Pérez", "", RolHuerto.PARTICIPANTE, HOY, HOY);
            participantes.eliminar("0102030400");
            verificar(participantes.buscarPorCedula("0102030400") == null);
        });
        prueba("RF-03 Calcula horas y porcentaje del período", () -> {
            List<ResumenParticipacion> r = participantes.calcularParticipacion(HOY.minusDays(60), HOY, HOY);
            double total = 0;
            double porcentajes = 0;
            for (ResumenParticipacion fila : r) {
                total = total + fila.getHoras();
                porcentajes = porcentajes + fila.getPorcentaje();
            }
            verificar(total == 14.5 && Math.abs(porcentajes - 100) < 0.01);
        });
        prueba("RF-03 Sin labores en 30 días pasa a Inactivo (Carmen)", () -> {
            participantes.calcularParticipacion(HOY.minusDays(60), HOY, HOY);
            verificar(participantes.buscarPorCedula("1705566774").getEstado() == EstadoParticipante.INACTIVO
                    && participantes.buscarPorCedula("1709876542").estaActivo());
        });

        prueba("RF-04 Rechaza parcela con área 0", () -> esperarError(
                () -> cultivos.registrarParcela("P-09", "0", "Patio", TipoRiego.MANUAL)));
        prueba("RF-04 Rechaza código de parcela repetido", () -> esperarError(
                () -> cultivos.registrarParcela("p-01", "5", "Patio", TipoRiego.MANUAL)));
        prueba("RF-04 No elimina parcela con cultivos activos",
                () -> esperarError(() -> cultivos.eliminarParcela("P-01")));
        prueba("RF-04 Calcula área ocupada y libre (P-01: 9 de 12 m²)", () -> {
            Parcela p = cultivos.obtenerParcela("P-01");
            verificar(p.getAreaOcupada() == 9 && p.getAreaLibre() == 3);
        });
        prueba("RF-05 Calcula fecha estimada y kilos esperados", () -> {
            Parcela p = cultivos.obtenerParcela("P-02");
            Cultivo c = cultivos.registrarSiembra(p, huerto.buscarTipoCultivo("Rábano"), "blanco", HOY, "2", HOY);
            verificar(c.calcularFechaEstimadaCosecha().equals(HOY.plusDays(30)) && c.calcularKgEsperados() == 3.0
                    && c.getEstado() == EstadoCultivo.SEMBRADO);
        });
        prueba("RF-05 Rechaza siembra mayor que el área libre", () -> esperarError(() -> cultivos.registrarSiembra(
                cultivos.obtenerParcela("P-01"), huerto.buscarTipoCultivo("Cebolla"), "paiteña", HOY, "4", HOY)));
        prueba("RF-06 Rechaza transición Sembrado → Listo para cosecha", () -> {
            Cultivo c = cultivos.registrarSiembra(cultivos.obtenerParcela("P-02"), huerto.buscarTipoCultivo("Rábano"),
                    "blanco", HOY, "1", HOY);
            esperarError(() -> cultivos.cambiarEstado(c, EstadoCultivo.LISTO_PARA_COSECHA, null, HOY));
        });
        prueba("RF-06 Exige causa al marcar Perdido", () -> esperarError(
                () -> cultivos.cambiarEstado(cultivo("Acelga"), EstadoCultivo.PERDIDO, null, HOY)));
        prueba("RF-06 Perdido libera área y cancela labores abiertas", () -> {
            Cultivo acelga = cultivo("Acelga");
            double libreAntes = acelga.getParcela().getAreaLibre();
            cultivos.cambiarEstado(acelga, EstadoCultivo.PERDIDO, CausaPerdida.CLIMA, HOY);
            boolean hayAbiertas = false;
            for (Labor l : huerto.getLabores()) {
                if (l.getCultivo() == acelga && l.estaAbierta()) {
                    hayAbiertas = true;
                }
            }
            verificar(acelga.getParcela().getAreaLibre() == libreAntes + 5 && !hayAbiertas);
        });
        prueba("RF-06 Propone Listo para cosecha a la lechuga", () -> {
            List<Cultivo> propuestas = cultivos.getPropuestasListoParaCosecha(HOY);
            verificar(propuestas.size() == 1 && propuestas.get(0) == cultivo("Lechuga"));
        });

        prueba("RF-07 Labor recurrente se repite hasta la cosecha estimada", () -> {
            Cultivo acelga = cultivo("Acelga");
            List<Labor> creadas = labores.programar(TipoLabor.DESHIERBE, acelga, jose(), HOY, "10", HOY);
            Labor ultima = creadas.get(creadas.size() - 1);
            verificar(creadas.size() == 6 && !ultima.getFechaProgramada().isAfter(acelga.calcularFechaEstimadaCosecha()));
        });
        prueba("RF-07 No asigna labores a participantes inactivos", () -> {
            participantes.cambiarEstado("1709876542", EstadoParticipante.INACTIVO);
            esperarError(() -> labores.programar(TipoLabor.RIEGO, cultivo("Acelga"), jose(), HOY, "0", HOY));
        });
        prueba("RF-07 No programa labores en cultivos perdidos", () -> esperarError(
                () -> labores.programar(TipoLabor.RIEGO, cultivo("Zanahoria"), jose(), HOY, "0", HOY)));
        prueba("RF-08 Completa la labor y suma horas al responsable", () -> {
            Labor l = labores.programar(TipoLabor.PODA, cultivo("Tomate riñón"), jose(), HOY, "0", HOY).get(0);
            double antes = jose().getHorasAcumuladas();
            labores.registrarCumplimiento(l, HOY, "2,5", "", HOY);
            verificar(l.getEstado() == EstadoLabor.COMPLETADA && jose().getHorasAcumuladas() == antes + 2.5);
        });
        prueba("RF-08 Rechaza horas fuera de 0,5 a 8", () -> {
            Labor l = labores.programar(TipoLabor.PODA, cultivo("Tomate riñón"), jose(), HOY, "0", HOY).get(0);
            esperarError(() -> labores.registrarCumplimiento(l, HOY, "9", "", HOY));
        });
        prueba("RF-08 No registra dos veces una labor completada", () -> {
            Labor l = labores.programar(TipoLabor.PODA, cultivo("Tomate riñón"), jose(), HOY, "0", HOY).get(0);
            labores.registrarCumplimiento(l, HOY, "1", "", HOY);
            esperarError(() -> labores.registrarCumplimiento(l, HOY, "1", "", HOY));
        });
        prueba("RF-09 Labor pendiente con fecha pasada queda Atrasada", () -> {
            Labor l = labores.programar(TipoLabor.RIEGO, cultivo("Tomate riñón"), jose(), HOY.minusDays(1), "0",
                    HOY.minusDays(3)).get(0);
            labores.actualizarAtrasadas(HOY);
            verificar(l.getEstado() == EstadoLabor.ATRASADA);
        });
        prueba("RF-09 La agenda cuenta atrasadas por responsable", () -> {
            AgendaSemanal agenda = labores.generarAgenda(HOY, HOY, null, null);
            verificar(agenda.getTotalAtrasadas() == 2
                    && agenda.getAtrasadasPorResponsable().get("María Quishpe") == 1
                    && agenda.getAtrasadasPorResponsable().get("José Guamán") == 1);
        });

        prueba("RF-10 Solo cosecha cultivos Listo para cosecha", () -> esperarError(
                () -> cosechas.registrarCosecha(cultivo("Acelga"), HOY, "3", HOY)));
        prueba("RF-10 Rechaza cantidad 0", () -> esperarError(
                () -> cosechas.registrarCosecha(cultivo("Brócoli"), HOY, "0", HOY)));
        prueba("RF-10 Calcula rendimiento y pasa a Cosechado", () -> {
            Cosecha c = cosechas.registrarCosecha(cultivo("Brócoli"), HOY, "5,4", HOY);
            verificar(Math.abs(c.calcularRendimiento() - 90.0) < 0.001
                    && cultivo("Brócoli").getEstado() == EstadoCultivo.COSECHADO);
        });
        prueba("RF-11 Reparto proporcional con reserva del 10 % (rábano 4,2 kg)", () -> {
            Reparto r = cosechas.calcularReparto(huerto.getCosechas().get(0), "10");
            verificar(r.getKgReserva() == 0.4 && kgDe(r, "José Guamán") == 1.9 && kgDe(r, "Rosa Tipán") == 1.1
                    && kgDe(r, "Luis Chicaiza") == 0.8 && kgDe(r, "María Quishpe") == 0.0);
        });
        prueba("RF-11 La suma del reparto es exacta", () -> {
            Reparto r = cosechas.calcularReparto(huerto.getCosechas().get(0), "15");
            verificar(Math.abs(r.getKgReserva() + r.getKgRepartidos() - 4.2) < 0.001);
        });
        prueba("RF-11 No reparte dos veces la misma cosecha", () -> {
            Cosecha c = huerto.getCosechas().get(0);
            cosechas.guardarReparto(c, cosechas.calcularReparto(c, "10"));
            esperarError(() -> cosechas.calcularReparto(c, "10"));
        });
        prueba("RF-11 Rechaza reserva mayor que 100 %", () -> esperarError(
                () -> cosechas.calcularReparto(huerto.getCosechas().get(0), "120")));
        prueba("RF-12 Reporte: kilos, kg/m² y % de cultivos perdidos", () -> {
            ReporteProduccion r = cosechas.generarReporte(HOY.minusDays(30), HOY);
            verificar(r.getKgTotales() == 4.2 && Math.abs(r.getFilas().get(0).getKgPorM2() - 1.4) < 0.001
                    && r.getPorcentajePerdidos() == 50.0 && r.getCausasPerdida().get(CausaPerdida.PLAGA) == 1);
        });

        System.out.println("\nResultado: " + aprobadas + " pruebas aprobadas, " + fallidas + " fallidas.");
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private interface Caso {
        void ejecutar();
    }

    private static void prueba(String nombre, Caso caso) {
        huerto = DatosEjemplo.crearHuerto(HOY);
        participantes = new GestorParticipantes(huerto);
        cultivos = new GestorCultivos(huerto);
        labores = new GestorLabores(huerto);
        cosechas = new GestorCosechas(huerto, cultivos);
        try {
            caso.ejecutar();
            aprobadas++;
            System.out.println("[OK]    " + nombre);
        } catch (RuntimeException | AssertionError e) {
            fallidas++;
            System.out.println("[FALLA] " + nombre + " → " + e);
        }
    }

    private static void verificar(boolean condicion) {
        if (!condicion) {
            throw new AssertionError("la condición esperada no se cumplió");
        }
    }

    private static void esperarError(Caso caso) {
        try {
            caso.ejecutar();
        } catch (ReglaNegocioException e) {
            return;
        }
        throw new AssertionError("se esperaba un mensaje de error de regla de negocio");
    }

    private static Cultivo cultivo(String tipo) {
        for (Cultivo c : huerto.getCultivos()) {
            if (c.getTipo().getNombre().equals(tipo)) {
                return c;
            }
        }
        throw new AssertionError("no existe el cultivo " + tipo);
    }

    private static Participante jose() {
        return participantes.buscarPorCedula("1709876542");
    }

    private static double kgDe(Reparto r, String nombre) {
        for (ItemReparto item : r.getItems()) {
            if (item.getParticipante().getNombreCompleto().equals(nombre)) {
                return item.getKg();
            }
        }
        throw new AssertionError(nombre + " no aparece en el reparto");
    }
}
