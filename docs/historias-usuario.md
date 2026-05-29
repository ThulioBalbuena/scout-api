# Histórias de Usuário

Este documento apresenta as principais histórias de usuário que guiaram o desenvolvimento da Scout API. Cada história possui prioridade, status esperado e critérios de aceitação no formato Given/When/Then.

---

## HU01 - Cadastro de presidentes

**História de usuário:**  
Como jogador, eu quero cadastrar presidentes no sistema para que eles possam participar do fantasy game.

**Explicação:**  
Essa história representa o início do fluxo do sistema. Antes de montar times, participar do leilão ou disputar o campeonato, é necessário que os presidentes estejam cadastrados.

**Prioridade:** Alta  
**Motivo da prioridade:** Sem o cadastro dos presidentes, o restante do fluxo do jogo não pode acontecer.  
**Status esperado:** Entregue

**Critérios de aceitação:**

```gherkin
Given que o jogo está na fase de cadastro
When o usuário envia os dados válidos de um presidente
Then o sistema deve cadastrar o presidente corretamente

Given que um presidente já está cadastrado
When o usuário consulta a lista de presidentes
Then o sistema deve retornar o presidente cadastrado

Given que o usuário informa dados inválidos ou incompletos
When tenta cadastrar um presidente
Then o sistema deve rejeitar a operação ou retornar uma mensagem de erro adequada
```

---

## HU02 - Leilão de jogadores

**História de usuário:**
Como presidente, eu quero dar lances em jogadores no leilão para que eu possa montar um time competitivo.

**Explicação:**
Essa história cobre uma das principais regras de negócio do sistema. O leilão permite que os presidentes disputem jogadores e formem seus elencos de forma estratégica.

**Prioridade:** Alta

**Status esperado:** Entregue

**Critérios de aceitação:**

```gherkin
Given que o jogo está na fase de leilão
When um presidente envia um lance válido para um jogador disponível
Then o sistema deve registrar o lance corretamente

Given que o presidente não possui saldo suficiente
When ele tenta dar um lance
Then o sistema deve rejeitar a operação

Given que um jogador já foi adquirido ou não está disponível
When um presidente tenta dar lance nesse jogador
Then o sistema deve impedir a operação
```

---

## HU03 - Sorteio automático de jogadores

**História de usuário:**  
Como sistema, eu quero distribuir automaticamente os jogadores restantes para que todos os presidentes possam completar seus times.

**Explicação:**  
Depois do leilão, podem sobrar jogadores ou times incompletos. Essa história garante que o sistema consiga completar os elencos automaticamente e permitir o avanço para a próxima fase do jogo.

**Prioridade:** Alta  
**Motivo da prioridade:** Sem os times completos, a simulação do campeonato pode ficar comprometida.  
**Status esperado:** Entregue

**Critérios de aceitação:**

```gherkin
Given que o leilão foi encerrado  
When o sorteio automático é executado  
Then o sistema deve distribuir os jogadores restantes entre os presidentes

Given que existem presidentes com elenco incompleto  
When o sorteio é realizado  
Then o sistema deve completar os times respeitando as regras definidas

Given que todos os times foram completados  
When o processo de sorteio termina  
Then o sistema deve permitir o avanço para a próxima fase do jogo
```

---

## HU04 - Simulação do campeonato

**História de usuário:**  
Como jogador, eu quero simular o campeonato para que seja possível acompanhar resultados, pontuação e classificação dos times.

**Explicação:**  
Essa história representa o objetivo principal após a formação dos times. O sistema deve permitir a simulação das partidas e gerar os resultados do campeonato.

**Prioridade:** Alta  
**Motivo da prioridade:** A simulação do campeonato é essencial para fechar o ciclo principal da aplicação.  
**Status esperado:** Entregue

**Critérios de aceitação:**

```gherkin
Given que os times estão formados  
When o campeonato é iniciado ou simulado  
Then o sistema deve gerar partidas entre os times

Given que uma partida foi simulada  
When o resultado é calculado  
Then o sistema deve atualizar pontuação, vitórias, empates, derrotas e classificação

Given que todas as partidas foram simuladas  
When o usuário consulta o campeonato  
Then o sistema deve retornar a classificação final
```

---

## HU05 - Consulta de relatórios

**História de usuário:**  
Como jogador, eu quero consultar relatórios do campeonato para que eu possa analisar o desempenho dos times e jogadores.

**Explicação:**  
Essa história cobre a parte de análise do sistema. Após o campeonato ser simulado, os relatórios ajudam a visualizar informações importantes como classificação, artilharia e desempenho dos times.

**Prioridade:** Média  
**Motivo da prioridade:** Os relatórios são importantes para a experiência final, mas dependem das etapas anteriores, principalmente da simulação do campeonato.  
**Status esperado:** Entregue

**Critérios de aceitação:**

```gherkin
Given que o campeonato possui partidas simuladas  
When o usuário consulta os relatórios  
Then o sistema deve retornar informações de desempenho dos times

Given que existem gols registrados nas partidas  
When o usuário consulta o relatório de artilharia  
Then o sistema deve retornar os jogadores com melhor desempenho ofensivo

Given que a classificação foi calculada  
When o usuário consulta o relatório geral  
Then o sistema deve retornar dados como classificação, desempenho dos times e estatísticas relevantes
```