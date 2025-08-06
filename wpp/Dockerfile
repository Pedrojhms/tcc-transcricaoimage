FROM node:20-alpine
WORKDIR /app

# Install system dependencies
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
    && rm -rf /var/cache/apk/*

# Create non-root user
RUN addgroup -g 1001 -S nodegroup && \
    adduser -S nodeuser -u 1001 -G nodegroup

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install --production && npm cache clean --force

# Copy application code
COPY . .

# Create directories and set permissions
RUN mkdir -p /app/.wwebjs_auth /app/logs && \
    chown -R nodeuser:nodegroup /app

# Configure environment
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true \
    PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium-browser \
    NODE_ENV=production

# Configure volumes
VOLUME ["/app/.wwebjs_auth", "/app/logs"]

# Switch to non-root user
USER nodeuser

# Health check
#HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
#    CMD curl -f http://whatsapp:3000/health || exit 1

EXPOSE 3000

ENTRYPOINT ["dumb-init", "--"]
CMD ["node", "index.js"]