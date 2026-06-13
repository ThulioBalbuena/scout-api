# Uso de Inteligência Artificial no Projeto

Durante o desenvolvimento do projeto Scout API, a equipe utilizou ferramentas de Inteligência Artificial como apoio ao desenvolvimento, organização do processo e documentação. A IA foi utilizada como ferramenta auxiliar, mas as decisões finais, validações, testes e adaptações foram feitas pelos integrantes da equipe.

## Modelos utilizados

Foram utilizados principalmente os seguintes modelos:

* Codex 5.5, com nível de raciocínio muito alto;
* ChatGPT 5.5, com nível de raciocínio alto.

## Finalidades de uso

A IA foi utilizada principalmente para:

* Apoiar melhorias no frontend da aplicação;
* Sugerir ajustes de interface e experiência do usuário;
* Auxiliar na criação e organização das histórias de usuário;
* Ajudar na escrita de documentação do projeto;
* Apoiar pequenas correções no código e na estrutura do projeto;
* Auxiliar na análise de erros de testes e pipeline CI/CD.

A IA não foi utilizada para substituir a implementação completa do projeto. O código gerado ou sugerido foi revisado, adaptado e validado manualmente pela equipe.

---

## Exemplos principais de prompts utilizados

### Prompt 1 — Refatoração e melhoria do frontend

Modelo utilizado: Codex 5.5
Objetivo: melhorar a interface do frontend, traduzir alertas e ajustar o fluxo visual da aplicação.

Prompt utilizado:

```text
Observe que esse projeto, o scout-api, tem uma parte que chama "Scout-front", quero que você faça o seguinte:

Analise essa pasta e faça as seguintes alterações:

Adicionar: Na parte de dar Sign presidents, tem um campo "PRESIDENT NAME", e um campo "EMAIL", adicione outro campo com o nome do clube, que será apenas visual, para exibição na tabela do campeonato.

Arrumar: os alertas, como "Nova temporada iniciada!", "Cadastro de presidentes", etc..., estão em português, mas nossa aplicação é em inglês, identifique todos os alertas em português e os traduza para o inglês.

A interface está muito poluída, altere a interface para exibir apenas as informações necessárias por rodada, se inspire no site do jogo "SeteaZero", além disso, mostre sempre a posição de cada clube também coloque um campo exibindo o dinheiro restante de cada clube (na fase de auction).

Arrume o erro: o aviso inicial mostra: "Leilao iniciado! Primeiro jogador: Hulk", isso não está correto, o primeiro jogador sempre está sendo o Léo Ortiz, ou então ele está bugando e pulando o Hulk, verifique isso.

Na segunda fase, a fase de Auction, existe o botão "Finalize player" e "Next auction", aparentemente eles fazem a mesma coisa, caso isso seja verdade, mantenha apenas 1 botão.

Adicione também número mínimo de presidentes, pode ser 10 presidentes no mínimo, coloque também um botão de criação automática, e coloque 10 clubes padrão, Atlético Mineiro, Palmeiras, Flamengo, Cruzeiro, Corinthians, São Paulo, Santos, Fluminense, Grêmio e Internacional. O e-mail e nome dos presidentes podem ser padrão também, invente um nome de presidente para cada clube nesse botão, por exemplo: Galo - Rafael Menin - rafael.menin@email.com, repita esse padrão para os outros clubes, mas inventando nomes diferentes, se possível, pesquise os presidentes de cada time que eu deixei de exemplo, coloque apenas nome + sobrenome, igual o Rafael Menin, e o email coloca nome.sobrenome@email.com.

Na fase de campeonato (fase 4) ao invés de precisar gerar o calendário por meio do botão "Generate schedule", já faça o que esse botão faz automaticamente, já crie o calendário sem precisar clicar em botão nenhum, além disso, o botão "Open transfers", que fica liberado apenas após a rodada 3, pode deixar ele desabilitado até chegar na rodada 3.

Os alertas, estão aparecendo em cima da tela, mas faça uma caixa de notificação ao invés disso. Essa caixa irá mostrar o alerta e o usuário deve clicar no "Ok" para tirar o aviso da tela e prosseguir jogando. Esse anúncio virá em forma de card.

Assim que todas as rodadas acabarem, finalize a temporada automaticamente, remova o botão "Finish season", uma vez que os jogos devem ser finalizados para que a temporada acabe. Mantenha o "New season", pois deve ser possível recomeçar sempre que desejado.

E lembre-se, tudo isso deve ser feito seguindo a regra que eu falei, que deve ser menos poluída a tela, exiba apenas as informações realmente necessárias.

Caso você identifique alguma melhoria que possa ser feita, seguindo os princípios de UI/UX, pode melhorar.
```

Resultado obtido:

O resultado foi positivo para a reorganização da interface, tradução de alertas e melhoria geral da experiência do usuário. A IA ajudou a reduzir a poluição visual da tela e a deixar o fluxo do jogo mais claro. No entanto, as alterações não corrigiram automaticamente todos os testes afetados pelas mudanças de mensagens e fluxo da aplicação. Por isso, a equipe precisou revisar e corrigir manualmente os testes para que eles ficassem compatíveis com a nova interface e com as mensagens em inglês.

Status: resposta parcialmente aceita e ajustada manualmente.

---

### Prompt 2 — Melhorias na simulação do campeonato

Modelo utilizado: Codex 5.5
Objetivo: melhorar a fase de campeonato, permitindo simular todas as rodadas e visualizar os resultados.

Prompt utilizado:

```text
Preciso de outra rápida alteração, preciso de um botão na fase do torneio para simular todos os jogos de uma vez, sem ir 1 por 1, a pessoa vai ter a possibilidade de simular tudo ou 1 por 1.

Outro ponto importante: ao clicar em simular rodada, ele simula, mas não mostra os resultados dos jogos, eu quero que você faça que a tela mostre os resultados dos jogos e apareça um botão para ir para a próxima rodada, e no final do campeonato, mostre uma lista completa de todos os jogos por rodada, apenas mostrando o placar, saldo de gols, gols feitos e gols sofridos, parecido com o que tem na tabela do google, só isso, coisa simples, só para o jogador conseguir acompanhar os resultados dos jogos.
```

Resultado obtido:

A resposta foi útil para melhorar a experiência da fase de campeonato. A IA sugeriu e auxiliou na criação de um fluxo mais claro para simulação das rodadas, exibição dos resultados e acompanhamento da tabela. O resultado foi aproveitado, mas passou por revisão da equipe para manter a interface simples e evitar excesso de informações na tela.

Status: resposta aceita com ajustes manuais.

---

### Prompt 3 — Histórias de usuário

Modelo utilizado: ChatGPT 5.5
Objetivo: auxiliar na criação e organização das histórias de usuário do projeto.

Prompt utilizado:

```text
Com base no projeto Scout API, gere histórias de usuário no formato:

Como <perfil>, eu quero <ação> para que <benefício>.

As histórias devem ter critérios de aceitação no formato Given/When/Then, prioridade, status final e relação com funcionalidades do sistema, como cadastro de presidentes, leilão de jogadores, sorteio automático, simulação do campeonato e consulta de relatórios.
```

Resultado obtido:

A IA gerou histórias de usuário bem estruturadas e compatíveis com o formato solicitado. A maior parte das histórias já estava alinhada com funcionalidades existentes no projeto, como cadastro de presidentes, leilão, sorteio, campeonato e relatórios. A equipe utilizou essas sugestões como base para organizar a documentação e complementar a rastreabilidade entre histórias, issues, pull requests e testes automatizados.

Status: resposta aceita e adaptada ao contexto real do projeto.

---

## Avaliação dos resultados

De forma geral, o uso de IA trouxe bons resultados para acelerar tarefas de documentação, organização do projeto e melhoria da interface. As respostas foram úteis principalmente como ponto de partida, mas não foram aceitas de forma cega.

A equipe revisou as alterações, ajustou trechos de código, corrigiu testes manualmente e validou o comportamento final da aplicação. Um exemplo disso foi a alteração das mensagens da aplicação para inglês, que exigiu ajustes nos testes automatizados para que eles continuassem compatíveis com o backend e com a nova interface.

Além desses exemplos principais, a IA também foi utilizada em pequenas correções pontuais, como sugestões de nomes de commits, organização de etapas de CI/CD, ajustes de documentação e apoio na interpretação de erros durante a execução dos testes e do Jenkins.

## O que não foi feito por IA

A IA não foi responsável por decidir sozinha a arquitetura final do projeto, nem por validar automaticamente a entrega. As decisões finais sobre funcionalidades, organização do repositório, aceite das histórias, testes, commits e pull requests foram feitas pela equipe.

Também coube à equipe executar os testes, revisar os resultados no Jenkins, corrigir problemas de integração e garantir que o projeto estivesse funcional para a entrega.
