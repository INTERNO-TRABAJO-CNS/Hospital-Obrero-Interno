import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitest/config'

// Pruebas de models, services y stores (sin levantar Nuxt). Los stores reciben su service por parametro.
export default defineConfig({
  resolve: {
    alias: {
      '~': fileURLToPath(new URL('./', import.meta.url)),
    },
  },
  test: {
    environment: 'happy-dom',
    include: ['tests/**/*.test.ts'],
  },
})
