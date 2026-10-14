package com.example.shiftsync.utils;

package com.seuprojeto.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class CepUtils {

    private CepUtils() {
        // Construtor privado para evitar instanciação da classe utilitária
    }

    /**
     * Consulta o CEP na API ViaCEP e retorna uma String com o endereço completo concatenado,
     * sem a necessidade de criar classes DTO.
     *
     * @param cepInformado String contendo o CEP (com ou sem máscara)
     * @return String formatada (ex: "Rua X, Complemento - Bairro, Cidade/UF") ou null se não encontrar.
     */
    public static String buscarEndereco(String cepInformado) {
        if (cepInformado == null || cepInformado.isBlank()) {
            throw new IllegalArgumentException("O CEP não pode ser nulo ou vazio.");
        }

        // Limpa o CEP mantendo apenas os números
        String cepLimpo = cepInformado.replaceAll("\\D", "");

        if (cepLimpo.length() != 8) {
            throw new IllegalArgumentException("CEP inválido. Deve conter exatamente 8 dígitos.");
        }

        String url = "https://viacep.com.br/ws/" + cepLimpo + "/json/";

        try {
            RestTemplate restTemplate = new RestTemplate();

            // Faz a requisição trazendo o JSON como String pura
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.contains("\"erro\": true")) {
                return null;
            }

            // Converte o JSON diretamente para um Map, eliminando a necessidade de DTOs
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> dados = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, String>>() {});

            // Se o ViaCEP retornar o campo cep como nulo, o CEP não existe
            if (dados.containsKey("erro") || !dados.containsKey("cep") || dados.get("cep") == null) {
                return null;
            }

            String logradouro = dados.get("logradouro");
            String complemento = dados.get("complemento");
            String bairro = dados.get("bairro");
            String localidade = dados.get("localidade");
            String uf = dados.get("uf");

            // Concatena os campos de forma inteligente, ignorando os nulos ou vazios
            StringBuilder sb = new StringBuilder();

            if (logradouro != null && !logradouro.isBlank()) {
                sb.append(logradouro);
            }
            if (complemento != null && !complemento.isBlank()) {
                sb.append(", ").append(complemento);
            }
            if (bairro != null && !bairro.isBlank()) {
                sb.append(" - ").append(bairro);
            }
            if (localidade != null && !localidade.isBlank()) {
                sb.append(", ").append(localidade);
            }
            if (uf != null && !uf.isBlank()) {
                sb.append("/").append(uf);
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar o serviço de CEP: " + e.getMessage(), e);
        }
    }
}