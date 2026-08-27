package com.example.shiftsync.entities;

public class Turno {

    public Turno(){}

    private Long horario;

    private String periodo;

    private Long horaExtra;

    public String getPeriodo(){
        return this.periodo;}
    public void setPeriodo(String periodo){
        this.periodo = periodo;}

    public Long getHorario(){
        return this.horario;}
    public void setHorario(Long horario){
        this.horario = horario;}

    public Long getHoraExtra(){
        return this.horaExtra;}
    public void setHoraExtra(Long horaExtra) {
        this.horaExtra = horaExtra;
    }
}
