# Plano do Projeto (5 Semanas)

Este projeto usa um motor de jogo rudimentar (ver `docs/ENGINE_PT.md`) para vos dar um ambiente jogável enquanto aplicam as práticas de POO ao construir um mini‑jogo. O foco é conceber e implementar um modelo de objetos limpo (classes, hierarquias, interfaces, classes abstratas, overriding) e cinco pilares de jogabilidade:

1) Terreno com vários tipos de vóxel/materiais
2) Objetos do mundo (itens/props) com interações significativas
3) Crafting (receitas e inventário)
4) NPCs (aliados e inimigos) com IA simples
5) Geração de terreno (com estruturas e aleatoriedade)

Cada pilar tem critérios objetivos, especificados abaixo.

Podem usar um LLM para **tarefas de âmbito reduzido** sob **regras estritas** (ver *Política de IA*). Devem **documentar toda a utilização de LLMs** num registo separado.

---

## Visão Semanal

- Semana 1 — Fundamentos: Classes, Objetos, Encapsulamento, Setup do Projeto
- Semana 2 — Hierarquias e Vóxeis: Herança, Interfaces, Múltiplos Materiais
- Semana 3 — Sistemas: Crafting + Inventário + NPCs básicos, Geração (rascunho)
- Semana 4 — Polimorfismo, IA e Erros: Aliados/Inimigos, Receitas e Persistência, Geração v2
- Semana 5 — Integração e Qualidade: Balanceamento, Performance
- Semana 6 — Polimento, Demo Final

Cada semana inclui objetivos, atividades, entregáveis e ênfase na rubrica. Os critérios objetivos por pilar estão abaixo.

---

## Critérios Objetivos e Avaliáveis (Pilares)

A) Terreno / Tipos de Vóxel (20%)
- Pelo menos 4 tipos de bloco no total (não incluindo `Air` e `Stone`; adicionar ≥4 novos como Wood/Sand/etc).
- Cada tipo define o seu material/visual distinto (cor/textura) e a sua solidez (isSolid).
- Deverá existir pelo menos uma distinção funcional entre cada tipo de terreno (Um exemplo seria gravidade, alterar a velocidade do jogador, etc)
- Tipos registados via `voxelWorld.getPalette().register(new YourBlockType())` e usados nos dados do mundo.

B) Objetos e Interações (20%)
- Pelo menos 5 `GameObject`s distintos (mistura de Item/Prop/Character) com responsabilidades claras.
- Pelo menos 3 objetos expõem interações (E) com efeitos visíveis/registados ou alterações de estado.
- Encapsulamento: campos privados/protegidos, APIs claras, Javadoc nos membros públicos.

C) Crafting e Inventário (20%)
- Inventário com pelo menos 6 espaços (ou capacidade razoável justificada).
- Pelo menos 4 receitas (ex.: combinar dois itens produz outro); receitas documentadas.
- Fluxo simples de crafting (teclas, menu ou consola) que valida entradas e produz resultados; tratamento de erros para receitas inválidas.

D) NPCs (Aliados/Inimigos) (20%)
- Pelo menos 2 aliados com um comportamento (seguir jogador, trocar/log, diálogo simples).
- Pelo menos 2 inimigos com um comportamento (patrulhar/perseguir/atacar com log ou efeito no jogador).
- Lógica de NPC expressa via interfaces/classes abstratas e métodos sobrescritos (ex.: `updateAI()`), demonstrando polimorfismo.

E) Geração de Terreno (rudimentar) (20%)
- População automática de terreno com um algoritmo simples (heightmap, ruído, ou regras em camadas) que coloca vários tipos de vóxel.
- (Opcional) Geração parametrizada (constantes/config) e determinística para a mesma seed.
- Código estruturado e comentado (helpers, funções pequenas) explicando a abordagem.

Nota: Total 100%. O docente pode ajustar pesos mantendo critérios objetivos.

---

## Semana 1 — Fundamentos

Objetivos
- Entender a barreira entre o motor, o jogo e o fluxo de build/execução.
- Definir classes simples com campos/métodos, construtores e encapsulamento.
- Usar composição de forma sensata.

Atividades
- Ler `docs/ENGINE_PT.md` (Visão Geral, Controlos, Registar Objetos, Paleta de vóxeis).
- Compilar e correr:

  ```bat
  gradlew.bat run
  ```
  - Ou no IntelliJ:
    - Abrir o projeto.
    - Configurar JDK 17+.
    - Executar a configuração `Run` criada automaticamente.
<br />
<br />
- Criar 2–3 classes concretas em `jogo.gameobject.*` (neutras em relação ao motor):
  - Ex.: `Chave`, `Porta`, `Tree`, `Slime`.
  - Adicionar campos, construtores, getters/setters, `toString()` e, se útil, `equals()/hashCode()`.
- Registar 1 objeto em `Test.simpleInitApp()` via `GameRegistry` e verificar a renderização.

Entregáveis
- Novas classes com Javadoc em cada tipo e método públicos.
- Esboço UML com classes e relações.
- Nota curta em `docs/` sobre escolhas e responsabilidades.

Rubrica (S1)
- Clareza de design de classes — 40%
- Encapsulamento e qualidade de API — 40%
- Funcionamento do build/execução — 20%

---

## Semana 2 — Hierarquias e Vóxeis

Objetivos
- Aplicar herança e interfaces; preferir composição salvo hierarquia claramente útil.
- Estender a paleta com múltiplos tipos e materiais.

Atividades
- Estender a hierarquia (`GameObject` → `Character`, `Terrain`, `Item`) com 1–2 subclasses.
- Adicionar ≥2 novos `VoxelBlockType` (ex.: Dirt, Wood) e registá‑los na paleta.
- Atribuir estes tipos a células do mundo e reconstruir malhas/física; visuais distintos.

Entregáveis
- UML atualizada com hierarquia e métodos sobrescritos.
- Código que regista e usa os novos tipos (screenshot/vídeo curto aceitável).

Rubrica (S2)
- Solidez da hierarquia e naming — 40%
- Tipos de vóxel (≥2 novos), materiais e uso — 60%

---

## Semana 3 — Sistemas: Crafting, Inventário, NPCs, Geração (rascunho)

Objetivos
- Desenhar interfaces para capacidades; usar classes abstratas para partilha de estado + comportamento parcial.
- Implementar crafting/inventário mínimo.
- Introduzir NPCs básicos; iniciar a geração.

Atividades
- Definir pelo menos uma interface de capacidade (ex.: `Interactable`, `Craftable`, etc) e uma base abstrata.
- Implementar inventário + crafting com ≥2 receitas esta semana (meta ≥4 até S5).
- Adicionar pelo menos 1 aliado e 1 inimigo com `updateAI()` básico (log/alteração de estado) e ligá‑los a um ciclo (AppState simples ou chamadas manuais por agora).
- Iniciar geração: função de altura ou ruído “perlin‑like” que coloque pelo menos 2 tipos de vóxel.

Entregáveis
- UML atualizada com interfaces e classes abstratas.
- Demo de 1–2 receitas e comportamento de NPC (logs/visuais).

Rubrica (S3)
- Design de interfaces/classes abstratas — 35%
- Crafting e inventário (inicial) — 35%
- NPCs (inicial) — 15%
- Geração (rascunho) — 15%

---

## Semana 4 — Polimorfismo, IA e Erros

Objetivos
- Usar polimorfismo sobre interfaces/abstratas em vez de concretas.
- Evoluir IA com máquinas de estados simples ou estratégias; diferenciar aliados/inimigos.
- Robustecer crafting com contratos claros e tratamento de erros.

Atividades
- Implementar uma pequena máquina de estados para inimigos (ex.: Idle → Chase → Attack) e um comportamento para aliados (ex.: Follow/Interact).
- Expandir crafting para ≥4 receitas; persistir inventário/resultados se possível.
- Melhorar a geração (segunda passagem): regra/bioma ou parâmetros; manter simples/rápido.
- Adicionar testes/harness pequenos para validar contratos e polimorfismo.

Entregáveis
- Demo: inimigos com pelo menos dois estados e uma transição; aliados com comportamento distinto.
- Testes/harness a mostrar polimorfismo e casos de erro.

Rubrica (S4)
- Polimorfismo e contratos — 30%
- IA (comportamentos e correção) — 30%
- Crafting (≥4 receitas, erros) — 20%
- Geração (v2) — 20%

---

## Semana 5 — Integração, Qualidade e Demo

Objetivos
- Integrar os pilares num ciclo coerente; polir e balancear.
- Refatorar para clareza, performance e manutenção.

Atividades
- Finalizar funcionalidades; garantir E (interagir) e movimento/salto.
- Balancear materiais, crafting e dificuldade de NPCs.
- Refatorar: remover duplicação, apertar visibilidade, completar documentação.
- Preparar walkthrough de 3–5 minutos: modelo de objetos, tipos de vóxel, crafting, NPCs, geração, e demo no motor.

Entregáveis
- Classes finais (neutras em relação ao motor do jogo) com Javadoc completo (classe/métodos).
- UML final e nota da arquitetura de software (1–2 páginas) sobre decisões e *trade‑offs*.
- Registo de utilização de IA num documento.

Rubrica (S5)
- Coerência de integração e completude — 40%
- Clareza do código e documentação — 40%
- Prontidão da demo e profissionalismo — 20%

---

## Lista de Verificação (Submissão)

- [ ] Código compila e corre.
- [ ] Tipos de vóxel: ≥6 (Air, Stone + ≥4 novos) com visuais, funcionalidade e solidez distintos
- [ ] Objetos: ≥5 distintos, ≥3 interativos (E)
- [ ] Crafting e inventário: ≥4 receitas, tratamento de erros
- [ ] NPCs: ≥2 aliados, ≥2 inimigos; comportamentos básicos
- [ ] Geração de terreno: determinística, usa ≥4 tipos de vóxel, parâmetros documentados
- [ ] Classes dos estudantes: neutras em relação ao motor, documentadas
- [ ] Diagramas UML (inicial → final) em `docs/`
- [ ] Testes/harness para polimorfismo/contratos (S4)
- [ ] Texto final (S5)
- [ ] Registo de IA com prompts, outputs, reflexão e referências de commits

---

## Política de Uso de IA (Ler com Atenção)

**Permitido** (com documentação):
- Geração de *boilerplate*: getters/setters, equals/hashCode/toString, DTOs simples.
- Esclarecimentos sobre sintaxe de linguagem/biblioteca.
- Brainstorming de edge cases ou cenários de teste.
- Rascunhos de comentários/Javadoc que depois revêm e editam.
- Algoritmos específicos para poupar trabalho.

**Não permitido**:
- Prompts “guarda‑chuva” (ex.: *"Constrói todo o meu projeto/jogo com base neste enunciado"*).
- Colar código gerado sem revisão/compreensão.
- Substituir o vosso esforço de design (ex.: pedir hierarquia/contratos ao LLM).
- Usar LLMs para código que depois **não consigam explicar/defender**.

Requisitos de documentação (obrigatório):
- Manter um registo contínuo num documento com:
  - Contexto (o que estavas a trabalhar, e porque é que precisavas de ajuda)
  - Prompt (texto exato)
  - Output (apenas a parte relevante)
  - Decisão (usar/alterar/rejeitar)
  - Reflexão (1–3 frases sobre o que aprendeste)
- Para os casos de "completar a linha", é boa ideia saber explicar o que foi adicionado, e talvez documentar.

Barra de qualidade
- A responsabilidade pelo código e correção é vossa. Rever, testar e reescrever.
- O output deve respeitar o vosso design e convenções de código.
- **Devem ser capazes de explicar qualquer código assistido por IA**.

Integridade académica
- Falta de documentação do uso de IA pode ser tratada como violação de integridade.
- Conteúdo gerado por IA que não consigam explicar será **desqualificado e a nota para o projeto será 0**.

---

## Dicas e Recursos

- Prefiram interfaces pequenas, com propósito e composição em vez de heranças profundas.
- Usem nomes claros e mantenham APIs públicas pequenas; escondam os internos.
- Façam um esboço em UML antes do código; atualizem à medida que o design e a estrutura evolui.
- Testes não precisam de ser complexos. Testes pequenos com prints/asserts chegam.
- Mantenham o âmbito semanal contido; entreguem algo pequeno mas completo cada semana.
