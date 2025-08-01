# Use uma imagem Node.js oficial como base
FROM node:20-alpine

# Crie e defina o diretório de trabalho
WORKDIR /app

# Instale as dependências do sistema necessárias para o chromium do puppeteer
RUN apk add --no-cache \
    chromium \
    nss \
    freetype \
    harfbuzz \
    ca-certificates \
    ttf-freefont \
    nodejs \
    yarn

# Copie o package.json e o package-lock.json/yarn.lock
COPY package*.json ./

# Instale as dependências do Node.js
RUN npm install

# Copie o restante dos arquivos da aplicação
COPY . .

# Defina a variável de ambiente para o puppeteer usar o chromium do sistema
ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium-browser

# Exponha a porta que você irá usar (ajuste conforme necessário)
EXPOSE 3000

# Comando para rodar sua aplicação (ajuste conforme seu app)
CMD ["node", "index.js"]