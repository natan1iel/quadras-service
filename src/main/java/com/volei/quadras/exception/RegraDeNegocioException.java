package com.volei.quadras.exception;

import org.springframework.http.HttpStatus;

public class RegraDeNegocioException extends RuntimeException {
    private final HttpStatus status;
    private final String codigo;

    private RegraDeNegocioException(HttpStatus status, String codigo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
    }

    public static RegraDeNegocioException invalida(String mensagem) {
        return new RegraDeNegocioException(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", mensagem);
    }

    public static RegraDeNegocioException negada(String mensagem) {
        return new RegraDeNegocioException(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", mensagem);
    }

    public static RegraDeNegocioException quadraNaoEncontrada(Long quadraId) {
        return new RegraDeNegocioException(HttpStatus.NOT_FOUND, "QUADRA_NAO_ENCONTRADA",
                "Quadra " + quadraId + " não encontrada");
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }
}
