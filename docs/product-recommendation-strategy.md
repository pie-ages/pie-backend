# Estratégia de recomendação de produtos (PIE-104)

Primeira versão do algoritmo de recomendação de peças: determinístico, sem ML/IA, baseado nas preferências já disponíveis do usuário.

## Endpoint

`GET /users/me/recommendations` (autenticado, bearer) → `200 OK` com lista de `RecommendedProductDTO` ordenada por score decrescente.

## Critérios e pontuação

Score máximo 100 pontos (`RecommendationScorer`, uma **Port** declarada em `domain/service`; a implementação da v1 é `HeuristicRecommendationScorer`, em `infrastructure/service`):

| Critério | Peso | Regra |
|---|---|---|
| Estilo | 50 | cobertura proporcional: `50 * |estilos do produto ∩ estilos do usuário| / |estilos do produto|` |
| Cor | 50 | binário: 50 se a cor do produto (normalizada) está nas cores favoritas |

Normalização: `trim` + minúsculas (`Locale.ROOT`) nos dois lados, com responsabilidades separadas. Quem chama (`RecommendationService.collectUserStyleIds` / `collectFavoriteColors`) normaliza as preferências; o scorer normaliza os estilos do produto. Estilos são validados contra os IDs canônicos de `ProductStyle`.

Preferências do usuário (união):
- `User.styles` (`List<ProductStyle>`)
- `BodyProfile.identifiedStyle` (resultado do quiz de estilo)
- `BodyProfile.favoriteColors` (array de strings)

Sem nenhuma preferência → lista vazia (sem fallback neutro na v1).

## Seleção e ordenação

- Catálogo filtrado: `active = true` AND `status = PUBLISHED` (`ProductRepository.findByActiveTrueAndStatus`).
- Produtos com score 0 são descartados.
- Ordenação: score desc; desempate por `createdAt` desc.
- Limite: 20 por padrão (`recommendForUser(userId, limit)` permite outro).

## Onde mexer para evoluir

- **Pesos**: constantes `STYLE_WEIGHT` / `COLOR_WEIGHT` em `HeuristicRecommendationScorer`.
- **Novos critérios**: somar componentes em `HeuristicRecommendationScorer.score` e coletar a preferência em `RecommendationService`.
- **Outro critério**: implementar `RecommendationScorer` (por exemplo, um modelo de IA) em `infrastructure` — `RecommendationService` só conhece a interface.
- **Colorimetria (fora da v1)**: `BodyProfile.colorPalette`/`zylaPalette` mapeados para IDs de `ProductColor` — planejado e adiado por decisão de escopo.

## Débitos técnicos

- `Product.color` é texto livre (não enum). A normalização atual tolera diferenças de caixa/espaços, mas valores fora dos IDs canônicos de `ProductColor` não pontuam. Migrar para enum/dicionário canônico em versão futura.
- `identifiedStyle` em `BodyProfile` é `String`; inválidos são ignorados silenciosamente.
