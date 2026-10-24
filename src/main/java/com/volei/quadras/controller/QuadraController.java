package com.volei.quadras.controller;

import com.volei.quadras.dto.DisponibilidadeDTO;
import com.volei.quadras.dto.QuadraRequest;
import com.volei.quadras.dto.QuadraResponse;
import com.volei.quadras.exception.RegraDeNegocioException;
import com.volei.quadras.service.QuadraService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/quadras")
public class QuadraController {
    private final QuadraService quadraService;

    public QuadraController(QuadraService quadraService) {
        this.quadraService = quadraService;
    }

    @PostMapping
    public ResponseEntity<QuadraResponse> cadastrar(
            @RequestHeader(value = "X-Usuario-Papel", required = false) String papel,
            @RequestBody QuadraRequest requisicao) {
        exigirAdmin(papel);
        return ResponseEntity.status(HttpStatus.CREATED).body(quadraService.cadastrar(requisicao));
    }

    @GetMapping
    public List<QuadraResponse> listar() {
        return quadraService.listarAtivas();
    }

    @GetMapping("/{id}")
    public QuadraResponse detalhar(@PathVariable Long id) {
        return quadraService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public QuadraResponse atualizar(@RequestHeader(value = "X-Usuario-Papel", required = false) String papel,
            @PathVariable Long id, @RequestBody QuadraRequest requisicao) {
        exigirAdmin(papel);
        return quadraService.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@RequestHeader(value = "X-Usuario-Papel", required = false) String papel,
            @PathVariable Long id) {
        exigirAdmin(papel);
        quadraService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/disponibilidade")
    public DisponibilidadeDTO disponibilidade(@PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return quadraService.consultarDisponibilidade(id, data);
    }

    private void exigirAdmin(String papel) {
        if (!"ADMIN".equals(papel)) {
            throw RegraDeNegocioException.negada("Apenas ADMIN pode executar esta operação");
        }
    }
}
