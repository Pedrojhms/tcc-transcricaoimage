FROM node:20-alpine
WORKDIR /app

# Install only essential system dependencies
RUN apk add --no-cache \
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
    chown -R nodeuser:nodegroup /app && \
    chmod -R 755 /app/.wwebjs_auth

# Configure environment
ENV NODE_ENV=production

# Configure volumes
VOLUME ["/app/.wwebjs_auth", "/app/logs"]

# Switch to non-root user
USER nodeuser

EXPOSE 3000

ENTRYPOINT ["dumb-init", "--"]
CMD ["node", "index.js"]