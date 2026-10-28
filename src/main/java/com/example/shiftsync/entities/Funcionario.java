package com.example.shiftsync.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

/**
 * Cadastro do funcionário usado pelas consultas administrativas e pelo controle
 * de jornada. O departamento e a jornada direta são relações com outras entidades;
 * a vigência histórica das mudanças de jornada é mantida em {@link AtribuicaoTurno}.
 *
 * A entidade é mapeada pelo JPA, e o campo id usa geração de identidade do banco.
 * Métodos get/set são acessores simples usados pelo Hibernate, DTOs e controllers.
 */
@Entity
public class Funcionario {


        public Funcionario(){}

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)// CRIA SIQUENCI
        private Long id;

        private String nome;

        private String cpf;

        private String telefone;

        private String endereco;

        private String turno;

        /** Departamento atual do funcionário, armazenado por uma chave estrangeira. */
        @ManyToOne
        @JoinColumn(name = "departamento_id")
        private Departamento departamento;

        /** Jornada de referência mantida para compatibilidade com o cadastro atual. */
        @ManyToOne
        @JoinColumn(name = "turno_jornada_id")
        private Turno jornada;

        private LocalDateTime jornadaVigenciaInicio;

        private String funcao;

        private LocalDateTime dataCadastro;

        private LocalDateTime dataAtualizacao;

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

        public String getCpf() {
                return cpf;
        }
        public void setCpf(String cpf) {
                this.cpf = cpf;
        }

        public String getTelefone() {
                return telefone;
        }
        public void setTelefone(String telefone) {
                this.telefone = telefone;
        }

        public String getEndereco() {
                return endereco;
        }
        public void setEndereco(String endereco) {
                this.endereco = endereco;
        }

        public String getTurno() {
                return turno;
        }
        public void setTurno(String turno) {
                this.turno = turno;
        }

        public Departamento getDepartamento() {
                return departamento;
        }
        public void setDepartamento(Departamento departamento) {
                this.departamento = departamento;
        }

        public Turno getJornada() {
                return jornada;
        }
        public void setJornada(Turno jornada) {
                this.jornada = jornada;
        }

        public LocalDateTime getJornadaVigenciaInicio() {
                return jornadaVigenciaInicio;
        }
        public void setJornadaVigenciaInicio(LocalDateTime jornadaVigenciaInicio) {
                this.jornadaVigenciaInicio = jornadaVigenciaInicio;
        }

        public String getFuncao() {
                return funcao;
        }
        public void setFuncao(String funcao) {
                this.funcao = funcao;
        }

        public LocalDateTime getDataCadastro() {
                return dataCadastro;
        }
        public void setDataCadastro(LocalDateTime dataCadastro) {
                this.dataCadastro = dataCadastro;
        }

        public LocalDateTime getDataAtualizacao() {
                return dataAtualizacao;
        }
        public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
                this.dataAtualizacao = dataAtualizacao;
        }

        public String getStatus() {
                return status;
        }
        public void setStatus(String status) {
                this.status = status;
        }
}
