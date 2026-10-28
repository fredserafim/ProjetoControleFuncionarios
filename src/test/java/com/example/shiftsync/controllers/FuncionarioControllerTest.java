package com.example.shiftsync.controllers;

import com.example.shiftsync.entities.Departamento;
import com.example.shiftsync.entities.Funcionario;
import com.example.shiftsync.entities.AtribuicaoTurno;
import com.example.shiftsync.entities.Turno;
import com.example.shiftsync.repository.AtribuicaoTurnoRepository;
import com.example.shiftsync.repository.DepartamentoRepository;
import com.example.shiftsync.repository.FuncionarioRepository;
import com.example.shiftsync.repository.TurnoRopository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa os endpoints de funcionário usando repositórios simulados.
 *
 * Abrange associações a departamentos e turnos, vigências históricas,
 * duplicidade de atribuições e respostas de erro HTTP.
 */
@ExtendWith(MockitoExtension.class)
class FuncionarioControllerTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private DepartamentoRepository departamentoRepository;

    @Mock
    private TurnoRopository turnoRepository;

    @Mock
    private AtribuicaoTurnoRepository atribuicaoTurnoRepository;

    @InjectMocks
    private FuncionarioController funcionarioController;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(funcionarioController).build();
    }

    @Test
    void consultaFuncionariosPorIdEDepartamento() throws Exception {
        Funcionario funcionario = funcionario(7L, departamento(3L, "Financeiro"));
        when(funcionarioRepository.findAll()).thenReturn(List.of(funcionario));
        when(funcionarioRepository.findById(7L)).thenReturn(Optional.of(funcionario));
        when(funcionarioRepository.findByDepartamento_Id(3L)).thenReturn(List.of(funcionario));

        mockMvc.perform(get("/funcionario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].departamentoId").value(3));

        mockMvc.perform(get("/funcionario/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Silva"));

        mockMvc.perform(get("/funcionario/3/departamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departamento").value("Financeiro"));
    }

    @Test
    void cadastraFuncionarioComDepartamentoPorId() throws Exception {
        when(departamentoRepository.findById(3L))
                .thenReturn(Optional.of(departamento(3L, "Financeiro")));
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocacao -> {
            Funcionario funcionario = invocacao.getArgument(0);
            funcionario.setId(7L);
            return funcionario;
        });

        mockMvc.perform(post("/funcionario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva",
                                  "cpf": "12345678900",
                                  "telefone": "11999990000",
                                  "departamentoId": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.departamentoId").value(3));
    }

    @Test
    void cadastraFuncionarioComJornadaEGravaVigencia() throws Exception {
        Turno turno = turno(11L);
        when(departamentoRepository.findById(3L))
                .thenReturn(Optional.of(departamento(3L, "Financeiro")));
        when(turnoRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(turno));
        when(atribuicaoTurnoRepository
                .findFirstByFuncionario_IdAndVigenciaFimIsNullOrderByVigenciaInicioDesc(7L))
                .thenReturn(Optional.empty());
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocacao -> {
            Funcionario funcionario = invocacao.getArgument(0);
            funcionario.setId(7L);
            return funcionario;
        });

        mockMvc.perform(post("/funcionario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva",
                                  "cpf": "12345678900",
                                  "telefone": "11999990000",
                                  "departamentoId": 3,
                                  "turnoId": 11,
                                  "turnoVigenciaInicio": "2026-10-26T08:30:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnoId").value(11))
                .andExpect(jsonPath("$.turno").value("Jornada diurna"))
                .andExpect(jsonPath("$.turnoVigenciaInicio").value("2026-10-26T08:30:00"));

        verify(atribuicaoTurnoRepository).save(any());
    }

    @Test
    void naoDuplicaHistoricoQuandoJornadaAtualPermaneceSelecionada() throws Exception {
        Turno turno = turno(11L);
        Funcionario funcionario = funcionario(7L, departamento(3L, "Financeiro"));
        funcionario.setJornada(turno);
        funcionario.setJornadaVigenciaInicio(LocalDateTime.of(2026, 10, 26, 8, 30));
        when(funcionarioRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(funcionario));
        when(funcionarioRepository.save(any(Funcionario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        mockMvc.perform(put("/funcionario/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "turno": "Jornada diurna",
                                  "turnoId": 11,
                                  "turnoVigenciaInicio": "2026-10-26T08:30:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnoId").value(11));

        org.mockito.Mockito.verifyNoInteractions(atribuicaoTurnoRepository);
        org.mockito.Mockito.verifyNoInteractions(turnoRepository);
    }

    @Test
    void retorna409SemPersistirQuandoNovaAtribuicaoConflitaComHistorico() throws Exception {
        Funcionario funcionario = funcionario(7L, departamento(3L, "Financeiro"));
        Turno turnoAnterior = turnoSemanal(11L, "Anterior", LocalTime.of(8, 0), LocalTime.of(10, 0));
        Turno turnoEmConflito = turnoSemanal(12L, "Outra jornada", LocalTime.of(11, 0), LocalTime.of(13, 0));
        Turno novoTurno = turnoSemanal(13L, "Novo turno", LocalTime.of(9, 0), LocalTime.of(12, 0));
        LocalDateTime inicioHistorico = LocalDateTime.of(2026, 10, 19, 0, 0);
        LocalDateTime vigenciaNova = LocalDateTime.of(2026, 10, 26, 0, 0);
        AtribuicaoTurno atribuicaoAnterior = atribuicao(
                1L, funcionario, turnoAnterior, inicioHistorico, null);
        AtribuicaoTurno atribuicaoConflitante = atribuicao(
                2L, funcionario, turnoEmConflito, vigenciaNova,
                LocalDateTime.of(2026, 10, 28, 0, 0));

        when(funcionarioRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(funcionario));
        when(turnoRepository.findByIdForUpdate(13L)).thenReturn(Optional.of(novoTurno));
        when(atribuicaoTurnoRepository
                .findFirstByFuncionario_IdAndVigenciaFimIsNullOrderByVigenciaInicioDesc(7L))
                .thenReturn(Optional.of(atribuicaoAnterior));
        when(atribuicaoTurnoRepository.findByFuncionario_IdOrderByVigenciaInicioAsc(7L))
                .thenReturn(List.of(atribuicaoAnterior, atribuicaoConflitante));

        mockMvc.perform(put("/funcionario/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "turno": "Novo turno",
                                  "turnoId": 13,
                                  "turnoVigenciaInicio": "2026-10-26T00:00:00"
                                }
                                """))
                .andExpect(status().isConflict());

        verify(funcionarioRepository, never()).save(any(Funcionario.class));
        verify(atribuicaoTurnoRepository, never()).save(any(AtribuicaoTurno.class));
        org.junit.jupiter.api.Assertions.assertNull(atribuicaoAnterior.getVigenciaFim());
    }

    @Test
    void retornaNaoEncontradoQuandoDepartamentoPorIdNaoExiste() throws Exception {
        when(departamentoRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/funcionario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva",
                                  "departamentoId": 99
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void retornaRequisicaoInvalidaQuandoDepartamentoNaoFoiInformado() throws Exception {
        mockMvc.perform(post("/funcionario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/funcionario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva",
                                  "departamento": "  "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizaFuncionarioUsandoNomeDeDepartamentoComEspacosExcedentes() throws Exception {
        Funcionario funcionario = funcionario(7L, departamento(2L, "Antigo"));
        when(funcionarioRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(funcionario));
        when(departamentoRepository.findByDepartamentoIgnoreCase("Financeiro"))
                .thenReturn(Optional.of(departamento(3L, "Financeiro")));
        when(funcionarioRepository.save(any(Funcionario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        mockMvc.perform(put("/funcionario/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "telefone": "11888880000",
                                  "endereco": "Rua Um",
                                  "funcao": "Analista",
                                  "turno": "Manhã",
                                  "departamento": "  Financeiro  "
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departamentoId").value(3))
                .andExpect(jsonPath("$.telefone").value("11888880000"));

        verify(departamentoRepository).findByDepartamentoIgnoreCase("Financeiro");
    }

    @Test
    void atualizaNomeECpfDoFuncionario() throws Exception {
        Funcionario funcionario = funcionario(7L, departamento(3L, "Financeiro"));
        funcionario.setCpf("12345678900");
        when(funcionarioRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(funcionario));
        when(departamentoRepository.findById(3L))
                .thenReturn(Optional.of(departamento(3L, "Financeiro")));
        when(funcionarioRepository.save(any(Funcionario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        mockMvc.perform(put("/funcionario/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Souza",
                                  "cpf": "98765432100",
                                  "telefone": "11888880000",
                                  "endereco": "Rua Um",
                                  "funcao": "Analista",
                                  "turno": "Manhã",
                                  "departamentoId": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.cpf").value("98765432100"));
    }

    @Test
    void retornaRequisicaoInvalidaQuandoDepartamentoPorNomeNaoExiste() throws Exception {
        Funcionario funcionario = funcionario(7L, departamento(2L, "Antigo"));
        when(funcionarioRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(funcionario));
        when(departamentoRepository.findByDepartamentoIgnoreCase("Inexistente"))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/funcionario/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "departamento": "Inexistente"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizaEDesativaStatusDoFuncionario() throws Exception {
        Funcionario funcionario = funcionario(7L, departamento(3L, "Financeiro"));
        when(funcionarioRepository.findById(7L)).thenReturn(Optional.of(funcionario));
        when(funcionarioRepository.save(any(Funcionario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        mockMvc.perform(patch("/funcionario/7/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "I"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Status atualizado com sucesso!"));

        mockMvc.perform(delete("/funcionario/7"))
                .andExpect(status().isOk());

        verify(funcionarioRepository, org.mockito.Mockito.times(2)).save(funcionario);
    }

    private static Funcionario funcionario(Long id, Departamento departamento) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(id);
        funcionario.setNome("Ana Silva");
        funcionario.setTelefone("11999990000");
        funcionario.setDepartamento(departamento);
        return funcionario;
    }

    private static Departamento departamento(Long id, String nome) {
        Departamento departamento = new Departamento();
        departamento.setId(id);
        departamento.setDepartamento(nome);
        return departamento;
    }

    private static Turno turno(Long id) {
        Turno turno = new Turno();
        turno.setId(id);
        turno.setNome("Jornada diurna");
        turno.setStatus("A");
        turno.setTipoJornada("DIURNA");
        turno.setTipoEscala("SEMANAL_FIXA");
        turno.setDiasSemana(Set.of(DayOfWeek.MONDAY));
        turno.setHorarioInicio(LocalTime.of(8, 0));
        turno.setHorarioFim(LocalTime.of(17, 0));
        return turno;
    }

    private static Turno turnoSemanal(Long id, String nome, LocalTime inicio, LocalTime fim) {
        Turno turno = turno(id);
        turno.setNome(nome);
        turno.setHorarioInicio(inicio);
        turno.setHorarioFim(fim);
        return turno;
    }

    private static AtribuicaoTurno atribuicao(
            Long id, Funcionario funcionario, Turno turno,
            LocalDateTime inicio, LocalDateTime fim) {
        AtribuicaoTurno atribuicao = new AtribuicaoTurno();
        atribuicao.setId(id);
        atribuicao.setFuncionario(funcionario);
        atribuicao.setTurno(turno);
        atribuicao.setVigenciaInicio(inicio);
        atribuicao.setVigenciaFim(fim);
        return atribuicao;
    }
}
