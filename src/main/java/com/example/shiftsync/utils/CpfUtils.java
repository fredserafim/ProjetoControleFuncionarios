package com.example.shiftsync.utils;

/**
 * Funções estáticas para normalizar e verificar o dígito verificador de CPF.
 *
 * O construtor é privado porque esta classe não guarda estado: ela apenas reúne
 * operações reutilizáveis.
 */
public class CpfUtils {

    private CpfUtils() {
        // Construtor privado para evitar instanciação da classe utilitária
    }

    /**
     * Valida um número de CPF (com ou sem máscara).
     *
     * @param cpf String contendo o CPF.
     * @return true se o CPF for válido, false caso contrário.
     */
    public static boolean isValidCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return false;
        }

        String cpfLimpo = normalizar(cpf);

        // Verifica se tem 11 dígitos
        if (cpfLimpo.length() != 11) {
            return false;
        }

        // Rejeita CPFs com todos os dígitos iguais (ex: 000.000.000-00, 111.111.111-11)
        if (cpfLimpo.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            // Multiplica os nove primeiros números por pesos decrescentes e compara o dígito calculado.
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpfLimpo.charAt(i)) * (10 - i);
            }
            int primeiroDigito = 11 - (soma % 11);
            if (primeiroDigito > 9) {
                primeiroDigito = 0;
            }

            if (primeiroDigito != Character.getNumericValue(cpfLimpo.charAt(9))) {
                return false;
            }

            // Repete o cálculo incluindo o primeiro dígito verificador.
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpfLimpo.charAt(i)) * (11 - i);
            }
            int segundoDigito = 11 - (soma % 11);
            if (segundoDigito > 9) {
                segundoDigito = 0;
            }

            return segundoDigito == Character.getNumericValue(cpfLimpo.charAt(10));

        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Remove a máscara e quaisquer caracteres que não sejam dígitos.
     *
     * @param cpf CPF original; pode ser nulo.
     * @return sequência numérica, ou texto vazio quando a entrada é nula.
     */
    public static String normalizar(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }
}