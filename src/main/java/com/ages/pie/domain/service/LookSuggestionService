package com.ages.pie.domain.service;

import java.util.List;

/**
 * Exemplo de Domain Service — e, nesse caso específico, também uma Porta
 * (Port) de arquitetura hexagonal.
 *
 * Regra que envolve mais de uma entidade (ou depende de algo externo ao
 * domínio, como um serviço de IA) não deve morar dentro de uma única
 * Entity. Aqui, o domínio apenas declara "eu preciso de algo que sugira
 * looks" através de uma interface — sem saber COMO isso é feito.
 *
 * A implementação real (chamada HTTP para um serviço de IA, por exemplo)
 * fica em infrastructure, implementando esta interface. Isso é Inversão
 * de Dependência: o domínio não depende de infraestrutura, é o contrário.
 *
 * Regra para o time: se essa interface não tivesse @annotations do Spring
 * nem soubesse nada de HTTP/JSON, ela poderia ser testada sem subir o
 * contexto do Spring inteiro. Esse é o objetivo.
 */
public interface LookSuggestionService {

    LookSuggestion sugerirLook(Long userId);

    /**
     * Objeto de retorno simples do domínio (não é um DTO de API).
     * Contém apenas os ids das peças que compõem a sugestão.
     */
    record LookSuggestion(List<Long> wardrobeItemIds, List<Long> productIds) {
    }
}