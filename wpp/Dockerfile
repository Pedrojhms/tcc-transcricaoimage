FROM node:20-alpine
WORKDIR /app

# Install system dependencies including Chromium
RUN apk add --no-cache \
    chromium \
    nss \
    freetype \
    freetype-dev \
    harfbuzz \
    ca-certificates \
    ttf-freefont \
    curl \
    dumb-init \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /var/cache/apk/*

# Create non-root user
RUN addgroup -g 1001 -S nodegroup && \
    adduser -S nodeuser -u 1001 -G nodegroup

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install --production && npm cache clean --force

# Copy application code
COPY . .

# Create directories and set permissions BEFORE switching user
RUN mkdir -p /app/.wwebjs_auth /app/logs /tmp/chrome-user-data && \
    chown -R nodeuser:nodegroup /app && \
    chown -R nodeuser:nodegroup /tmp/chrome-user-data && \
    chmod -R 777 /app/.wwebjs_auth && \
    chmod -R 755 /tmp/chrome-user-data

# Configure environment for Puppeteer
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true \
    PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium-browser \
    NODE_ENV=production \
    CHROME_DEVEL_SANDBOX=/usr/lib/chromium/chrome_sandbox

# Configure volumes
VOLUME ["/app/.wwebjs_auth", "/app/logs"]

# Switch to non-root user
USER nodeuser

EXPOSE 3000

ENTRYPOINT ["dumb-init", "--"]
CMD ["node", "index.js"]