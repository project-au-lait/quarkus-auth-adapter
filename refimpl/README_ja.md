# QAA Reference Implementation

このディレクトリは QAA のリファレンス実装を格納しています。
各ディレクトリには以下の資材を格納しています。

- container
  Keycloak、及び SMTP サーバー([Mailpit](https://mailpit.axllent.org/))の コンテナ
- keycloak-refimpl
  IAM Provider として Keycloak を使用した Backend
- svelte-refimpl
  SvelteKit で実装した Frontend
- e2etest
  以上を end-to-end でテストするための Playwright テスト

リファレンス実装の動作には、QAA の使用方法で挙げたソフトウェアに加え以下のものが必要です。

- Git
- Docker
- Node.js
- pnpm
- VSCode

まず、以下のコマンドで QAA 全体のプロジェクト資材を取得し、ビルドします。

```sh
git clone http://sitoolkit-dev.monocrea.co.jp/gitbucket/git/project-au-lait/quarkus-auth-adapter.git
cd quarkus-auth-adapter

./mvnw install -T 1C -P setup

# TODO: コマンドのみで動作させる手段を提供
```

次に、同ディレクトリから以下のコマンドでプロジェクトを VSCode ワークスペースとして開きます。

```sh
code quarkus-auth-adapter.code-workspace
```

VSCode Task: `start-backend`, `start-frontend`を実行し、Backend・Frontend を起動します。
