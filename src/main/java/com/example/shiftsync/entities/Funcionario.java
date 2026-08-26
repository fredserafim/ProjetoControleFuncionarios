package com.example.shiftsync.entities;

public class Funcionario {

        private Long id;

        private String nome;

        private Departamento departamento;

        private  Turno turno;

        private String funcao;


        public Long getId() {
                return id;}
        public void setId(Long id) {
                this.id = id;}

        public String getNome() {
                return nome;}
        public void setNome(String nome) {
                this.nome = nome;}

        public Turno getTurno() {
                return turno;
        }

        public void setTurno(Turno turno) {
                this.turno = turno;}
}
