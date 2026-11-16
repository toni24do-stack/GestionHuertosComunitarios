package huertos.negocio;

import huertos.modelo.CausaPerdida;
import huertos.modelo.Cultivo;
import huertos.modelo.EstadoCultivo;
import huertos.modelo.Huerto;
import huertos.modelo.Labor;
import huertos.modelo.Parcela;
import huertos.modelo.Participante;
import huertos.modelo.RolHuerto;
import huertos.modelo.TipoLabor;
import huertos.modelo.TipoRiego;

import java.time.LocalDate;
import java.util.List;

public final class DatosEjemplo {
    private DatosEjemplo() {
    }

    public static Huerto crearHuerto(LocalDate hoy) {
        Huerto huerto = new Huerto("La Esperanza");
        GestorParticipantes participantes = new GestorParticipantes(huerto);
        GestorCultivos cultivos = new GestorCultivos(huerto);
        GestorLabores labores = new GestorLabores(huerto);
        GestorCosechas cosechas = new GestorCosechas(huerto, cultivos);

        Participante maria = participantes.registrar("1712345675", "María", "Quishpe", "0991234567",
                RolHuerto.COORDINADOR, hoy.minusDays(200), hoy);
        Participante jose = participantes.registrar("1709876542", "José", "Guamán", "0987654321",
                RolHuerto.PARTICIPANTE, hoy.minusDays(180), hoy);
        Participante rosa = participantes.registrar("1723456784", "Rosa", "Tipán", "0998877665",
                RolHuerto.PARTICIPANTE, hoy.minusDays(150), hoy);
        Participante luis = participantes.registrar("1711223345", "Luis", "Chicaiza", "",
                RolHuerto.PARTICIPANTE, hoy.minusDays(120), hoy);
        Participante carmen = participantes.registrar("1705566774", "Carmen", "Andrango", "022345678",
                RolHuerto.PARTICIPANTE, hoy.minusDays(90), hoy);

        Parcela p01 = cultivos.registrarParcela("P-01", "12", "Cama norte", TipoRiego.GOTEO);
        Parcela p02 = cultivos.registrarParcela("P-02", "8", "Cama sur", TipoRiego.MANUAL);
        Parcela p03 = cultivos.registrarParcela("P-03", "15", "Terraza comunal", TipoRiego.GOTEO);

        Cultivo lechuga = cultivos.registrarSiembra(p01, huerto.buscarTipoCultivo("Lechuga"), "crespa",
                hoy.minusDays(65), "4", hoy);
        Cultivo rabano = cultivos.registrarSiembra(p02, huerto.buscarTipoCultivo("Rábano"), "rojo",
                hoy.minusDays(40), "3", hoy);
        Cultivo acelga = cultivos.registrarSiembra(p01, huerto.buscarTipoCultivo("Acelga"), "verde",
                hoy.minusDays(20), "5", hoy);
        Cultivo tomate = cultivos.registrarSiembra(p03, huerto.buscarTipoCultivo("Tomate riñón"), "chonto",
                hoy.minusDays(30), "6", hoy);
        Cultivo zanahoria = cultivos.registrarSiembra(p02, huerto.buscarTipoCultivo("Zanahoria"), "nantes",
                hoy.minusDays(50), "3", hoy);
        Cultivo brocoli = cultivos.registrarSiembra(p03, huerto.buscarTipoCultivo("Brócoli"), "calabrese",
                hoy.minusDays(95), "4", hoy);

        completar(labores, TipoLabor.RIEGO, rabano, jose, hoy.minusDays(35), "1,5", hoy);
        completar(labores, TipoLabor.ABONADO, rabano, rosa, hoy.minusDays(30), "2", hoy);
        completar(labores, TipoLabor.DESHIERBE, rabano, jose, hoy.minusDays(25), "1", hoy);
        completar(labores, TipoLabor.CONTROL_DE_PLAGAS, rabano, luis, hoy.minusDays(20), "1,5", hoy);
        completar(labores, TipoLabor.RIEGO, rabano, jose, hoy.minusDays(15), "1", hoy);

        List<Labor> riegoLechuga = labores.programar(TipoLabor.RIEGO, lechuga, maria, hoy.minusDays(14), "7", hoy);
        labores.registrarCumplimiento(riegoLechuga.get(0), hoy.minusDays(14), "1", "", hoy);

        List<Labor> riegoAcelga = labores.programar(TipoLabor.RIEGO, acelga, rosa, hoy.minusDays(6), "7", hoy);
        labores.registrarCumplimiento(riegoAcelga.get(0), hoy.minusDays(6), "1", "Se revisó el goteo", hoy);

        completar(labores, TipoLabor.DESHIERBE, tomate, luis, hoy.minusDays(10), "2", hoy);
        labores.programar(TipoLabor.ABONADO, tomate, jose, hoy.minusDays(2), "0", hoy);
        labores.programar(TipoLabor.PODA, tomate, luis, hoy.plusDays(3), "0", hoy);

        completar(labores, TipoLabor.CONTROL_DE_PLAGAS, brocoli, rosa, hoy.minusDays(40), "1,5", hoy);
        completar(labores, TipoLabor.RIEGO, brocoli, jose, hoy.minusDays(20), "1", hoy);
        labores.programar(TipoLabor.DESHIERBE, brocoli, luis, hoy.plusDays(1), "0", hoy);

        completar(labores, TipoLabor.DESHIERBE, zanahoria, carmen, hoy.minusDays(45), "1", hoy);
        labores.programar(TipoLabor.RIEGO, zanahoria, jose, hoy.minusDays(12), "0", hoy);

        cultivos.cambiarEstado(lechuga, EstadoCultivo.EN_CRECIMIENTO, null, hoy.minusDays(40));
        cultivos.cambiarEstado(rabano, EstadoCultivo.EN_CRECIMIENTO, null, hoy.minusDays(28));
        cultivos.cambiarEstado(rabano, EstadoCultivo.LISTO_PARA_COSECHA, null, hoy.minusDays(10));
        cultivos.cambiarEstado(acelga, EstadoCultivo.EN_CRECIMIENTO, null, hoy.minusDays(8));
        cultivos.cambiarEstado(tomate, EstadoCultivo.EN_CRECIMIENTO, null, hoy.minusDays(12));
        cultivos.cambiarEstado(brocoli, EstadoCultivo.EN_CRECIMIENTO, null, hoy.minusDays(60));
        cultivos.cambiarEstado(brocoli, EstadoCultivo.LISTO_PARA_COSECHA, null, hoy.minusDays(4));
        cultivos.cambiarEstado(zanahoria, EstadoCultivo.EN_CRECIMIENTO, null, hoy.minusDays(30));
        cultivos.cambiarEstado(zanahoria, EstadoCultivo.PERDIDO, CausaPerdida.PLAGA, hoy.minusDays(10));

        cosechas.registrarCosecha(rabano, hoy.minusDays(8), "4,2", hoy);

        labores.actualizarAtrasadas(hoy);
        return huerto;
    }

    private static void completar(GestorLabores labores, TipoLabor tipo, Cultivo cultivo, Participante responsable,
                                  LocalDate fecha, String horas, LocalDate hoy) {
        Labor labor = labores.programar(tipo, cultivo, responsable, fecha, "0", hoy).get(0);
        labores.registrarCumplimiento(labor, fecha, horas, "", hoy);
    }
}
