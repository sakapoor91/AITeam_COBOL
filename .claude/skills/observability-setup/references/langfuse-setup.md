# Langfuse Setup Reference

## Docker Compose Configuration

```yaml
# metrics/docker-compose.observability.yml
version: "3.8"
services:
  langfuse:
    image: langfuse/langfuse:latest
    ports:
      - "3000:3000"
    environment:
      - DATABASE_URL=postgresql://langfuse:langfuse@langfuse-db:5432/langfuse
      - NEXTAUTH_URL=http://localhost:3000
      - NEXTAUTH_SECRET=your-secret-here-change-in-production
      - SALT=your-salt-here-change-in-production
    depends_on:
      - langfuse-db

  langfuse-db:
    image: postgres:16
    environment:
      - POSTGRES_USER=langfuse
      - POSTGRES_PASSWORD=langfuse
      - POSTGRES_DB=langfuse
    volumes:
      - langfuse-data:/var/lib/postgresql/data

volumes:
  langfuse-data:
```

## Environment Variables
```bash
export LANGFUSE_PUBLIC_KEY="pk-lf-your-key"
export LANGFUSE_SECRET_KEY="sk-lf-your-key"
export LANGFUSE_BASE_URL="http://localhost:3000"
```

## Initial Setup
1. Navigate to http://localhost:3000
2. Create an account
3. Create a project named "carddemo-modernization"
4. Copy the public and secret keys from Project Settings → API Keys
