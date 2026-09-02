package com.example.shiftsync.DTO;

public class GerenteRequest {

    public GerenteRequest() {
    }

    public GerenteRequest(Long id, String nome, String departamento, String turno) {
        this.id = id;
        this.nome = nome;
        this.departamento = departamento;
        this.turno = turno;
    }

    private Long id;

    private String nome;

    private String departamento;

    private  String turno;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }
}
