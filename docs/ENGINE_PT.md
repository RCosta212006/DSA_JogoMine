# Visão Geral do Motor (Engine) e Guia para Estudantes

Este repositório fornece uma “casca” de motor (engine shell) pequena e opinativa construída sobre o jMonkeyEngine (jME3) onde o vosso projeto/jogo irá assentar. Foi pensado para uma unidade curricular de Programação Orientada a Objetos, em que os estudantes implementam a camada de jogo usando classes simples, neutras em relação ao motor, enquanto a camada do motor trata de renderização, física, entrada (input) e ciclo de vida da cena.

O objetivo: permitir‑vos praticar o design OO nuclear (hierarquias de classes, composição, comportamentos, tratamento de erros) sem ficarem bloqueados por APIs de rendering ou de física.

---

## Visão Geral

- Camada do estudante
  - Escrevem classes neutras em relação ao motor dentro de `jogo.gameobject.*`.
  - Classe base: `GameObject` (guarda apenas um nome e uma posição: `jogo.framework.math.Vec3`).
  - Hierarquia fornecida para estender:
    - `GameObject`
      - `character.Character` (ex.: `Player`, `NPC`)
      - `terrain.Terrain` (ex.: `Árvore`, `Rocha`) com metadados de semi‑extensões (half‑extents)
      - `item.Item` (ex.: `Chave`, `Picareta`)
  - Estas classes contêm apenas estado e lógica. Não importam o motor (sem tipos jME).

- Camada do motor (não modificar salvo indicação)
  - AppStates
    - `InputAppState` — centraliza mapeamentos de entrada e expõe o estado de input.
    - `WorldAppState` — constrói o mundo (mundo de vóxeis simples), gere luzes e edições de vóxeis.
    - `PlayerAppState` — controlador de personagem em primeira pessoa (física + câmara) e uma luz que segue a cabeça.
    - `RenderAppState` — renderiza `GameObject`s dos estudantes com malhas de placeholder simples, mantém‑nos sincronizados e limpa os visuais quando objetos são removidos do registo.
    - `InteractionAppState` — faz raycast a partir da câmara e encaminha interações para itens dos estudantes; também localiza blocos de vóxel para um exercício dos estudantes.
    - `HudAppState` — sobreposição (GUI) minimalista (mira).
  - Utilitários
    - `GameRegistry` — registo (engine‑side) de objetos dos estudantes a renderizar/interagir.
    - `RenderIndex` — mapeamento de `Spatial` (jME) de volta para `GameObject` para seleção (picking) e testes de interseção de raios.
    - `voxel.*` — representação do mundo de vóxeis, paleta de blocos e geração de malha (interno do motor).
      - Os tipos de vóxel são baseados em classes (`VoxelBlockType`); a paleta por omissão auto‑regista `AirBlockType` (id 0, não sólido) e `StoneBlockType` (id 1, sólido). Registem mais tipos com `voxelWorld.getPalette().register(new YourBlockType())`.
    - `util.ProcTextures` — texturas procedimentais para visuais do motor.

- Fronteira
  - A camada do motor usa classes jME3. A camada do estudante não.
  - Os AppStates do motor adaptam os vossos dados a visuais/física/interação.

---

## Controlos (em execução)

- Captura do rato: Tab (cursor oculto quando capturado)
- Movimento: W/A/S/D
- Sprint: Shift esquerdo
- Salto: Espaço (requer estar no chão)
- Interagir: E (lançamento de raios — raycast — a partir da câmara; chama `Item.onInteract()`; se não encontrar um item, encontra um bloco de vóxel e deixa um TODO para os estudantes, ver `InteractionAppState.update()`)
- Partir vóxel: Botão esquerdo do rato
- Reaparecer (respawn): R (teleporta para um ponto seguro)
- Alternar rendering: L (alterna Sombreamento/Lit ↔ Unshaded, Wireframe On/Off, Culling On/Off)
  - Predefinições no arranque: Shading=On, Wireframe=Off, Culling=On
- ESC: Sair (comportamento padrão do jME)

---

## Arquitetura do Motor

- Os AppStates orquestram o motor: initialize → update → cleanup.
- A física usa `BetterCharacterControl` do Bullet/Minie (gravidade, caminhar, salto).
- Mundo de vóxeis
  - Uma grelha compacta 16×16×16 fornece uma superfície simples.
  - A cena inicial inclui exatamente um bloco de Stone sob o jogador para arrancar de forma minimal.
  - Os tipos de vóxel vêm de uma paleta de instâncias `VoxelBlockType` (por omissão: Air, Stone). Docentes/estudantes podem adicionar tipos (ex.: Dirt, Wood) registando‑os na paleta antes de gerar a malha.
- O rendering e a interação dos vossos objetos são fornecidos por duas camadas:
  - `RenderAppState` cria geometrias de placeholder (cilindros para personagens, caixas para terreno/itens), regista‑as no `RenderIndex`, sincroniza posições a partir de `GameObject.position` e remove os visuais de objetos retirados do registo.
  - `InteractionAppState` escuta o input Interagir (E); primeiro tenta resolver um `GameObject` renderizado (ex.: `Item`) sob a mira; se não houver, encontra um bloco de vóxel e imprime um TODO — este é o ponto de extensão onde os estudantes podem implementar interações com vóxeis (alternar/colocar/inspecionar).

Notas:
- As malhas/cores de placeholder são predefinições; podem estender o `RenderAppState` com adapters (ex.: modelos personalizados por classe/interface).

---

## Camada do Estudante: Como Estender

- Criem novos objetos de jogo em `jogo.gameobject.*` sem imports do motor.
  - Exemplos:
    - `class Slime extends Character { ... }`
    - `class Tree extends Terrain { ... }`
    - `class Key extends Item { @Override public void onInteract() { /* lógica */ } }`

- Guardem apenas dados e lógica de jogo
  - Exemplos: vida, inventário, estados (idle/attacking), temporizadores (cooldowns).
  - Usem `getPosition()/setPosition()` para gerir posições lógicas.

- Interação
  - Implementem `Item.onInteract()` para os itens afetados pela tecla E.
  - Para interação com vóxeis, abram o `InteractionAppState` e implementem o TODO onde o motor reporta as coordenadas do vóxel alvo.

- Rendering
  - O motor renderiza uma forma genérica por tipo (caixa/cilindro). Não adicionem código jME.
  - Para alterar visuais no futuro, adicionem um adapter no `RenderAppState` (lado do motor).

- Tipos de vóxel (avançado)
  - Adicionem um novo tipo de bloco estendendo `VoxelBlockType` (ex.: `DirtBlockType`) e registrem‑no: `byte DIRT_ID = voxelWorld.getPalette().register(new DirtBlockType());`
  - Atribuam células no mundo ao ID do vosso novo tipo antes de reconstruir malhas/física.

---

## Registar Objetos para Rendering/Interação

A inicialização do motor do jogo é feita em `Test.simpleInitApp()`. Para adicionar objetos que querem ver e com os quais querem interagir (objetos não‑vóxel):

```java
// Criar o registo partilhado do motor
GameRegistry registry = new GameRegistry();
RenderIndex renderIndex = new RenderIndex();
stateManager.attach(new RenderAppState(rootNode, assetManager, registry, renderIndex));
stateManager.attach(new InteractionAppState(rootNode, cam, input, renderIndex, world));

// Exemplos: adicionar objetos neutros em relação ao motor
// MyProp prop = new MyProp(/* tamanhos, etc. */);
// prop.setPosition(…);
// registry.add(prop);

// MyItem item = new MyItem();
// item.setPosition(…);
// registry.add(item);
```

Estes exemplos são ilustrativos — a cena por omissão começa minimal, apenas com o mundo de vóxeis. Quando adicionarem os vossos objetos ao registo, o `RenderAppState` irá desenhá‑los e o `InteractionAppState` permitirá interagir com itens via E.

---

## Execução e Compilação

Windows (cmd):

```bat
gradlew.bat run
```

Criar pacotes distribuíveis (sem testes):

```bat
gradlew.bat build -x jogo
```

> As dependências (jME3, Minie, Lemur) são geridas pelo Gradle; não é necessário mais nada.

---

## FAQ / Armadilhas Comuns

- Não consigo mover a câmara
  - Carrega em Tab para capturar o rato.
- Não consigo interagir com itens
  - Olha diretamente para o item (mira por cima) e carrega em E com o rato capturado.
- Quero interagir com vóxeis
  - Implementa o TODO no `InteractionAppState` onde são impressas as coordenadas do vóxel.
- Adicionei um objeto mas não o vejo
  - Verifica se o adicionaste ao `GameRegistry`; o renderizador só desenha objetos registados. Para o remover em execução, chama `registry.remove(obj)` — o respetivo visual será removido automaticamente.
- Está tudo escuro
  - Carrega em L para alternar para Unshaded + Wireframe + Culling Off, ou usa a luz local do jogador.

---

## Contribuir Melhorias ao Motor

Se modificarem o motor, prefiram novos AppStates ou utilitários em vez de alterarem classes dos estudantes. Mantenham as classes dos estudantes neutras em relação ao motor.

Áreas recomendadas de melhoria:
- Meshing de vóxeis por “chunks” e física para desempenho.
- Visuais orientados ao tipo: materiais/fábricas por `VoxelBlockType`.
- Os fluxos de remoção/cleanup no `RenderAppState` estão implementados; estendam‑nos se adicionarem pooling ou instancing.
- Uma camada de adapters mais rica para visuais (modelos, texturas, animações).
- Prompts de interação e feedback de UI.
- Sombras (shadow mapping) e skybox.

Mantenham os PRs pequenos e bem documentados.
