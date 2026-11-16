# Sistema de gestión de huertos comunitarios

Proyecto final de **ISWZ1103 – Programación II** (Universidad de las Américas).
Aplicación de escritorio en **Java 17 + Swing** para organizar un huerto comunitario urbano:
participantes, parcelas y cultivos, labores de cuidado, cosechas y su reparto.

## Cómo abrirlo en IntelliJ IDEA

1. Descomprimir el archivo `.zip`.
2. IntelliJ → **File › Open…** → seleccionar la carpeta `HuertosComunitarios`.
3. Si pide un JDK: **File › Project Structure › Project › SDK** y elegir un JDK 17 o superior.
4. Ejecutar la configuración **«Aplicación (Main)»** (clase `huertos.interfaz.Main`).
5. Pruebas: configuración **«Pruebas de reglas de negocio»** (clase `huertos.pruebas.PruebasReglasNegocio`,
   carpeta `test`). Resultado esperado: `36 pruebas aprobadas, 0 fallidas`.

No usa librerías externas. Al iniciar se carga el huerto de ejemplo «La Esperanza» con fechas relativas a hoy.
Los datos se guardan en memoria mientras la aplicación está abierta.

## Paquetes

| Paquete | Contenido |
| --- | --- |
| `huertos.modelo` | Entidades del huerto: `Huerto`, `Persona`, `Participante`, `Parcela`, `TipoCultivo`, `Cultivo`, `Labor`, `Cosecha`, `Reparto`, `ItemReparto` y sus enumeraciones |
| `huertos.negocio` | Gestores con las reglas de cada módulo y clases de resultados (`AgendaSemanal`, `ReporteProduccion`…) |
| `huertos.interfaz` | `Main`, `VentanaPrincipal` y una pantalla por módulo |
| `huertos.pruebas` (carpeta `test`) | `PruebasReglasNegocio` |

## Módulos

| Módulo | Requerimientos | Gestor | Pantalla |
| --- | --- | --- | --- |
| M1 – Gestión de participantes (Líder) | RF-01 a RF-03 | `GestorParticipantes` | `PanelParticipantes` |
| M2 – Gestión de parcelas y cultivos (Análisis y diseño) | RF-04 a RF-06 | `GestorCultivos` | `PanelCultivos` |
| M3 – Gestión de labores (Desarrollo) | RF-07 a RF-09 | `GestorLabores` | `PanelLabores` |
| M4 – Gestión de cosechas y reparto (Pruebas) | RF-10 a RF-12 | `GestorCosechas` | `PanelCosechas` |

## Diagramas UML

En la carpeta `docs`: `entidades.png` (paquete modelo), `enumeraciones.png` y `gestores.png` (paquete negocio).
Los archivos `.dot` son la fuente de los diagramas (Graphviz).
