package com.Smtd.GestionPerteDoc.dtos;

public class RechercherDeclarantRequest {
    private String email;
    private String numNina;
    private String numPassePort;
    private String numCarteIdentite;

    // Getters et setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNumNina() { return numNina; }
    public void setNumNina(String numNina) { this.numNina = numNina; }

    public String getNumPassePort() { return numPassePort; }
    public void setNumPassePort(String numPassePort) { this.numPassePort = numPassePort; }

    public String getNumCarteIdentite() { return numCarteIdentite; }
    public void setNumCarteIdentite(String numCarteIdentite) { this.numCarteIdentite = numCarteIdentite; }
}

