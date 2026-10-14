package com.example.shiftsync.utils;

public class OnlyNumbers {

    private OnlyNumbers() {
        // Construtor privado para evitar a instanciação da classe utilitária
    }

    /**
     * Remove todos os caracteres não numéricos de uma string,
     * mantendo apenas os dígitos (0 a 9).
     *
     * @param valor String original contendo máscaras ou caracteres especiais.
     * @return String contendo apenas os números, ou string vazia caso o valor seja nulo/branco.
     */
    public static String onlyNumbers(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        // Substitui tudo o que não for dígito por uma string vazia
        return valor.replaceAll("\\D", "");
    }
}
