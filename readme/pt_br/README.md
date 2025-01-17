# Kapture - Gravador de tela ![Static Badge](https://img.shields.io/badge/versão-v1.4.0-green) [![Static Badge](https://img.shields.io/badge/licença-Apache_2.0-orange.svg)](https://opensource.org/licenses/Apache-2.0) ![Static Badge](https://img.shields.io/badge/tamanho_do_apk-155_MB-7C39E0)
Altere o idioma do README: 
[![en](https://img.shields.io/badge/idioma-en-blue.svg)](https://github.com/hms-douglas/kapture/blob/master/README.md)
[![pt-br](https://img.shields.io/badge/idioma-pt--br-blue.svg)](https://github.com/hms-douglas/kapture/blob/master/readme/pt_br/README.md)
</br>
</br>
Kapture é um gravador de tela para android que permite capturar o áudio do microfone enquanto o compartilha com outros apps.
</br>
</br>
Eu estava procurando por um app que me permitisse gravar a tela e compartilhar o áudio microfone com outros apps, como jogos.
Infelizmente eu não encontrei nenhum com essa capacidade na Google Play.
Os que eu encontrei tinham quer ser instalados por fora, o que me deixou um pouco preocupado, uma vez que eles usavam permissões "perigosas" e eu não tinha acesso ao código.
Portanto eu decidi criar o meu gravador. Também resolvi compartilhar ele aqui. Você pode baixar o .apk e instalar, ou voce pode baixar o código fonte, lê-lo e gerar o app (built).
</br>
</br>
<img src="../app.gif" width="40%"/>
##
### Recursos
<ul>
  <li>Capturar tela:
    <ul>
      <li>Definir resolução;</li>
      <li>Definir qualidate (bit rate / taxa de transferência de bits);</li>
      <li>Definir orientação;</li>
      <li>Definir FPS.</li>
    </ul>
  </li>
  <li>Capturar o áudio interno (se o app que estiver reproduzindo permitir a captura):
    <ul>
        <li>Capturar em modo mono ou estéreo.</li>
    </ul>
  </li>
  <li>Capturar o áudio do microfone (enquanto o compartilha com outros apps):
    <ul>
      <li>Aumentar/Diminuir o volume.</li>
    </ul>
  </li>
  <li>Pausar/Retomar a captura;</li>
  <li>Perfis;</li>
  <li>Interface flutuante;
    <ul>
        <li>Menu (Parar, pausar, screenshot, desenhar, minimizar, fechar, tempo, tempo limite, câmera);</li>
         <li>Câmera (frontal, traseira / estilização);</li>
         <li>Texto (estilização);</li>
         <li>Desenho (estilização).</li>
         <li>Imagem;</li>
         <li>Atalhos.</li>
    </ul>
  </li>
  <li>Gerar arquivos de vídeo extras:
   <ul>
      <li>Sem áudio;</li>
      <li>Apenas com o áudio interno;</li>
      <li>Apenas com o áudio do microfone.</li>
    </ul>
  </li>
  <li>Gerar arquivos de áudio extras:
    <ul>
      <li>Ambos os áudios;</li>
      <li>Apenas o áudio interno;</li>
      <li>Apenas o áudio do microfone.</li>
    </ul>
  </li>
  <li>Atalhos nas notificações;</li>
  <li>Botão de atalho (Tile):
    <ul>
      <li>Iniciar/Parar uma captura;</li>
      <li>Compartilhar por WiFi.</li>
    </ul>
  </li>
  <li>Widgets:
    <ul>
      <li>Básico - Iniciar/Parar;</li>
      <li>Completo - Iniciar/Parar e Pausar/Resumir;</li>
      <li>Atalho para compartilhar por WiFi;</li>
      <li>Atalho para perfis.</li>
    </ul>
  </li>
  <li>Botões de atalho (Launcher);</li>
  <li>Gerenciar todas as capturas feitas pelo app:
    <ul>
      <li>Verificar informações como resolução, data de criação, tamanho, ...</li>
      <li>Removê-las do app;</li>
      <li>Apagar o(s) arquivo(s) do celular;</li>
      <li>Verificar os arquivos extras gerados;</li>
      <li>Renomear;</li>
      <li>Compartilhar</li>
    </ul>
  </li>
  <li>Compartilhar por WiFi;</li>
  <li>Contagem regressiva para começar a capturar;</li>
  <li>Diversas opções de parada automática;</li>
  <li>Diversas opções de configuração automáticas para antes de iniciar;</li>
  <li>Visualizadores internos (reprodutores)
     <ul>
       <li>Reprodutor de áudio;</li>
       <li>Repordutor de vídeo.</li>
     </ul>
  </li>
  <li>Idiomas:
   <ul>
      <li>English</li>
      <li>Português (Brasil).</li>
    </ul>
  </li>
  <li>Tema claro e escuro (automaticamente ou manualmente).</li>
  <li>Controle de armazenamento.</li>
  <li>Interface para tablet.</li>
</ul>

##
### Compartilhando o microfone
De acordo com a <a href="https://developer.android.com/media/platform/sharing-audio-input?hl=pt-br" target="_blank" rel="noreferrer">documentação</a> do Android, a partir do Android 10/11, apps só podem compartilhar o microfone entre eles em casos especiais.
</br>
</br>
Kapture foi construido usando um serviço de acessibilidade, tornando-o um caso especial, como mencionado <a href="https://developer.android.com/media/platform/sharing-audio-input?hl=pt-br#accessibility_service_ordinary_app" target="_blank" rel="noreferrer">aqui</a> na documentação.

##
### Permissões (necessárias para utilizar o app)
<ul>
  <li>Microfone: Usado para capturar o áudio do microfone e interno:
    <ul>
      <li>android.permission.RECORD_AUDIO</li>
    </ul>
  </li>
  <li>Notificação: Usado para mostrar as notificações e iniciar o serviço no modo foreground:
    <ul>
      <li>android.permission.POST_NOTIFICATIONS</li>
    </ul>
  </li>
  <li>Armazenamento: Usado para criar e gerenciar os arquivos de captura:
    <ul>
      <li>android.permission.WRITE_EXTERNAL_STORAGE</li>
      <li>android.permission.READ_EXTERNAL_STORAGE</li>
      <li>android.permission.MANAGE_EXTERNAL_STORAGE</li>
    </ul>
  </li>
  <li>Configurações de segurança: Usado para compartilhar o áudio do microfone com outros apps e para iniciar/para o serviço de acessibilidate:
    <ul>
      <li>android.permission.WRITE_SECURE_SETTINGS</li>
      <li>android.permission.FOREGROUND_SERVICE</li>
    </ul>
  </li>
  <li>Internet: Usado para procurar por atualizações (o app não se auto atualiza!), para abrir links externos e para compartilhar arquivos por WiFi:
    <ul>
      <li>android.permission.INTERNET</li>
      <li>android.permission.ACCESS_NETWORK_STATE</li>
    </ul>
  </li>
  <li>Câmera: Usado para mostrar a câmera flutuante:
    <ul>
      <li>android.permission.CAMERA</li>
    </ul>
  </li>
  <li>Otimização: Usado como atalho para desativar a otimização da bateria para o app:
    <ul>
      <li>android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS</li>
    </ul>
  </li>
  <li>Pacotes: Usado para listar, adicionar e abrir apps:
    <ul>
      <li>android.permission.QUERY_ALL_PACKAGES</li>
    </ul>
  </li>
</ul>

##
### Instalação
#### Opção 1:
- Baixar o código fonte, ou clonar esse repositório, e construir o app utilizando o Android Studio.

#### Opção 2:
<ul>
  <li>Você pode baixar e instalar o .apk mais recente <a href="https://drive.google.com/file/d/1ARA2P2UIZ9leSuChZ1q2N5OHGd_t4vss/view" target="_blank" rel="noreferrer">aqui</a>*;</li>
  <li>Você pode verificar o link para baixar o .apk de versões anteriores <a href="https://github.com/hms-douglas/kapture/blob/master/dist/all.json" target="_blank" rel="noreferrer">aqui</a>*.</li>
</ul>
* Todos os .apks listados aqui foram contruidos por mim, eles não estão minificados e estão hospedados no Google Drive.
* O Google Play Protect pode impedir de instalar o .apk. Neste caso, desative o Google Play Protect, instale o arquivo e então ative-o novamente.

##
### Doações
- Caso você queira me apoiar, você pode fazer uma doação utilizando o botão abaixo... Valeu! ❤️
  <a href="https://www.paypal.com/donate/?hosted_button_id=7XGH7WXU5C7K6">
  <img src="./paypal.png" width="160" height="50"/>
  </a>

##
### Log
<b>v1.4.0</b>
<ul>
   <li>Botão de atalho para compartilhar por wifi adicionado (Tile);</li>
   <li>Atalho de apps adicionado ao menu flutuante;</li>
   <li>Senha adicionada ao compartilhar por wifi (+ opção de renovar a senha a cada nova conexão);</li>
   <li>Perfis adicionado;</li>
   <li>Widget de perfis adicionado;</li>
   <li>Opções para antes de iniciar a captura adicionado;</li>
   <li>Botão de volta ao topo adicionado no menu Kapturas;</li>
   <li>O idioma do app agora é gerenciado pelo menu de configurações do sistema android;</li>
   <li>As notificações foram aprimoradas;</li>
   <li>Correções;</li>
   <li>Pequenas mudanças na interface.</li>
</ul>
<b>v1.3.0</b>
<ul>
   <li>Opção de parada por nível de bateria adicionado;</li>
   <li>Controle de armazenamento + Opção de limpar cache adicionado;</li>
   <li>Interface flutuante de imagem adicionada;</li>
   <li>Widgets adicionados (Básico, Completo, Compartilhar por WiFi);</li>
   <li>Opção para controlar reciclagem do token adicionada;</li>
   <li>Opção de tornar a captura "seekable" adicionado no player de vídeo;</li>
   <li>Suporte para interface de tablet adicionado;</li>
   <li>A interface flutuante de desenho agora suporta cores com canal alfa;</li>
   <li>A interface flutuante de desenho agora suporta screenshots (do desenho e/o da tela);</li>
   <li>A licença do FFmpeg alterada do LGPL v3.0 para GPL v3.0;</li>
   <li>Correções;</li>
   <li>Pequenas mudanças na interface.</li>
</ul>
<b>v1.2.0</b>
<ul>
   <li>Melhora no carregamento;</li>
   <li>Opção de pausar/retormar captura adicionada;</li>
   <li>Atalhos estáticos adicionados (clicar e segurar o app na gaveta de aplicativos).</li>
</ul>
<b>v1.1.0</b>
<ul>
  <li>Correção da ações das notificações não fechando a atividade quando concluidas;</li>
  <li>Correção do nome do canal de notificação;</li>
  <li>Correção de tradução;</li>
  <li>Nova interface do menu flutuante;</li>
  <li>Câmera flutuante adicionda;</li>
  <li>Texto flutuante adicionado;</li>
  <li>Desenho flutuante adicionado;</li>
  <li>Compartilhar por WiFi adicionado;</li>
  <li>Contagem regressiva para iniciar a captura adicionado;</li>
  <li>Opções de parada automática adicionadas;</li>
  <li>Opção de tirar print (screenshot) enquanto captura adicionado;</li>
  <li>Opção de ativar/desativar algumas notificações adicionada;</li>
  <li>Orientação da captura adicionado;</li>
  <li>Opção de ignorar a otimização de bateria do celular para o app adicionado;</li>
  <li>Interface atualizada;</li>
  <li>Créditos atualizado.</li>
</ul>
<b>v1.0.0</b>
<ul>
  <li>Lançamento.</li>
</ul>

##
### Licença
Copyright 2024 Douglas Silva

Licenciado sob a Licença Apache, Versão 2.0 (a "Licença");
você não pode usar este arquivo exceto em conformidade com a Licença.
Você pode obter uma cópia da Licença em

     http://www.apache.org/licenses/LICENSE-2.0

A menos que exigido pela lei aplicável ou acordado por escrito, o software
distribuído sob a Licença é distribuído "COMO ESTÁ",
SEM GARANTIAS OU CONDIÇÕES DE QUALQUER TIPO, expressas ou implícitas.
Consulte a Licença para o idioma específico que rege as permissões e
limitações sob a Licença.
