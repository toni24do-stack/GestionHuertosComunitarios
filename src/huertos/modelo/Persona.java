package huertos.modelo;

public abstract class Persona {
    private final String cedula;
    private String nombres;
    private String apellidos;
    private String telefono;

    public Persona(String cedula, String nombres, String apellidos, String telefono) {
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
    }

    public String getCedula() {
        return cedula;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public abstract String getDescripcion();

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
