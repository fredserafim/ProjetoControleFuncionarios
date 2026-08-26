package com.example.shiftsync.entities;

public class Departamento {

    private String setor;

    private Turno turno;

    private Funcionario funcionario;

    public String getSetor(){
        return this.setor;}
    public void setSetor(String setor) {
        this.setor = setor;}

    public Turno getTurno() {
        return turno;}
    public void setTurno(Turno turno) {
        this.turno = turno;}

    public Funcionario getFuncionario() {
        return funcionario;}
    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;}
}
