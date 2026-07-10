# Selecktron Orcamento - Android

Este projeto empacota o site de orcamento em um app Android com WebView.

## Arquivos gerados no GitHub

Quando o projeto estiver no GitHub, a aba Actions gera:

- `selecktron-debug-apk`: arquivo APK para instalar e testar no celular.
- `selecktron-release-aab`: arquivo AAB para enviar para a Play Console.

## Observacao importante

Para publicar um app novo na Play Store, use o arquivo `.aab`.
O APK e util para testes fora da loja.

## Como publicar

1. Envie esta pasta para um repositorio no GitHub.
2. Abra a aba Actions do repositorio.
3. Rode o fluxo "Build Android app", se ele nao rodar automaticamente.
4. Baixe o artifact `selecktron-release-aab`.
5. Envie o `.aab` na Play Console.

Para o AAB sair assinado para producao, configure estes secrets no repositorio do GitHub:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Sem esses secrets, o GitHub ainda gera o APK de teste e um AAB de release, mas voce deve assinar o AAB antes de publicar na Play Console.
