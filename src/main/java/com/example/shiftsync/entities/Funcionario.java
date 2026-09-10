package com.example.shiftsync.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Funcionario {


        public Funcionario(){}

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String nome;

        //private Departamento departamento;

        //private  Turno turno;

        private String funcao;

        private LocalDateTime dataCadastro;

        private LocalDateTime dataAtualização;

        private String status;


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

        public Turno getTurno() {
                return turno;
        }
        public void setTurno(Turno turno) {
                this.turno = turno;
        }

        public String getFuncao() {
                return funcao;
        }
        public void setFuncao(String funcao) {
                this.funcao = funcao;
        }

        public Departamento getDepartamento() {
                return departamento;
        }
        public void setDepartamento(Departamento departamento) {
                this.departamento = departamento;
        }

        public LocalDateTime getDataCadastro() {
                return dataCadastro;
        }
        public void setDataCadastro(LocalDateTime dataCadastro) {
                this.dataCadastro = dataCadastro;
        }

        public LocalDateTime getDataAtualização() {
                return dataAtualização;
        }
        public void setDataAtualização(LocalDateTime dataAtualização) {
                this.dataAtualização = dataAtualização;
        }

        public String getStatus() {
                return status;
        }
        public void setStatus(String status) {
                this.status = status;
        }
}
